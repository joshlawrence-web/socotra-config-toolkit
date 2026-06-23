# Socotra Config Toolkit

A Claude Code plugin that bundles six interlocking [Agent Skills](https://code.claude.com/docs/en/skills) for building on **Socotra Enterprise Core (EC)** — from a business‑requirements spreadsheet all the way to verified, deployable Java plugins.

```
business-requirements .xlsx
        │  xlsx-extract            (read any workbook, zero deps)
        ▼
 socotra-config-from-xlsx          socotra-rating-from-xlsx
   → product config                  → rate-table CSVs + rating contract
        │                                   │
        ▼                                   ▼
                          socotra-rating-plugin
                          (RatePlugin Java)
                                   │  socotra-jar-building-block
                                   ▼  (coremodel/DataFetcher structure + JAR introspection)
                          socotra-verify-deploy
                          (compile against generated config jar — catch errors before deploy)
```

## Install

```text
/plugin marketplace add socotra/socotra-config-toolkit
/plugin install socotra-config-toolkit@socotra
```

Replace `socotra/socotra-config-toolkit` with your own `owner/repo` if you fork it. The first command registers this repository as a plugin marketplace; the second installs the bundled plugin. Restart or `/plugin` to confirm the skills loaded.

You can also install directly with the CLI:

```bash
claude plugin marketplace add socotra/socotra-config-toolkit
claude plugin install socotra-config-toolkit@socotra
```

## Skills

Once installed, Claude invokes these automatically when relevant, or you can call them explicitly as `/socotra-config-toolkit:<skill>`.

| Skill | What it does |
| --- | --- |
| `xlsx-extract` | Stdlib-only `.xlsx` parser — dumps cells/sheets/comments to text/csv/json. No openpyxl or pandas. |
| `socotra-config-from-xlsx` | Maps a "Config Template" workbook → a deployable `socotra-config` (entities + transform rules that pass `validateConfig`). |
| `socotra-rating-from-xlsx` | Turns a filled "Rating workbook" → rate-table CSVs + an auditable rating contract for the active config. |
| `socotra-jar-building-block` | Foundation skill: EC Java structure (coremodel, `DataFetcher`, generated customer package, plugin dispatch) + `javap` JAR introspection. |
| `socotra-rating-plugin` | Builds a valid `RatePlugin` for the active config — legal charges per element, `.rate()` vs `.amount()`, rate-table lookups. |
| `socotra-verify-deploy` | Compiles plugin Java against the generated `customer-config.jar` locally to catch errors before a tenant round-trip. |

## Repository layout

```
.
├── .claude-plugin/
│   ├── plugin.json          # plugin manifest
│   └── marketplace.json     # marketplace listing (this repo hosts one plugin)
├── skills/
│   ├── xlsx-extract/
│   ├── socotra-config-from-xlsx/
│   ├── socotra-rating-from-xlsx/
│   ├── socotra-jar-building-block/
│   ├── socotra-rating-plugin/
│   └── socotra-verify-deploy/
└── README.md
```

## License

MIT — see [LICENSE](LICENSE).
