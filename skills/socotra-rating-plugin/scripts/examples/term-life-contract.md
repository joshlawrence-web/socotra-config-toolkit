# Rating contract (derived from config)

Config: `/Users/joshualawrence/Projects/All_EC_Demo_configs/term-life`

> Java type names below (`<Product>QuoteRequest`, `ChargeType.x`, accessors) are DERIVED from config naming. **Confirm against the JAR** with the bedrock `scan_plugins.py` (exact overloads) and `build_catalog.py` (exact accessors) before relying on them.

**Concrete products** (each needs rate overloads): TermLife10Level, TermLife15Level, TermLife20Level, TermLife30Level
**Abstract base products** (not rated directly): TermLifeBase

## Charges defined in this config

| Charge | category | handling | RatingItem method | ChargeType (confirm) |
|---|---|---|---|---|
| fees | fee | normal *(default)* | `.rate(value)` | `ChargeType.fees` |
| premium | premium | normal *(default)* | `.rate(value)` | `ChargeType.premium` |

*handling defaults to `normal` when omitted -> use `.rate()`.*

## Rate tables (ResourceSelector lookups)

### ADBRates (table) — selectionTimeBasis `termStartTime`
- key: `ADBRates.makeKey(issueAge)`
- value columns: male:decimal, female:decimal
- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.

### BaseRates (table) — selectionTimeBasis `termStartTime`
- key: `BaseRates.makeKey(sex, age)`
- value columns: rate:decimal
- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.

### Fees (table) — selectionTimeBasis `termStartTime`
- key: `Fees.makeKey(feeType)`
- value columns: value:int
- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.

### Modality (table) — selectionTimeBasis `termStartTime`
- key: `Modality.makeKey(mode)`
- value columns: value:decimal
- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.

## Per-product rating contract

### Product: TermLife10Level  (Term Life 10 Year Level Premium)

Overloads to consider implementing (prune to what the JAR actually generates):
- `rate(TermLife10LevelQuoteRequest request)`
- `rate(TermLife10LevelQuickQuoteRequest request)`
- `rate(TermLife10LevelRequest request)  # segment/transaction level; request.segment() is Optional`

Chargeable elements and their LEGAL charges:

| Element | kind | locator | legal charges (-> method) |
|---|---|---|---|
| `TermLife10Level` | product | `quote.locator()  /  segment.locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `insured` | exposure | `quote.insured().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `termInsurance` | coverage | `quote.insured().termInsurance().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `accidentalDeathBenefit` | coverage | `quote.insured().accidentalDeathBenefit().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `waiverOfPremium` | coverage | `quote.insured().waiverOfPremium().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `terminalIllness` | coverage | `quote.insured().terminalIllness().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `child` | exposure | `quote.child().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `childTermRider` | coverage | `quote.child().childTermRider().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |

> ⚠️ Elements without a derivable charge set: TermLife10Level, insured, termInsurance, accidentalDeathBenefit, waiverOfPremium, terminalIllness, child, childTermRider. A missing `charges` array means this script cannot prove which charges are legal there. Confirm before emitting RatingItems for them.

### Product: TermLife15Level  (Term Life 15 Year Level Premium)

Overloads to consider implementing (prune to what the JAR actually generates):
- `rate(TermLife15LevelQuoteRequest request)`
- `rate(TermLife15LevelQuickQuoteRequest request)`
- `rate(TermLife15LevelRequest request)  # segment/transaction level; request.segment() is Optional`

Chargeable elements and their LEGAL charges:

| Element | kind | locator | legal charges (-> method) |
|---|---|---|---|
| `TermLife15Level` | product | `quote.locator()  /  segment.locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `insured` | exposure | `quote.insured().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `termInsurance` | coverage | `quote.insured().termInsurance().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `accidentalDeathBenefit` | coverage | `quote.insured().accidentalDeathBenefit().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `waiverOfPremium` | coverage | `quote.insured().waiverOfPremium().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `terminalIllness` | coverage | `quote.insured().terminalIllness().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `child` | exposure | `quote.child().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `childTermRider` | coverage | `quote.child().childTermRider().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |

> ⚠️ Elements without a derivable charge set: TermLife15Level, insured, termInsurance, accidentalDeathBenefit, waiverOfPremium, terminalIllness, child, childTermRider. A missing `charges` array means this script cannot prove which charges are legal there. Confirm before emitting RatingItems for them.

### Product: TermLife20Level  (Term Life 20 Year Level Premium)

Overloads to consider implementing (prune to what the JAR actually generates):
- `rate(TermLife20LevelQuoteRequest request)`
- `rate(TermLife20LevelQuickQuoteRequest request)`
- `rate(TermLife20LevelRequest request)  # segment/transaction level; request.segment() is Optional`

Chargeable elements and their LEGAL charges:

| Element | kind | locator | legal charges (-> method) |
|---|---|---|---|
| `TermLife20Level` | product | `quote.locator()  /  segment.locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `insured` | exposure | `quote.insured().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `termInsurance` | coverage | `quote.insured().termInsurance().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `accidentalDeathBenefit` | coverage | `quote.insured().accidentalDeathBenefit().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `waiverOfPremium` | coverage | `quote.insured().waiverOfPremium().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `terminalIllness` | coverage | `quote.insured().terminalIllness().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `child` | exposure | `quote.child().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `childTermRider` | coverage | `quote.child().childTermRider().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |

> ⚠️ Elements without a derivable charge set: TermLife20Level, insured, termInsurance, accidentalDeathBenefit, waiverOfPremium, terminalIllness, child, childTermRider. A missing `charges` array means this script cannot prove which charges are legal there. Confirm before emitting RatingItems for them.

### Product: TermLife30Level  (Term Life 30 Year Level Premium)

Overloads to consider implementing (prune to what the JAR actually generates):
- `rate(TermLife30LevelQuoteRequest request)`
- `rate(TermLife30LevelQuickQuoteRequest request)`
- `rate(TermLife30LevelRequest request)  # segment/transaction level; request.segment() is Optional`

Chargeable elements and their LEGAL charges:

| Element | kind | locator | legal charges (-> method) |
|---|---|---|---|
| `TermLife30Level` | product | `quote.locator()  /  segment.locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `insured` | exposure | `quote.insured().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `termInsurance` | coverage | `quote.insured().termInsurance().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `accidentalDeathBenefit` | coverage | `quote.insured().accidentalDeathBenefit().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `waiverOfPremium` | coverage | `quote.insured().waiverOfPremium().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `terminalIllness` | coverage | `quote.insured().terminalIllness().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `child` | exposure | `quote.child().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |
| `childTermRider` | coverage | `quote.child().childTermRider().locator()` | ⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review |

> ⚠️ Elements without a derivable charge set: TermLife30Level, insured, termInsurance, accidentalDeathBenefit, waiverOfPremium, terminalIllness, child, childTermRider. A missing `charges` array means this script cannot prove which charges are legal there. Confirm before emitting RatingItems for them.

## Before you write code

1. Run the bedrock `scan_plugins.py --project <dir>` to get the EXACT `rate(...)` overloads and request-record component names for this config.
2. Run the bedrock `build_catalog.py --project <dir>` to confirm the accessor paths above exist (e.g. `insured.termInsurance` -> the real return type).
3. Map each charge to `.rate()` vs `.amount()` using the handling column above — do not guess from the charge's name.
4. Emit at most one RatingItem per (elementLocator, chargeType) — duplicates throw.