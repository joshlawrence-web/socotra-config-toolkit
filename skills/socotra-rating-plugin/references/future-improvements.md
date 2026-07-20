# Future improvements

Running notes on gaps observed while using this skill on real configs. Each entry: what
happened, why it matters, and a concrete fix. Append as new cases surface.

> The resolved entries below were originally fixes to a `derive_rating_contract.py` script
> (since retired). That script's logic now lives as the reading-and-reasoning method in
> SKILL.md ("Derive the rating contract by reading the config"); the lessons are preserved
> there. Entries are kept for the reasoning, reworded to describe the derivation *method*.

---

## Open

_(none)_

---

## Resolved

### 1. Element walk did not resolve `policyLines/` — fixed 2026-06-19

A PersonalAuto product whose `contents` pointed at a **policy line**
(`policyLines/PersonalAutoLine`, itself containing `PersonalVehicle+` → PA coverages) was
emitted as `PersonalAutoLine | UNRESOLVED`, silently dropping the entire vehicle + coverage
subtree and leaving the PA contract nearly empty.

**Fix (now baked into the method):** the element lookup loads `policyLines/` and registers them
(kind `policyLine`), so a policy-line ref resolves and the walk recurses into its
`contents`/`charges` like any exposure. The policy line is treated as a chargeable element in
its own right. Verified on the PA+CGL config: the full `PersonalAutoLine → PersonalVehicle →
PA*` subtree renders. (See Step 0 / Step 4 of the SKILL.md derivation method.)

### 2. Name heuristics broke on ALL-CAPS / acronym names — fixed 2026-06-19

The config→Java name rule originally lower-cased the first letter unconditionally, producing
`ChargeType.gST` (real: `ChargeType.GST`) and accessor guesses `cGLBodilyInjury()` (real:
`CGLBodilyInjury()`), the latter then flagged with a **false ✗ "accessor NOT found"** — telling
users to fix correct code.

**Fix (now baked into the method):**
- The name rule preserves a leading run of 2+ capitals verbatim (`GST` → `GST`,
  `CGLBodilyInjury` → `CGLBodilyInjury`, `PABodilyInjury` → `PABodilyInjury`) and only
  lower-cases a single leading capital (`PersonalVehicle` → `personalVehicle`). (SKILL.md Step 3.)
- Accessor confirmation against the JAR is matched **case-insensitively**, so a remaining casing
  difference vs the JAR does not yield a false ✗.
- Wording is "could not confirm — verify the accessor name/path against the JAR," not
  "NOT found — fix the accessor."

Verified: charges table emits `ChargeType.GST`; PA/CGL coverage accessors render verbatim.

### 3. Global-vs-Product plugin scoping undocumented — fixed 2026-06-19

The skill framed a multi-product config as one Global `RatePlugin`, without noting that
plugins can also be **Product-scoped** (`<productName>/plugins/java`, which takes precedence
over Global). Risked asserting "the platform only allows one rate plugin."

**Fix applied:** added a "Plugin scoping: Global vs Product" subsection to
`references/rating-concepts.md` under the overload-set discussion, covering both structures
plus the SDK-template (Global) and plugin-isolation caveats (product-scoped plugins can't share
a helper plugin).
