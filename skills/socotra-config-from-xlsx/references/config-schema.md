# socotra-config JSON schema (distilled)

The per-entity JSON schema this skill maps workbook tabs onto. **Folder name = entity
name**; each entity is a `config.json` under `socotra-config/<section>/<Name>/`. This is
the minimal slice needed for the common config-authoring path — the full authority
(every section, plus automations/FNOL/work-management/etc.) lives in the external
`socotra-config` skill if you need a section not covered here.

The `validateConfig` result always wins over anything below — if they disagree, fix the
file, then fix this reference.

---

## Universal rules (memorise these — most validation failures are here)

- **`decimal` for ALL numbers, never `integer`.** `integer` fails with "type [Integer] is not defined".
- **`min` / `max` are STRINGS**, not numbers: `"min": "0"` not `"min": 0`. (Applies to decimal + date.)
- **Every concrete entity needs `"abstract": false`.** `abstract: true` = a base template only `extend`ed, never instantiated.
- **Optional field** = append `?` to the type: `"type": "string?"`.
- **Names are PascalCase** for entities; **quantifier suffix goes on the reference**, not the definition.

## Data field types (`data: {}` on any entity)

| Type | Constraints |
|------|-------------|
| `string` | `minLength`, `maxLength` (numbers), `regex`, `options` (array of allowed values) |
| `decimal` | `min`, `max` (strings), `precision` (number), `roundingMode` (`ceiling`/`down`/`floor`/`halfDown`/`halfEven`/`halfUp`/`up`) |
| `boolean` | none |
| `date` | `min`, `max` (ISO strings, e.g. `"2020-01-01"`) |
| `timestamp` | date + time |

Every field type also accepts:
- `displayName` (string) — human label.
- `defaultValue` (string) — used when a required field is absent at validation; **required to auto-create (`!`) an entity that has required fields**.
- `scope` (string) — `QQ` (quick quote) / `Q` (quote) / `P` (policy), comma-combinable (`"Q, P"`).
- `tag` (array) — UI hints: `hidden`, `readOnly`, `multiselect` (needs array type + `options`), `radio-group`, `currency.<ISO>` (e.g. `currency.USD`).

Array field types: `string*` / `string+` (zero-or-more / one-or-more) — used with `multiselect`.

```json
"data": {
  "vehicleYear": { "type": "decimal", "displayName": "Year", "min": "1900", "max": "2030" },
  "middleName":  { "type": "string?", "displayName": "Middle Name", "maxLength": 50 },
  "vehicleType": { "type": "string", "displayName": "Type", "options": ["Sedan","SUV","Truck"] }
}
```

## Quantifiers (on `contents` / `coverageTerms` / `charges` references)

| Suffix | Meaning |
|--------|---------|
| (none) | Exactly one, required, manual |
| `!` | Exactly one, **auto-created** |
| `?` | Zero or one (optional) |
| `*` | Zero or more |
| `+` | One or more |

**Auto-created (`!`)** entities MUST have: no required data fields without `defaultValue`, no
required coverage terms without a default option (`*`), and the same holds recursively for
sub-elements.

## Platform-managed fields — NEVER create these as custom data fields

- Dates: `policyStartDate`, `policyEndDate`, `effectiveDate`, `expirationDate`
- Identifiers: `policyNumber`, `quoteNumber`, `policyId`
- Status: `status`, `state`, `cancellationDate`
- Audit: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Financial: `totalPremium`, `writtenPremium`, `balance`

---

## `config.json` (root — Global Settings tab)

Required: `defaultCurrency`, `defaultTimeZone`, `defaultTermDuration`, `defaultDurationBasis`.

```json
{
  "defaultCurrency": "USD",
  "defaultTimeZone": "America/New_York",
  "defaultTermDuration": 12,
  "defaultDurationBasis": "months",          // months | years | days
  "enableSerialInvoiceNumbering": false,
  "contactRoles": ["agent", "broker"],        // LOWERCASE or name-format validation rejects
  "lossCategories": ["property", "liability"] // LOWERCASE
}
```
Optional `default*Plan` keys (`defaultInstallmentPlan`, `defaultAutoRenewalPlan`,
`defaultDelinquencyPlan`, …) reference plans by name — omit if you have no such plan
(don't fabricate one). Align `defaultDurationBasis`/`defaultTermDuration` with the rating
convention (annual EU = `years`/`1`; monthly US = `months`/`12` or `6`).

## `products/<Name>/config.json`

```json
{
  "displayName": "Auto Insurance",
  "abstract": false,
  "eligibleAccountTypes": ["ConsumerAccount"],   // each must exist as accounts/<Name>
  "contents": ["Vehicle+", "Liability!"],        // coverages/exposures/policyLines + quantifier
  "coverageTerms": ["PolicyDeductible!"],
  "charges": ["Premium", "PolicyFee"],
  "data": { "...": {} }
}
```
Optional: `extend`, `defaultInstallmentPlan`, `defaultDurationBasis`, `defaultTermDuration`,
`numberingTrigger` (`creation`), `numberingPlan` + `numberingString`, `staticData` (same
shape as `data`).

## `exposures/<Name>/config.json`

The repeating unit a policy holds many of (Vehicle, Location, Building). `contents` = the
coverages it carries.

```json
{
  "displayName": "Vehicle",
  "abstract": false,
  "contents": ["Liability!", "Collision?", "Comprehensive?"],
  "coverageTerms": ["..."],
  "charges": ["Premium"],
  "data": { "vin": { "type": "string", "maxLength": 17 } }
}
```

## `coverages/<Name>/config.json`

```json
{
  "displayName": "Liability Coverage",
  "abstract": false,
  "charges": ["Premium"],
  "coverageTerms": ["LiabilityLimit!", "LiabilityDeductible?"],
  "data": { "...": {} }
}
```
Optional: `extend`, `contents` (sub-coverages).

## `coverageTerms/<Name>/config.json`

A term uses EITHER `options` (finite set) OR `value` (free input) — never both. `type` is
`limit` or `deductible`.

```json
// options-based — prefix the default key with *
{
  "type": "deductible",
  "displayName": "Collision Deductible",
  "options": {
    "ded250":  { "displayName": "$250",  "value": 250 },
    "*ded500": { "displayName": "$500",  "value": 500 },   // default
    "ded1000": { "displayName": "$1,000","value": 1000 }
  }
}
// value-based
{ "type": "limit", "displayName": "Building Limit", "value": { "type": "decimal", "min": "10000", "max": "10000000" } }
```
- **Option keys must be lowercase** (`o_1000000`, `ded500`) — `O_1000000` fails name-format validation. Lowercase the default key too.
- A `!` term on its coverage needs a default option (`*`).

## `charges/<Name>/config.json`

```json
{ "category": "premium", "handling": "normal", "invoicing": "scheduled", "transactionBundlingEnabled": false }
```
- **`category` is built-in, not extendable**: `premium` | `tax` | `fee` | `surcharge` | `credit` | `nonfinancial`. Remap outside-set template values (`commission` → `nonfinancial`, `discount` → `credit`).
- `handling`: `normal` | `flat`. `invoicing`: `immediate` | `next` | `scheduled`.
- **`invoicing: immediate` (and `next`) is NOT allowed with `handling: normal`** — use `handling: flat` for immediate fees.
- `nonfinancial` charges are tracked but never invoiced (commissions, technical premium).
- Charge and coverage may share a name (different namespaces).

## `installmentPlans/<Name>/config.json`

```json
{
  "displayName": "Monthly Plan",
  "cadence": "monthly",              // fullPay | monthly | quarterly | semiannually | annually
  "anchorMode": "termStartDay",
  "dueLeadDays": 30,
  "generateLeadDays": 45,
  "maxInstallmentsPerTerm": 12,
  "installmentWeights": [1.0]        // proportions, one per installment; optional
}
```

## `numberingPlans/<Name>/config.json` (inferred — no workbook tab)

A product's `numberingString`/`numberingTrigger` **require** a numbering plan.

```json
{ "displayName": "Policy Numbering", "format": "\\P\\O\\L-########", "initialCoreNumber": "10000001" }
```
- **Escape EVERY literal character with `\`** in `format`: `GL-######` → `"\\G\\L-######"` (unescaped letters fail "unrecognized character"). `#` = a sequence digit.
- `{product}` placeholder + product's `numberingString` → per-product prefix (`AUTO-100001`).
- Optional: `copyFromQuote`, `quoteNumberFormat`, `initialQuoteCoreNumber`.

## `accounts/<Name>/config.json` (inferred — no workbook tab)

Products reference `eligibleAccountTypes`; if the workbook has no account tab, define each
referenced type here and tell the user you inferred it.

```json
{
  "displayName": "Consumer Account",
  "abstract": false,
  "data": {
    "firstName": { "type": "string", "displayName": "First Name", "maxLength": 100 },
    "lastName":  { "type": "string", "displayName": "Last Name",  "maxLength": 100 }
  }
}
```
Optional: `extend`, `numberingPlan` + `numberingTrigger`, `invoiceNumberingPlan`,
`defaultInvoiceDocument`, `contacts` (`{ "agent": ["Agent"] }`).

## `tables/<Name>/config.json` (rating lookup tables — used by the rating skills)

```json
{
  "selectionTimeBasis": "termStartTime",   // termStartTime | transactionTime | policyStartTime | currentTime
  "columns": {
    "territory":  { "dataType": "string",  "isKey": true },
    "baseFactor": { "dataType": "decimal", "isKey": false }
  }
}
```
- `isKey: true` = lookup/filter column; `isKey: false` = returned value.
- `dataType`: `string` | `decimal` | `boolean` — **`decimal` for all numbers**.
- CSV data lives in `bootstrap/resources/resourceFiles/tables/`; CSV headers must match `columns` names exactly.
