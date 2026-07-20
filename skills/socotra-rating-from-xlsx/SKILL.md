---
name: socotra-rating-from-xlsx
description: 'Turn a filled-out Socotra "Rating workbook" (.xlsx) into a deployable rater — rate-table CSVs plus a RatePlugin — for the active socotra-config. Runs with NO code execution and NO network: read the workbook by hand (the xlsx-extract method), then parse the readable formula DSL and assemble an auditable rating contract by reasoning over the grammar in references/. Bridges four things: the xlsx-extract skill (reads the file), this skill (defines the config-agnostic rating workbook format + the formula DSL + the contract shape), the socotra-rating-plugin skill (turns the contract into valid BigDecimal Java), and the rate-table schema (distilled in the sibling socotra-config-from-xlsx skill''s references/config-schema.md). Blank/example workbooks ship ready-made in references/ (inert). Use whenever a user hands you a Socotra rating spreadsheet — or needs the blank framework to fill one out. The workbook layout is NOT a contract — re-verify it against the actual file every time.'
---

# socotra-rating-from-xlsx

Convert a **Rating workbook** into a working rater for one specific config. The workbook is a
config-agnostic framework: blank tabs the customer fills with *their* charge/element/table names and
the pricing math, written as a small readable formula DSL. This skill is the **bridge** — it owns the
format and the DSL, not the file parsing and not the Java.

This skill runs with **no code execution and no network**. Nothing here is run; you read files and
reason over them.

- **Read the file** → `xlsx-extract` skill (a by-hand method: `unzip` the `.xlsx`, then `Read`/`Grep`
  `workbook.xml` for the sheet list, `sharedStrings.xml` + `worksheets/sheetN.xml` for the cells).
  No `openpyxl`, no `pandas`, no extractor script.
- **Write the Java** → `socotra-rating-plugin` skill. It owns the `RatePlugin` contract — `.rate()` vs `.amount()`, legal `(element, charge)` pairs, `makeKey` signatures, BigDecimal discipline, overload dispatch. This skill does **not** restate it; it produces the *spec* that skill consumes.
- **Shape the tables** → the `tables/` schema, distilled in the sibling `socotra-config-from-xlsx` skill's `references/config-schema.md` (§ tables).
- **Define + parse the workbook** → this document + `references/`.

There is no canonical customer rating template yet, so **this skill defines the format** and ships a
blank framework + a worked example as **inert reference workbooks**
(`references/socotra-rating-template.xlsx`, `references/socotra-rating-example.xlsx`). When a user
needs a blank to fill out, hand them the ready-made template — copy it out of `references/`; there
is nothing to generate. `references/template-format.md` is the source of truth for the layout.

## What this produces

A filled workbook → an **auditable rating contract** (a self-contained styled **HTML** page + JSON, reviewable by human and AI
before any code exists) → `tables/<Name>.csv` + a `RatePlugin`. The contract is the deliberate
checkpoint: every formula is parsed deterministically and every name is validated against the
workbook's own declarations, so a wrong reference is caught on a page you can read in seconds rather
than after a tenant deploy.

## Format (config-agnostic framework)

One row = one declaration; names are free text matching the customer's config. Full layout in
`references/template-format.md`; the formula language in `references/formula-dsl.md`. Tabs:

| Tab | Carries |
|---|---|
| `1 - Rating Setup` | products in scope + global defaults (currency, duration basis, rounding, on-miss, plugin scope) |
| `2 - Charges` | each charge · the element it lands on · **Handling (rate/amount)** · category |
| `3 - Inputs` | named rating variables → config accessor paths |
| `4 - Rate Tables` | each table: ordered key columns, value columns, exact/range, on-miss |
| `T - <TableName>` | one grid tab per table with the data |
| `N - Rating` | one DSL formula per (Element, Charge) — the algorithm |

The `N - Rating` tab is deliberately lean — `Element`, `Charge`, `Formula`, `Notes` — so it shows
only *what is priced* and *the formula*. The load-bearing, JAR-invisible **rate-vs-amount** fact the
rating skill warns about is **not** restated there; it is the charge's `Handling` on `2 - Charges`
(declared once), which the parser resolves onto each rule by `(charge, element)`. Get that handling
right and the generated `RatingItem` is valid; get it wrong and it throws at construction. The rare
full-term-total case is an optional `Basis = total` column (off by default) handled at code gen.

## ⚠️ Step 0 — re-verify the workbook before trusting the layout

The format **will drift** (renamed tabs/columns, reordered fields). Treat `template-format.md` as
*last known good*. Read the live file first (the `xlsx-extract` by-hand method) and map by **header
text, never column letter**:

- `unzip -o "FILE.xlsx" -d ./xlsx-tmp`, then `Read` `xl/workbook.xml` for the sheet list in display
  order and `xl/_rels/workbook.xml.rels` to map each `r:id` → `worksheets/sheetN.xml` (the
  equivalent of the old `--list`).
- `Read` `xl/sharedStrings.xml` (0-indexed `<si>` entries) and each `xl/worksheets/sheetN.xml` to
  recover header rows + cells.

If the tab set or a header row differs from `template-format.md`, derive the mapping from the live
headers, tell the user what changed, and update the format doc when you finish.

## Workflow

1. **Step 0 credibility check** (above). Map tabs/columns by header text, tolerate the common
   aliases (`formula-dsl.md` § "Read the declarations"), and eyeball the tabs for drift.
2. **Parse → contract by hand** (this is also the validation gate). Follow the full method in
   `references/formula-dsl.md` § "Parsing the workbook into the rating contract": tokenize and parse
   every `N - Rating` formula into an AST by reasoning over the grammar, resolve each variable
   against `3 - Inputs` and each table against `4 - Rate Tables` (key-count + value-column checks),
   and resolve each rule's `Handling` from `2 - Charges` (warning if the `(charge, element)` isn't
   declared there). Assemble the contract in the documented JSON shape and write `contract.json`
   (and a `contract.html` or Markdown rendering) with the `Write` tool. **If any rule has a ❌
   error, the run is failed** — stop and fix the workbook (or ask the user) before generating
   anything. Surface the contract to the user.
3. **Emit rate tables.** For each `T - <TableName>` grid, write `socotra-config/tables/<Name>.csv`
   (the CSV data) plus `tables/<Name>/config.json` (the column schema — shape in the
   `socotra-config-from-xlsx` skill's `references/config-schema.md` § tables). Column order =
   the grid header; key columns first.
4. **Generate the RatePlugin** via the **`socotra-rating-plugin`** skill, driving it from the
   contract: one rated `(element, charge)` per rule, `Handling`→`.rate()`/`.amount()`,
   `Basis=total`+rate → `MoneyService.getRateForTargetAmount`, formulas → BigDecimal per
   `formula-dsl.md`'s mapping. **Confirm every generated symbol** (ChargeType constants, table
   `makeKey` signatures, input accessors) against the JAR with that skill's deriver + the
   building-block scanners — the contract's names are *candidates*, the JAR wins.
5. **Ground conditional fill-ins, disclose assumptions.** The rating skill's discipline applies
   verbatim: if a formula references a value (`"coastal"`) or accessor that can't be confirmed
   against config/JAR, flag it — don't substitute a plausible guess. Append an assumptions block to
   the generated plugin.
6. **Verify + report.** Compile the plugin via `socotra-verify-deploy`. Report: the contract, every
   inferred/assumed/skipped decision, and any workbook drift you reconciled. Deployment is the
   user's to run, not yours.

## Producing a blank framework for the user

When the user has no spreadsheet yet, hand them the **ready-made inert workbooks** that ship with
this skill — nothing is generated:
- `references/socotra-rating-template.xlsx` — the blank framework to fill in (copy it out to their
  working directory).
- `references/socotra-rating-example.xlsx` — the worked term-life example, as a DSL reference.

Its layout is fully documented in `references/template-format.md` (tabs, exact headers, defaults,
and the worked example's contents) if you need to explain any cell.

## v1 scope

Flat/constant, table base × factors, and banded/range rating, at **quote level**. Segment/renewal
and multi-product overloads are deferred — but the format already carries `Product` + `Plugin Scope`,
so adding them is additive (a `Dispatch` tab + the rating skill's pattern 6), not a reshape.

## Files / artifacts

This skill contains **no runnable code** — only inert reference material you read and reason over,
plus the artifacts you produce with the `Write` tool. (Historically it shipped
`build_rating_template.py` and `parse_rating_workbook.py`; both were removed when the skill moved to
a no-code, no-network method. The template workbooks now ship pre-built and the parsing is prose.)

- `references/template-format.md` — the full tab/column spec, exact headers, defaults, and the
  worked example's contents. **Source of truth for the layout.**
- `references/formula-dsl.md` — the formula grammar + its Socotra/Java mapping (rate-vs-amount,
  basis, table lookups, on-miss) **and** the complete by-hand parsing/validation/contract-shape
  method (tokens, AST, errors, warnings, contract JSON/HTML).
- `references/socotra-rating-template.xlsx` / `socotra-rating-example.xlsx` — inert blank framework +
  worked example workbooks (hand these to the user; do not regenerate).
- `examples/` — sample contracts (`.xlsx` inputs with their `contract.html`/`.json`/README) to
  match your hand-produced contract structure against.

## Maintenance

This format is new and will move. When a real customer workbook differs, or you hit a DSL construct
the grammar doesn't cover, update the two reference docs (`template-format.md` for layout,
`formula-dsl.md` for the grammar + parsing method) together, and edit the inert reference workbooks
to match the layout — keep docs and workbooks in lockstep so the next run starts from a correct
baseline. There is no code to keep in sync.
