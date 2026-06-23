#!/usr/bin/env python3
"""
parse_rating_workbook.py - read a filled Socotra Rating workbook, parse every formula,
validate references, and emit the auditable "rating contract" (self-contained HTML + JSON).

This is the intermediate artifact a human/AI reviews BEFORE any Java is written, and the
input the socotra-rating-plugin skill turns into a RatePlugin. It proves the DSL parses
deterministically and that every name resolves against the workbook's own declarations.

    python3 parse_rating_workbook.py FILE.xlsx --out contract.html --json contract.json

Reuses the xlsx-extract skill as the reader (no openpyxl). Pure stdlib otherwise.
"""
import argparse
import html
import importlib.util
import json
import os
import re
import sys

XLSX = os.path.expanduser("~/.claude/skills/xlsx-extract/xlsx_extract.py")


def _load_extractor():
    spec = importlib.util.spec_from_file_location("xlsx_extract", XLSX)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


# --------------------------------------------------------------------------- #
# Formula DSL: tokenizer + recursive-descent parser -> AST (plain dicts).
# --------------------------------------------------------------------------- #

TOKEN_RE = re.compile(r"""
    \s*(?:
      (?P<num>\d+\.\d+|\d+)
    | (?P<str>"[^"]*")
    | (?P<op><=|>=|==|!=|<|>|\+|\-|\*|/)
    | (?P<punc>[\(\)\[\],\.])
    | (?P<name>[A-Za-z_][A-Za-z0-9_]*)
    )
""", re.VERBOSE)

KEYWORDS = {"and", "or", "not", "if"}
FUNCS = {"min", "max", "round", "abs", "band"}


def tokenize(s):
    toks, i = [], 0
    while i < len(s):
        if s[i].isspace():
            i += 1
            continue
        m = TOKEN_RE.match(s, i)
        if not m or m.end() == i:
            raise SyntaxError(f"unexpected character {s[i]!r} at offset {i}")
        i = m.end()
        kind = m.lastgroup
        val = m.group(kind)
        toks.append((kind, val))
    toks.append(("end", ""))
    return toks


class Parser:
    def __init__(self, toks):
        self.toks = toks
        self.pos = 0
        self.vars = set()
        self.tables = []  # (name, key_exprs, value_col)

    def peek(self):
        return self.toks[self.pos]

    def next(self):
        t = self.toks[self.pos]
        self.pos += 1
        return t

    def expect(self, val):
        k, v = self.next()
        if v != val:
            raise SyntaxError(f"expected {val!r}, got {v!r}")

    def parse(self):
        node = self.expr()
        if self.peek()[0] != "end":
            raise SyntaxError(f"trailing tokens at {self.peek()[1]!r}")
        return node

    def expr(self):
        return self.or_expr()

    def or_expr(self):
        n = self.and_expr()
        while self.peek() == ("name", "or"):
            self.next()
            n = {"node": "or", "l": n, "r": self.and_expr()}
        return n

    def and_expr(self):
        n = self.not_expr()
        while self.peek() == ("name", "and"):
            self.next()
            n = {"node": "and", "l": n, "r": self.not_expr()}
        return n

    def not_expr(self):
        if self.peek() == ("name", "not"):
            self.next()
            return {"node": "not", "x": self.not_expr()}
        return self.comparison()

    def comparison(self):
        n = self.add_expr()
        if self.peek()[0] == "op" and self.peek()[1] in ("==", "!=", "<", "<=", ">", ">="):
            op = self.next()[1]
            n = {"node": "cmp", "op": op, "l": n, "r": self.add_expr()}
        return n

    def add_expr(self):
        n = self.mul_expr()
        while self.peek()[0] == "op" and self.peek()[1] in ("+", "-"):
            op = self.next()[1]
            n = {"node": "bin", "op": op, "l": n, "r": self.mul_expr()}
        return n

    def mul_expr(self):
        n = self.unary()
        while self.peek()[0] == "op" and self.peek()[1] in ("*", "/"):
            op = self.next()[1]
            n = {"node": "bin", "op": op, "l": n, "r": self.unary()}
        return n

    def unary(self):
        if self.peek() == ("op", "-"):
            self.next()
            return {"node": "neg", "x": self.unary()}
        return self.primary()

    def arglist(self, close=")"):
        args = []
        if self.peek()[1] != close:
            args.append(self.expr())
            while self.peek()[1] == ",":
                self.next()
                args.append(self.expr())
        self.expect(close)
        return args

    def primary(self):
        k, v = self.peek()
        if k == "num":
            self.next()
            return {"node": "num", "value": v}
        if k == "str":
            self.next()
            return {"node": "str", "value": v[1:-1]}
        if v == "(":
            self.next()
            n = self.expr()
            self.expect(")")
            return n
        if v == "if":
            self.next()
            self.expect("(")
            cond = self.expr(); self.expect(",")
            a = self.expr(); self.expect(",")
            b = self.expr(); self.expect(")")
            return {"node": "if", "cond": cond, "then": a, "else": b}
        if k == "name" and v in FUNCS:
            self.next()
            self.expect("(")
            if v == "band":
                return self.band()
            args = self.arglist()
            return {"node": "call", "func": v, "args": args}
        if k == "name":
            self.next()
            # table lookup?  Name[...]
            if self.peek()[1] == "[":
                self.next()
                keys = self.arglist("]")
                col = None
                if self.peek()[1] == ".":
                    self.next()
                    col = self.next()[1]
                self.tables.append((v, len(keys), col))
                return {"node": "lookup", "table": v, "keys": keys, "col": col}
            self.vars.add(v)
            return {"node": "var", "name": v}
        raise SyntaxError(f"unexpected token {v!r}")

    def band(self):
        # band(Table, bound)  | band(Table, [k1,k2], bound)  | (+ optional , mode)
        table = self.next()[1]
        self.expect(",")
        keys = []
        if self.peek()[1] == "[":
            self.next()
            keys = self.arglist("]")
            self.expect(",")
        bound = self.expr()
        mode = None
        if self.peek()[1] == ",":
            self.next()
            mode = self.next()[1]
        self.expect(")")
        self.tables.append((table, len(keys), None))
        return {"node": "band", "table": table, "keys": keys, "bound": bound, "mode": mode}


def parse_formula(text):
    p = Parser(tokenize(text))
    ast = p.parse()
    return ast, sorted(p.vars), p.tables


# --------------------------------------------------------------------------- #
# Workbook reading (tolerant header lookup) + contract assembly.
# --------------------------------------------------------------------------- #

def find_sheet(sheets, prefix):
    for name, p in sheets:
        if name.lower().startswith(prefix.lower()):
            return (name, p)
    return None


def rows_of(ext, z, shared, sheet):
    return ext.grid(ext.read_sheet(z, sheet[1], shared))


def header_map(header):
    return {h.strip().lower(): i for i, h in enumerate(header) if h.strip()}


def col(row, hmap, *aliases, default=""):
    for a in aliases:
        if a.lower() in hmap and hmap[a.lower()] < len(row):
            return row[hmap[a.lower()]].strip()
    return default


# --------------------------------------------------------------------------- #
# Rendering: a self-contained, styled HTML contract (no external assets).
# --------------------------------------------------------------------------- #

HTML_CSS = """
:root {
  --bg: #fbfbfa; --card: #ffffff; --ink: #1f2328; --muted: #656d76;
  --line: #d8dee4; --accent: #0969da; --code-bg: #f3f4f6;
  --err-bg: #fff1f0; --err-ink: #b42318; --err-line: #f5c2bd;
  --warn-bg: #fff8e6; --warn-ink: #8a6100; --warn-line: #f2dca0;
  --ok: #1a7f37;
}
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--ink);
  font: 15px/1.55 -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif; }
.wrap { max-width: 860px; margin: 0 auto; padding: 40px 24px 80px; }
h1 { font-size: 26px; margin: 0 0 4px; letter-spacing: -0.01em; }
.summary { color: var(--muted); margin: 0 0 28px; font-size: 14px; }
.summary .pill { display: inline-block; padding: 2px 10px; border-radius: 999px;
  font-weight: 600; font-size: 12.5px; margin-right: 8px; border: 1px solid var(--line); }
.pill.rules { background: #eef4ff; color: var(--accent); border-color: #cfe0ff; }
.pill.err { background: var(--err-bg); color: var(--err-ink); border-color: var(--err-line); }
.pill.warn { background: var(--warn-bg); color: var(--warn-ink); border-color: var(--warn-line); }
.pill.clean { background: #e9f6ec; color: var(--ok); border-color: #b6e0c2; }
.rule { background: var(--card); border: 1px solid var(--line); border-radius: 10px;
  padding: 18px 20px; margin: 0 0 16px; box-shadow: 0 1px 2px rgba(27,31,36,0.04); }
.rule h2 { font-size: 17px; margin: 0 0 12px; display: flex; align-items: baseline; gap: 10px; flex-wrap: wrap; }
.handling { font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em;
  padding: 2px 8px; border-radius: 6px; background: var(--code-bg); color: var(--muted); }
.handling.rate { background: #eef4ff; color: var(--accent); }
.handling.amount { background: #e9f6ec; color: var(--ok); }
dl { display: grid; grid-template-columns: max-content 1fr; gap: 6px 16px; margin: 0; }
dt { color: var(--muted); font-size: 13px; }
dd { margin: 0; }
code, .formula { font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace; font-size: 13px; }
.formula { display: inline-block; background: var(--code-bg); border: 1px solid var(--line);
  border-radius: 6px; padding: 3px 8px; word-break: break-word; }
.diag { list-style: none; padding: 0; margin: 12px 0 0; display: flex; flex-direction: column; gap: 6px; }
.diag li { padding: 7px 12px; border-radius: 7px; font-size: 13.5px; border: 1px solid transparent; }
.diag .err { background: var(--err-bg); color: var(--err-ink); border-color: var(--err-line); }
.diag .warn { background: var(--warn-bg); color: var(--warn-ink); border-color: var(--warn-line); }
.empty { color: var(--muted); font-style: italic; }
footer { margin-top: 36px; color: var(--muted); font-size: 12px; border-top: 1px solid var(--line); padding-top: 14px; }
"""


def render_html(contract, n_err, n_warn):
    esc = html.escape
    n_rules = len(contract["rules"])
    out = []
    out.append("<!doctype html>")
    out.append('<html lang="en"><head><meta charset="utf-8">')
    out.append('<meta name="viewport" content="width=device-width, initial-scale=1">')
    out.append("<title>Rating contract</title>")
    out.append(f"<style>{HTML_CSS}</style></head><body><div class=\"wrap\">")
    out.append("<h1>Rating contract</h1>")

    pills = [f'<span class="pill rules">{n_rules} rule{"s" if n_rules != 1 else ""}</span>']
    if n_err:
        pills.append(f'<span class="pill err">{n_err} error{"s" if n_err != 1 else ""}</span>')
    if n_warn:
        pills.append(f'<span class="pill warn">{n_warn} warning{"s" if n_warn != 1 else ""}</span>')
    if not n_err and not n_warn:
        pills.append('<span class="pill clean">✓ clean</span>')
    out.append(f'<p class="summary">{"".join(pills)}</p>')

    if not contract["rules"]:
        out.append('<p class="empty">No rating rules found.</p>')

    for r in contract["rules"]:
        head = " / ".join(x for x in [r["product"], r["element"], r["charge"]] if x)
        out.append('<section class="rule">')
        h2 = f"<h2>{esc(head)}"
        if r["handling"]:
            h2 += f' <span class="handling {esc(r["handling"])}">{esc(r["handling"])}</span>'
        h2 += "</h2>"
        out.append(h2)

        out.append("<dl>")
        out.append(f'<dt>formula</dt><dd><span class="formula">{esc(r["formula"])}</span></dd>')
        if r.get("vars_used"):
            out.append(f'<dt>inputs</dt><dd><code>{esc(", ".join(r["vars_used"]))}</code></dd>')
        if r.get("tables_used"):
            out.append(f'<dt>tables</dt><dd><code>{esc(", ".join(r["tables_used"]))}</code></dd>')
        if r.get("notes"):
            out.append(f'<dt>notes</dt><dd>{esc(r["notes"])}</dd>')
        out.append("</dl>")

        if r["errors"] or r["warnings"]:
            out.append('<ul class="diag">')
            for e in r["errors"]:
                out.append(f'<li class="err">❌ {esc(e)}</li>')
            for w in r["warnings"]:
                out.append(f'<li class="warn">⚠️ {esc(w)}</li>')
            out.append("</ul>")
        out.append("</section>")

    out.append('<footer>Generated by socotra-rating-from-xlsx · parse_rating_workbook.py</footer>')
    out.append("</div></body></html>")
    return "\n".join(out)


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("file")
    ap.add_argument("--out", help="rating contract HTML path")
    ap.add_argument("--json", help="rating contract JSON path")
    a = ap.parse_args()

    ext = _load_extractor()
    sheets, shared, z = ext.load_workbook(a.file)

    # declarations
    declared_vars = {}
    s = find_sheet(sheets, "3 - Inputs") or find_sheet(sheets, "Inputs")
    if s:
        rows = rows_of(ext, z, shared, s)
        h = header_map(rows[0]) if rows else {}
        for r in rows[1:]:
            name = col(r, h, "Variable")
            if name:
                declared_vars[name] = col(r, h, "Source (config accessor)", "Source")

    declared_tables = {}
    s = find_sheet(sheets, "4 - Rate Tables") or find_sheet(sheets, "Rate Tables")
    if s:
        rows = rows_of(ext, z, shared, s)
        h = header_map(rows[0]) if rows else {}
        for r in rows[1:]:
            name = col(r, h, "Table Name")
            if name:
                declared_tables[name] = {
                    "type": col(r, h, "Type", default="exact"),
                    "keys": [k.strip() for k in col(r, h, "Key Columns").split(",") if k.strip()],
                    "values": [v.strip() for v in col(r, h, "Value Columns").split(",") if v.strip()],
                    "on_miss": col(r, h, "On Miss", default="error"),
                }

    declared_charges = {}
    s = find_sheet(sheets, "2 - Charges") or find_sheet(sheets, "Charges")
    if s:
        rows = rows_of(ext, z, shared, s)
        h = header_map(rows[0]) if rows else {}
        for r in rows[1:]:
            name = col(r, h, "Charge Name", "Charge")
            if name:
                declared_charges.setdefault(name, []).append({
                    "element": col(r, h, "Lands On (Element)", "Lands On", "Element"),
                    "handling": col(r, h, "Handling").lower(),
                })

    # rating rows
    contract = {"declared": {"inputs": declared_vars, "tables": declared_tables,
                             "charges": declared_charges}, "rules": []}
    s = find_sheet(sheets, "N - Rating") or find_sheet(sheets, "Rating")
    if not s:
        sys.exit("no Rating tab found")
    rows = rows_of(ext, z, shared, s)
    h = header_map(rows[0]) if rows else {}
    for r in rows[1:]:
        formula = col(r, h, "Formula")
        element = col(r, h, "Element")
        charge = col(r, h, "Charge")
        if not (formula and element and charge):
            continue
        # rate-vs-amount lives on 2 - Charges (one source of truth); the Rating tab no longer carries it.
        handling = ""
        for decl in declared_charges.get(charge, []):
            if decl["element"] == element and decl["handling"]:
                handling = decl["handling"]
                break
        rule = {
            "product": col(r, h, "Product"),   # optional; blank for single-product books
            "element": element,
            "charge": charge,
            "handling": handling,
            "formula": formula,
            "notes": col(r, h, "Notes"),
            "errors": [],
            "warnings": [],
        }
        try:
            ast, vars_used, tables_used = parse_formula(formula)
            rule["ast"] = ast
            rule["vars_used"] = vars_used
            rule["tables_used"] = sorted({t for t, _, _ in tables_used})
            for v in vars_used:
                if v not in declared_vars:
                    rule["errors"].append(f"undeclared input variable '{v}' (add it to 3 - Inputs)")
            for tname, nkeys, vcol in tables_used:
                td = declared_tables.get(tname)
                if not td:
                    rule["errors"].append(f"undeclared table '{tname}' (add it to 4 - Rate Tables)")
                    continue
                if td["keys"] and nkeys != len(td["keys"]):
                    rule["errors"].append(
                        f"table '{tname}' declares {len(td['keys'])} key column(s) but formula passes {nkeys}")
                if vcol and td["values"] and vcol not in td["values"]:
                    rule["errors"].append(f"table '{tname}' has no value column '{vcol}'")
        except SyntaxError as e:
            rule["errors"].append(f"parse error: {e}")
        # the (element, charge) must be declared on 2 - Charges, which also supplies its handling
        if charge not in declared_charges:
            rule["warnings"].append(f"charge '{charge}' not declared on 2 - Charges")
        elif not handling:
            rule["warnings"].append(
                f"no handling (rate/amount) for charge '{charge}' on '{element}' in 2 - Charges")
        contract["rules"].append(rule)

    n_err = sum(len(r["errors"]) for r in contract["rules"])
    n_warn = sum(len(r["warnings"]) for r in contract["rules"])

    if a.json:
        with open(a.json, "w") as f:
            json.dump(contract, f, indent=2)
        print(f"wrote {a.json}")

    text = render_html(contract, n_err, n_warn)
    if a.out:
        with open(a.out, "w") as f:
            f.write(text)
        print(f"wrote {a.out}")
    else:
        print(text)

    sys.exit(1 if n_err else 0)


if __name__ == "__main__":
    main()
