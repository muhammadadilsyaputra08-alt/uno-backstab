#!/usr/bin/env python3
"""Harness peta app BACKSTAB!: memverifikasi peta layar, aset, deck, dan PRD terhadap kode.

Pemakaian:
  python3 harness/scripts/appmap_check.py            # verifikasi (exit 1 jika gagal)
  python3 harness/scripts/appmap_check.py --write    # regenerasi docs/APP_MAP.md
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "app/src/main/java/com/backstab/game"
ASSETS = ROOT / "app/src/main/assets"
H = ROOT / "harness"
errors, notes = [], []


def err(msg):
    errors.append(msg)


def read(p):
    return Path(p).read_text(encoding="utf-8")


amap = json.loads(read(H / "app_map.json"))
manifest = json.loads(read(H / "card_manifest.json"))
screens = {s["id"]: s for s in amap["screens"]}

# 1. Enum Screen di MainActivity == peta
main = read(SRC / "MainActivity.kt")
m = re.search(r"enum class Screen \{([^}]*)\}", main)
enum_ids = {x.strip() for x in m.group(1).split(",") if x.strip()} if m else set()
if enum_ids != set(screens):
    err(f"Enum Screen {sorted(enum_ids)} != peta {sorted(screens)}")

# 2. Composable & referensi ada
for s in amap["screens"]:
    f = SRC / s["file"]
    if not f.exists():
        err(f"{s['id']}: file {s['file']} tidak ada")
        continue
    if not re.search(rf"fun {s['composable']}\(", read(f)):
        err(f"{s['id']}: composable {s['composable']} tidak ditemukan di {s['file']}")
    if not (H / "reference_screens" / s["reference"]).exists():
        err(f"{s['id']}: referensi {s['reference']} tidak ada")
    for o in s.get("overlays", []):
        if not re.search(rf"fun {o['composable']}\(", read(SRC / s["file"])):
            err(f"overlay {o['id']}: {o['composable']} tidak ditemukan")
        if o["reference"] and not (H / "reference_screens" / o["reference"]).exists():
            err(f"overlay {o['id']}: referensi {o['reference']} tidak ada")

# 3. Edge valid + semua layar terjangkau dari entry
for s in amap["screens"]:
    for e in s["edges"]:
        if e["to"] not in screens:
            err(f"{s['id']} -> {e['to']}: tujuan tidak ada")
seen, stack = set(), [amap["entry"]]
while stack:
    x = stack.pop()
    if x in seen:
        continue
    seen.add(x)
    stack += [e["to"] for e in screens[x]["edges"]]
if seen != set(screens):
    err(f"Layar tidak terjangkau: {sorted(set(screens) - seen)}")
# Setiap Screen.X dipakai di navigasi MainActivity
for sid in screens:
    if main.count(f"Screen.{sid}") < 2:
        err(f"Screen.{sid} tidak dipakai di navigasi MainActivity")

# 4. Deck: manifest vs PRD vs Kotlin CardType
inv = amap["invariants"]
cards = manifest["cards"]
if len(cards) != inv["deck_total"]:
    err(f"Manifest {len(cards)} kartu != {inv['deck_total']}")
nums = [c for c in cards if c["card_type"] == "number"]
acts = [c for c in cards if c["card_type"] == "action"]
if len(nums) != inv["number_cards"] or len(acts) != inv["action_cards"]:
    err(f"Komposisi manifest {len(nums)}/{len(acts)} != {inv['number_cards']}/{inv['action_cards']}")
models = read(SRC / "engine/Models.kt")
kt_qty = {n: int(q) for n, _p, q in re.findall(r"\b([A-Z_]+)\((\d), (\d+)\)", models)}
by_type = {}
for c in acts:
    by_type[c["name"]] = by_type.get(c["name"], 0) + 1
for name, q in by_type.items():
    if kt_qty.get(name) != q:
        err(f"CardType.{name} qty {kt_qty.get(name)} != manifest {q}")
if kt_qty.get("NUMBER") != inv["number_cards"]:
    err("CardType.NUMBER qty != 80")
if sum(q for n, q in kt_qty.items() if n != "NUMBER") != inv["action_cards"]:
    err("Total kartu aksi di Kotlin != 40")
for c in cards:
    if not (H / "reference_screens").exists():
        break
    if not (ASSETS / c["asset"]).exists():
        err(f"Aset kartu hilang: {c['asset']}")
if not (ASSETS / "cards/png/back.png").exists():
    err("Aset punggung kartu hilang")

# 5. Ikon yang dipakai kode ada
used_icons = set(re.findall(r'"(ic_[a-z_]+)"', "".join(read(p) for p in SRC.rglob("*.kt"))))
for ic in sorted(used_icons):
    if not (ASSETS / "icons" / f"{ic}.png").exists():
        err(f"Ikon dipakai tapi tidak ada: {ic}")

# 6. Resource wajib
for rel in ["font/poppins_bold.ttf", "font/inter_regular.otf", "font/inter_semibold.otf", "font/inter_bold.otf",
            "mipmap-anydpi-v26/ic_launcher.xml", "mipmap-xxxhdpi/ic_launcher.png", "values/colors.xml",
            "values/themes.xml", "values/strings.xml"]:
    if not (ROOT / "app/src/main/res" / rel).exists():
        err(f"Resource hilang: {rel}")
manifest_xml = read(ROOT / "app/src/main/AndroidManifest.xml")
if "sensorLandscape" not in manifest_xml:
    err("Orientasi landscape tidak diset di manifest")

# 7. Konstanta PRD di engine
eng = read(SRC / "engine/GameEngine.kt")
if "repeat(7)" not in eng:
    err("Pembagian 7 kartu awal tidak ditemukan")
if "threshold: Int = 15" not in eng:
    err("Ambang eliminasi default 15 tidak ditemukan")
if "check(totalCards() == Deck.SIZE)" not in eng:
    err("Invariant conservation tidak ada")

# 8. Dokumen peta
def render():
    out = ["# Peta Aplikasi BACKSTAB!", "", "_Dihasilkan oleh `harness/scripts/appmap_check.py --write` — jangan edit manual._", "",
           "## Navigasi layar", "", "```mermaid", "flowchart LR"]
    for s in amap["screens"]:
        for e in s["edges"]:
            out.append(f'  {s["id"]} -->|"{e["on"]}"| {e["to"]}')
    out += ["```", "", "## Layar", "", "| ID | Composable | Berkas | Referensi | Fungsi |", "|---|---|---|---|---|"]
    for s in amap["screens"]:
        out.append(f'| {s["id"]} | `{s["composable"]}` | `{s["file"]}` | `{s["reference"]}` | {s["purpose"]} |')
    out += ["", "## Overlay di layar GAME", "", "| ID | Composable | Referensi |", "|---|---|---|"]
    for o in screens["GAME"]["overlays"]:
        out.append(f'| {o["id"]} | `{o["composable"]}` | `{o["reference"] or "-"}` |')
    sm = amap["engine_state_machine"]
    out += ["", "## State machine engine", "", f'Sumber: {sm["source"]}', "", "| Phase | Mencakup (PRD §14.1) |", "|---|---|"]
    for k, v in sm["phases"].items():
        out.append(f"| `{k}` | {v} |")
    out += ["", "Urutan resolusi aksi (PRD §15): " + " → ".join(sm["pipeline_prd_15"]) + ".", "", "## Invarian", ""]
    for k, v in inv.items():
        out.append(f"- **{k}**: {v}")
    out.append("")
    return "\n".join(out)


doc = ROOT / "docs/APP_MAP.md"
if "--write" in sys.argv:
    doc.write_text(render(), encoding="utf-8")
    notes.append(f"Menulis {doc.relative_to(ROOT)}")
elif not doc.exists() or read(doc) != render():
    err("docs/APP_MAP.md usang — jalankan: python3 harness/scripts/appmap_check.py --write")

for n in notes:
    print("•", n)
if errors:
    print(f"\nHARNESS GAGAL ({len(errors)} masalah):")
    for e in errors:
        print(" ✗", e)
    sys.exit(1)
print(f"HARNESS OK — {len(screens)} layar, {len(cards)} kartu, {len(used_icons)} ikon terverifikasi")
