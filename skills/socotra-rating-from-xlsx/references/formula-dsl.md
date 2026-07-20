# Rating formula DSL — grammar + Java mapping

The `Formula` column on the `Rating` tab holds one expression in this small language. It is
deliberately tiny, reads like algebra, is filled by hand today, and is designed as the **target
of a future LLM transpile** from customers' native Excel formulas (`=VLOOKUP(...)*...`). **This
prose is the grammar** — there is no parser program; you parse each formula by reasoning over the
rules below (no code execution, no network).

## Grammar (precedence low → high)

```
expr        := or
or          := and ('or' and)*
and         := not ('and' not)*
not         := 'not' not | comparison
comparison  := add ( ('=='|'!='|'<'|'<='|'>'|'>=') add )?
add         := mul ( ('+'|'-') mul )*
mul         := unary ( ('*'|'/') unary )*
unary       := '-' unary | primary
primary     := number | string | var | lookup | band | call | if | '(' expr ')'

number      := 123 | 12.5
string      := "double-quoted"
var         := identifier declared on the 3 - Inputs tab
lookup      := Name '[' expr (',' expr)* ']' ( '.' colName )?
band        := 'band' '(' Name ',' [ '[' expr (',' expr)* ']' ',' ] bound (',' mode)? ')'
call        := ('min'|'max'|'round'|'abs') '(' args ')'
if          := 'if' '(' expr ',' expr ',' expr ')'
```

- `mode` for `band` is `linear` | `stepUp` | `stepDown` (default from the table's `On Miss`).
- `round(x)` uses the workbook's Default Rounding Mode; `round(x, HALF_UP)` overrides.

## What each construct means

| DSL | Meaning | Socotra mapping |
|---|---|---|
| `BaseRates[sex,age]` | exact table lookup; key exprs match the table's **Key Columns in order** | `ResourceSelector.get(ctx).getTable(BaseRates.class).getRecord(BaseRates.makeKey(<keys, stringified>))` |
| `BaseRates[sex,age].rate` | pick a named value column (default = the table's first Value Column) | `…map(r -> new BigDecimal(r.rate()))` |
| `band(AgeBand, age)` | range/banded lookup on bound value | `getRangeTable(AgeBand.class).getRecord(makeKey(), age)` |
| `band(AgeBand,[territory],age,linear)` | banded lookup with non-range keys + interpolation | `getRangeTable(...).interpolate(makeKey(territory), age, r->…, Interpolation.linear)` |
| `face`, `age` | input variable | the `Source (config accessor)` from the Inputs tab, e.g. `quote.data().faceAmount()` |
| `"Gold"`, `"M"` | string literal | matched against config enum/table key/allowed value |
| `+ - * / ( )` | arithmetic | **all `BigDecimal`**; `/` uses Default Rounding Mode + scale 10 |
| `min/max/round/abs` | numeric functions | `BigDecimal` equivalents |
| `if(cond, a, b)` | branch | ternary; `cond` is a comparison / boolean expr |

## What becomes of the number (resolved off-tab, not by the customer)

The formula yields a number. Whether it lands as a **rate** or a fixed **amount** is the one fact
the rating skill warns is invisible in the JAR — but it is **not** on the Rating tab. It is the
charge's `Handling`, declared once on `2 - Charges`:

- **`Handling = rate`** (charge `handling=normal`) → `RatingItem.…rate(value)`. The value is a
  **per-duration-unit rate**; the platform multiplies by segment duration. A formula like
  `BaseRate[state] * (amount/1000)` already yields such a rate → emit it directly.
- **`Handling = amount`** (charge `handling=flat`/`retention`) → `RatingItem.…amount(value)`. Fixed,
  not scaled by duration (a flat `30.00` fee).

So the generator looks up handling by `(charge, element)` and picks `.rate()`/`.amount()` itself —
the customer never restates it on the Rating tab.

**Null inputs:** default to fail-loud (never silently zero a premium). The rating skill's discipline
applies at code generation; it is not a per-row column on the demo template.

**Advanced — full-term totals:** if a formula produces a *term total* rather than a per-duration
rate while the charge handling is `rate`, convert first with
`new MoneyService(currency).getRateForTargetAmount(value, request.duration())` (emitting a total as a
rate over-charges — the platform multiplies by duration again). Signal this with an optional
`Basis = total` column; it is omitted from the default template since ordinary rate formulas don't
need it.

## Generator rules (DSL → Java), from the rating skill

When the socotra-rating-plugin skill turns the contract into Java:
- Stay in `BigDecimal` end to end — never `double` (an explicit anti-pattern in that skill).
- Every `divide`/`setScale` gets `RoundingMode.HALF_EVEN`.
- Table key columns are passed to `makeKey` as **String** — stringify numeric keys (`String.valueOf(age)`).
- Generated value columns return `String` — wrap in `new BigDecimal(...)`.
- Table miss → honour the table's `On Miss` (`error` → `orElseThrow` with a clear message; a
  literal → `orElse(new BigDecimal(...))`). Fail loud by default.
- One `RatingItem` per `(elementLocator, chargeType)` — multiple Rating rows for the same pair are
  summed into one item.
- Confirm every generated symbol (`ChargeType.premium`, `BaseRates.makeKey`, accessor paths) against
  the JAR via the building-block scanners before trusting it — the contract's names are candidates.

---

# Parsing the workbook into the rating contract (by hand)

There is no `parse_rating_workbook.py` and nothing to run. You read the filled workbook with the
`xlsx-extract` method, then parse each formula and assemble the contract **by reasoning** over the
rules here. This section is the complete specification of what that former parser did — tokens,
grammar, the AST shape, every validation it raised, and the exact JSON/HTML contract it emitted —
so you can reproduce the same contract by hand and write it out with the `Write` tool.

## 1. Tokenize each formula

Scan left to right, skipping whitespace, matching the longest token at each position. Token kinds:

- **num** — `\d+\.\d+` or `\d+` (e.g. `1000`, `12.5`). Kept as a string; never coerced to float.
- **str** — a double-quoted run `"…"` (no embedded quotes); the value is the text inside the quotes.
- **op** — one of `<= >= == != < > + - * /` (match the two-char operators before the one-char ones).
- **punc** — one of `( ) [ ] , .`
- **name** — `[A-Za-z_][A-Za-z0-9_]*`.

Any character that matches none of these is a **syntax error** ("unexpected character"). Reserved
names: `and`, `or`, `not`, `if` (keywords) and `min`, `max`, `round`, `abs`, `band` (functions);
every other `name` is either a variable or a table (decided by whether it is followed by `[`).

## 2. Parse to an AST (recursive descent, precedence low → high)

Follow the grammar in the section above. The resulting AST uses these node shapes (the exact dict
shapes the contract JSON carries under each rule's `ast`):

| Construct | AST node |
|---|---|
| number | `{"node":"num","value":"<digits>"}` (value is the original string) |
| string literal | `{"node":"str","value":"<text without quotes>"}` |
| variable | `{"node":"var","name":"<id>"}` |
| table lookup `T[k1,k2].col` | `{"node":"lookup","table":"T","keys":[<exprs>],"col":"<col or null>"}` |
| `band(T, x)` / `band(T,[k1],x,mode)` | `{"node":"band","table":"T","keys":[<exprs>],"bound":<expr>,"mode":"<mode or null>"}` |
| `min/max/round/abs(args…)` | `{"node":"call","func":"<name>","args":[<exprs>]}` |
| `if(cond,a,b)` | `{"node":"if","cond":<expr>,"then":<expr>,"else":<expr>}` |
| binary `+ - * /` | `{"node":"bin","op":"<op>","l":<expr>,"r":<expr>}` |
| comparison `== != < <= > >=` | `{"node":"cmp","op":"<op>","l":<expr>,"r":<expr>}` |
| `and` / `or` | `{"node":"and","l":…,"r":…}` / `{"node":"or","l":…,"r":…}` |
| `not x` | `{"node":"not","x":<expr>}` |
| unary minus `-x` | `{"node":"neg","x":<expr>}` |

Parsing details that were load-bearing:
- **`lookup`**: after a `name`, if the next token is `[`, parse a comma-separated key expr list until
  `]`; then if the next token is `.`, the following `name` is the value column `col` (else `col` is
  null). Record `(table, key-count, col)` as a table use.
- **`band`**: first arg is the table `name`; then `,`; then an **optional** `[k1,k2,…]` key list
  followed by `,`; then the `bound` expression; then an **optional** `, mode` (`linear`|`stepUp`|
  `stepDown`); then `)`. Record `(table, key-count, null)` as a table use.
- A bare `name` not followed by `[` is a **variable use**; collect it.
- After parsing the top expression there must be **no trailing tokens** (else syntax error), and
  a missing expected `)`/`]`/`,` is a syntax error.

From each formula collect: the AST, the **sorted unique set of variable names used**, and the list
of table uses `(name, key-count, col)`.

## 3. Read the declarations (tolerant header lookup)

Locate tabs by a **case-insensitive prefix match on the display name** (from `workbook.xml`), with
fallbacks: `3 - Inputs` (or any `Inputs`), `4 - Rate Tables` (or `Rate Tables`), `2 - Charges` (or
`Charges`), `N - Rating` (or `Rating`). Within a tab, map columns by **header text** (row 1),
trimmed and lower-cased; accept these aliases and never rely on column position:

- **Inputs** → `declared_vars[Variable] = "Source (config accessor)"` (alias `Source`). One entry
  per non-empty `Variable`.
- **Rate Tables** → `declared_tables[Table Name] = { type (default "exact"), keys = Key Columns
  split on commas, values = Value Columns split on commas, on_miss (default "error") }`.
- **Charges** → `declared_charges[Charge Name]` (alias `Charge`) is a **list** of `{ element =
  "Lands On (Element)" (aliases "Lands On", "Element"), handling = Handling lower-cased }`, one per
  row (a charge may land on several elements).

## 4. Build one rule per `N - Rating` row

If no Rating tab exists, that is a hard stop ("no Rating tab found"). Read row 1 as headers, then
for each data row read `Element`, `Charge`, `Formula`, optional `Product` and `Notes`. **Skip any
row missing Formula, Element, or Charge.** For each kept row build a rule:

```
{ "product": <Product or "">, "element": …, "charge": …, "handling": …,
  "formula": <raw text>, "notes": …, "errors": [], "warnings": [],
  "ast": <parsed AST>, "vars_used": [<sorted>], "tables_used": [<sorted unique table names>] }
```

**Resolve `handling` from `2 - Charges`, not the Rating tab**: among `declared_charges[charge]`,
take the `handling` of the first entry whose `element` equals this rule's element; if none, leave
handling empty.

## 5. Validation — the exact errors and warnings

Per rule, in this order:

**Errors** (❌ — these block code generation; if any rule has ≥1 error, stop and fix the workbook or
ask the user before generating anything):
- **Parse error** — the formula failed to tokenize/parse: `parse error: <message>`.
- **Undeclared input variable** — a used variable is not in `declared_vars`:
  `undeclared input variable '<v>' (add it to 3 - Inputs)`.
- **Undeclared table** — a used table is not in `declared_tables`:
  `undeclared table '<T>' (add it to 4 - Rate Tables)`.
- **Key-count mismatch** — the table declares N key columns but the formula passed a different
  count (only checked when the table declared keys):
  `table '<T>' declares <N> key column(s) but formula passes <M>`.
- **Unknown value column** — a `.col` was used that is not among the table's declared Value Columns
  (only checked when both are present): `table '<T>' has no value column '<col>'`.

**Warnings** (⚠️ — surface but do not block):
- Charge not declared at all on `2 - Charges`: `charge '<charge>' not declared on 2 - Charges`.
- Charge declared but no handling for this element:
  `no handling (rate/amount) for charge '<charge>' on '<element>' in 2 - Charges`.

Tally `n_err` and `n_warn` across all rules. **Any error ⇒ the run is failed** (the old tool exited
non-zero): do not proceed to tables/Java until the workbook is clean or the user accepts.

## 6. The contract output (reproduce this shape)

Produce the same two artifacts the tool did, written with the `Write` tool:

**`contract.json`** — a single object:

```
{ "declared": { "inputs": {<var>: <accessor>, …},
                "tables": {<name>: {"type","keys","values","on_miss"}, …},
                "charges": {<name>: [{"element","handling"}, …], …} },
  "rules": [ {product, element, charge, handling, formula, notes,
              errors:[…], warnings:[…], ast:{…}, vars_used:[…], tables_used:[…]}, … ] }
```

(See `examples/cgl/contract.json` for a concrete instance — match that structure exactly.)

**`contract.html`** — a self-contained, styled page (no external assets, inline `<style>`) a human
or AI reviews before any Java exists. It carries:
- an `<h1>Rating contract</h1>` and a summary line of pills: `<n> rule(s)`, plus `<n> error(s)`
  and/or `<n> warning(s)` when present, or a `✓ clean` pill when there are none.
- one card per rule, headed `product / element / charge` (blank parts omitted) with a `rate`/`amount`
  handling badge, and a definition list: `formula` (in a code style), `inputs` (vars_used),
  `tables` (tables_used), and `notes` when present.
- under each rule, any errors (❌) and warnings (⚠️) for that rule.
- an empty-state line "No rating rules found." when there are zero rules.

The HTML is a review convenience; a Markdown rendering of the same content (rules, handling,
inputs/tables, and the error/warning tally) is an acceptable substitute if that reads better in the
current surface. The JSON is the machine-readable contract the `socotra-rating-plugin` skill consumes.
