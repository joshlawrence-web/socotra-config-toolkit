# Rating formula DSL — grammar + Java mapping

The `Formula` column on the `Rating` tab holds one expression in this small language. It is
deliberately tiny, reads like algebra, is filled by hand today, and is designed as the **target
of a future LLM transpile** from customers' native Excel formulas (`=VLOOKUP(...)*...`). The
parser (`scripts/parse_rating_workbook.py`) implements exactly this grammar; keep the two in sync.

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
