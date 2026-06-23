# Rating contract (derived from config)

Config: `/Users/joshualawrence/Projects/All_EC_Demo_configs/accident-and-health`

> Java type names below (`<Product>QuoteRequest`, `ChargeType.x`, accessors) are DERIVED from config naming. **Confirm against the JAR** with the bedrock `scan_plugins.py` (exact overloads) and `build_catalog.py` (exact accessors) before relying on them.

**Concrete products** (each needs rate overloads): AccidentAndHealth

## Charges defined in this config

| Charge | category | handling | RatingItem method | ChargeType (confirm) |
|---|---|---|---|---|
| CededPremium | premium | normal | `.rate(value)` | `ChargeType.cededPremium` |
| Commission | nonFinancial | normal | `.rate(value)` | `ChargeType.commission` |
| Fee | tax | normal | `.rate(value)` | `ChargeType.fee` |
| Premium | premium | normal | `.rate(value)` | `ChargeType.premium` |
| Tax | tax | normal | `.rate(value)` | `ChargeType.tax` |

*handling defaults to `normal` when omitted -> use `.rate()`.*

## Rate tables (ResourceSelector lookups)

### SampleTable (table) — selectionTimeBasis `policyStartTime`
- key: `SampleTable.makeKey(sampleColumn1)`
- value columns: sampleColumn2:decimal
- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.

## Per-product rating contract

### Product: AccidentAndHealth  (Accident and Health Insurance)

Overloads to consider implementing (prune to what the JAR actually generates):
- `rate(AccidentAndHealthQuoteRequest request)`
- `rate(AccidentAndHealthQuickQuoteRequest request)`
- `rate(AccidentAndHealthRequest request)  # segment/transaction level; request.segment() is Optional`

Chargeable elements and their LEGAL charges:

| Element | kind | locator | legal charges (-> method) |
|---|---|---|---|
| `AccidentAndHealth` | product | `quote.locator()  /  segment.locator()` | `Premium` normal -> `.rate(value)`<br>`Fee` normal -> `.rate(value)` |
| `Classification` | exposure | `quote.classifications()[i].locator()` | `Premium` normal -> `.rate(value)`<br>`Commission` normal -> `.rate(value)`<br>`Tax` normal -> `.rate(value)`<br>`Fee` normal -> `.rate(value)` |
| `Location` | exposure | `quote.locations()[i].locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `AccidentalDeathAndDismemberment` | coverage | `quote.accidentalDeathAndDismemberment().locator()` | `Premium` normal -> `.rate(value)` |
| `OfficeVisits` | coverage | `quote.officeVisits().locator()` | `Premium` normal -> `.rate(value)` |
| `DentalServices` | coverage | `quote.dentalServices().locator()` | `Premium` normal -> `.rate(value)` |
| `TreatmentOfHernia` | coverage | `quote.treatmentOfHernia().locator()` | `Premium` normal -> `.rate(value)` |
| `AmbulatoryMedicalOrSurgicalCenter` | coverage | `quote.ambulatoryMedicalOrSurgicalCenter().locator()` | `Premium` normal -> `.rate(value)` |
| `HmoPpoDenial` | coverage | `quote.hmoPpoDenial().locator()` | `Premium` normal -> `.rate(value)` |
| `InpatientPrivateSemiPrivateRoom` | coverage | `quote.inpatientPrivateSemiPrivateRoom().locator()` | `Premium` normal -> `.rate(value)` |
| `InpatientIcuCcu` | coverage | `quote.inpatientIcuCcu().locator()` | `Premium` normal -> `.rate(value)` |

> ⚠️ Elements without a derivable charge set: Location. A missing `charges` array means this script cannot prove which charges are legal there. Confirm before emitting RatingItems for them.

## Before you write code

1. Run the bedrock `scan_plugins.py --project <dir>` to get the EXACT `rate(...)` overloads and request-record component names for this config.
2. Run the bedrock `build_catalog.py --project <dir>` to confirm the accessor paths above exist (e.g. `insured.termInsurance` -> the real return type).
3. Map each charge to `.rate()` vs `.amount()` using the handling column above — do not guess from the charge's name.
4. Emit at most one RatingItem per (elementLocator, chargeType) — duplicates throw.