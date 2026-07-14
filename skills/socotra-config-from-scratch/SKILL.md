---
name: socotra-config-from-scratch
description: Build a deployable socotra-config folder tree from conversational, document, or any non-xlsx requirements. Sibling of socotra-config-from-xlsx — same output contract, same schema reference, same validateConfig loop — for when the user has NO filled-out Config Template workbook and instead describes the product in chat, a requirements doc, a PDF, or notes. If a Config Template .xlsx exists, use socotra-config-from-xlsx instead.
---

# socotra-config-from-scratch

Turn free-form product requirements into a `socotra-config/` tree that passes `./gradlew validateConfig`. This skill owns the **workflow only** — the schema lives in one shared place.

## Sources of truth (do not restate, read them)

- **Entity JSON shapes + hard-won validation rules** → `../socotra-config-from-xlsx/references/config-schema.md` (this plugin). Read it before writing any entity. The "Universal rules" section is where most validation failures die.
- **Sections not covered there** (automations, FNOL, work management, …) → the external `socotra-config` skill.
- **`validateConfig` output beats both.** On conflict: fix the config, then fix the reference.

## Output contract (identical to the xlsx sibling)

Write the standard folder tree under **`socotra-config/`** at the project root — never one big flattened `config.json`:

```
socotra-config/
├── config.json                      # root globals ONLY
├── accounts/<Name>/config.json
├── products/<Name>/config.json
├── policyLines/<Name>/config.json       # if used
├── exposureGroups/<Name>/config.json    # if used
├── exposures/<Name>/config.json
├── coverages/<Name>/config.json
├── coverageTerms/<Name>/config.json
├── charges/<Name>/config.json
├── installmentPlans/<Name>/config.json  # if used
└── numberingPlans/<Name>/config.json    # if used
```

Folder name = entity name (PascalCase). Each `config.json` holds only that entity's body — no wrapper key. This is what `socotra-ec-config-developer` packages and what the platform returns on download.

Containment hierarchy for reference:

```
Product → contents: policyLines | exposureGroups | exposures | coverages
  policyLine → exposureGroups | exposures | coverages
    exposureGroup → exposures
      exposure → coverages
        coverage → coverageTerms, charges (sub-coverages allowed)
```

## Workflow

1. **Intake.** Read whatever the user gives (conversation, doc, notes). Draft one structured summary — product name, account type(s), containment tree with quantifiers, coverage terms, charges, data fields, billing — and list every **assumption** you had to make. If the session is interactive, show it and get one confirmation; if you were handed requirements to execute autonomously, proceed and carry the assumptions into the final report instead.
2. **Fill the gaps yourself, visibly.** Free-form requirements never mention accounts, numbering plans, or charge handling. Create what referenced entities require (an `eligibleAccountTypes` entry needs an `accounts/<Name>/`; `numberingString` needs a `numberingPlans/<Name>/`) and mark each as *inferred*. Omit what nothing requires — no fabricated default plans.
3. **Build the tree** per the schema reference. Entity by entity, not one blob split at the end.
4. **Validate loop.** `./gradlew validateConfig`; fix reported errors (most map to a rule in the schema reference); repeat until clean. No SDK project in the workspace → set one up first (`setup-sdk` agent or the SDK template) — validation is not optional. The only validation command is `validateConfig`; deployment (`createTenant`/`deployConfig`) is the **user's** to run.
5. **Report.** What was built (tree listing), every inferred/assumed/omitted decision, and any requirement that needs a plugin instead of static config (e.g. "date can't be in the future" → validation plugin).

## Maintenance

New validation failure not covered by the schema reference? Fix the config, then add the rule to `../socotra-config-from-xlsx/references/config-schema.md` so both skills inherit it.
