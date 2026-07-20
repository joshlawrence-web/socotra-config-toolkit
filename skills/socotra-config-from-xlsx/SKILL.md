---
name: socotra-config-from-xlsx
description: 'Turn a Socotra "Config Template" Excel workbook into a deployable socotra-config. Bridges the xlsx-extract skill (reads the file by hand) with this skill, which maps tabs/columns → config entities, carries the per-entity JSON schema for the common path (references/config-schema.md), and records the transform rules that make validateConfig pass. Runs with NO network and NO code execution: the workbook is read by hand via the xlsx-extract skill, and the config audit is a prescriptive checklist you apply by reading files — nothing to run; reference files are inert. Use whenever a user hands you a Socotra business-requirements .xlsx and asks for a config. IMPORTANT: the template layout is NOT a contract — re-verify it against the actual file every time before trusting the mapping below.'
---

# socotra-config-from-xlsx

Convert a Socotra **Config Template** workbook into a `socotra-config/` tree that passes `./gradlew validateConfig`.

This skill runs with **no network and no code execution.** You never run a parser, a linter, or any script here: the workbook is read **by hand** via the `xlsx-extract` skill, and the config audit at the end of this document is a checklist you apply by **reading** the generated files and reasoning. The only files that ship with this skill are inert reference docs. `./gradlew validateConfig` is the one remote step, and it is the **user's** to run.

This skill is the **bridge** plus the schema slice it needs — not a parser:

- **Read the file** → use the `xlsx-extract` skill, which reads an `.xlsx` **by hand** (`unzip` the file, then `Read`/`Grep` the XML parts — `sharedStrings.xml`, `workbook.xml` + rels for tab order/names, `worksheets/sheetN.xml` for cells, the comments parts, `styles.xml` for fills). There is no `xlsx_extract.py`, no `openpyxl`, no `pandas` — reading the XML directly *is* the method.
- **Shape the JSON** → `references/config-schema.md` in this skill owns the per-entity shapes, data types, quantifiers, and platform fields for the common config-authoring path. Consult it for the exact JSON of each entity below. *(For a section it doesn't cover — automations, FNOL, work-management, and the like — the external `socotra-config` skill is the full authority, if installed.)*
- **Map between them** → this document: which tab feeds which entity, and the transform rules that turn human cells into valid config.

## ⚠️ Step 0 — re-verify the template before trusting anything below

The Excel format **will change** (new tabs, renamed columns, reordered fields, the tab-number drift already seen). Treat the mapping in this skill as *last known good*, not as truth. Before mapping, probe the live file with the **`xlsx-extract` skill's by-hand method** and reconcile:

- **List the tabs first** — use the `xlsx-extract` skill to build the ordered tab list from `xl/workbook.xml` + `xl/_rels/workbook.xml.rels` (name → `sheetN.xml`, in true display order). This is the equivalent of the old tab listing — trust it, never file order or a "How to Use" tab's numbering.
- **Read each tab's headers + comments** — for every tab, read its instruction/header rows (rows ~1–4) from `worksheets/sheetN.xml`, resolving text through `sharedStrings.xml`, and read the cell comments (via the sheet's `.rels` → the `comments*.xml` part). Comments hide "see other tab" notes, examples, and field hints — don't skip them.

Check these and **stop to reconcile if any drift**:
- Tab set differs from the table below (added/removed/renamed tabs).
- A tab's header row (the `(PascalCase)` / `(comma-separated)` labels) names columns this skill doesn't list, or in a different order — **map by column header text, never by fixed column letter**.
- Cells reference a tab number that no longer matches a tab name (the workbook's own "How to Use" tab has historically been out of sync with real tab names — trust the ordered tab list you resolved from `workbook.xml` + rels, not the index).

If drift is material, derive the mapping from the live headers and **tell the user what changed** rather than silently following stale rules. When you finish a conversion against a changed template, update this skill's tables so the next run starts from a correct baseline.

**Green rows are examples — ignore them.** In this template, rows shaded green are illustrative sample data, not real config, and must be excluded from the conversion. Reading a cell's *value* doesn't reveal its fill, so distinguish example rows with the **`xlsx-extract` skill's style-detection method** (its optional style/fill step) — no script or extra dependency, just `unzip` + the `Read`/`Grep` tools and your own index-mapping:

1. **Crack the file open** — an `.xlsx` is a zip: `unzip -o "FILE.xlsx" -d <dir>` (or `unzip -p "FILE.xlsx" xl/styles.xml` to stream one member). The parts you need are `xl/styles.xml`, `xl/workbook.xml` (+ `xl/_rels/workbook.xml.rels`), and `xl/worksheets/sheetN.xml`.
2. **Find the green fill's index** — in `xl/styles.xml`, `Read` the `<fills>` list. Each `<fill>`'s **0-based position** is its `fillId`. Find the one with a green solid `fgColor rgb`. Observed palette in this template family (trust the *rgb you actually see*, not these constants — they drift): green example = **`FFE2EFDA`**, blue fill-in row = `FFD9E1F2`, dark-blue header = `FF1F3864`, yellow note = `FFFFF2CC`.
3. **Map fillId → style index** — `Read` the `<cellXfs>` list; each `<xf>`'s **0-based position** is a cell style index (the value cells carry in their `s="…"` attribute). Note every `<xf>` whose `fillId=` matches the green fill — those style indices mark green cells.
4. **Find the green rows** — resolve the sheet name to its `sheetN.xml` via `workbook.xml` + the rels file (the `📋 How to Use` tab's numbering is unreliable — map by `r:id`, not by position). In that sheet XML, any `<row>` whose data `<c … s="…">` cells use a green style index is an example row → **drop it**.
5. **Cross-check against values** — a green row almost always carries the sample names from the `How to Use` tab (e.g. `PersonalAuto`, `VehicleBase`, `PersonalVehicle`); the real config is the *other* product(s) and their entities. In the CGL template, everything under `PersonalAuto` is green (example) and only `CommercialGeneralLiability` is real.

If the fill color can't be resolved for a given file, or the palette is ambiguous, **don't guess** — tell the user you can't distinguish the green example rows and ask them to confirm which rows are examples before building.

## Tab → entity map (last known good)

The workbook is one row = one entity. Names are PascalCase; quantifier suffixes (`! ? + *`, blank) come straight from the cells. For each entity below, `references/config-schema.md` has the exact `config.json` shape and constraints.

| Tab (by name) | Produces | socotra-config location |
|---|---|---|
| `1 - Global Settings` | tenant settings | `config.json` (root) |
| `2 - Products` | product(s) | `products/<Name>/config.json` |
| `3 - Exposures` | exposures + their coverage containment | `exposures/<Name>/config.json` |
| `4 - Coverages` | coverages | `coverages/<Name>/config.json` |
| `5 - Coverage Terms` | terms (Part A) + options (Part B) | `coverageTerms/<Name>/config.json` |
| `8 - Data Fields` | `data{}` blocks on product/exposure/coverage | merged into the parent entity's `config.json` |
| `9 - Charges` | charge types | `charges/<Name>/config.json` |
| `10 - Installment Plans` | installment plans | `installmentPlans/<Name>/config.json` |

Things the template implies but does **not** give its own tab — create them yourself and tell the user you inferred them:
- **Accounts** — products reference `eligibleAccountTypes`; if there's no account tab, define `accounts/<Name>/config.json`.
- **Numbering plan** — see the numbering rule below.

## Per-tab column semantics + transform rules

These are the conversions that make the difference between "looks right" and "validateConfig passes". Most were learned from real validation failures against the sandbox tenant.

### Config root directory
Write the tree under **`socotra-config/`** at the project root. That is what the `socotra-ec-config-developer` Gradle plugin packages. (`config/currentlyDeployed/` is the *download* target for `downloadConfigAndPlugins`, **not** the source dir — putting your config there yields `packageConfigBundle NO-SOURCE`.)

### 1 - Global Settings
- `defaultCurrency`, `defaultTimeZone`, `defaultTermDuration`, `defaultDurationBasis` map directly.
- `Enable Serial Invoice Numbering` → `enableSerialInvoiceNumbering` (bool; `1`/`true`).
- **`contactRoles` and `lossCategories` must be lowercase** (`agent`, not `Agent`; `liability`, not `Liability`) or validation rejects the name format.
- Cells describing settings with **no known schema key** (e.g. a "Billing Trigger" cell) → omit rather than invent a key; note the omission to the user.
- Blank "default plan" cells → leave the setting out (don't fabricate a default plan name).

### 2 - Products
- `Duration Basis = "global default"` → inherit; set the product's basis to the global one (or omit to inherit).
- `Eligible Account Types` (comma-separated) → `eligibleAccountTypes` array; ensure each account type exists as an entity.
- **Product Numbering** (e.g. `GL-######`): a product's `numberingString`/`numberingTrigger` **require** a `numberingPlan`. So create `numberingPlans/<Name>/config.json`, and in the `format` **escape every literal character with `\`**: `GL-######` → `"\\G\\L-######"` (unescaped letters fail with "unrecognized character"). `#` = sequence digit. Reference the plan from the product.
- Exposures attach to the product via `contents` using the exposure name + a quantifier (commonly `Location+`). A coverage whose parent is the product (not an exposure) goes directly in the product's `contents`.

### 3 - Exposures
- The **"Coverages with Quantifiers"** column is the authoritative containment for that exposure → exposure `contents`.
- `Abstract?` column may be `0`/`1` or `true`/`false` → `abstract` bool.

### 4 - Coverages
- `Coverage Terms with Quantifiers` → coverage `coverageTerms`; `Charges` → coverage `charges`.
- **Reconcile with tab 5's "Used On Coverages"**: a term may be listed there but missing from a coverage's term cell (or vice-versa). Take the union and tell the user which one you trusted and why.

### 5 - Coverage Terms (two parts)
- Part A defines the term (`type`, display, used-on, default option key prefixed `*`). Part B lists each option (key, display, numeric value).
- **Option keys must be lowercase** snake/`o_1000000`-style — `O_1000000` fails name-format validation. Lowercase the default key too.
- A term marked **mandatory (`!`)** on its coverage needs a **default option** (`*`). Pick the default in this order: (a) if the options include an "Excluded" choice, default to `*..._excluded`; (b) if a term has a single implied option (default key but no Part B rows), synthesize that one option from the key/value and make it the default; (c) if the term has options but none is "Excluded" and Part A names no default, default to the **lowest / most conservative** value (e.g. the smallest deductible); (d) if a mandatory term has **no options and no default at all** (seen with `other`-type terms like Credit Monitoring), synthesize a two-option `excluded`(value 0) / `included`(value 1) set and default to `*..._excluded`.
- The template's `type` may say `other` (e.g. ERP, Credit Monitoring). `references/config-schema.md` documents only `limit`/`deductible`; if `other` is rejected by validation, model it as a `limit` with explicit options (e.g. excluded/included or year choices — give the "Excluded" option a numeric `value` of `0`). Verify against the live schema rather than assuming.
- **Orphaned / misplaced entities** (found via the referential-integrity pass): if a term's `Used On Coverages` (tab 5) names a coverage whose own term cell (tab 4) omits it, attach it as **optional (`?`)** — the least-invasive union that keeps it satisfiable. If a coverage is defined (tab 4) but appears in **no** exposure's `contents` (tab 3) and no product `contents`, don't leave it dangling: attach it where it belongs by domain — a policy-wide coverage (e.g. Waiver of Subrogation) goes on the **product** `contents` as `?`, a per-unit coverage on the exposure. Record the placement as an inferred decision.

### 8 - Data Fields
- Group rows by `Parent Entity` + `Entity Type` and merge into that entity's `data{}`.
- **`Required?`**: `yes` → plain type (`"string"`); `no` → optional, append `?` (`"string?"`).
- **Use `decimal` for all numbers** (never `integer`); `min`/`max` as **strings**.
- `Options` (comma-separated) → string field `options` array.
- A `Min/Max` like `None / Today` or "can't be in the future" is a business rule a static config can't enforce — leave the field plain and flag that it needs a validation plugin; don't hardcode today's date.
- **Watch for incomplete rows** (a field name with no parent/type/required) — skip and surface them to the user instead of guessing.

### 9 - Charges
- **`category` must be a built-in**: `premium`, `tax`, `fee`, `surcharge`, `credit`, `nonfinancial`. Template categories outside this set need remapping — observed: `commission` → `nonfinancial`, `discount` → `credit`.
- **`handling` + `invoicing` interact**: `invoicing: immediate` (and `next`) is **not allowed** with `handling: normal` — use `handling: flat` for immediate fees.
- Charges and coverages can share a name (different namespaces) — a `BodilyInjury` charge and a `BodilyInjury` coverage coexist fine.

### 10 - Installment Plans
- Map cadence to the schema enum (`fullPay`, `monthly`, `quarterly`, `semiannually`, `annually`); `Max Installments Per Term` → `maxInstallmentsPerTerm`; `Generate Lead Days`/`Due Lead Days` → `generateLeadDays`/`dueLeadDays`. Set `anchorMode` (e.g. `termStartDay`).

## Handling conflicts and gaps — ask, don't assume

The transform rules above tell you *how* to shape valid config; this section governs *when to stop and talk to the user*. Do this pass **after extraction, before building** — and again whenever a `validateConfig` error exposes a fresh conflict or gap. Default to a conversation, not a silent decision.

### Conflicting information → surface it, offer fixes, let the user pick

A conflict is any place the spreadsheet says two incompatible things. Watch especially for:
- A coverage's `Coverage Terms with Quantifiers` (tab 4) vs. a term's `Used On Coverages` (tab 5) disagreeing about containment.
- The same entity named two ways (spelling/casing/quantifier) across tabs.
- A cell that references a tab, plan, account type, or coverage that doesn't exist elsewhere in the workbook.
- A value that contradicts a rule above (e.g. a charge `category` or `handling`/`invoicing` combo that can't both hold; a default option key with no matching option row).
- Numbers/settings that can't co-exist (e.g. `maxInstallmentsPerTerm` inconsistent with the stated cadence).

When you find one, **don't quietly pick a side.** Raise it conversationally and lay out the concrete options, e.g.:

> Heads up — tab 4 lists `BodilyInjury` under the `Auto` coverage, but tab 5's `Used On Coverages` for that term only names `Liability`. How should I resolve it?
> - **A** — Trust tab 4 (attach the term to `Auto`).
> - **B** — Trust tab 5 (attach it to `Liability` only).
> - **C** — Take the union (attach to both).
>
> I'd lean toward **C** since the two tabs usually drift rather than contradict, but your call.

Use the `AskUserQuestion` tool when the options are clean and enumerable; use plain prose when the conflict needs explaining first. Always include a recommended option with a one-line *why*. Only proceed on your recommendation without an answer if the user has told you to stop asking.

### Missing information → ask first, but offer to decide for them

When a required field, key, quantifier, default option, or inferred entity (account type, numbering plan) is absent, **don't assume a value.** Ask the user — and in the same breath offer to fill it in yourself from Socotra + insurance domain knowledge, so they can delegate if they'd rather not decide. For example:

> The `GeneralLiability` product doesn't list any `eligibleAccountTypes`. Two ways to go:
> - **Tell me** which account type(s) apply, or
> - **Let me decide** — for a commercial GL product I'd define a single `Organization` account type and reference it.
>
> Which do you prefer?

If the user says "you decide," make the call using the transform rules and your Socotra/insurance knowledge, then **record it as an inferred decision** in the final report (see the workflow's report step). If they give an answer, use it verbatim. Never let a gap silently become a guess that isn't flagged.

The specific fallbacks named in the rules above (union of term containment, `*..._excluded` / lowest-value / synthesized `excluded`-`included` defaults, `commission → nonfinancial`, orphaned term → optional, unplaced policy-wide coverage → product `contents`, etc.) are exactly the **recommended options** to present here — not licenses to skip the conversation.

## Never edit the original — version the spreadsheet that represents the config

The workbook is the source of truth for the config, so it must stay in sync with every change (conflict resolution, gap fill, remap) — but the **original file is never modified.** Instead, each round of changes is captured in a new, versioned copy, and each version forks from the *latest* version, not the original:

- **First change** → copy the original and apply the change to the copy. Name it `<original-basename>-agent-v1`. Example: `product-template.xlsx` → `product-template-agent-v1.xlsx`.
- **Second change** → fork from `...-agent-v1` (not the original) → `product-template-agent-v2.xlsx`.
- **Nth change** → always base the new copy on `...-agent-v(N-1)`, name it `...-agent-v(N)`.

Rules:
- A "change" = one coherent round of edits (e.g. everything you resolved in a single conflict+gap pass, or the fixes from one `validateConfig` loop). Don't spawn a version per cell; do spawn one per pass so the history is legible.
- Always start from the **latest** `-agent-vN` copy — determine N by listing existing `...-agent-v*` files and taking the highest, so versions never collide or reset.
- The copy must reflect the *decided* values (what the user chose or delegated to you), so the latest version is always a faithful, deployable-intent snapshot of the config.
- Leave the original and every prior version untouched — they are the audit trail.
- Writing cells back into an `.xlsx` is **out of scope** here — there is no code execution, so no writer library is available and the `xlsx-extract` method is read-only. `cp` the latest file to the new version name, tell the user which cells still need updating (cell ref + old → new value), and note it in the report rather than silently skipping the version bump.

## Config audit checklist (apply by reading, don't run anything)

Before `./gradlew validateConfig`, **audit the generated tree by reading the `config.json` files and reasoning** — there is no linter to run. This catches the documented failure classes locally so the remote validation has less to reject. A clean audit still needs the remote `validateConfig` before deploy. Walk every check below; each maps to a real validation failure. Report anything you can't satisfy rather than guessing.

**Known sections** (folders that carry linted entities): `accounts`, `products`, `policyLines`, `exposureGroups`, `exposures`, `coverages`, `coverageTerms`, `charges`, `installmentPlans`, `numberingPlans`, `tables`. Any other section (automations, documents, delinquencyPlans, dataTypes, …) is fine to have — just confirm its `config.json` files are valid JSON; the entity-level checks below target the known sections. **Containable** sections (referenceable via `contents`): `policyLines`, `exposureGroups`, `exposures`, `coverages`.

### Structure
- **Every entity is `<section>/<Name>/config.json`.** Each entity folder has a `config.json`; missing one is an error.
- **Each `config.json` is valid JSON.** A parse failure is an error — read it and fix the syntax.
- **The body is the entity itself, not wrapped in its own name key.** `coverages/Liability/config.json` holds `{ "abstract": false, … }`, NOT `{ "Liability": { … } }`. A body whose single top-level key equals the folder name is the wrap bug.
- **The root `config.json` holds global settings only, not entity maps.** If the root file contains a `products` / `coverages` / `charges` / … object, that's wrong — those entities live in `<section>/<Name>/config.json` folders, not inline in the root.

### Root config.json (Global Settings)
- **Required keys present:** `defaultCurrency`, `defaultTimeZone`, `defaultTermDuration`, `defaultDurationBasis`. A missing one is an error.
- **`contactRoles` and `lossCategories` are all-lowercase.** `"Agent"` / `"Liability"` fail name-format validation — must be `"agent"` / `"liability"`.

### Every concrete entity (accounts, products, policyLines, exposureGroups, exposures, coverages)
- **Has `"abstract"` as a boolean** (`false` for concrete, `true` only for base templates that are `extend`ed, never instantiated). Missing/non-boolean `abstract` is an error.
- **`contents` references resolve.** `contents` must be an array; each entry, with its quantifier suffix (`! ? * +`) stripped, must name a defined **containable** entity (a `policyLines` / `exposureGroups` / `exposures` / `coverages` folder). A dangling name is an error.
- **`coverageTerms` references resolve.** Array; each stripped name must be a defined `coverageTerms/<Name>`.
- **`charges` references resolve.** Array; each stripped name must be a defined `charges/<Name>`.

### Data fields (`data: {}` on any entity) — CRITICAL, includes the `state`→`locationState` bug class
- **`data` is an object**; each field spec is an object.
- **Never redefine a platform-managed field name.** These names are owned by the platform and must NOT appear as keys in any entity's `data{}`. If the workbook asks for one, **rename it** (e.g. `state` → `locationState`, `status` → `riskStatus`, `balance` → `accountBalance`) and flag the rename to the user. The reserved set:
  - **Dates:** `policyStartDate`, `policyEndDate`, `effectiveDate`, `expirationDate`
  - **Identifiers:** `policyNumber`, `quoteNumber`, `policyId`
  - **Status:** `status`, `state`, `cancellationDate`
  - **Audit:** `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
  - **Financial:** `totalPremium`, `writtenPremium`, `balance`
- **Never `type: "integer"`.** It fails "type [Integer] is not defined" — use `"decimal"` for anything financial (or `"int"`). The built-in type set is `string`, `int`, `long`, `decimal`, `datetime`, `date`, `boolean`, `object`. A base type outside this set is only valid if it names a custom `dataTypes/<Name>` entity — otherwise it's an unknown-type error. (Strip any `? * +` quantifier from the type before checking the base.)
- **`min` / `max` are STRINGS**, not numbers: `"min": "0"`, never `"min": 0`. A numeric bound is an error.
- **`options` is an array** when present.

### Products
- **`eligibleAccountTypes` resolve** to defined `accounts/<Name>` entities.
- **`defaultInstallmentPlan` resolves** (quantifier stripped) to a defined `installmentPlans/<Name>`.
- **Numbering requires a plan:** if a product sets `numberingString` or `numberingTrigger`, it MUST also set `numberingPlan`, and that `numberingPlan` must name a defined `numberingPlans/<Name>`.

### Coverage terms
- **Exactly one of `options` / `value`.** A term with both, or neither, is an error.
- **At most one default option.** In an options-based term, the default option's key is prefixed with `*`. More than one `*` key is an error.
- **Option keys are valid identifiers** — start with a letter or underscore, then letters/digits/underscores only (`[A-Za-z_]\w*`, after stripping a leading `*`). An invalid key is an error.
- **Prefer a lowercase-start option key.** Docs allow any identifier, but an uppercase-start key like `O_1000000` was observed rejected by `validateConfig` while `o_1000000` / `zeroDeductible` deploy — treat uppercase-start as a verify-against-validateConfig, not an auto-reject, and prefer lowercasing it.
- **Auto-created (`!`) terms need a default.** If any entity references a term with a `!` quantifier and that term is options-based, one of its option keys MUST be a default (`*`). A `!` term with options but no `*` default is an error.

### Charges
- **`category` is present** and is a built-in: `premium`, `tax`, `fee`, `surcharge`, `credit`, `nonfinancial`. A missing category is an error. A category outside the set is a verify-against-validateConfig (deployed configs show extras like `invoiceFee`/`nonFinancial`, so don't hard-fail) — remap the common template values: `commission` → `nonfinancial`, `discount` → `credit`.
- **`handling: normal` forbids `invoicing: immediate` or `next`.** (Handling defaults to `normal` when absent.) An immediate/next fee needs `handling: flat` — the `normal` + `immediate`/`next` combo is an error.

### Numbering plans
- **Every literal character in `format` is escaped with `\`.** After removing escaped chars (`\X`), placeholders (`{product}` etc.), and `#` sequence digits, no bare `[A-Za-z]` letter may remain. `GL-######` must be `"\\G\\L-######"` — an unescaped letter fails "unrecognized character".

### Orphans (soft check — surface, don't necessarily fail)
- **Flag any `coverages`, `coverageTerms`, `charges`, `exposures`, `policyLines`, or `exposureGroups` entity referenced by nothing** (no `contents` / `coverageTerms` / `charges` / `eligibleAccountTypes` anywhere points at it). `validateConfig` may tolerate orphans, but an unreferenced entity usually signals a missed containment — reconcile it per the orphaned/misplaced-entity rule under tab 5 rather than leaving it dangling.

## Workflow

1. **Step 0 credibility check** (above) — with the `xlsx-extract` skill: resolve the ordered tab list, then read each tab's headers + comments; reconcile drift.
2. Read every tab's cells and comments by hand (the `xlsx-extract` method), resolving shared strings and tab order.
3. **Conflict + gap pass** (above) — scan for contradictions and missing values; ask the user about each, offering options and (for gaps) the "let me decide" path. Collect answers before building.
4. **Version the spreadsheet** (above) — capture this round's decided changes in the next `...-agent-vN` copy, forked from the latest existing version (or the original, for v1). Never edit the original.
5. Build `socotra-config/` per the map + transform rules and the user's decisions; create inferred entities (accounts, numbering plan).
6. **Config audit** (above) — read every generated `config.json` and walk the checklist; fix everything you can catch by reasoning (platform-field renames, `integer`→`decimal`, string bounds, dangling refs, default options, numbering escapes, charge category/handling). Nothing to run — this is a read-and-reason pass.
7. `./gradlew validateConfig` (the only allowed validation command here, and the user's to run). Fix reported errors — most map to a rule above. If an error exposes a *new* conflict or gap, loop back to step 3 (and bump to the next `-agent-vN` for that round's fixes) rather than guessing.
8. Report: what was built, the current spreadsheet version name, every **inferred/remapped/skipped** decision (including anything the user asked you to decide), any unresolved questions, and (if the template drifted) what changed + which rules you updated.

Deployment (`createTenant`/`deployConfig`/etc.) is the **user's** to run, not yours.

## Maintenance

This skill runs with **no network and no code execution**: the workbook is read **by hand** via the `xlsx-extract` skill, the config audit is a **checklist you apply by reading**, and the only shipped files are inert reference docs (`references/config-schema.md`). Nothing here is meant to be executed — this skill previously carried a `scripts/lint_config.py` linter and pointed at an `xlsx_extract.py` parser; both are gone, and their rules now live as prose (the audit checklist encodes every check the linter performed). Don't reintroduce runnable scripts, `openpyxl`, or any network call; if you find yourself wanting to "run" a check, turn it into a checklist item instead.

This skill encodes a moving target. When you hit a *new* validation error or a *changed* template and resolve it, add the rule/column/audit-item here so the baseline stays credible. If a rule above ever contradicts a fresh `validateConfig` result, the live result wins — fix the file, then fix this skill.
