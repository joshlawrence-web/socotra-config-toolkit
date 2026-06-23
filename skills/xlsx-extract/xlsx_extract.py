#!/usr/bin/env python3
"""Extract an .xlsx into readable text/csv/json/tsv using only the Python stdlib.

No openpyxl / pandas required — an .xlsx is a zip of XML, so this unzips it in
memory and resolves sharedStrings + the workbook's sheet order itself.

Usage:
    xlsx_extract.py FILE.xlsx                  # text grid, all sheets
    xlsx_extract.py FILE.xlsx --format csv     # csv per sheet
    xlsx_extract.py FILE.xlsx --format tsv
    xlsx_extract.py FILE.xlsx --format json    # {sheet: [[row], ...]}
    xlsx_extract.py FILE.xlsx --sheet "2 - Products"   # one sheet (name or index)
    xlsx_extract.py FILE.xlsx --comments       # also dump cell comments
    xlsx_extract.py FILE.xlsx --list           # just list sheet names

text format (default) prints only non-empty cells as `A1=value | B1=value`,
which is the most token-efficient view for sparse business-template sheets.
csv/tsv/json emit dense rectangular grids (good for round-tripping data).
"""
import argparse, json, re, sys, zipfile
import xml.etree.ElementTree as ET

NS = "{http://schemas.openxmlformats.org/spreadsheetml/2006/main}"
RID = "{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"


def _text(el):
    """Concatenate every <t> descendant (handles rich-text runs)."""
    return "".join(t.text or "" for t in el.iter(NS + "t"))


def _col_row(ref):
    """'B12' -> (col_index_1based, row_number)."""
    m = re.match(r"([A-Z]+)(\d+)", ref)
    col = 0
    for c in m.group(1):
        col = col * 26 + (ord(c) - 64)
    return col, int(m.group(2))


def load_workbook(path):
    """Return (ordered list of (sheet_name, xml_path), shared_strings, zipfile)."""
    z = zipfile.ZipFile(path)
    shared = []
    if "xl/sharedStrings.xml" in z.namelist():
        root = ET.fromstring(z.read("xl/sharedStrings.xml"))
        shared = [_text(si) for si in root.findall(NS + "si")]
    rels = ET.fromstring(z.read("xl/_rels/workbook.xml.rels"))
    rel_target = {r.get("Id"): r.get("Target") for r in rels}
    wb = ET.fromstring(z.read("xl/workbook.xml"))
    sheets = []
    for s in wb.find(NS + "sheets").findall(NS + "sheet"):
        tgt = rel_target[s.get(RID)]
        tgt = tgt[1:] if tgt.startswith("/") else "xl/" + tgt
        sheets.append((s.get("name"), tgt))
    return sheets, shared, z


def read_sheet(z, xml_path, shared):
    """Return {(col,row): value} for all non-empty cells."""
    root = ET.fromstring(z.read(xml_path))
    cells = {}
    for row in root.iter(NS + "row"):
        for c in row.findall(NS + "c"):
            ref, t = c.get("r"), c.get("t")
            v = c.find(NS + "v")
            if v is not None and v.text is not None:
                val = shared[int(v.text)] if t == "s" else v.text
            else:
                isv = c.find(NS + "is")
                val = _text(isv) if isv is not None else ""
            if val not in (None, ""):
                cells[_col_row(ref)] = val
    return cells


def read_comments(z, sheet_xml):
    """Return list of (cell_ref, text) for a sheet, if a comments part exists."""
    rels_path = sheet_xml.rsplit("/", 1)[0] + "/_rels/" + sheet_xml.rsplit("/", 1)[1] + ".rels"
    if rels_path not in z.namelist():
        return []
    rels = ET.fromstring(z.read(rels_path))
    out = []
    for r in rels:
        if "comments" in (r.get("Type") or ""):
            tgt = r.get("Target").replace("../", "xl/")
            if not tgt.startswith("xl/"):
                tgt = "xl/" + tgt
            try:
                croot = ET.fromstring(z.read(tgt))
            except KeyError:
                continue
            for cm in croot.iter(NS + "comment"):
                out.append((cm.get("ref"), _text(cm).strip()))
    return out


def col_letter(col):
    s = ""
    while col:
        col, rem = divmod(col - 1, 26)
        s = chr(65 + rem) + s
    return s


def grid(cells):
    """Dense rectangular grid (list of rows of strings)."""
    if not cells:
        return []
    maxc = max(c for c, _ in cells)
    maxr = max(r for _, r in cells)
    return [[cells.get((c, r), "") for c in range(1, maxc + 1)] for r in range(1, maxr + 1)]


def select(sheets, key):
    if key is None:
        return sheets
    if key.isdigit():
        return [sheets[int(key)]]
    for name, p in sheets:
        if name == key:
            return [(name, p)]
    sys.exit(f"sheet not found: {key!r}. Available: {[n for n, _ in sheets]}")


def main():
    ap = argparse.ArgumentParser(description="Extract an .xlsx with the stdlib only.")
    ap.add_argument("file")
    ap.add_argument("--format", choices=["text", "csv", "tsv", "json"], default="text")
    ap.add_argument("--sheet", help="sheet name or 0-based index (default: all)")
    ap.add_argument("--comments", action="store_true", help="also dump cell comments")
    ap.add_argument("--list", action="store_true", help="list sheet names and exit")
    a = ap.parse_args()

    sheets, shared, z = load_workbook(a.file)
    if a.list:
        for i, (n, _) in enumerate(sheets):
            print(f"{i}\t{n}")
        return

    chosen = select(sheets, a.sheet)
    result = {}
    for name, xml_path in chosen:
        result[name] = read_sheet(z, xml_path, shared)

    if a.format == "json":
        print(json.dumps({n: grid(c) for n, c in result.items()}, indent=2, ensure_ascii=False))
    elif a.format in ("csv", "tsv"):
        import csv
        sep = "," if a.format == "csv" else "\t"
        w = csv.writer(sys.stdout, delimiter=sep)
        for name, cells in result.items():
            print(f"# === {name} ===")
            for r in grid(cells):
                w.writerow(r)
            print()
    else:  # text
        for name, cells in result.items():
            print(f"=== SHEET: {name} ===")
            rows = {}
            for (c, r), v in cells.items():
                rows.setdefault(r, []).append((c, v))
            for r in sorted(rows):
                line = " | ".join(f"{col_letter(c)}{r}={v}" for c, v in sorted(rows[r]))
                print(line)
            print()

    if a.comments:
        for name, xml_path in chosen:
            cmts = read_comments(z, xml_path)
            if cmts:
                print(f"=== COMMENTS: {name} ===")
                for ref, txt in cmts:
                    print(f"{ref} :: {txt}")
                print()


if __name__ == "__main__":
    main()
