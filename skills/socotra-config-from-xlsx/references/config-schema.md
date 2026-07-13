# socotra-config JSON schema (distilled)

The per-entity JSON schema this skill maps workbook tabs onto. **Folder name = entity
name**; each entity is a `config.json` under `socotra-config/<section>/<Name>/`. This is
the minimal slice needed for the common config-authoring path — the full authority
(every section, plus automations/FNOL/work-management/etc.) lives in the external
`socotra-config` skill if you need a section not covered here.

The `validateConfig` result always wins over anything below — if they disagree, fix the
file, then fix this reference.

---

## How the entities compose (read this before building a multi-level product)

The folders are **flat** — every entity lives at `socotra-config/<section>/<Name>/`, never physically nested. The product *tree* is expressed **by reference**: a parent names its children (with a quantifier) in its `contents` / `coverageTerms` / `charges` arrays. Wiring a config = defining leaf entities, then naming them upward.

```
config.json                         ← tenant/root settings
└─ products/<Product>               contents: exposures &/or coverages (+ quantifier)
   └─ exposures/<Exposure>          the repeating insured unit; contents: coverages
      └─ coverages/<Coverage>       a protection; may nest sub-coverages via contents
         ├─ coverageTerms/<Term>    limit / deductible (options- or value-based)
         └─ charges/<Charge>        premium / fee / tax / …

referenced by name, not nested:  accounts/  numberingPlans/  installmentPlans/  tables/
```

**When to introduce each layer:**

| Layer | Add it when… |
|---|---|
| **Exposure** | the policy holds repeating insured units (vehicles, locations, buildings, lives) — coverages hang off the exposure. *Skip it and attach coverages straight to the product's `contents` when the policy is one indivisible risk with no repeating units.* |
| **Sub-coverage** (`coverages/…/contents`) | a coverage decomposes into named parts that each carry their own terms/charges. |
| **Grouping / policy-line layer** | multi-location or multi-line packages (BOP, commercial package). *Not shape-documented here — see the authoritative `exposure-groups.md` / `policy-lines.md`.* |

**End-to-end wiring (2-coverage auto product).** Note how each name in a `contents`/`coverageTerms`/`charges` array must resolve to an entity folder of that type:

```
products/Auto            contents: ["Vehicle+"]
exposures/Vehicle        contents: ["Liability!", "Collision?"]
coverages/Liability      coverageTerms: ["BiLimit!"]          charges: ["Premium"]
coverages/Collision      coverageTerms: ["CollisionDed!"]     charges: ["Premium"]
coverageTerms/BiLimit    coverageTerms/CollisionDed           charges/Premium
```

Reading it, in plain terms — the quantifier suffix on each reference sets whether it's **mandatory** or **optional**:
- `+` (one or more) — an `Auto` policy must have at least one `Vehicle`, and can have many.
- `!` (**mandatory, auto-created**) — every `Vehicle` gets a `Liability` coverage automatically; the user can't leave it off. `Liability` likewise auto-creates its `BiLimit` term.
- `?` (**optional, may add**) — `Collision` is offered on a `Vehicle` but only attached if the user adds it.

Because `Liability` is mandatory (`!`) on the exposure, its mandatory (`!`) `BiLimit` term **must** define a default option — a mandatory/auto-created branch has to be fully satisfiable with no user input, so the obligation recurses all the way down (see Quantifiers).

---

## Universal rules (memorise these — most validation failures are here)

- **`decimal` for ALL numbers, never `integer`.** `integer` fails with "type [Integer] is not defined".
- **`min` / `max` are STRINGS**, not numbers: `"min": "0"` not `"min": 0`. (Applies to decimal + date.)
- **Every concrete entity needs `"abstract": false`.** `abstract: true` = a base template only `extend`ed, never instantiated.
- **Optional field** = append `?` to the type: `"type": "string?"`.
- **Names are PascalCase** for entities; **quantifier suffix goes on the reference**, not the definition.

## Data field types (`data: {}` on any entity)

| Type | Constraints |
|------|-------------|
| `string` | `minLength`, `maxLength` (numbers), `regex`, `options` (array of allowed values) |
| `decimal` | `min`, `max` (strings), `precision` (number), `roundingMode` (`ceiling`/`down`/`floor`/`halfDown`/`halfEven`/`halfUp`/`up`) |
| `boolean` | none |
| `date` | `min`, `max` (ISO strings, e.g. `"2020-01-01"`) |
| `timestamp` | date + time |

Every field type also accepts:
- `displayName` (string) — human label.
- `defaultValue` (string) — used when a required field is absent at validation; **required to auto-create (`!`) an entity that has required fields**.
- `scope` (string) — `QQ` (quick quote) / `Q` (quote) / `P` (policy), comma-combinable (`"Q, P"`).
- `tag` (array) — UI hints: `hidden`, `readOnly`, `multiselect` (needs array type + `options`), `radio-group`, `currency.<ISO>` (e.g. `currency.USD`).

Array field types: `string*` / `string+` (zero-or-more / one-or-more) — used with `multiselect`.

```jsonc
{
  "data": {
    "vehicleYear": { "type": "decimal", "displayName": "Year", "min": "1900", "max": "2030" },
    "middleName":  { "type": "string?", "displayName": "Middle Name", "maxLength": 50 },
    "vehicleType": { "type": "string", "displayName": "Type", "options": ["Sedan","SUV","Truck"] }
  }
}
```

## Quantifiers (on `contents` / `coverageTerms` / `charges` references)

| Suffix | Meaning |
|--------|---------|
| (none) | Exactly one, required, manual |
| `!` | Exactly one, **auto-created** |
| `?` | Zero or one (optional) |
| `*` | Zero or more |
| `+` | One or more |

**Auto-created (`!`)** entities MUST have: no required data fields without `defaultValue`, no
required coverage terms without a default option (`*`), and the same holds recursively for
sub-elements.

## Platform-managed fields — NEVER create these as custom data fields

- Dates: `policyStartDate`, `policyEndDate`, `effectiveDate`, `expirationDate`
- Identifiers: `policyNumber`, `quoteNumber`, `policyId`
- Status: `status`, `state`, `cancellationDate`
- Audit: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Financial: `totalPremium`, `writtenPremium`, `balance`

---

## Inheritance: `abstract` base + `extend`

`extend` lets one entity inherit another's `data`, `coverageTerms`, `charges`, and `contents`, then build on them. Use it to factor shared structure out of near-identical coverages / exposures / products / accounts.

- **Abstract base** (`"abstract": true`) — a template that is *never* instantiated on a policy; it exists only to be `extend`ed, so it's exempt from the mandatory/auto-create satisfiability rules.
- **Concrete child** (`"abstract": false`, `"extend": "BaseName"`) — inherits the base's members, adds its own, and (being concrete) must itself satisfy every `!` rule.
- **Merge, not replace** — when a child redefines an array/object the base also set (`coverageTerms`, `contents`, `charges`, `data`), the child's entries are **added** to the inherited ones (union). You don't re-list what you want to keep.

Worked example — two coverages sharing a base:

```jsonc
// coverages/AbstractPropertyCoverage/config.json  — never instantiated
{
  "abstract": true,
  "charges": ["Premium"],
  "coverageTerms": ["PropertyDeductible!"],
  "data": { "constructionType": { "type": "string", "options": ["Frame", "Masonry"] } }
}
```
```jsonc
// coverages/Dwelling/config.json  — inherits Premium + PropertyDeductible + constructionType,
// and adds DwellingLimit on top (merge)
{
  "abstract": false,
  "extend": "AbstractPropertyCoverage",
  "displayName": "Dwelling",
  "coverageTerms": ["DwellingLimit!"]
}
```

Gotchas:
- Referencing an `abstract: true` entity in a parent's `contents` fails — reference the concrete children, not the base.
- A typo'd or undefined name in `extend` fails validation (unresolved reference).

---

## `config.json` (root — Global Settings tab)

Required: `defaultCurrency`, `defaultTimeZone`, `defaultTermDuration`, `defaultDurationBasis`.

```jsonc
{
  "defaultCurrency": "USD",
  "defaultTimeZone": "America/New_York",
  "defaultTermDuration": 12,
  "defaultDurationBasis": "months",           // months | years | days
  "enableSerialInvoiceNumbering": false,
  "contactRoles": ["agent", "broker"],        // LOWERCASE or name-format validation rejects
  "lossCategories": ["property", "liability"] // LOWERCASE
}
```
Optional `default*Plan` keys (`defaultInstallmentPlan`, `defaultAutoRenewalPlan`,
`defaultDelinquencyPlan`, …) reference plans by name — omit if you have no such plan
(don't fabricate one). Align `defaultDurationBasis`/`defaultTermDuration` with the rating
convention (annual EU = `years`/`1`; monthly US = `months`/`12` or `6`).

## `products/<Name>/config.json`

```jsonc
{
  "displayName": "Auto Insurance",
  "abstract": false,
  "eligibleAccountTypes": ["ConsumerAccount"],   // each must exist as accounts/<Name>
  "contents": ["Vehicle+", "Liability!"],        // coverages/exposures/policyLines + quantifier
  "coverageTerms": ["PolicyDeductible!"],
  "charges": ["Premium", "PolicyFee"],
  "data": { "...": {} }
}
```
Optional: `extend`, `defaultInstallmentPlan`, `defaultDurationBasis`, `defaultTermDuration`,
`numberingTrigger` (`creation`), `numberingPlan` + `numberingString`, `staticData` (same
shape as `data`).

## `exposures/<Name>/config.json`

The repeating unit a policy holds many of (Vehicle, Location, Building). `contents` = the
coverages it carries.

```jsonc
{
  "displayName": "Vehicle",
  "abstract": false,
  "contents": ["Liability!", "Collision?", "Comprehensive?"],
  "coverageTerms": ["..."],
  "charges": ["Premium"],
  "data": { "vin": { "type": "string", "maxLength": 17 } }
}
```

## `coverages/<Name>/config.json`

```jsonc
{
  "displayName": "Liability Coverage",
  "abstract": false,
  "charges": ["Premium"],
  "coverageTerms": ["LiabilityLimit!", "LiabilityDeductible?"],
  "data": { "...": {} }
}
```
Optional: `extend`, `contents` (sub-coverages — see below).

### Sub-coverages (a coverage's own `contents`)

A coverage can itself contain child coverages — model this when one named coverage decomposes into parts that each carry their own terms/charges (e.g. Liability → Bodily Injury + Property Damage). The children are ordinary `coverages/<Name>/` entities referenced by name + quantifier, exactly the way an exposure references its coverages.

```jsonc
// coverages/Liability/config.json — BI + PD as sub-coverages
{
  "displayName": "Liability",
  "abstract": false,
  "contents": ["BodilyInjury!", "PropertyDamage!"],
  "charges": ["Premium"]
}
```
```jsonc
// coverages/BodilyInjury/config.json — a normal coverage, just referenced as a child
{
  "displayName": "Bodily Injury",
  "abstract": false,
  "coverageTerms": ["BiLimit!"],
  "charges": ["Premium"]
}
```

- Quantifier + recursion rules are unchanged: a mandatory (`!`) sub-coverage must be fully auto-satisfiable down its whole branch.
- Nesting can go deeper, but keep it shallow — deep trees are harder to rate and reason about.

## `coverageTerms/<Name>/config.json`

A term uses EITHER `options` (finite set) OR `value` (free input) — never both. `type` is
`limit` or `deductible`.

Options-based — prefix the default key with `*`:

```jsonc
{
  "type": "deductible",
  "displayName": "Collision Deductible",
  "options": {
    "ded250":  { "displayName": "$250",  "value": 250 },
    "*ded500": { "displayName": "$500",  "value": 500 },   // default
    "ded1000": { "displayName": "$1,000","value": 1000 }
  }
}
```

Value-based:

```jsonc
{ "type": "limit", "displayName": "Building Limit", "value": { "type": "decimal", "min": "10000", "max": "10000000" } }
```
- **Option keys must be lowercase** (`o_1000000`, `ded500`) — `O_1000000` fails name-format validation. Lowercase the default key too.
- A `!` term on its coverage needs a default option (`*`).

### Advanced coverage-term forms

Most terms are a simple options list or a single `value`. Complex products need a few more shapes:

- **Split limits** (e.g. `100/300` — $100k per person / $300k per occurrence). These are two independent limits, so model them as **two coverage terms** (`BiLimitPerPerson`, `BiLimitPerOccurrence`), each with its own options; a combined "100/300" label is presentation only. `[verify]` EC may also support a paired display via a `tag` on options — confirm the exact mechanism against the authoritative `coverage-terms.md` / a validateConfig run before collapsing it into a single term.
- **Per-occurrence vs. aggregate limits.** Same pattern — separate terms (`PerOccurrenceLimit`, `AggregateLimit`); don't try to encode both bounds in one term.
- **Percentage deductibles** (e.g. 2% of dwelling limit). Store the *percentage* as the option/value (e.g. `2`); **applying** it (limit × % → dollars) is a rating/loss-plugin computation, not something the term resolves on its own. `[verify]` whether EC has a dedicated percentage tag vs. a plain decimal value.
- **Free-input within bounds.** The value-based form (shown above) with `min`/`max` gives constrained free numeric entry — use when limits aren't a small finite set.

## `charges/<Name>/config.json`

```jsonc
{ "category": "premium", "handling": "normal", "invoicing": "scheduled", "transactionBundlingEnabled": false }
```
- **`category` is built-in, not extendable**: `premium` | `tax` | `fee` | `surcharge` | `credit` | `nonfinancial`. Remap outside-set template values (`commission` → `nonfinancial`, `discount` → `credit`).
- `handling`: `normal` | `flat`. `invoicing`: `immediate` | `next` | `scheduled`.
- **`invoicing: immediate` (and `next`) is NOT allowed with `handling: normal`** — use `handling: flat` for immediate fees.
- `nonfinancial` charges are tracked but never invoiced (commissions, technical premium).
- Charge and coverage may share a name (different namespaces).

## `installmentPlans/<Name>/config.json`

```jsonc
{
  "displayName": "Monthly Plan",
  "cadence": "monthly",              // fullPay | monthly | quarterly | semiannually | annually
  "anchorMode": "termStartDay",
  "dueLeadDays": 30,
  "generateLeadDays": 45,
  "maxInstallmentsPerTerm": 12,
  "installmentWeights": [1.0]        // proportions, one per installment; optional
}
```

## `numberingPlans/<Name>/config.json` (inferred — no workbook tab)

A product's `numberingString`/`numberingTrigger` **require** a numbering plan.

```jsonc
{ "displayName": "Policy Numbering", "format": "\\P\\O\\L-########", "initialCoreNumber": "10000001" }
```
- **Escape EVERY literal character with `\`** in `format`: `GL-######` → `"\\G\\L-######"` (unescaped letters fail "unrecognized character"). `#` = a sequence digit.
- `{product}` placeholder + product's `numberingString` → per-product prefix (`AUTO-100001`).
- Optional: `copyFromQuote`, `quoteNumberFormat`, `initialQuoteCoreNumber`.

## `accounts/<Name>/config.json` (inferred — no workbook tab)

Products reference `eligibleAccountTypes`; if the workbook has no account tab, define each
referenced type here and tell the user you inferred it.

```jsonc
{
  "displayName": "Consumer Account",
  "abstract": false,
  "data": {
    "firstName": { "type": "string", "displayName": "First Name", "maxLength": 100 },
    "lastName":  { "type": "string", "displayName": "Last Name",  "maxLength": 100 }
  }
}
```
Optional: `extend`, `numberingPlan` + `numberingTrigger`, `invoiceNumberingPlan`,
`defaultInvoiceDocument`, `contacts` (`{ "agent": ["Agent"] }`).

## `tables/<Name>/config.json` (rating lookup tables — used by the rating skills)

```jsonc
{
  "selectionTimeBasis": "termStartTime",   // termStartTime | transactionTime | policyStartTime | currentTime
  "columns": {
    "territory":  { "dataType": "string",  "isKey": true },
    "baseFactor": { "dataType": "decimal", "isKey": false }
  }
}
```
- `isKey: true` = lookup/filter column; `isKey: false` = returned value.
- `dataType`: `string` | `decimal` | `boolean` — **`decimal` for all numbers**.
- CSV data lives in `bootstrap/resources/resourceFiles/tables/`; CSV headers must match `columns` names exactly.

---

## What must be a plugin, not JSON (the config/plugin boundary)

Static config expresses **structure and fixed constraints** — which entities exist, their cardinality, finite option sets, and *constant* bounds/defaults. Anything **conditional, derived, or calculated** cannot live in the JSON and must be a plugin. When a requirement crosses this line, model the static part and **flag the dynamic part for a plugin** — never fake it with a hardcoded value (e.g. don't bake today's date into a `max`).

| Requirement | Where it lives |
|---|---|
| Entity/field exists; required / optional / repeating | schema — `data`, quantifiers (`! ? * +`) |
| Fixed bounds or pattern (`min`/`max`/`maxLength`/`regex`) | schema field keys — constants only |
| Finite allowed values | schema — `options` |
| Constant default | schema — `defaultValue`, or default option `*` |
| Coarse scoping (quote vs. policy, hidden/readOnly) | schema — `scope`, `tag` |
| **Cross-field / conditional validation** ("X required when Y = Z") | **validation plugin** — schema can't reference another field |
| **Conditional visibility / eligibility** | **plugin** — no "show if" key exists |
| **Derived / dynamic default** ("default to today", "= field A × 2") | **plugin** — `defaultValue` is a constant |
| **Premium, rating factors, modifiers** | **rate plugin + `tables/`** — factors are computed in the rate plugin, not a config entity |
| **Underwriting referral / decline rules** | **underwriting plugin** |
| **Endorsement / mid-term-change behavior** | **transaction types + plugin** |
| **External calls / webhooks / side effects** | **automation plugin** |

Cross-links: plugin authoring lives in the `socotra-plugin` skill; rating specifically in the `socotra-rating-plugin` / `socotra-rating-from-xlsx` skills. This mirrors the xlsx skill's own rule — a "can't be in the future" / "None / Today" cell is a business rule a static config can't enforce: leave the field plain and flag that it needs a validation plugin.

---

## Policy-lifecycle config surface (renewals, cancellation, endorsements)

Lifecycle *behavior* is plugin/logic territory (see the config/plugin boundary above), but the *config entities* that enable each lifecycle event are:

- **Renewals** — an **auto-renewal plan** controls automatic renewal timing/type; reference it from the product or root defaults via `defaultAutoRenewalPlan`. `[verify]` folder name (likely `autoRenewalPlans/`) and shape against the authoritative `auto-renewal-plans.md`.
- **Cancellation / non-payment** — a **delinquency plan** defines the grace period and the cancellation-for-nonpay timeline; reference via `defaultDelinquencyPlan`. Cancellation *reasons* and manual-cancellation flows are platform/plugin-driven, not shaped here.
- **Endorsements / mid-term changes** — modeled as **transaction types**; the *rules* for what a change does (proration, re-rating, eligibility) are plugin logic. `[verify]` transaction-type folder (likely `transactionTypes/`) and shape.

For the full shapes of these plans, see the authoritative `socotra-config` references (`auto-renewal-plans.md`, `delinquency-plans.md`); this file intentionally carries only the pointers.

---

## Build order & referential integrity

Author entities so every reference resolves, then verify the graph.

**Order (leaves → root):**
1. `charges/`, `coverageTerms/` — leaves, no outbound references.
2. `coverages/` — reference their terms + charges; do sub-coverage children before their parents.
3. `exposures/` — reference their coverages.
4. `products/` — reference exposures/coverages, terms, charges, plans, account types.
5. Cross-cutting: `accounts/`, `numberingPlans/`, `installmentPlans/`, `tables/`.
6. `config.json` root — defaults that reference plans by name.

**Referential-integrity checklist (run after building, and after any rename/removal):**
- Every name in a `contents` / `coverageTerms` / `charges` array resolves to an entity of the correct type.
- Every `eligibleAccountTypes` entry exists under `accounts/`.
- Every `default*Plan` / `numberingPlan` / `defaultInstallmentPlan` references an existing plan.
- No `contents` references an `abstract: true` entity.
- Every mandatory (`!`) branch is fully auto-satisfiable down to its leaves (required data fields have `defaultValue`; `!` terms have a `*` default option) — recursively.

---

## See also — authoritative `socotra-config` reference

This file is the distilled slice for the common-to-complex authoring path. For sections it deliberately doesn't shape, use the external `socotra-config` skill's references:

- Billing chain: `payments.md`, `disbursements.md`, `delinquency-plans.md`, shortfall-tolerance plans.
- Documents: `documents.md`.
- Multi-location / multi-line structure: `exposure-groups.md`, `policy-lines.md`.
- Lifecycle plans: `auto-renewal-plans.md`, `delinquency-plans.md`.
- Work-management / underwriting tasks: `work-management.md`.
- Tenant-creation resources (table CSVs, document templates): `bootstrap.md`.
