---
name: socotra-config-from-scratch
description: >-
  Context for building a deployable socotra-config folder tree from free-form
  (non-xlsx) product requirements — conversation, docs, notes, or PDFs. Sibling
  of socotra-config-from-xlsx: same output contract, same shared schema, same
  validateConfig-oriented audit. Use when there is no Config Template workbook;
  if an .xlsx template exists, use socotra-config-from-xlsx instead. Knowledge
  only — no agent UX, confirmation loops, or tooling orchestration.
---

# socotra-config-from-scratch

Knowledge for turning free-form product requirements into a `socotra-config/`
tree that can pass `./gradlew validateConfig`.

This skill is **context only** (for UI / chatbot integration). It does not
define conversational agent behavior, approval loops, or tool orchestration.
Schema shapes and hard-won validation rules live in one shared place — do not
restate them here.

## When to use which skill

| Situation | Skill |
|---|---|
| Socotra **Config Template** `.xlsx` present | `socotra-config-from-xlsx` |
| Free-form requirements (chat, doc, notes, PDF; no workbook) | **this skill** |

## Sources of truth

- **Entity JSON shapes + universal validateConfig rules** →
  `../socotra-config-from-xlsx/references/config-schema.md`
  (composition, universal rules, quantifiers, platform fields, per-entity shapes)
- **Pre-validateConfig audit checks** →
  `../socotra-config-from-xlsx/SKILL.md` § "Config audit checklist"
- **Sections not shaped there** (automations, FNOL, work-management, documents,
  payments, …) → external `socotra-config` reference if available; otherwise
  those sections are out of scope — do not invent shapes
- **`validateConfig` wins** over every reference. On conflict: fix the config,
  then fix `config-schema.md` so both siblings inherit the correction

## Output contract (identical to the xlsx sibling)

Emit the standard **folder tree** under `socotra-config/`.

**Never** emit one big nested `config.json` with inline `accounts` / `products` /
`coverages` maps. That single-file convention is obsolete — the packaging plugin
and the audit checklist expect per-entity folders.

```
socotra-config/
├── config.json                         # root globals ONLY
├── accounts/<Name>/config.json
├── products/<Name>/config.json
├── policyLines/<Name>/config.json      # only if needed
├── exposureGroups/<Name>/config.json   # only if needed
├── exposures/<Name>/config.json
├── coverages/<Name>/config.json
├── coverageTerms/<Name>/config.json
├── charges/<Name>/config.json
├── installmentPlans/<Name>/config.json # only if referenced
└── numberingPlans/<Name>/config.json   # only if product numbering needs one
```

- Folder name = entity name (PascalCase)
- Each `config.json` is the entity body — **no wrapper key** matching the folder name
- Containment is by reference (`contents` / `coverageTerms` / `charges` + quantifiers)
- See `config-schema.md` for when to use exposures vs product-level coverages,
  and when policy-lines / exposure-groups belong

## Requirements → config mapping

Free-form requirements describe product intent; they rarely name every
platform entity. Map what is said into the folder tree, and fill only what a
valid tree requires.

### Typical intake fields to extract

- Product name and display name
- Account type(s) (consumer vs commercial / organization)
- Containment: exposures (repeating units) vs coverages directly on the product
- Coverages with intent for mandatory vs optional (→ quantifiers `!` `?` `+` `*`)
- Coverage terms (limits / deductibles) and option sets
- Charges (premium / fee / tax / …)
- Data fields on product / exposure / coverage
- Currency, timezone, term duration / basis
- Billing cadence / installment intent (only if stated)
- Policy numbering pattern (only if stated)

### Inferred entities (required by references, often unstated)

| Need | Infer |
|---|---|
| Product `eligibleAccountTypes` | `accounts/<Name>/` — consumer products → a consumer-style account; commercial → organization-style |
| Product `numberingString` / `numberingTrigger` | `numberingPlans/<Name>/` with **every literal character escaped** in `format` (e.g. `GL-######` → `"\\G\\L-######"`) |
| Immediate-invoice fee | Charge with `handling: flat` (never `normal` + `invoicing: immediate` / `next`) |
| Mandatory (`!`) options-based term, no default named | Default to `*…_excluded` if Excluded exists; else most conservative value; else synthesize `excluded`/`included` per sibling rules |
| Unstated installment / delinquency / auto-renewal plans | **Omit** — do not fabricate defaults nothing references |

### Static config vs plugin

Business rules that cannot be expressed as field types / options / containment
(e.g. “date can’t be in the future”) stay as plain fields and need a
**validation plugin** — do not fake them with hardcoded dates or invented keys.

## Build order

1. Root `config.json` — globals only (`defaultCurrency`, `defaultTimeZone`,
   `defaultTermDuration`, `defaultDurationBasis`; lowercase `contactRoles` /
   `lossCategories` if present)
2. Leaves: `charges`, `coverageTerms`, then `coverages`, then exposures / groups / lines
3. `accounts` and plans referenced by the product
4. Wire `contents` / `coverageTerms` / `charges` upward with quantifiers
5. Merge `data{}` onto owning entities — never redefine platform-managed names
   (`state`, `status`, …); rename per schema
6. Types: `decimal` not `integer`; `min` / `max` as strings

Build only what the requirements imply — no filler example products.

## Audit before validateConfig

Apply the shared **Config audit checklist** (xlsx sibling) to the tree:
wrap-bug, root-inline entities, dangling refs, `!` terms without `*` defaults,
charge handling/invoicing, numbering escapes, reserved field names,
uppercase-start option keys, orphans.

`./gradlew validateConfig` is the remote authority when credentials exist in the
SDK project. Do not embed credentials in config or skill files.

## Out of scope

- Config Template workbooks → `socotra-config-from-xlsx`
- Java plugins → `socotra-rating-plugin` / `socotra-jar-building-block` (and
  external skills for validation / UW / precommit / automation)
- SDK clone / credential setup
- Unshaped sections without an external schema reference

## Maintenance

New validation failure? Fix the config, then add the rule to
`../socotra-config-from-xlsx/references/config-schema.md` (and the audit
checklist in the xlsx skill if structural). Do not grow a second schema copy
here.
