# Rating workbook format (the framework design)

A standalone, **config-agnostic** workbook: a blank framework the customer fills with their own
socotra-config entity names and rating rules. One row = one declaration. Names are free text and
must match the customer's config exactly. The workbook does **not** self-compute — the formulas
are a readable DSL the skill parses (see `formula-dsl.md`).

`scripts/build_rating_template.py` is the source of truth for this layout — it emits both a blank
framework and a filled term-life example (`references/socotra-rating-template.xlsx` /
`…-example.xlsx`). If you change a tab/column there, update this doc.

> ⚠️ Like the config template, this layout is **last known good, not a contract**. Re-verify the
> live file with `xlsx-extract --list` + headers before trusting any column position. Map by
> **header text, never by column letter**.

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

## v1 scope

Supports **flat/constant**, **table base × factors**, and **banded/range** rating, at **quote
level**. Segment/transaction and multi-product overloads are deferred but the format already carries
`Product` and `Plugin Scope`, so adding a `Dispatch` tab later is additive — no reshaping.

## Worked example (term-life-style, in the example workbook)

- `2 - Charges`: `Premium`→`DeathBenefit` (`rate`), `PolicyFee`→`TermLife` (`amount`).
- `3 - Inputs`: `sex, age, face, mode, territory` with accessors.
- `4 - Rate Tables`: `BaseRates[sex,age]→rate`, `Modality[mode]→factor`, `TerritoryFactor[territory]→factor`.
- `N - Rating`:
  - `DeathBenefit / Premium`: `BaseRates[sex,age] * (face/1000) * Modality[mode] * TerritoryFactor[territory]`
  - `TermLife / PolicyFee`: `25.00`
