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
- A term marked **auto-created (`!`)** on its coverage needs a **default option** (`*`). If Part A gives no default and the options include an "Excluded" choice, default to `*..._excluded`. If a term has a single implied option (default key but no Part B rows), synthesize that one option from the key/value.
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

## Workflow

1. **Step 0 credibility check** (above) — `--list` + headers + comments; reconcile drift.
2. Extract every tab (text format) and the comments.
3. Build `socotra-config/` per the map + transform rules; create inferred entities (accounts, numbering plan).
4. `./gradlew validateConfig` (the only allowed validation command here). Fix reported errors — most map to a rule above.
5. Report: what was built, every **inferred/remapped/skipped** decision, and (if the template drifted) what changed + which rules you updated.

Deployment (`createTenant`/`deployConfig`/etc.) is the **user's** to run, not yours.

## Maintenance

This skill encodes a moving target. When you hit a *new* validation error or a *changed* template and resolve it, add the rule/column here so the baseline stays credible. If a rule above ever contradicts a fresh `validateConfig` result, the live result wins — fix the file, then fix this skill.
