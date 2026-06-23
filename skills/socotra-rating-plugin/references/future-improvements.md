# Future improvements

Running notes on gaps observed while using this skill on real configs. Each entry: what
happened, why it matters, and a concrete fix. Append as new cases surface.

---

## Open

_(none)_

---

## Resolved

### 1. `derive_rating_contract.py` did not resolve `policyLines/` — fixed 2026-06-19

A PersonalAuto product whose `contents` pointed at a **policy line**
(`policyLines/PersonalAutoLine`, itself containing `PersonalVehicle+` → PA coverages) emitted
`PersonalAutoLine | UNRESOLVED` and silently dropped the entire vehicle + coverage subtree,
leaving the PA contract nearly empty.

**Fix applied:** `Config` now loads `policyLines/` and registers them in the element lookup
(kind `policyLine`), so `lookup_element` resolves a policy-line ref and `walk_element_tree`
recurses into its `contents`/`charges` like any exposure. The policy line is treated as a
chargeable element in its own right. Verified on the PA+CGL config: the full
`PersonalAutoLine → PersonalVehicle → PA*` subtree now renders.

### 2. Name heuristics broke on ALL-CAPS / acronym names — fixed 2026-06-19

`_camel` lower-cased the first letter unconditionally, producing `ChargeType.gST` (real:
`ChargeType.GST`) and accessor guesses `cGLBodilyInjury()` (real: `CGLBodilyInjury()`), the
latter then flagged with a **false ✗ "accessor NOT found"** — telling users to fix correct code.

**Fix applied:**
- `_camel` now preserves a leading run of 2+ capitals verbatim (`GST` → `GST`,
  `CGLBodilyInjury` → `CGLBodilyInjury`, `PABodilyInjury` → `PABodilyInjury`) and only
  lower-cases a single leading capital (`PersonalVehicle` → `personalVehicle`).
- `_verify_in_catalog` matches **case-insensitively**, so a remaining casing difference vs the
  JAR no longer yields a false ✗.
- Legend reworded from "✗ = NOT found — fix the accessor" to "✗ = could not confirm — verify
  the accessor name/path against the JAR."

Verified: charges table emits `ChargeType.GST`; PA/CGL coverage accessors render verbatim.

### 3. Global-vs-Product plugin scoping undocumented — fixed 2026-06-19

The skill framed a multi-product config as one Global `RatePlugin`, without noting that
plugins can also be **Product-scoped** (`<productName>/plugins/java`, which takes precedence
over Global). Risked asserting "the platform only allows one rate plugin."

**Fix applied:** added a "Plugin scoping: Global vs Product" subsection to
`references/rating-concepts.md` under the overload-set discussion, covering both structures
plus the SDK-template (Global) and plugin-isolation caveats (product-scoped plugins can't share
a helper plugin).
