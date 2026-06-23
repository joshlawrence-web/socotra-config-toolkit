# Example: Commercial General Liability (epsilon config)

A deliberately small, teach-friendly rating workbook built against the real **Commercial General
Liability** product in the `epsilon` socotra-config — not placeholder names. One table, two inputs,
three rules. Use it as a reference for filling the blank framework against an actual config.

## Files

- `cgl-rating.xlsx` — the filled Rating workbook.
- `contract.html` — the auditable rating contract (open in a browser).
- `contract.json` — same contract, machine-readable (drives the RatePlugin generator).

Regenerate the contract after editing the workbook:

```bash
python3 scripts/parse_rating_workbook.py examples/cgl/cgl-rating.xlsx \
  --out examples/cgl/contract.html --json examples/cgl/contract.json
```

## What it rates

Every name matches the epsilon config exactly (product `CommercialGeneralLiability`, exposure
`Location`, its coverages, and the `Premium` / `Fee` charges).

| Element (coverage) | Charge | Handling | Pricing |
|---|---|---|---|
| `BodilyInjury` | `Premium` | rate | `BaseRate[state] * (grossSales/1000)` |
| `MedicalPayments` | `Premium` | amount | flat `150.00` (single $5k med-expense limit) |
| `WaiverOfSubro` | `Fee` | amount | flat `250.00` |

Three rules, three teaching points:

1. **A rated premium from a table** — look up the state's rate per $1,000 of sales, multiply by
   sales in thousands. The one bit of arithmetic in the book.
2. **A flat premium amount** — a fixed dollar figure, no table, no math.
3. **A flat fee** — same shape, but a `Fee` charge instead of `Premium`.

That covers both charge handlings (`rate` vs `amount`) and a single table lookup — and nothing else.

## Inputs and tables

- **Inputs** (two): `state` and `grossSales`, both from the `Location` exposure.
- **Rate table** (one): `BaseRate` keyed by `state` → `ratePer1000`.

## Worked number

For a Texas location with $500,000 gross sales: `BaseRate[TX]` = `0.45`, so the Bodily Injury
premium rate is `0.45 * (500000 / 1000)` = **`225.00`**. Add the flat `150.00` med-pay premium, and
the `250.00` fee only if Waiver of Subrogation is added.

## Caveat — input accessors are candidates, not confirmed

The `Source (config accessor)` paths on the `3 - Inputs` tab (e.g.
`location.data().annualGrossSales()`) are plausible placeholders. Per the skill workflow, confirm
every accessor and `ChargeType`/table `makeKey` signature against the generated JAR before trusting
them when you build the RatePlugin.
