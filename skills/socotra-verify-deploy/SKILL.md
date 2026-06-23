---
name: socotra-verify-deploy
description: Pre-deploy verifier for Socotra EC plugin projects — compiles plugin Java against the generated customer-config.jar to catch errors locally before a tenant round-trip. Use as a verify-in-loop step after writing or editing any Socotra plugin (rating, validation, underwriting, precommit, automation, document) and before deploying. Reuses the project's own gradle compileJava; warns when the generated config jar is stale; flags untyped JsonNode string refs.
---

# Socotra verify-deploy

A local gate that answers **"will this plugin compile against the config it deploys to?"** without round-tripping to a tenant. Run it after every plugin edit and before any deploy. It's the verify half of a write→verify loop.

## Why it works

Socotra realises configuration as **generated Java symbols** in `build/customer-config.jar`: coverage classes, the `ChargeType` enum, table types + `makeKey(...)` signatures + column accessors, quote/segment/item types. So a plain compile against that jar already catches the bulk of "config reference" mistakes (typo a charge, a coverage, a table column → compile error). This tool wraps that compile and adds the two checks a compile alone misses.

## What it checks

| Tier | Check | Catches | Speed |
|------|-------|---------|-------|
| **tier1** COMPILE | gradle `compileJava` (authoritative classpath), else direct `javac` over `build/*.jar` | bad signatures, missing imports, type errors, **and** config-symbol typos (coverages, charges, tables, columns, key args, accessors) | ~3s (gradle daemon) |
| **tier2-stale** GUARD | `socotra-config/` mtime vs `customer-config.jar` mtime | stale generated symbols → compile result not trustworthy | instant |
| **tier2-untyped** LINT | untyped `JsonNode` keys: `.get("x")`, `.path("x")`, `.has("x")` | string refs the compiler **cannot** see (aux data, external JSON) — advisory only | instant |

**Out of scope (compile can't see these):** runtime logic, null from `DataFetcher`, invalid rating values, config-JSON structural validity, deploy packaging. For config-JSON validity use `./gradlew validateConfig` (server-side); for true semantics, deploy to a test tenant.

## Usage

```bash
python3 ~/.claude/skills/socotra-verify-deploy/verify_deploy.py <PROJECT_DIR> [--json] [--no-gradle] [--quiet]
```

- `PROJECT_DIR` — a project containing `build.gradle.kts` + `socotra-config/` (default: cwd).
- `--json` — machine-readable report (human summary goes to stderr). Use this in an LLM loop.
- `--no-gradle` — force direct `javac` (offline, no gradle daemon/plugin resolution).
- `--quiet` — suppress the human summary.

**Exit codes:** `0` PASS (no compile errors) · `1` FAIL (compile errors) · `2` ERROR (project/plugins/toolchain not found).

## How to use it in a loop

1. Write/edit the plugin Java.
2. Run the verifier (`--json`).
3. If `verdict == "FAIL"`: read `findings[]` where `tier == "tier1"` — each has `file`, `line`, `message` (with the offending `symbol:`/`location:`). Fix and re-run. **Do not deploy.**
4. If `verdict == "PASS"` but a `tier2-stale` warning is present: the generated jar predates your config edits — run `./gradlew refreshReferenceDatamodel` (needs network/creds) and re-verify before trusting the PASS.
5. Treat `tier2-untyped` notes as a manual checklist — confirm each string key exists in the config/aux data.

## Notes & limits

- **Staleness is the main correctness risk.** `customer-config.jar` is built server-side from `socotra-config/`; editing config JSON does **not** regenerate it locally. A PASS against a stale jar is provisional — the guard flags this; honour it.
- **Two plugin layouts** are supported: `src/main/java/...` (compiled by gradle) and `socotra-config/plugins/java/...` (older layout, compiled by direct javac).
- **javac fallback classpath** picks the single highest `core-datamodel` version in `build/`. If a project pins an older datamodel this can be inaccurate — the gradle path is authoritative, so prefer it.
- Related skills: `socotra-jar-building-block` (introspect the JARs to get signatures right *before* writing), `socotra-rating-plugin` (rating contract). This skill verifies *after*.
