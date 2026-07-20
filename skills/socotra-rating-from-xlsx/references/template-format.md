# Rating workbook format (the framework design)

A standalone, **config-agnostic** workbook: a blank framework the customer fills with their own
socotra-config entity names and rating rules. One row = one declaration. Names are free text and
must match the customer's config exactly. The workbook does **not** self-compute — the formulas
are a readable DSL the skill parses (see `formula-dsl.md`).

**This document is the source of truth for the layout.** Two inert reference workbooks ship
alongside it and match this spec exactly: `references/socotra-rating-template.xlsx` (a blank
framework) and `references/socotra-rating-example.xlsx` (a filled term-life example). When the user
needs a blank to fill out, hand them the ready-made template workbook (copy it out of `references/`);
there is nothing to generate. If you ever revise a tab/column, edit this doc first, then the
reference workbooks to match.

> ⚠️ Like the config template, this layout is **last known good, not a contract**. Re-verify a
> customer's *live* file by reading it with the `xlsx-extract` method (unzip → read `workbook.xml`
> for the sheet list, `sharedStrings.xml` + `worksheets/sheetN.xml` for cells) before trusting any
> column position. Map by **header text, never by column letter**.

## Tabs

| Tab | Purpose |
|---|---|
| `0 - How to Use` | conventions + the formula DSL cheat-sheet |
| `1 - Rating Setup` | products in scope + global defaults |
| `2 - Charges` | every charge, the element it lands on, **handling (rate/amount)** |
| `3 - Inputs` | named rating variables → config accessors |
| `4 - Rate Tables` | declare each table (keys / values / type / on-miss) |
| `T - <TableName>` | one grid tab per table holding the actual data |
| `N - Rating` | one formula per (Element, Charge) — the algorithm |

### 1 - Rating Setup  (key/value)
`Products In Scope` (comma-sep product names) · `Default Currency` (ISO) · `Default Duration Basis`
(`annual`/`months`/`days`) · `Default Rounding Mode` (`HALF_EVEN`) · `Default On-Miss`
(`error`/`passthrough`/literal) · `Plugin Scope` (`global`/`product`). Rows on other tabs override
these defaults.

### 2 - Charges
`Charge Name` · `Lands On (Element)` · `Handling` (`rate`|`amount`) · `Category`
(`premium`/`fee`/`tax`/`surcharge`/`credit`/`nonfinancial`) · `Notes`.
One row per (charge, element) pair the charge can land on. **`Handling` is the single most
load-bearing cell in the book** — it is the rate-vs-amount fact that is invisible in the generated
JAR and that the rating skill says raters get wrong most often. `rate` ⇔ config `handling=normal`;
`amount` ⇔ `flat`/`retention`.

### 3 - Inputs
`Variable` · `Source (config accessor)` · `Type` (`decimal`/`int`/`string`/`enum`/`bool`) ·
`Allowed Values` (enums, comma-sep) · `Notes`. The `Variable` names are what formulas reference.
`Source` is the config accessor (dot notation, e.g. `quote.insured().data().issueAge()`); it can be
left as a friendly placeholder for a later transpile pass and confirmed against the JAR catalog then.

### 4 - Rate Tables  (index)
`Table Name` · `Type` (`exact`|`range`) · `Key Columns` (ordered, comma-sep — this is the `makeKey`
order) · `Range Column` (the banded column, range tables only) · `Value Columns` (comma-sep) ·
`On Miss` (`error`/`interpolate`/`extrapolate`/literal) · `Grid Tab` (the `T - …` tab holding data) ·
`Notes`.

### T - <TableName>  (one per table)
Plain grid. Header row = column names: key columns first, then value columns. **Range tables** add
`rangeStart`/`rangeEnd` (or `low`/`high`) bound columns. Customer pastes data rows here.

### N - Rating  (the heart)
`Element` · `Charge` · `Formula` · `Notes`. One row per (Element, Charge) priced — kept deliberately
lean so it's the only customer-facing surface that matters: *what is priced* and *the formula*. Two
rows for the same (Element, Charge) are **summed** into one `RatingItem` (the platform's
one-item-per-pair rule). `Formula` is the DSL (`formula-dsl.md`).

Everything else is plumbing the tool resolves, not the customer's problem:
- **rate vs amount** comes from the charge's `Handling` on `2 - Charges` (declared once, there).
- **duration / per-unit conversion** is inferred from that handling + the config; only a full-term
  *target total* needs special treatment, handled at code generation, not on this tab.

(Advanced, optional: a `Product` column disambiguates multi-product books, and a `Basis = total`
column can force target-total→rate conversion. Neither is needed for ordinary single-product rating
and both are omitted from the default template.)

## Exact header rows (as shipped in the reference workbooks)

Row 1 of each tab is the header. These are the canonical column names and order:

- **`1 - Rating Setup`** (key/value, header `Setting · Value · Notes`), rows in this order:
  `Products In Scope` · `Default Currency` · `Default Duration Basis` · `Default Rounding Mode`
  (default value `HALF_EVEN`) · `Default On-Miss` (default value `error`) · `Plugin Scope`
  (default value `global`).
- **`2 - Charges`**: `Charge Name · Lands On (Element) · Handling · Category · Notes`.
- **`3 - Inputs`**: `Variable · Source (config accessor) · Type · Allowed Values · Notes`.
- **`4 - Rate Tables`**: `Table Name · Type · Key Columns · Range Column · Value Columns · On Miss · Grid Tab · Notes`.
- **`T - <TableName>`**: header row = the table's own columns (key columns first, then value
  columns). The blank workbook ships one placeholder grid tab, `T - ExampleTable`, with a note row
  telling the customer to rename it to `T - <YourTableName>` and match the columns declared on
  `4 - Rate Tables`.
- **`N - Rating`**: `Element · Charge · Formula · Notes`.

### `0 - How to Use` tab (conventions + DSL cheat-sheet)

The blank and example workbooks open with a `0 - How to Use` tab. It is documentation only (the
tool ignores it — `formula-dsl.md` is authoritative for the grammar). It states the fill order
(`1 - Rating Setup` → `2 - Charges` → `3 - Inputs` → `4 - Rate Tables` → `N - Rating`), a formula
cheat-sheet (`Table[k1,k2]` exact lookup, `Table[k1,k2].col` value-column pick, `band(Table, x)`
range lookup, `band(Table,[k1],x,linear)` with keys + interpolation, `+ - * / ( )` arithmetic in
BigDecimal/HALF_EVEN, `min/max/round/abs`, `if(cond,a,b)` with `== != < <= > >= and/or/not`,
variable names from `3 - Inputs`, string literals in double quotes), and these conventions:

- Names are free text and must match the customer's socotra-config entity names exactly.
- `Handling` MUST be `rate` (config `handling=normal`) or `amount` (flat/retention).
- Two `N - Rating` rows for the same `(Element, Charge)` are summed into one charge.
- The workbook does NOT self-compute; the skill parses the formulas into Java.

## v1 scope

Supports **flat/constant**, **table base × factors**, and **banded/range** rating, at **quote
level**. Segment/transaction and multi-product overloads are deferred but the format already carries
`Product` and `Plugin Scope`, so adding a `Dispatch` tab later is additive — no reshaping.

## Worked example (term-life-style, in `references/socotra-rating-example.xlsx`)

- `1 - Rating Setup`: `Products In Scope = TermLife`, `Default Currency = USD`, `Default Duration
  Basis = annual`, `Default Rounding Mode = HALF_EVEN`, `Default On-Miss = error`, `Plugin Scope = global`.
- `2 - Charges`: `Premium`→`DeathBenefit` (`rate`, category `premium`), `PolicyFee`→`TermLife`
  (`amount`, category `fee`).
- `3 - Inputs`: `sex` (`quote.insured().data().sex()`, enum `M,F`), `age`
  (`quote.insured().data().issueAge()`, int), `face` (`quote.data().faceAmount()`, decimal),
  `mode` (`quote.data().paymentMode()`, enum `annual,monthly`), `territory`
  (`quote.data().territory()`, enum `coastal,inland`).
- `4 - Rate Tables`: `BaseRates` (exact, keys `sex,age`, value `rate`, on-miss `error`, grid
  `T - BaseRates`), `Modality` (exact, key `mode`, value `factor`, on-miss `error`), `TerritoryFactor`
  (exact, key `territory`, value `factor`, on-miss literal `1.00`).
- Grid tabs (header row then data):
  - `T - BaseRates` — `sex,age,rate`: `M,35,1.10` · `M,36,1.18` · `F,35,0.95` · `F,36,1.01`.
  - `T - Modality` — `mode,factor`: `annual,1.00` · `monthly,1.08`.
  - `T - TerritoryFactor` — `territory,factor`: `coastal,1.20` · `inland,1.00`.
- `N - Rating`:
  - `DeathBenefit / Premium`: `BaseRates[sex,age] * (face/1000) * Modality[mode] * TerritoryFactor[territory]`
  - `TermLife / PolicyFee`: `25.00`
