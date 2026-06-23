# Rating-plugin tooling

The config-side complement to the bedrock JAR scanners. The bedrock
`scan_plugins.py`/`build_catalog.py` read the **generated JAR** (exact overloads,
accessor paths). They cannot read the **`socotra-config` JSON**, where the two facts a
rating plugin most often gets wrong live:

1. which charges are **legal** on which element (`charges: [...]` per element), and
2. each charge's **handling** → whether the `RatingItem` needs `.rate()` or `.amount()`.

`derive_rating_contract.py` fills that gap.

## derive_rating_contract.py

Walks a `socotra-config` directory tree and emits a per-product **rating contract**:
candidate `rate(...)` overloads, every chargeable element with its legal charges (each
tagged with the required `RatingItem` method), and the rate tables with their
`makeKey(...)` signatures. Resolves product `extend` inheritance and the `contents`
element tree (handling repeating `+`/`*`, optional `?`, required `!` modifiers).

```
python3 derive_rating_contract.py --config <socotra-config-dir> [--out contract.md] [--json contract.json]
python3 derive_rating_contract.py --config <dir> --catalog catalog.json   # note accessor cross-checks
```

- **Input:** a directory containing `products/` and `charges/` (and usually
  `coverages/`, `exposures/`, `tables/`), each component a folder holding `config.json`.
- **Output:** Markdown to `--out` (or stdout) and/or raw JSON to `--json`.
- Pure Python stdlib; **no JDK required** (unlike the bedrock scanners).

### What it cannot know (and flags)

Type/accessor names it prints (`<Product>QuoteRequest`, `ChargeType.x`, `quote.foo()`)
are **derived from config naming conventions** and must be confirmed against the JAR.
- An element with **no `charges` array** is flagged ⚠️ — the legal charge set is not
  derivable from config there; confirm via config review or `Chargeable.charges()`.
- A `contents` ref that resolves to neither a coverage nor an exposure is marked
  `UNRESOLVED` (e.g. a sub-product or policy line in a package product) — confirm via the JAR.
- Repeating-element collection accessor **names** (pluralization) are not guessable;
  confirm with the bedrock `build_catalog.py`.

### examples/

Sample output from real EC demo configs: `term-life-contract.md` (+ `.json`) — a
config that declares **no** `charges` arrays, so every element is flagged ⚠️;
`accident-and-health-contract.md` — a config that declares charges and `handling`, so
the legal-charge / rate-vs-amount columns are populated. Both run config-only (no
`--catalog`); pass `--catalog catalog.json` against a real SDK build to add the
accessor `✓/✗` verification column.

### Recommended order

1. `scan_plugins.py --project <sdk>` → exact overloads + request records (JAR truth).
2. `build_catalog.py --project <sdk>` → exact accessor paths + return types (JAR truth).
3. `derive_rating_contract.py --config <config>` → legal charges + handling (config truth).
4. Reconcile; the JAR wins on names, the contract is authoritative on which-charge/which-handling.
