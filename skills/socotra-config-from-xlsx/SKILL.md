---
name: socotra-config-from-xlsx
description: Turn a Socotra "Config Template" Excel workbook into a deployable socotra-config. Bridges the xlsx-extract skill (reads the file) with this skill, which maps tabs/columns → config entities, carries the per-entity JSON schema for the common path (references/config-schema.md), and records the transform rules that make validateConfig pass. Use whenever a user hands you a Socotra business-requirements .xlsx and asks for a config. IMPORTANT: the template layout is NOT a contract — re-verify it against the actual file every time before trusting the mapping below.
---

# socotra-config-from-xlsx

Convert a Socotra **Config Template** workbook into a `socotra-config/` tree that passes `./gradlew validateConfig`.

This skill is the **bridge** plus the schema slice it needs — not a parser:

- **Read the file** → use the `xlsx-extract` skill (`python3 ~/.claude/skills/xlsx-extract/xlsx_extract.py FILE.xlsx --comments`). Don't hand-parse XML; don't reach for openpyxl.
- **Shape the JSON** → `references/config-schema.md` in this skill owns the per-entity shapes, data types, quantifiers, and platform fields for the common config-authoring path. Consult it for the exact JSON of each entity below. *(For a section it doesn't cover — automations, FNOL, work-management, and the like — the external `socotra-config` skill is the full authority, if installed.)*
- **Map between them** → this document: which tab feeds which entity, and the transform rules that turn human cells into valid config.

## ⚠️ Step 0 — re-verify the template before trusting anything below

The Excel format **will change** (new tabs, renamed columns, reordered fields, the tab-number drift already seen). Treat the mapping in this skill as *last known good*, not as truth. Before mapping, probe the live file and reconcile:

```bash
X=~/.claude/skills/xlsx-extract/xlsx_extract.py
python3 $X "FILE.xlsx" --list                 # tab names + indices
# then read each tab's instruction + header rows (rows ~1-4) and comments:
python3 $X "FILE.xlsx" --comments
```

Check these and **stop to reconcile if any drift**:
- Tab set differs from the table below (added/removed/renamed tabs).
- A tab's header row (the `(PascalCase)` / `(comma-separated)` labels) names columns this skill doesn't list, or in a different order — **map by column header text, never by fixed column letter**.
- Cells reference a tab number that no longer matches a tab name (the workbook's own "How to Use" tab has historically been out of sync with real tab names — trust `--list`, not the index).

If drift is material, derive the mapping from the live headers and **tell the user what changed** rather than silently following stale rules. When you finish a conversion against a changed template, update this skill's tables so the next run starts from a correct baseline.

**Green rows are examples — ignore them.** In this template, rows shaded green are illustrative sample data, not real config, and must be excluded from the conversion. Detect them by cell fill: read the sheet's fill styling (e.g. `xlsx_extract.py ... --styles`/`--fills`, or inspect `xl/styles.xml` + each cell's `s=` style index for a green `fgColor`) and drop any row whose cells carry that green fill. If fill color can't be resolved for a given file, **don't guess** — tell the user you can't distinguish the green example rows and ask them to confirm which rows are examples before building.

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
- A term marked **mandatory (`!`)** on its coverage needs a **default option** (`*`). If Part A gives no default and the options include an "Excluded" choice, default to `*..._excluded`. If a term has a single implied option (default key but no Part B rows), synthesize that one option from the key/value.
- The template's `type` may say `other` (e.g. ERP, Credit Monitoring). `references/config-schema.md` documents only `limit`/`deductible`; if `other` is rejected by validation, model it as a `limit` with explicit options (e.g. excluded/included or year choices). Verify against the live schema rather than assuming.

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

If the user says "you decide," make the call using the transform rules and your Socotra/insurance knowledge, then **record it as an inferred decision** in the final report (see workflow step 6). If they give an answer, use it verbatim. Never let a gap silently become a guess that isn't flagged.

The specific fallbacks named in the rules above (union of term containment, `*..._excluded` defaults, `commission → nonfinancial`, etc.) are exactly the **recommended options** to present here — not licenses to skip the conversation.

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
- Writing cells back into an `.xlsx` needs a writer library (e.g. `openpyxl`); the `xlsx-extract` skill is **read-only** and does not do this. If no writer is available, `cp` the latest file to the new version name, tell the user which cells still need updating, and note it in the report rather than silently skipping the version bump.

## Workflow

1. **Step 0 credibility check** (above) — `--list` + headers + comments; reconcile drift.
2. Extract every tab (text format) and the comments.
3. **Conflict + gap pass** (above) — scan for contradictions and missing values; ask the user about each, offering options and (for gaps) the "let me decide" path. Collect answers before building.
4. **Version the spreadsheet** (above) — capture this round's decided changes in the next `...-agent-vN` copy, forked from the latest existing version (or the original, for v1). Never edit the original.
5. Build `socotra-config/` per the map + transform rules and the user's decisions; create inferred entities (accounts, numbering plan).
6. **Validate — tiered** (see "Validation without tenant credentials" below). Always run the local lint first; run `./gradlew validateConfig` (the only allowed gradle validation command here) when tenant credentials exist. Fix reported errors — most map to a rule above. If an error exposes a *new* conflict or gap, loop back to step 3 (and bump to the next `-agent-vN` for that round's fixes) rather than guessing.
7. Report: what was built, the current spreadsheet version name, every **inferred/remapped/skipped** decision (including anything the user asked you to decide), any unresolved questions, and (if the template drifted) what changed + which rules you updated.

Deployment (`createTenant`/`deployConfig`/etc.) is the **user's** to run, not yours.

## Validation without tenant credentials

`./gradlew validateConfig` is **remote**: it zips the bundle and uploads it to the tenant kernel API, so it needs `apiUrl` + `tenantLocator` + `personalAccessToken` in the SDK project's `settings.gradle.kts` (plus `GITHUB_USER`/`GITHUB_TOKEN` just to resolve the gradle plugin). Users may not want to hand these over.

**Detect first, never assume.** Before invoking gradle, check the SDK project:
```bash
grep -E 'tenantLocator|personalAccessToken|apiUrl' settings.gradle.kts
```
Credentials are *absent* when the block is missing, values are empty/placeholder, or they read from env vars (`System.getenv`) that aren't set. Don't run `validateConfig` just to see it fail — and never ask the user to paste a PAT into chat.

**Tiered validation:**
1. **Tier 1 — local lint (always, no credentials):** `python3 <this skill>/scripts/lint_config.py socotra-config`. Stdlib-only; encodes the failure classes in `references/config-schema.md` (types, name formats, dangling references, `!`-term defaults, charge handling/invoicing, numbering escapes) plus orphan detection. Exit 0 = clean.
2. **Tier 2 — remote `validateConfig` (when credentials exist):** the authority. Run it and fix; on disagreement with the lint, the remote result wins — then fix the lint/reference.
3. **No credentials:** deliver after a clean Tier 1, and say exactly this in the report: *locally linted only — remote `validateConfig` still required before deploy* (user runs it themselves, or supplies locator + PAT). Never present a lint-only config as validated.

**Credential hygiene:** if the user does provide credentials, put them in env vars read via `System.getenv(...)` in `settings.gradle.kts` — never hardcode a PAT into a file that gets committed, and never echo a PAT into output or logs.

## Maintenance

This skill encodes a moving target. When you hit a *new* validation error or a *changed* template and resolve it, add the rule/column here so the baseline stays credible. If a rule above ever contradicts a fresh `validateConfig` result, the live result wins — fix the file, then fix this skill.
