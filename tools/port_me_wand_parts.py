# -*- coding: utf-8 -*-
"""
把 MiddleEarth-Thaumaturgy 的 24 杖柄 / 24 杖芯 / 10 杖端接入前置的法杖部件系统：
1. 追加 wand_rods.json（容量 100/250，工艺 12/15，materialId = me_<派系>[_staff]）。
2. 追加 wand_caps.json（秘银 ×0.65/6、加尔沃恩/魔古尔 ×0.8/5、黑乌鲁克/精灵钢 ×0.9/4、
   其余 ×0.95/4）。
3. 追加 #thaumcraft:wand_rods / staff_cores / wand_caps 三个标签。
4. 校验模型贴图尺寸（杖杆 32×32、杖端方形）并报告。

运行：python tools/port_me_wand_parts.py
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "thaumcraft")
NS = "thaumicdyesexplor"

FACTIONS = ["gondor", "hobbit", "ranger_north", "blue_mountains", "high_elf", "gundabad",
            "angmar", "wood_elf", "dol_guldur", "dale", "dwarf", "galadhrim",
            "dunland", "uruk_hai", "fangorn", "rohan", "mordor", "dorwinion",
            "rhun", "near_harad", "moredain", "tauredain", "half_troll", "bree"]
# WandCap(mithril 0.65/6, galvorn 0.8/5, morgulsteel 0.8/5, blackuruk 0.9/4,
#         elvensteel 0.9/4, 其余 0.95/4) —— 源 MiddleEarthThaumaturgyItems
CAPS = {"mithril": (0.65, 6), "orcsteel": (0.95, 4), "dwarvensteel": (0.95, 4),
        "galvorn": (0.80, 5), "uruksteel": (0.95, 4), "morgulsteel": (0.80, 5),
        "bluedwarvensteel": (0.95, 4), "blackuruksteel": (0.90, 4),
        "elvensteel": (0.90, 4), "gildediron": (0.95, 4)}

# ---- wand_rods.json ----
rods_path = os.path.join(DATA, "data_maps", "item", "wand_rods.json")
with open(rods_path, encoding="utf-8") as f:
    rods = json.load(f)
existing = set(rods["values"])
order = 200
for i, faction in enumerate(FACTIONS):
    rod_id = NS + ":me_wand_core_" + faction
    staff_id = NS + ":me_staff_core_" + faction
    tex = NS + ":textures/models/me_wand_rod_" + faction + ".png"
    if rod_id not in existing:
        rods["values"][rod_id] = {"material": NS + ":me_" + faction, "capacity": 100,
                                  "craft_cost": 12, "texture": tex, "order": order + i * 2}
    if staff_id not in existing:
        rods["values"][staff_id] = {"material": NS + ":me_" + faction + "_staff", "capacity": 250,
                                    "craft_cost": 15, "texture": tex, "order": order + i * 2 + 1}
with open(rods_path, "w", encoding="utf-8", newline="\n") as f:
    json.dump(rods, f, ensure_ascii=False, indent=2)
    f.write("\n")

# ---- wand_caps.json ----
caps_path = os.path.join(DATA, "data_maps", "item", "wand_caps.json")
with open(caps_path, encoding="utf-8") as f:
    caps = json.load(f)
for i, (cap, (modifier, craft)) in enumerate(CAPS.items()):
    cap_id = NS + ":me_wand_cap_" + cap
    if cap_id not in caps["values"]:
        caps["values"][cap_id] = {
            "material": NS + ":me_" + cap,
            "base_cost_modifier": modifier,
            "special_cost_modifier_aspects": [],
            "special_cost_modifier": modifier,
            "texture": NS + ":textures/models/me_wand_cap_" + cap + ".png",
            "craft_cost": craft,
            "order": 110 + i}
with open(caps_path, "w", encoding="utf-8", newline="\n") as f:
    json.dump(caps, f, ensure_ascii=False, indent=2)
    f.write("\n")

# ---- 标签 ----
def append_tag(name, ids):
    path = os.path.join(DATA, "tags", "item", name + ".json")
    with open(path, encoding="utf-8") as f:
        tag = json.load(f)
    changed = False
    for item in ids:
        if item not in tag["values"]:
            tag["values"].append(item)
            changed = True
    if changed:
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(tag, f, ensure_ascii=False, indent=2)
            f.write("\n")

append_tag("wand_rods", [NS + ":me_wand_core_" + f for f in FACTIONS])
append_tag("staff_cores", [NS + ":me_staff_core_" + f for f in FACTIONS])
append_tag("wand_caps", [NS + ":me_wand_cap_" + c for c in CAPS])

# ---- 模型贴图检查 ----
models = os.path.join(ROOT, "src", "main", "resources", "assets", NS, "textures", "models")
problems = []
for faction in FACTIONS:
    p = os.path.join(models, "me_wand_rod_%s.png" % faction)
    if not os.path.isfile(p):
        problems.append("missing " + p)
        continue
    size = Image.open(p).size
    if size != (32, 32):
        problems.append("me_wand_rod_%s.png is %s (expect 32x32)" % (faction, size))
for cap in CAPS:
    p = os.path.join(models, "me_wand_cap_%s.png" % cap)
    if not os.path.isfile(p):
        problems.append("missing " + p)
        continue
    w, h = Image.open(p).size
    if h % w != 0:
        problems.append("me_wand_cap_%s.png height %d not multiple of width %d" % (cap, h, w))
    mcmeta = p + ".mcmeta"
    if os.path.isfile(mcmeta):
        problems.append("me_wand_cap_%s.png.mcmeta present but renderer binds texture directly (must be static)" % cap)

print("杖杆/杖芯 datamap 条目：%d，杖端：%d" % (len(rods["values"]), len(caps["values"])))
if problems:
    print("贴图问题：")
    for p in problems:
        print("  -", p)
else:
    print("模型贴图检查通过")
