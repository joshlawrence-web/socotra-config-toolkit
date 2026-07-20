---
name: xlsx-extract
description: Read the contents of an .xlsx (Excel) spreadsheet by hand — no code, no libraries, no network. An .xlsx is a zip of XML; this skill teaches you to unzip it and read the sharedStrings, workbook order, sheet cells, and cell comments directly with the Read/Grep tools. Use whenever a task hands you an .xlsx (business-requirements templates, data tables, config spreadsheets) and you need its cell values, sheet by sheet, with correct tab names.
---

# xlsx-extract

Read an `.xlsx` **by hand**, with no code execution and no third-party packages. An xlsx is just a zip of XML parts. This skill is a **method**, not a program: you `unzip` the file and read a handful of XML parts with the `Read`/`Grep` tools, resolving the two things a naive `unzip | grep` gets wrong — the **shared-strings table** (most text lives there, not in the cells) and the **real sheet order + display names** (file order can differ).

> **Why by hand:** this environment runs with **no code execution and no network**. There is no `xlsx_extract.py`, no `openpyxl`, no `pandas`. Don't reach for any of them — reason over the XML directly. `unzip` (a standard archive tool, not code execution) is the one shell utility you rely on; if it isn't available, note that to the user rather than trying to script around it.

## Step 1 — crack the file open

An `.xlsx` is a zip. List its members, then extract the parts you need:

```bash
unzip -l "FILE.xlsx"                    # see what's inside
unzip -o "FILE.xlsx" -d ./xlsx-tmp      # extract all parts to a temp dir
```

Prefer a scratch dir you control (or the session scratchpad). To peek at a single member without extracting everything, stream it:

```bash
unzip -p "FILE.xlsx" xl/workbook.xml
```

The parts that matter:

| Part | What it gives you |
|---|---|
| `xl/workbook.xml` | the sheet list — `<sheet name=… r:id=…>` in **display order** |
| `xl/_rels/workbook.xml.rels` | maps each `r:id` → the actual `worksheets/sheetN.xml` path |
| `xl/sharedStrings.xml` | the interned string table — cells of type `s` index into this |
| `xl/worksheets/sheetN.xml` | one per sheet: the cells (`<c>`) and their values |
| `xl/worksheets/_rels/sheetN.xml.rels` + `xl/comments*.xml` | cell comments (author hints often hide here) |
| `xl/styles.xml` | fills/formats — only needed for color/style detection (e.g. example-row shading) |

## Step 2 — build the sheet list (name → xml path, in order)

**Never trust file order or a "How to Use" tab's numbering.** Resolve it properly:

1. `Read` `xl/workbook.xml`. Under `<sheets>`, each `<sheet name="2 - Products" sheetId="…" r:id="rId3"/>` appears in true display order. Record `name` and the `r:id` (the attribute is namespaced — it renders as `r:id`).
2. `Read` `xl/_rels/workbook.xml.rels`. Each `<Relationship Id="rId3" Target="worksheets/sheet2.xml"/>` maps the `r:id` to a path. Join on the `Id`. A `Target` starting with `/` is absolute (strip the leading `/`); otherwise it's relative to `xl/` (prepend `xl/`).

The result is your ordered `[(sheet name, sheetN.xml path)]` list — the equivalent of the old `--list`.

## Step 3 — resolve shared strings

`Read` `xl/sharedStrings.xml`. It's a flat list of `<si>` elements. **The 0-based position of each `<si>` is its string index.** So the first `<si>` is index 0, the second is index 1, etc.

For a cell's text, concatenate **every `<t>` descendant** inside its `<si>` — rich-text cells split one string across multiple `<r><t>…</t></r>` runs, and the true value is all of them joined with no separator. (A plain cell is just `<si><t>value</t></si>`.)

For a large table, don't try to hold the whole list in your head — `Grep` the value you expect and confirm its index, or read the specific `<si>` a cell points to.

## Step 4 — read a sheet's cells

`Read` the sheet's `xl/worksheets/sheetN.xml`. Cells look like:

```xml
<row r="1">
  <c r="A1" t="s"><v>0</v></c>       <!-- t="s": <v> is an index into sharedStrings -->
  <c r="B1"><v>42.5</v></c>          <!-- no t (or t="n"): <v> is the literal number -->
  <c r="C1" t="inlineStr"><is><t>hi</t></is></c>   <!-- inline string: read <is>'s <t>s -->
  <c r="D1" t="b"><v>1</v></c>       <!-- boolean: 1=true, 0=false -->
</row>
```

Resolution rules per cell:
- **`t="s"`** → the `<v>` is a **shared-string index**; look it up in your step-3 list. This is the common case for all text.
- **`t="inlineStr"`** → read the `<is>` element's `<t>` descendants (concatenate, same as shared strings).
- **no `t`, or `t="n"`** → `<v>` is the literal (number stored as text).
- **`t="b"`** → `<v>` is `1`/`0` = true/false.
- **empty `<c>`** (no `<v>`/`<is>`) → skip.

**Cell ref → column/row:** the `r` attribute (`"B12"`) is column letters + row number. Column letters are base-26: `A`=1, `B`=2, … `Z`=26, `AA`=27. Compute `col = col*26 + (letter-'A'+1)` across the letters. You rarely need the numeric column — the letter is usually enough to line values up against headers.

**Read row 1 (and often rows 2–4) first** — headers and instruction rows live there. Then map every data row's cells to those headers **by column-header text, never by fixed column letter** (templates reorder columns).

## Step 5 — cell comments (don't skip these)

Templates hide examples, "see other tab" notes, and field hints in **cell comments**, not cells. To read them:

1. `Read` `xl/worksheets/_rels/sheetN.xml.rels`. Find the `<Relationship>` whose `Type` contains `comments`; its `Target` (resolve `../` → `xl/`) points to the comments part (e.g. `xl/comments1.xml`).
2. `Read` that comments part. Each `<comment ref="B4">` carries text in `<t>` descendants — concatenate them, same rule as everywhere else. Report as `B4 :: note text`.

If a cell looks empty but the template implies content, the guidance is almost certainly a comment — go check the comments part before concluding it's blank.

## Step 6 — (optional) style / fill detection

Only when a task needs cell **formatting** — e.g. distinguishing green "example" rows from real data (see the `socotra-config-from-xlsx` skill's Step 0):

1. `Read` `xl/styles.xml`. Under `<fills>`, each `<fill>`'s **0-based position** is its `fillId`; find the one whose `<fgColor rgb="…"/>` is the color you're after (trust the rgb you actually see — palettes drift).
2. Under `<cellXfs>`, each `<xf>`'s **0-based position** is a style index — the value a cell carries in its `s="…"` attribute. Note every `<xf>` whose `fillId=` matches the fill you found.
3. Back in the sheet XML, any `<c … s="N">` whose `N` is one of those style indices is a cell with that fill.

## What this method handles

- sharedStrings interning and inline strings
- rich-text runs (concatenate all `<t>` fragments in a cell)
- true sheet order + display names via `workbook.xml` + rels (not file order)
- cell comments via the sheet's `.rels` → comments part
- cell fill/style detection via `styles.xml` when needed

## Limits (same as any xlsx reader)

- **Values, not formulas**: a cell stores the last cached value; a formula never recalculated reads empty.
- No merged-cell expansion — a merged value sits in its top-left cell only.
- Dates come through as Excel serial numbers unless the file stored them as text.
- Writing cells back into an `.xlsx` is **out of scope** — this is a read-only method. If a task needs an edited workbook, copy the file and tell the user which cells to change (see the config-from-xlsx versioning rule).
