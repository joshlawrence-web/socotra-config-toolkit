---
name: xlsx-extract
description: Extract the contents of an .xlsx (Excel) spreadsheet into readable text/csv/json without openpyxl or pandas — a zero-dependency, stdlib-only parser. Use whenever a task hands you an .xlsx (business-requirements templates, data tables, config spreadsheets) and you need to read its cells, sheet by sheet, including cell comments. Resolves sharedStrings and the workbook's real sheet order, so values and tab names come out correct.
---

# xlsx-extract

Read an `.xlsx` from the command line with **no third-party packages**. An xlsx is just a zip of XML; this skill ships a stdlib-only Python parser (`xlsx_extract.py`) that resolves the shared-strings table and the workbook's sheet order — the two things a naive `unzip | grep` gets wrong.

Reach for this the moment a task involves an `.xlsx`. Don't try `openpyxl`/`pandas` first — they're usually not installed and the import error wastes a turn.

## Usage

```bash
python3 ~/.claude/skills/xlsx-extract/xlsx_extract.py FILE.xlsx [options]
```

| Command | What you get |
|---------|--------------|
| `FILE.xlsx` | **text grid** (default) — every non-empty cell as `A1=value`, grouped by sheet. Most token-efficient; best for sparse templates. |
| `FILE.xlsx --list` | sheet index + names only (cheap first look) |
| `FILE.xlsx --format json` | `{sheet: [[row],…]}` dense grid — round-trip data |
| `FILE.xlsx --format csv` / `--format tsv` | dense delimited grid per sheet |
| `FILE.xlsx --sheet "2 - Products"` | one sheet by **name** |
| `FILE.xlsx --sheet 2` | one sheet by **0-based index** |
| `FILE.xlsx --comments` | append cell comments (`B4 :: note…`) — templates often hide instructions here |

## Recommended flow

1. `--list` to see the tabs.
2. Default **text** format to read everything (or `--sheet` to focus).
3. Add `--comments` — author hints, examples, and "see other tab" notes commonly live in comments, not cells.

## What it handles

- sharedStrings (string interning) and inline strings
- rich-text runs (concatenates all `<t>` fragments in a cell)
- true sheet order + display names via `workbook.xml` + rels (not file order, which can differ)
- cell comments via the sheet's `.rels` → comments part
- empty cells skipped in text mode; padded in csv/tsv/json

## Limits

- **Values, not formulas**: returns the cached/last-computed value stored in the file. A formula with no cached result reads empty.
- No styles/colors/merged-cell expansion (a merged value sits in its top-left cell).
- Dates come through as Excel serial numbers unless the file stored them as text.

When a cell looks empty but the template implies content, re-run with `--comments` — the guidance is probably a comment.
