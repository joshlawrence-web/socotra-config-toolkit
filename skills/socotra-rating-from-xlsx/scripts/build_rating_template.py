#!/usr/bin/env python3
"""
build_rating_template.py - generate the Socotra Rating workbook (blank framework or filled example).

Zero-dependency, stdlib only (no openpyxl). Writes a valid .xlsx using inlineStr cells,
which the companion xlsx-extract skill reads back without a sharedStrings table.

    python3 build_rating_template.py --out socotra-rating-template.xlsx          # blank framework
    python3 build_rating_template.py --out rating-example.xlsx --example         # filled term-life example

The workbook layout produced here is the SOURCE OF TRUTH for the format. The skill's
references/template-format.md documents it; if you change a tab/column here, update that doc.
"""
import argparse
import zipfile
from xml.sax.saxutils import escape

MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"


def col_letter(n):  # 1-based -> A, B, ... AA
    s = ""
    while n:
        n, r = divmod(n - 1, 26)
        s = chr(65 + r) + s
    return s


def cell_xml(col, row, value):
    ref = f"{col_letter(col)}{row}"
    if value is None or value == "":
        return ""
    return (f'<c r="{ref}" t="inlineStr"><is><t xml:space="preserve">'
            f'{escape(str(value))}</t></is></c>')


def sheet_xml(rows):
    out = [f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
           f'<worksheet xmlns="{MAIN}"><sheetData>']
    for r_idx, row in enumerate(rows, start=1):
        cells = "".join(cell_xml(c_idx, r_idx, v) for c_idx, v in enumerate(row, start=1))
        out.append(f'<row r="{r_idx}">{cells}</row>')
    out.append("</sheetData></worksheet>")
    return "".join(out)


def write_workbook(path, sheets):
    """sheets = list of (name, rows); rows = list of list-of-cell-strings."""
    content_types = [f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
                     '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">',
                     '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>',
                     '<Default Extension="xml" ContentType="application/xml"/>',
                     '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>']
    for i in range(len(sheets)):
        content_types.append(
            f'<Override PartName="/xl/worksheets/sheet{i+1}.xml" '
            f'ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>')
    content_types.append("</Types>")

    root_rels = (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
                 f'<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
                 f'<Relationship Id="rId1" Type="{R}/officeDocument" Target="xl/workbook.xml"/>'
                 f'</Relationships>')

    sheet_tags = "".join(
        f'<sheet name="{escape(name)}" sheetId="{i+1}" r:id="rId{i+1}"/>'
        for i, (name, _) in enumerate(sheets))
    workbook = (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
                f'<workbook xmlns="{MAIN}" xmlns:r="{R}"><sheets>{sheet_tags}</sheets></workbook>')

    wb_rels = [f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
               f'<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">']
    for i in range(len(sheets)):
        wb_rels.append(f'<Relationship Id="rId{i+1}" Type="{R}/worksheet" '
                       f'Target="worksheets/sheet{i+1}.xml"/>')
    wb_rels.append("</Relationships>")

    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", "".join(content_types))
        z.writestr("_rels/.rels", root_rels)
        z.writestr("xl/workbook.xml", workbook)
        z.writestr("xl/_rels/workbook.xml.rels", "".join(wb_rels))
        for i, (_, rows) in enumerate(sheets):
            z.writestr(f"xl/worksheets/sheet{i+1}.xml", sheet_xml(rows))


# --------------------------------------------------------------------------- #
# Layout definition. Blank = headers + a couple of empty rows to fill.
# Example = a worked term-life-style rater (config-agnostic placeholder names).
# --------------------------------------------------------------------------- #

HOW_TO_USE = [
    ["Socotra Rating Workbook"],
    ["Config-agnostic framework. Fill every cell with YOUR socotra-config names + rules."],
    [""],
    ["Fill order:"],
    ["  1 - Rating Setup     : products in scope + global defaults"],
    ["  2 - Charges          : every charge, the element it lands on, and rate-vs-amount handling"],
    ["  3 - Inputs           : name each rating variable + its config accessor"],
    ["  4 - Rate Tables      : declare each table (keys/values/type); put the data on its own tab"],
    ["  N - Rating           : one formula per (Element, Charge) -- THE algorithm"],
    [""],
    ["Formula DSL cheat-sheet (see references/formula-dsl.md for the full grammar):"],
    ["  Table[key1,key2]        exact lookup; keys match the table's Key Columns in order"],
    ["  Table[key1,key2].col    pick a specific value column (default = table's first value col)"],
    ["  band(Table, x)          range/banded lookup on bound value x"],
    ["  band(Table,[k1],x,linear)  banded lookup with non-range keys + interpolation mode"],
    ["  + - * / ( )             arithmetic (all BigDecimal, HALF_EVEN)"],
    ["  min(a,b)  max(a,b)  round(x[,mode])  abs(x)"],
    ["  if(cond, a, b)          cond uses == != < <= > >= and/or/not"],
    ["  variable names          from the 3 - Inputs tab; string literals in \"double quotes\""],
    [""],
    ["Conventions:"],
    ["  - Names are free text -> they must match your socotra-config entity names exactly."],
    ["  - Handling MUST be 'rate' (handling=normal in config) or 'amount' (flat/retention)."],
    ["  - Two Rating rows for the same (Element,Charge) are summed into one charge."],
    ["  - This workbook does NOT self-compute; the skill parses the formulas into Java."],
]

SETUP_BLANK = [
    ["Setting", "Value", "Notes"],
    ["Products In Scope", "", "comma-separated config product names"],
    ["Default Currency", "", "ISO code, e.g. USD"],
    ["Default Duration Basis", "", "annual | months | days (matches config)"],
    ["Default Rounding Mode", "HALF_EVEN", "platform convention"],
    ["Default On-Miss", "error", "error | passthrough | <literal> when a table lookup finds no row"],
    ["Plugin Scope", "global", "global | product (per-product plugin folders)"],
]

CHARGES_BLANK = [
    ["Charge Name", "Lands On (Element)", "Handling", "Category", "Notes"],
    ["", "", "", "", ""],
    ["", "", "", "", ""],
]

INPUTS_BLANK = [
    ["Variable", "Source (config accessor)", "Type", "Allowed Values", "Notes"],
    ["", "", "", "", ""],
    ["", "", "", "", ""],
]

TABLES_BLANK = [
    ["Table Name", "Type", "Key Columns", "Range Column", "Value Columns", "On Miss", "Grid Tab", "Notes"],
    ["", "exact", "", "", "", "", "", "exact = makeKey lookup"],
    ["", "range", "", "", "", "", "", "range = banded; put bands on the grid tab"],
]

RATING_BLANK = [
    ["Element", "Charge", "Formula", "Notes"],
    ["", "", "", ""],
    ["", "", "", ""],
    ["", "", "", ""],
]

GRID_BLANK = [
    ["# Rename this tab to 'T - <YourTableName>' and match the Table's columns below"],
    ["key_col_1", "key_col_2", "value_col"],
    ["", "", ""],
]


def blank_sheets():
    return [
        ("0 - How to Use", HOW_TO_USE),
        ("1 - Rating Setup", SETUP_BLANK),
        ("2 - Charges", CHARGES_BLANK),
        ("3 - Inputs", INPUTS_BLANK),
        ("4 - Rate Tables", TABLES_BLANK),
        ("T - ExampleTable", GRID_BLANK),
        ("N - Rating", RATING_BLANK),
    ]


def example_sheets():
    setup = [
        ["Setting", "Value", "Notes"],
        ["Products In Scope", "TermLife", ""],
        ["Default Currency", "USD", ""],
        ["Default Duration Basis", "annual", ""],
        ["Default Rounding Mode", "HALF_EVEN", ""],
        ["Default On-Miss", "error", ""],
        ["Plugin Scope", "global", ""],
    ]
    charges = [
        ["Charge Name", "Lands On (Element)", "Handling", "Category", "Notes"],
        ["Premium", "DeathBenefit", "rate", "premium", "main coverage premium"],
        ["PolicyFee", "TermLife", "amount", "fee", "flat once-per-term fee on the product"],
    ]
    inputs = [
        ["Variable", "Source (config accessor)", "Type", "Allowed Values", "Notes"],
        ["sex", "quote.insured().data().sex()", "enum", "M,F", ""],
        ["age", "quote.insured().data().issueAge()", "int", "", "issue age"],
        ["face", "quote.data().faceAmount()", "decimal", "", "face amount"],
        ["mode", "quote.data().paymentMode()", "enum", "annual,monthly", ""],
        ["territory", "quote.data().territory()", "enum", "coastal,inland", ""],
    ]
    tables = [
        ["Table Name", "Type", "Key Columns", "Range Column", "Value Columns", "On Miss", "Grid Tab", "Notes"],
        ["BaseRates", "exact", "sex,age", "", "rate", "error", "T - BaseRates", "per-1000 base rate"],
        ["Modality", "exact", "mode", "", "factor", "error", "T - Modality", "modal load"],
        ["TerritoryFactor", "exact", "territory", "", "factor", "1.00", "T - TerritoryFactor", "default 1.00 if missing"],
    ]
    base_rates = [
        ["sex", "age", "rate"],
        ["M", "35", "1.10"],
        ["M", "36", "1.18"],
        ["F", "35", "0.95"],
        ["F", "36", "1.01"],
    ]
    modality = [
        ["mode", "factor"],
        ["annual", "1.00"],
        ["monthly", "1.08"],
    ]
    territory = [
        ["territory", "factor"],
        ["coastal", "1.20"],
        ["inland", "1.00"],
    ]
    rating = [
        ["Element", "Charge", "Formula", "Notes"],
        ["DeathBenefit", "Premium",
         "BaseRates[sex,age] * (face/1000) * Modality[mode] * TerritoryFactor[territory]",
         "base x modal x territory"],
        ["TermLife", "PolicyFee", "25.00", "flat policy fee"],
    ]
    return [
        ("0 - How to Use", HOW_TO_USE),
        ("1 - Rating Setup", setup),
        ("2 - Charges", charges),
        ("3 - Inputs", inputs),
        ("4 - Rate Tables", tables),
        ("T - BaseRates", base_rates),
        ("T - Modality", modality),
        ("T - TerritoryFactor", territory),
        ("N - Rating", rating),
    ]


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--out", required=True, help="output .xlsx path")
    ap.add_argument("--example", action="store_true", help="emit the filled term-life example instead of a blank framework")
    a = ap.parse_args()
    sheets = example_sheets() if a.example else blank_sheets()
    write_workbook(a.out, sheets)
    print(f"wrote {a.out} ({len(sheets)} sheets, {'example' if a.example else 'blank'})")


if __name__ == "__main__":
    main()
