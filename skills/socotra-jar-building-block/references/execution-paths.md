# Execution Paths: Where the Platform Invokes Plugins, Per Object

> **Scope: invocation map ONLY.** This doc answers "given this platform object, which plugin types fire, when, and what does my return value control?" Per-plugin semantics (how to write the logic, response validation rules, idioms) live in dedicated child skills and in `references/plugin-interfaces.md`.
>
> Sources: core-datamodel v1.7.61 sources (`sources/com/socotra/coremodel/*.java` state enums), generated SPI `of(...)` factories (`generated-examples/zencover/`, with the multi-product `generated-examples/credit-card-protection/` as contrast), and docs.socotra.com (cited per section). Anything inferred rather than directly verified is marked **(inferred)**. Request record names use the "ZenCover" example product and its `PersonalAccount`/`StandardPayment` account/payment types; substitute your config's names, and in a multi-product config expect one request record set per product (see `references/plugin-interfaces.md` § "What varies per configuration"). Which plugin types exist at all also depends on the core-datamodel version (v1.6.180 lacks cancellation, configMigration, paymentPostProcessing, documentConsolidationSelection, workplan, and automation entries).

Cross-cutting rule (docs: [plugins overview](https://docs.socotra.com/configuration/plugins/overview.html)): PreCommit is "tethered" to validation — every validation request runs **PreCommitPlugin first, then ValidationPlugin**. The nominal lifecycle plugin order for quotes/transactions is: draft (none) → validation (PreCommit, Validation) → pricing (Rating) → underwriting (Underwriting) → accept (none) → issue (none).

## Account

`AccountState`: `draft → validated` (or `discarded`). Mutable only in `draft`.

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Account validate request (pre-commit) | `PreCommitPlugin` | `PersonalAccountRequest(account, trigger)` | Mutated account that gets committed |
| Account validate request | `ValidationPlugin` | `PersonalAccountRequest(account)` | Errors block `draft → validated` |
| Entity migrated to new config version | `ConfigMigrationPlugin` | `PersonalAccountRequest(Account)` + transformer | Account shape under the new config |

No rating/underwriting/document plugins target accounts. Account-level billing objects (invoices, delinquencies) have their own sections below.

## QuickQuote

`QuickQuoteState`: `draft → validated → priced → quoted` (or `discarded`). No underwriting process, no documents, cannot issue — only spawns a full Quote ([quick quotes guide](https://docs.socotra.com/featureGuide/policyQuotation/quickQuotes.html)).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Validate (pre-commit) | `PreCommitPlugin` | `ZenCoverQuickQuoteRequest(quote, trigger)` | Mutated quick quote |
| Validate | `ValidationPlugin` | `ZenCoverQuickQuoteRequest(quote)` | Errors block `draft → validated` |
| Price | `RatePlugin` (`rate`/`statelessRate`) | `ZenCoverQuickQuoteRequest(quote, duration, durationBasis)` | `RatingSet` becomes the quick-quote pricing; `ok=false` halts |

## Quote

`QuoteState` standard flow: `draft → validated → priced → underwritten → accepted → issued`; atypical: `underwrittenBlocked`, `declined`, `rejected`, `refused`, `discarded` ([quotes guide](https://docs.socotra.com/featureGuide/policyQuotation/quotes.html)). The enum also contains `earlyUnderwritten` (between `validated` and `priced` in enum order — position **(inferred from enum order; not described in fetched docs)**). Reset returns a quote to `draft`, deleting pricing.

Ordered invocation sketch: PreCommit → Validation (at validate) → Rating (at price) → Underwriting (at underwrite, evaluation then sets `underwritten`/`underwrittenBlocked`/`declined`/`rejected`) → accept/issue (no plugin). Document plugins fire asynchronously alongside these states (see Document rendering).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Validate request, pre-commit (also manual invoke in `draft`) | `PreCommitPlugin` | `ZenCoverQuoteRequest(quote, trigger)`; trigger ∈ `validate`/`manual` in practice | Mutated quote committed before validation |
| Validate request | `ValidationPlugin` | `ZenCoverQuoteRequest(quote)` | Errors block `draft → validated` |
| Price request | `RatePlugin` | `ZenCoverQuoteRequest(quote, duration, durationBasis)` | `RatingSet` → quote pricing; `ok=false` halts pricing |
| Underwrite request | `UnderwritingPlugin` | `ZenCoverQuoteRequest(quote, flags)` | Flags created/cleared; evaluation of remaining flags decides `underwritten` vs blocked/declined/rejected |
| Document trigger states (async) | `DocumentSelection` / `DocumentDataSnapshot` / consolidation variants | `ZenCoverQuoteRequest` overloads | See Document rendering |
| Entity migrated to new config version | `ConfigMigrationPlugin` | `ZenCoverQuoteRequest(interfaces.Quote)` + transformer | Quote shape under new config |

## Policy / Transaction / Segment

A Policy is only created by issuing a quote; all subsequent change flows through Transactions (issuance, change, renewal, cancellation, reinstatement, reversal). `TransactionState` mirrors QuoteState plus `initialized` (after `draft`, segment materialized from change instructions), `invalidated`, `reversed` ([policy transactions guide](https://docs.socotra.com/featureGuide/policyManagement/policyTransactions.html)). `PolicyStatus` (`pending, onRisk, inGap, expired, cancelled, cancelPending, delinquent, doNotRenew`) is derived — no plugin fires on status change directly.

Transaction-scoped plugins never see `draft` transactions: every generated transaction-level `of(...)` factory throws `IllegalStateException` if `transactionState == draft` (source-verified). Ordered sketch: `draft → initialized` (no plugin) → PreCommit+Validation (`→ validated`) → Rating (`→ priced`; CancellationPlugin instead/in addition for cancellation-category transactions) → Underwriting (`→ underwritten`) → accept → issue (`→ issued`; installments generated → see Invoice/Installments).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Transaction validate, pre-commit | `PreCommitPlugin` | `ZenCoverTransactionRequest(policy, transaction, changeInstructions, trigger)` | `PreCommitTransactionResponse` — revised change instructions |
| Segment commit during validate | `PreCommitPlugin` | `ZenCoverRequest(policy, transaction, segment, trigger)` (segment non-Optional) | Mutated segment |
| Transaction validate | `ValidationPlugin` | `ZenCoverRequest(policy, transaction, Optional<segment>)` | Errors block `→ validated` |
| Transaction price | `RatePlugin` | `ZenCoverRequest(policy, transaction, Optional<segment>, duration, durationBasis)` | `RatingSet` → transaction pricing |
| Cancellation-transaction price — after pro-rated charges computed, before persisted; also stateless previews | `CancellationPlugin` | `ZenCoverRequest(policy, transaction, segment, charges)` | `CancellationPluginResponse(retentionCharges)` adjusts retained amount; failure/`ok=false` halts pricing, transaction stays `validated` ([cancellation](https://docs.socotra.com/configuration/plugins/cancellation.html)) |
| Transaction underwrite | `UnderwritingPlugin` | `ZenCoverRequest(policy, transaction, Optional<segment>, flags)` | Flags created/cleared; evaluation gates progression |
| Issue → installment schedule creation | `InstallmentsPlugin` | see Invoice/Installments | Installment timing |
| Document trigger states (async) | document plugins | `ZenCoverRequest` overloads | See Document rendering |
| Segment migrated to new config version | `ConfigMigrationPlugin` | `ZenCoverRequest(Policy, interfaces.Segment)` + transformer | Segment shape under new config |

## Term (renewal / auto-renewal)

Renewal management operates at term level; only the latest term can carry an active Auto-Renewal (`AutoRenewalState`: `active, issued, doNotRenew, terminated, discarded, error, invalidated`). A *manual* renewal is just a transaction (previous section applies; **no RenewalPlugin call** for manual advancement). For auto-renewals, the plugin fires **before each non-null trigger-time action** ([renewal management](https://docs.socotra.com/featureGuide/policyManagement/renewalManagement.html)).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Auto-renewal trigger time reached, before automatic create / accept / issue of the renewal transaction | `RenewalPlugin` | `AutoRenewalRequest(event, autoRenewal)`; `AutoRenewalEvent` ∈ `create, accept, issue` (source-verified) | `AutoRenewalResponse` (all-Optional) overrides: auto-renewal state, renewal transaction type, new term duration, create/accept/issue times |

The renewal transaction itself then runs the normal transaction plugin chain (validate/price/underwrite) as it advances.

## Invoice / Installments (billing)

Flow per issued quote/transaction ([invoicing](https://docs.socotra.com/featureGuide/billing/invoicing.html), [installments](https://docs.socotra.com/configuration/plugins/installments.html), [autopay](https://docs.socotra.com/featureGuide/billing/autopay.html), [payment execution](https://docs.socotra.com/featureGuide/billing/paymentExecutionService.html)): issuance → installment lattice creates installments → **InstallmentsPlugin** (before finalization) → at `generateTime` installments are grouped into an Invoice (`InvoiceState`: `open → settled`) → at invoice `autopayTime` **AutopayPlugin** → payment (`requested → executing → posted/failed`) execution attempt → **PaymentPostProcessingPlugin** → unsettled past `dueTime` → Delinquency (next section).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Installments created from lattice, before finalized (issuance, changes) | `InstallmentsPlugin` | `ZenCoverRequest(context, installments, installmentLattice)` | `Map<locator, InstallmentUpdate>` overriding `generateTime`/`dueTime`/`autopayTime` |
| Invoice `autopayTime` reached | `AutopayPlugin` | `AutopayRequest(invoice, invoicingHoldActive)` | Optional `PaymentCreateRequest` (creates the payment) + next request time |
| After payment execution attempted with provider | `PaymentPostProcessingPlugin` | `PaymentPostProcessingRequest(context)` | Post-execution handling, incl. `nextRequestTime` for retries; plugin failure moves payment to `failed` |
| Payment validate request | `PreCommitPlugin` → `ValidationPlugin` | `StandardPaymentRequest(payment[, trigger])` — overloads exist only when the config defines payment methods | Mutation / errors block payment commit |
| Invoice document rendering | `DocumentDataSnapshotPlugin` / `DocumentConsolidationSnapshotPlugin` | `InvoiceDetailsRequest(invoiceDetails, config[, consolidationInfo])` | See Document rendering |

## Delinquency

Created when an invoice passes `dueTime` unsettled and a delinquency plan resolves (policy → account → tenant default). `DelinquencyState`: `preGrace → inGrace → lapseTriggered → settled`; level per plan: `policy` or `invoice` (`DelinquencyLevel`). At `graceEndsAt`, the system creates a lapse cancellation transaction and auto-advances it per `advanceLapseTo` — that transaction then hits the normal transaction + CancellationPlugin chain ([delinquency](https://docs.socotra.com/featureGuide/billing/delinquency.html), [delinquency events](https://docs.socotra.com/featureGuide/billing/delinquencyEvents.html)).

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Delinquency committed — exact moment **(inferred from PreCommit request shape; not specified in fetched docs)** | `PreCommitPlugin` | `DelinquencyRequest(delinquency)` | `PreCommitDelinquencyResponse(settings, graceEndAt, lapseTransactionEffectiveDate)` — overrides grace end / lapse effective date |
| Configured event time reached (`offsetBasis` `gracePeriodStart`/`gracePeriodEnd` + `offsetDays`), delinquency still unsettled | `DelinquencyEventPlugin` | `ZenCoverRequest(delinquency, delinquencyEvent)`; `DelinquencyEventState`: `active, triggered, cancelled` | `Map<String, DelinquencyEventUpdateRequest>` updating events |

## Fnol / Claim

**No direct plugin invocation points in core-datamodel v1.7.61.** `FnolState` (`draft, validated, onClaim, completed, rejected, discarded`) and `ClaimState` exist, and `DataFetcher` exposes `getFnol`/`getFnolLosses`/`getFnolClaims`/`getFnolContacts`, but no generated plugin interface carries an Fnol/Claim request record, `PluginType` has no claims entry, and the FNOL feature guide describes no plugin. Custom claim-side logic runs only indirectly — automation plugins reacting to events, or workplan plugins (below).

## Document rendering (cross-cutting: quotes, transactions, invoices)

`DocumentTrigger` enum (ordinal order is meaningful — "latest happened" comparison, source-verified): `validated, priced, accepted, underwritten, issued, generated`. Pipeline per trigger, executed **asynchronously** ([document selection](https://docs.socotra.com/configuration/plugins/documentSelection.html), [document data snapshot](https://docs.socotra.com/configuration/plugins/documentDataSnapshot.html), [consolidation](https://docs.socotra.com/featureGuide/documents/documentConsolidation.html)):

1. `DocumentSelectionPlugin` — which configured documents to act on.
2. `DocumentDataSnapshotPlugin` — per selected document, after selection succeeds; also before invoice rendering and on the manual Render Document API. Moves dynamic docs to `dataReady`, static to `ready`.
3. For consolidated documents (trigger = latest of subdocument triggers): `DocumentConsolidationSelectionPlugin` then `DocumentConsolidationSnapshotPlugin`.

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Quote/transaction reaches a `DocumentTrigger` state | `DocumentSelectionPlugin` | `ZenCoverQuoteRequest` / `ZenCoverRequest` (+ documentConfigs, trigger) | `Map<docName, DocumentSelectionAction>` (`generate, noChange, generateIfAbsent, remove, defaultAction`) |
| After selection; manual render; before invoice rendering | `DocumentDataSnapshotPlugin` | quote / transaction / `InvoiceDetailsRequest` overloads | `DocumentDataSnapshot(metadata, renderingData)` fed to the template |
| Consolidated document due | `DocumentConsolidationSelectionPlugin` | quote / transaction overloads + config, documents | Ordered `List<ULID>` of subdocument locators |
| Consolidated document assembly | `DocumentConsolidationSnapshotPlugin` | quote / transaction / invoice overloads + consolidationInfo | `DocumentDataSnapshot` for the packet |

## Config migration (cross-cutting)

`ConfigMigrationPlugin` runs when persisted entities (Account, Quote, Policy+Segment) are migrated to a new config version; requests carry the *interface* types plus a `ConfigMigrationTransformer`. Exact invocation timing relative to deployment is **not specified in source/fetched docs** (note: the docs "Migration" feature guide covers book-of-business data migration, a different feature with no plugin).

## Custom events, automation, work management (cross-cutting)

| Trigger moment | Plugin type | Request | Return controls |
|---|---|---|---|
| Manual call to Execute Automation Plugin API (your generated endpoint per configured action) | `@AutomationPlugin` interface, action method | Generated per-action request record | Generated per-action response record (your API response) |
| Platform/custom event, with `enableWebhooks: true` (one event-driven implementation max; Global-scope only) | `@AutomationPlugin`, `handleWebhookEvent` | Webhook event payload | Event handling result ([automation](https://docs.socotra.com/configuration/plugins/automation.html)) |
| Configured `workplanTriggers` event fires | `WorkplanSelectionPlugin` then `WorkplanExecutionPlugin` (documented order) | `WorkplanSelectionRequest` / `WorkplanExecutionRequest` | Which workplans execute; then the tasks/associations each creates ([workplans](https://docs.socotra.com/featureGuide/workManagement/workplans.html)) |
| Custom scheduled events (`customEvents` config) at their scheduled times | none directly — emitted to the event stream; consumed via webhooks/event-driven automation | — | — |

Plugins can also *emit* custom events (`EventsService.getInstance().createEvent(CustomEvent.X, data)`), feeding the same event-driven paths.

## Appendix: state enums referenced above (source-verified, `sources/com/socotra/coremodel/`)

```java
QuoteState        { draft, validated, earlyUnderwritten, priced, underwritten, accepted, issued,
                    underwrittenBlocked, declined, rejected, refused, discarded }
TransactionState  { draft, initialized, validated, earlyUnderwritten, priced, underwritten, accepted,
                    issued, underwrittenBlocked, declined, rejected, refused, discarded, invalidated, reversed }
QuickQuoteState   { draft, validated, priced, quoted, discarded }
AccountState      { draft, validated, discarded }
PolicyStatus      { pending, inGap, expired, cancelled, cancelPending, onRisk, delinquent, doNotRenew }  // derived
InvoiceState      { open, settled }
DelinquencyState  { preGrace, inGrace, lapseTriggered, settled }
DelinquencyLevel  { policy, invoice }
DelinquencyEventState { active, triggered, cancelled }
AutoRenewalState  { active, discarded, doNotRenew, issued, error, terminated, invalidated }
AutoRenewalEvent  { create, accept, issue }
FnolState         { draft, validated, onClaim, completed, rejected, discarded }
ClaimState        { draft }
DocumentTrigger   { validated, priced, accepted, underwritten, issued, generated }   // ordinal order significant
PreCommitTrigger  { create, update, validate, manual }   // docs: only validate/manual currently delivered
```

## Not covered / unresolved

- `deserialization` PluginType exists in the dispatch table but has no generated example or docs page in the mirrored corpus — trigger moment not specified in source/docs.
- `QuoteState.earlyUnderwritten` / `TransactionState.earlyUnderwritten` placement is enum-order inference only.
- Exact relative ordering of document-plugin execution vs the synchronous state transition that triggers it: docs only say "asynchronous".
