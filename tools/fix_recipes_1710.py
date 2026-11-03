# -*- coding: utf-8 -*-
"""
根据 1.7.10 源码修补 1.21.1 配方 JSON（第 27 轮）。
基准：
- Thaumic-Dyes: ConfigRecipes.java / ConfigResearch.java / DyesRecipes.java
- Thaumic Exploration: ModRecipes.java
- MiddleEarth-Thaumaturgy: Recipes.java / GeneralRecipes.java
"""
import json, os, re

ROOT = r"D:/.codepy/trea/slash/SkydeityThaumicDyesExplo_1.21/src/main/resources/data/thaumicdyesexplor/recipe"
C = os.path.join(ROOT, "crafting")
I = os.path.join(ROOT, "infusion")
A = os.path.join(ROOT, "arcane")

def item(i, n=1):
    d = {"item": i}
    if n != 1:
        d["count"] = n
    return d

def tag(t):
    return {"tag": t}

def load(sub, name):
    with open(os.path.join(sub, name), encoding="utf8") as f:
        return json.load(f)

def save(sub, name, d):
    with open(os.path.join(sub, name), "w", encoding="utf8") as f:
        json.dump(d, f, ensure_ascii=False, indent=2)
        f.write("\n")

def arcane(res, vis, pattern, key, research):
    return {"type": "thaumcraft:arcane_shaped", "research": research, "vis": vis,
            "pattern": pattern, "key": key, "result": res}

def infusion(res, vis_aspects, central, comps, inst, research):
    d = {"type": "thaumcraft:infusion", "research": research, "central": central,
         "components": comps, "result": res, "instability": inst, "aspects": vis_aspects}
    return d

changed = []
def mark(p):
    changed.append(os.path.relpath(p, ROOT))

# ---------------------------------------------------------------- crafting --
R_ARMORSETS = "thaumicdyesexplor:ARMORSETS"
R_ARMORMADE = "thaumicdyesexplor:ARMORMADE"
R_ROOT = "thaumicdyesexplor:ROOT"
FAB = "thaumicdyesexplor:fabric_resource"

# fabric: 1.7.10 竖排 红染料/附魔布料/虚空种子 (TD.CRIMSON)
save(C, "fabric_resource.json", arcane(
    item("thaumicdyesexplor:fabric_resource"),
    {"perditio": 2, "ordo": 5},
    ["R", "F", "V"],
    {"R": item("minecraft:red_dye"), "F": item("thaumcraft:enchanted_fabric"),
     "V": item("thaumcraft:void_seed")},
    R_ROOT)); mark(C + "/fabric_resource.json")

# blank seal: 魔力油脂 + 染料 (TE BLANKSEAL)
save(C, "blank_seal.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": [" X ", "XZX", " X "],
    "key": {"X": item("thaumcraft:magic_tallow"), "Z": tag("c:dyes")},
    "result": item("thaumicdyesexplor:blank_seal")}); mark(C + "/blank_seal.json")

# ancient mask = TD 残酷面具 (TD.MASKEVIL): LVL/L L/EVE
save(C, "ancient_mask.json", arcane(
    item("thaumicdyesexplor:ancient_mask"),
    {"perditio": 60, "ignis": 40, "aer": 40},
    ["LVL", "L L", "EVE"],
    {"L": item("minecraft:leather"), "V": item("thaumcraft:void_ingot"),
     "E": item("thaumcraft:eldritch_object")},
    R_ARMORSETS)); mark(C + "/ancient_mask.json")

# --- 血腥神官长袍 (TD.CRIMSONCLERIC) ---
vis_cleric = {"aqua": 10, "perditio": 10, "ordo": 20}
save(C, "cultist_robe_helmet.json", arcane(
    item("thaumicdyesexplor:cultist_robe_helmet"), vis_cleric,
    ["RRR", "R R"], {"R": item(FAB)}, R_ARMORSETS)); mark(C + "/cultist_robe_helmet.json")
save(C, "cultist_robe_chestplate.json", arcane(
    item("thaumicdyesexplor:cultist_robe_chestplate"), vis_cleric,
    ["R R", "RRR", "RGR"],
    {"R": item(FAB), "G": item("minecraft:gold_ingot")}, R_ARMORSETS)); mark(C + "/cultist_robe_chestplate.json")
save(C, "cultist_robe_leggings.json", arcane(
    item("thaumicdyesexplor:cultist_robe_leggings"), vis_cleric,
    ["RGR", "RIR", "R R"],
    {"R": item(FAB), "G": item("minecraft:gold_ingot"), "I": item("minecraft:iron_ingot")},
    R_ARMORSETS)); mark(C + "/cultist_robe_leggings.json")
# 血腥教徒靴（1.7.10 TD.CRIMSON 的 itemBootsCultist 配方，之前缺失）
save(C, "cultist_boots.json", arcane(
    item("thaumcraft:cultist_boots"),
    {"terra": 10, "perditio": 10, "ordo": 20},
    ["R R", "R R"], {"R": item(FAB)}, R_ARMORSETS)); mark(C + "/cultist_boots.json")

# --- 血腥骑士板甲 (TD.CRIMSONKNIGHT) ---
vis_knight = {"ignis": 10, "perditio": 10, "ordo": 20}
save(C, "cultist_knight_helmet.json", arcane(
    item("thaumicdyesexplor:cultist_knight_helmet"), vis_knight,
    ["III", "IGI", " R "],
    {"I": item("minecraft:iron_ingot"), "G": item("minecraft:gold_ingot"), "R": item(FAB)},
    R_ARMORSETS)); mark(C + "/cultist_knight_helmet.json")
save(C, "cultist_knight_chestplate.json", arcane(
    item("thaumicdyesexplor:cultist_knight_chestplate"), vis_knight,
    ["I I", "IRI", "IRI"],
    {"I": item("minecraft:iron_ingot"), "R": item(FAB)}, R_ARMORSETS)); mark(C + "/cultist_knight_chestplate.json")
save(C, "cultist_knight_leggings.json", arcane(
    item("thaumicdyesexplor:cultist_knight_leggings"), vis_knight,
    ["IGI", "IRI", "I I"],
    {"I": item("minecraft:iron_ingot"), "G": item("minecraft:gold_ingot"), "R": item(FAB)},
    R_ARMORSETS)); mark(C + "/cultist_knight_leggings.json")

# --- 血腥游侠 (TD.CRIMSONRANGER) 胸甲用神官长袍、护胫是长袍腿 ---
vis_ranger = {"terra": 10, "perditio": 10, "ordo": 20}
save(C, "cultist_ranger_helmet.json", arcane(
    item("thaumicdyesexplor:cultist_ranger_helmet"), vis_ranger,
    [" I ", "IHI", " R "],
    {"I": item("minecraft:iron_ingot"), "H": item("minecraft:iron_helmet"), "R": item(FAB)},
    R_ARMORSETS)); mark(C + "/cultist_ranger_helmet.json")
save(C, "cultist_ranger_chestplate.json", arcane(
    item("thaumicdyesexplor:cultist_ranger_chestplate"), vis_ranger,
    ["c", "C", "R"],
    {"c": item("minecraft:chest"), "C": item("thaumcraft:cultist_robe_chestplate"), "R": item(FAB)},
    R_ARMORSETS)); mark(C + "/cultist_ranger_chestplate.json")
save(C, "cultist_ranger_leggings.json", arcane(
    item("thaumicdyesexplor:cultist_ranger_leggings"), vis_ranger,
    ["sLs", "bBb"],
    {"s": item("minecraft:string"), "L": item("thaumcraft:cultist_robe_leggings"),
     "b": item("minecraft:glass_bottle"), "B": item("minecraft:blaze_powder")},
    R_ARMORSETS)); mark(C + "/cultist_ranger_leggings.json")

# --- 血腥射手 (TD.CRIMSONARCHER) 护胫用骑士板甲腿 ---
vis_archer = {"aer": 10, "perditio": 10, "ordo": 20}
save(C, "cultist_archer_helmet.json", arcane(
    item("thaumicdyesexplor:cultist_archer_helmet"), vis_archer,
    ["IHI", " g "],
    {"I": item("minecraft:iron_ingot"), "H": item("minecraft:iron_helmet"),
     "g": item("minecraft:gold_ingot")}, R_ARMORSETS)); mark(C + "/cultist_archer_helmet.json")
save(C, "cultist_archer_chestplate.json", arcane(
    item("thaumicdyesexplor:cultist_archer_chestplate"), vis_archer,
    ["I I", "RIR", "IlI"],
    {"I": item("minecraft:iron_ingot"), "R": item(FAB), "l": item("minecraft:leather")},
    R_ARMORSETS)); mark(C + "/cultist_archer_chestplate.json")
save(C, "cultist_archer_leggings.json", arcane(
    item("thaumicdyesexplor:cultist_archer_leggings"), vis_archer,
    ["ILI", " R "],
    {"I": item("minecraft:iron_ingot"), "L": item("thaumcraft:cultist_plate_leggings"),
     "R": item(FAB)}, R_ARMORSETS)); mark(C + "/cultist_archer_leggings.json")

# --- 圣殿骑士头盔 (TD.PALADIN): 执政官头盔+金 ---
save(C, "cultist_paladin_helmet.json", arcane(
    item("thaumicdyesexplor:cultist_paladin_helmet"),
    {"ignis": 20, "perditio": 10, "ordo": 25},
    [" g ", "GHG", " g "],
    {"g": item("minecraft:gold_nugget"), "G": item("minecraft:gold_ingot"),
     "H": item("thaumicdyesexplor:cultist_leader_helmet")}, R_ARMORSETS)); mark(C + "/cultist_paladin_helmet.json")

# --- 太阳骑士 (TD.SUNNYD): 染色骑士甲+染料 ---
save(C, "sun_knight_helmet.json", arcane(
    item("thaumicdyesexplor:sun_knight_helmet"),
    {"ignis": 20, "aer": 20},
    [" g ", " H ", " g "],
    {"g": item("minecraft:light_gray_dye"),
     "H": item("thaumicdyesexplor:cultist_knight_helmet")}, R_ARMORSETS)); mark(C + "/sun_knight_helmet.json")
save(C, "sun_knight_chestplate.json", arcane(
    item("thaumicdyesexplor:sun_knight_chestplate"),
    {"ignis": 20, "aer": 20},
    [" r ", "rCr", " y "],
    {"r": item("minecraft:red_dye"), "y": item("minecraft:yellow_dye"),
     "C": item("thaumicdyesexplor:cultist_knight_chestplate")}, R_ARMORSETS)); mark(C + "/sun_knight_chestplate.json")

# --- ME 瓶缚/进阶瓶缚 (GeneralRecipes.addGeneralLotrRecipes) ---
save(C, "me_jar_binder.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": [" S ", "SXS", " S "],
    "key": {"S": item("minecraft:amethyst_shard"), "X": item("minecraft:slime_ball")},
    "result": item("thaumicdyesexplor:me_jar_binder", 2)}); mark(C + "/me_jar_binder.json")
save(C, "me_jar_binder_advanced.json", {
    "type": "minecraft:crafting_shaped",
    "pattern": [" S ", "SXS", " S "],
    "key": {"S": item("minecraft:emerald"), "X": item("thaumicdyesexplor:me_jar_binder")},
    "result": item("thaumicdyesexplor:me_jar_binder_advanced")}); mark(C + "/me_jar_binder_advanced.json")

# --- 未充能杖端 (TE UNCHARGEDSOJOURNER / UNCHARGEDMECHANIST) ---
save(A, "sojourner_cap_uncharged.json", arcane(
    item("thaumicdyesexplor:sojourner_cap_uncharged"),
    {"aer": 6, "perditio": 6, "ordo": 6},
    ["ABA", "A A"],
    {"A": item("minecraft:lapis_lazuli"), "B": item("minecraft:diamond")},
    "thaumicdyesexplor:CAP_SOJOURNER")); mark(A + "/sojourner_cap_uncharged.json")
save(A, "mechanist_cap_uncharged.json", arcane(
    item("thaumicdyesexplor:mechanist_cap_uncharged"),
    {"ignis": 6, "perditio": 6, "ordo": 6},
    ["AAA", "ABA", " C "],
    {"A": item("minecraft:redstone"), "B": item("minecraft:piston"),
     "C": item("minecraft:repeater")},
    "thaumicdyesexplor:CAP_MECHANIST")); mark(A + "/mechanist_cap_uncharged.json")

# ---------------------------------------------------------------- infusion --
def set_infusion(name, sub=I, research=None, result=None, **kw):
    try:
        d = load(sub, name)
        research = d.get("research") or research
        result = d.get("result") or result
        existed = True
    except FileNotFoundError:
        existed = False
    assert research and result, name
    new = infusion(result, kw["aspects"], kw["central"], kw["components"], kw["inst"], research)
    save(sub, name, new)
    mark(sub + "/" + name)
    return existed

# --- 铁堡垒 (TD.IRONFORTRESS) 注魔 inst1（原 crafting 版删除，改为新建 infusion）---
iron_sets = {
    "helmet":    ("minecraft:iron_helmet",    ["minecraft:iron_ingot"] * 3 + ["minecraft:gold_ingot", "minecraft:diamond"]),
    "chestplate":("minecraft:iron_chestplate",["minecraft:iron_ingot"] * 4 + ["minecraft:gold_ingot", "minecraft:leather"]),
    "leggings":  ("minecraft:iron_leggings",  ["minecraft:iron_ingot"] * 3 + ["minecraft:gold_ingot", "minecraft:leather"]),
}
to_delete = []
for slot, (central, comps) in iron_sets.items():
    existed = set_infusion("iron_fortress_%s.json" % slot,
        research=R_ARMORMADE, result=item("thaumicdyesexplor:iron_fortress_%s" % slot),
        central=item(central), components=[item(x) for x in comps],
        aspects={"tutamen": 8, "metallum": 8, "fabrico": 8}, inst=1)
    if not existed:
        to_delete.append("iron_fortress_%s.json" % slot)

# --- 神秘堡垒染色 (TD.DYEDFORTRESS) inst5 ---
for slot in ("helmet", "chestplate", "leggings"):
    existed = set_infusion("fortress_%s.json" % slot,
        research=R_ARMORMADE, result=item("thaumicdyesexplor:fortress_%s" % slot),
        central=item("thaumcraft:fortress_%s" % slot),
        components=[item("thaumcraft:magic_tallow"), item("thaumcraft:magic_tallow"),
                    item("thaumcraft:enchanted_fabric"), item("thaumcraft:magic_tallow")],
        aspects={"tutamen": 16, "praecantatio": 16, "pannus": 8, "metallum": 16}, inst=5)
    if not existed:
        to_delete.append("fortress_%s.json" % slot)

# --- 亡居甲 (TD.INHABITEDPLATE) 以血腥板甲为基 inst3 ---
inhabited = {
    "helmet":    ("thaumcraft:cultist_plate_helmet",    16,
                  [("thaumcraft:entropy_shard", 1), ("minecraft:iron_ingot", 1), ("thaumcraft:void_seed", 1)]),
    "chestplate":("thaumcraft:cultist_plate_chestplate", 24,
                  [("thaumcraft:entropy_shard", 1), ("minecraft:iron_ingot", 2), ("thaumcraft:void_seed", 2)]),
    "leggings":  ("thaumcraft:cultist_plate_leggings",  20,
                  [("thaumcraft:entropy_shard", 1), ("minecraft:iron_ingot", 2), ("thaumcraft:void_seed", 1)]),
}
for slot, (central, a, comps) in inhabited.items():
    existed = set_infusion("inhabited_plate_%s.json" % slot,
        research=R_ARMORMADE, result=item("thaumicdyesexplor:inhabited_plate_%s" % slot),
        central=item(central),
        components=[item(i, n) for i, n in comps],
        aspects={"perditio": 16, "alienis": a, "metallum": a, "tutamen": a}, inst=3)
    if not existed:
        to_delete.append("inhabited_plate_%s.json" % slot)

# --- 神秘长袍染色 (TD.THAUMIUMROBE) inst4 ---
def robe_fix(name, central, comps, aspects, result):
    existed = set_infusion(name,
        research=R_ARMORMADE, result=item(result),
        central=item(central), components=comps, aspects=aspects, inst=4)
    if not existed:
        to_delete.append(name)

robe_fix("thaumium_robe_helmet.json", "thaumcraft:thaumium_helmet",
    [item("thaumcraft:goggles_of_revealing")] + [item("thaumcraft:enchanted_fabric")] * 2
    + [item("thaumcraft:salis_mundus")] + [item("thaumcraft:enchanted_fabric")] * 2,
    {"metallum": 16, "sensus": 16, "tutamen": 16, "pannus": 16, "praecantatio": 16},
    "thaumicdyesexplor:thaumium_robe_helmet")
robe_fix("thaumium_robe_chestplate.json", "thaumcraft:thaumium_chestplate",
    [item("thaumcraft:robe_chestplate"), item("thaumcraft:magic_tallow"),
     item("thaumcraft:magic_tallow"), item("thaumcraft:salis_mundus"),
     item("thaumcraft:enchanted_fabric"), item("minecraft:leather")],
    {"metallum": 24, "tutamen": 24, "pannus": 24, "praecantatio": 16},
    "thaumicdyesexplor:thaumium_robe_chestplate")
robe_fix("thaumium_robe_leggings.json", "thaumcraft:thaumium_leggings",
    [item("thaumcraft:robe_leggings"), item("thaumcraft:magic_tallow"),
     item("thaumcraft:magic_tallow"), item("thaumcraft:salis_mundus"),
     item("thaumcraft:enchanted_fabric"), item("minecraft:leather")],
    {"metallum": 20, "tutamen": 20, "pannus": 20, "praecantatio": 16},
    "thaumicdyesexplor:thaumium_robe_leggings")

# --- 虚空长袍染色 (TD.VOIDROBE, 1.7.10 坩埚 -> 注魔 inst2) ---
for slot in ("helmet", "chestplate", "leggings"):
    existed = set_infusion("void_robe_%s.json" % slot,
        research=R_ARMORMADE, result=item("thaumicdyesexplor:void_robe_%s" % slot),
        central=item("thaumcraft:void_fortress_%s" % slot),
        components=[item("thaumcraft:enchanted_fabric")],
        aspects={"pannus": 8, "vitium": 4, "permutatio": 8}, inst=2)
    if not existed:
        to_delete.append("void_robe_%s.json" % slot)

# --- 符文护甲基座 (ConfigRecipes.initRunicRecipes) ---
runic_base = {
    "helmet":    ["thaumcraft:thaumic_charm", "thaumcraft:amber", "thaumcraft:enchanted_fabric", "thaumcraft:nitor"],
    "chestplate":["thaumcraft:thaumic_charm", "thaumcraft:amber", "thaumcraft:enchanted_fabric",
                  "thaumcraft:nitor", "thaumcraft:nitor", "thaumcraft:nitor"],
    "leggings":  ["thaumcraft:thaumic_charm", "thaumcraft:amber", "thaumcraft:enchanted_fabric",
                  "thaumcraft:nitor", "thaumcraft:nitor"],
    "boots":     ["thaumcraft:thaumic_charm", "thaumcraft:amber", "thaumcraft:enchanted_fabric", "thaumcraft:nitor"],
}
for slot, comps in runic_base.items():
    set_infusion("runic_%s.json" % slot,
        central=item("minecraft:golden_%s" % slot),
        components=[item(x) for x in comps] + [item("thaumcraft:inkwell")],
        aspects={"tutamen": 30 if slot in ("helmet", "boots") else (60 if slot == "chestplate" else 45),
                 "praecantatio": 75 if slot in ("helmet", "boots") else (105 if slot == "chestplate" else 90),
                 "potentia": 75 if slot in ("helmet", "boots") else (105 if slot == "chestplate" else 90)},
        inst=4)

# --- 符文强化 (EnhRunicArmor) inst8 ---
enh_comps = ["minecraft:nether_star", "thaumcraft:enchanted_fabric", "thaumcraft:enchanted_fabric",
             "thaumcraft:enchanted_fabric", "minecraft:nether_star", "thaumcraft:balanced_shard",
             "thaumcraft:thaumic_charm", "thaumcraft:amber"]
for slot in ("helmet", "chestplate", "leggings", "boots"):
    set_infusion("runic_enhanced_%s.json" % slot,
        central=item("thaumicdyesexplor:runic_%s" % slot),
        components=[item(x) for x in enh_comps],
        aspects={"tutamen": 40, "praecantatio": 160, "potentia": 80, "alienis": 20,
                 "pannus": 24, "vitreus": 16, "permutatio": 20},
        inst=8)

# --- 符文原初 (PrmRunicArmor) inst10, 眼睛+对应槽位虚空袍 ---
primal_void = {"helmet": "thaumicdyesexplor:void_robe_helmet",
               "chestplate": "thaumicdyesexplor:void_robe_chestplate",
               "leggings": "thaumicdyesexplor:void_robe_leggings",
               "boots": "thaumicdyesexplor:void_boots_traveller"}
for slot, void_piece in primal_void.items():
    set_infusion("runic_primal_%s.json" % slot,
        central=item("thaumcraft:primordial_pearl"),
        components=[item("thaumicdyesexplor:runic_enhanced_%s" % slot),
                    item("thaumcraft:eldritch_object"), item(void_piece),
                    item("thaumcraft:eldritch_object")],
        aspects={"praecantatio": 240, "alienis": 120, "tenebrae": 60, "spiritus": 60, "permutatio": 40},
        inst=10)

# --- 符文升级 (initRunicUpgrades) 全部 180 个文件按 1.7.10 重建 ---
UPG = {
    1: ({"telum": 120, "praecantatio": 50, "ignis": 32},
        ["thaumcraft:balanced_shard", "thaumcraft:fire_shard", "thaumcraft:fire_shard",
         "minecraft:blaze_powder", "thaumcraft:fire_shard", "thaumcraft:fire_shard"]),
    2: ({"tutamen": 256, "praecantatio": 256, "potentia": 512, "vitreus": 32},
        ["thaumcraft:salis_mundus", "thaumcraft:balanced_shard", "thaumcraft:balanced_shard",
         "thaumcraft:nitor", "thaumcraft:balanced_shard", "thaumcraft:balanced_shard"]),
    3: ({"tutamen": 40, "praecantatio": 60, "aer": 80, "motus": 40},
        ["thaumcraft:balanced_shard", "thaumcraft:air_shard", "thaumcraft:air_shard",
         "minecraft:sugar", "thaumcraft:air_shard", "thaumcraft:air_shard"]),
    4: ({"tutamen": 16, "praecantatio": 64, "aqua": 32, "sano": 60},
        ["thaumcraft:balanced_shard", "thaumcraft:water_shard", "thaumcraft:water_shard",
         "minecraft:glistering_melon_slice", "thaumcraft:water_shard", "thaumcraft:water_shard"]),
    5: ({"tutamen": 48, "praecantatio": 32, "ordo": 32, "metallum": 40},
        ["thaumcraft:balanced_shard", "thaumcraft:order_shard", "thaumcraft:order_shard",
         "minecraft:iron_block", "thaumcraft:order_shard", "thaumcraft:order_shard"]),
    6: ({"tutamen": 40, "praecantatio": 56, "terra": 32, "potentia": 80},
        ["thaumcraft:balanced_shard", "thaumcraft:earth_shard", "thaumcraft:earth_shard",
         "minecraft:magma_cream", "thaumcraft:earth_shard", "thaumcraft:earth_shard"]),
    7: ({"tutamen": 24, "praecantatio": 32, "motus": 40, "perditio": 60},
        ["thaumcraft:balanced_shard", "thaumcraft:entropy_shard", "minecraft:iron_block",
         "thaumcraft:obsidian_tile", "minecraft:iron_block", "thaumcraft:entropy_shard"]),
    8: ({"victus": 320, "praecantatio": 64, "humanus": 80, "permutatio": 40, "spiritus": 40},
        ["thaumcraft:balanced_shard", "thaumcraft:water_shard", "thaumcraft:shimmerleaf",
         "thaumcraft:iron_golem_placer", "thaumcraft:shimmerleaf", "thaumcraft:earth_shard"]),
    9: ({"iter": 24, "praecantatio": 16, "volatus": 24, "motus": 32},
        ["thaumcraft:salis_mundus", "thaumcraft:air_shard", "thaumcraft:nitor",
         "thaumcraft:salis_mundus", "thaumcraft:nitor", "thaumcraft:order_shard"]),
    10: ({"telum": 200, "praecantatio": 64, "potentia": 96},
         ["thaumcraft:balanced_shard", "thaumcraft:alumentum", "thaumcraft:fire_shard",
          "thaumcraft:thaumium_sword", "thaumcraft:order_shard", "thaumcraft:alumentum"]),
}
pat = re.compile(r"^runic_upgrade_(\d+)_(runic_(?:enhanced_)?(?:primal_)?(?:helmet|chestplate|leggings|boots|goggles|boots_traveller))\.json$")
n_upg = 0
for fn in os.listdir(I):
    m = pat.match(fn)
    if not m:
        continue
    n = int(m.group(1))
    piece = m.group(2)
    aspects, comps = UPG[n]
    d = load(I, fn)
    new = {"type": "thaumcraft:infusion", "research": d.get("research"),
           "central": item("thaumicdyesexplor:" + piece),
           "components": [item(x) for x in comps],
           "result": d.get("result"),
           "instability": 6, "aspects": aspects}
    save(I, fn, new); mark(I + "/" + fn); n_upg += 1

# --- TE 法杖核心等 ---
set_infusion("amber_core.json",
    central=item("thaumcraft:amber_block"),
    components=[item("thaumcraft:salis_mundus"), item("thaumcraft:salis_mundus")],
    aspects={"praecantatio": 14, "auram": 4, "vinculum": 6}, inst=5)

set_infusion("bread_core.json",
    central=item("minecraft:bread"),
    components=[item("thaumcraft:salis_mundus")],
    aspects={"praecantatio": 8, "messis": 8, "fames": 4}, inst=3)

set_infusion("transmutation_core.json",
    central=item("thaumcraft:thaumic_charm"),
    components=[item("thaumcraft:air_shard"), item("thaumcraft:fire_shard"),
                item("thaumcraft:water_shard"), item("thaumcraft:earth_shard"),
                item("thaumcraft:order_shard"), item("thaumcraft:entropy_shard"),
                item("thaumcraft:salis_mundus")],
    aspects={"praecantatio": 32, "permutatio": 16, "auram": 4}, inst=6)

set_infusion("pure_zombie_brain.json",
    central=item("thaumcraft:zombie_brain"),
    components=[item("minecraft:splash_potion"), item("minecraft:golden_apple"),
                item("minecraft:water_bucket")],
    aspects={"humanus": 4, "cognitio": 6, "sano": 2}, inst=3)

set_infusion("boots_meteor.json",
    central=item("thaumcraft:boots_of_the_traveller"),
    components=[item("thaumcraft:fire_crystal_cluster"), item("minecraft:netherrack"),
                item("minecraft:netherrack"), item("minecraft:netherrack"),
                item("thaumcraft:focus_fire")],
    aspects={"ignis": 25, "potentia": 25, "iter": 25, "volatus": 25}, inst=4)

set_infusion("boots_comet.json",
    central=item("thaumcraft:boots_of_the_traveller"),
    components=[item("thaumcraft:water_crystal_cluster"), item("minecraft:snow_block"),
                item("minecraft:snow_block"), item("minecraft:snow_block"),
                item("thaumcraft:focus_frost")],
    aspects={"aqua": 25, "gelum": 25, "iter": 25, "motus": 25}, inst=4)

set_infusion("thaumium_boots_traveller.json",
    central=item("thaumcraft:boots_of_the_traveller"),
    components=[item("minecraft:leather"), item("thaumcraft:magic_tallow"),
                item("thaumcraft:magic_tallow"), item("thaumcraft:salis_mundus"),
                item("thaumcraft:enchanted_fabric"), item("thaumcraft:enchanted_fabric")],
    aspects={"metallum": 16, "tutamen": 16, "pannus": 16, "praecantatio": 16, "iter": 8}, inst=4)

set_infusion("void_boots_traveller.json",
    central=item("thaumcraft:boots_of_the_traveller"),
    components=[item("minecraft:leather"), item("thaumcraft:void_ingot"),
                item("thaumcraft:magic_tallow"), item("thaumcraft:salis_mundus"),
                item("thaumcraft:enchanted_fabric"), item("thaumcraft:enchanted_fabric")],
    aspects={"metallum": 16, "tutamen": 16, "pannus": 16, "praecantatio": 16, "iter": 8,
             "alienis": 16, "vacuos": 8}, inst=4)

set_infusion("sojourner_cap.json",
    central=item("thaumicdyesexplor:sojourner_cap_uncharged"),
    components=[item("thaumcraft:salis_mundus"), item("thaumcraft:salis_mundus")],
    aspects={"auram": 6, "praecantatio": 12, "permutatio": 16, "potentia": 12}, inst=5)

set_infusion("mechanist_cap.json",
    central=item("thaumicdyesexplor:mechanist_cap_uncharged"),
    components=[item("thaumcraft:salis_mundus"), item("thaumcraft:salis_mundus")],
    aspects={"machina": 16, "praecantatio": 12, "auram": 6, "potentia": 12}, inst=5)

set_infusion("stabilizer_belt.json",
    central=item("thaumcraft:mundane_belt"),
    components=[item("minecraft:iron_ingot"), item("minecraft:iron_ingot"),
                item("minecraft:gold_ingot"), item("minecraft:gold_ingot"),
                item("thaumcraft:order_shard"), item("thaumcraft:earth_shard")],
    aspects={"ordo": 12, "terra": 12, "tutamen": 4, "iter": 8}, inst=3)

set_infusion("talisman_food.json",
    central=item("thaumcraft:thaumic_charm"),
    components=[item("minecraft:obsidian"), item("minecraft:cooked_beef"),
                item("minecraft:cooked_chicken"), item("minecraft:cooked_porkchop"),
                item("minecraft:cooked_cod"), item("minecraft:bread")],
    aspects={"fames": 30, "corpus": 25, "messis": 25, "permutatio": 10}, inst=5)

set_infusion("tentacle_ring.json",
    central=item("thaumcraft:mundane_ring"),
    components=[item("thaumcraft:taint_slime"), item("thaumcraft:taint_tendril"),
                item("thaumcraft:taint_slime"), item("thaumcraft:taint_tendril")],
    aspects={"vitium": 32, "telum": 8, "tutamen": 16, "bestia": 8}, inst=4)

# --- ME 灌注材料 (Recipes.addBasicMaterialsRecipe) 要素/组件修正 ---
me_fix = {
    "me_infused_brighten_gildediron.json": (
        {"thaumicdyesexplor:rhudel": 16, "lucrum": 16, "praecantatio": 8},
        ["minecraft:diamond"] * 3, "minecraft:gold_ingot"),
    "me_infused_daedelos_galvorn.json": (
        {"thaumicdyesexplor:edhel": 16, "thaumicdyesexplor:nauglin": 16, "praecantatio": 8},
        ["minecraft:coal"] * 3, "minecraft:iron_ingot"),
    "me_infused_daedelos_morgul.json": (
        {"thaumicdyesexplor:orchoth": 16, "thaumicdyesexplor:mordor": 16, "praecantatio": 8},
        ["minecraft:blue_ice"] * 3, "minecraft:iron_ingot"),
    "me_infused_durnaur_orc.json": (
        {"thaumicdyesexplor:orchoth": 16, "thaumicdyesexplor:mordor": 16, "praecantatio": 8},
        ["minecraft:blaze_powder"] * 3, "minecraft:iron_ingot"),
    "me_infused_edhelmir_elven.json": (
        {"thaumicdyesexplor:edhel": 32, "praecantatio": 8},
        ["minecraft:emerald"] * 3, "minecraft:iron_ingot"),
    "me_infused_gulduril_black_uruk.json": (
        {"thaumicdyesexplor:mornogol": 16, "thaumicdyesexplor:mordor": 16, "praecantatio": 8},
        ["minecraft:amethyst_shard"] * 3, "minecraft:iron_ingot"),
    "me_infused_gulduril_uruk.json": (
        {"thaumicdyesexplor:mornogol": 16, "thaumicdyesexplor:isengard": 16, "praecantatio": 8},
        ["minecraft:amethyst_shard"] * 3, "minecraft:iron_ingot"),
    "me_infused_ithildin_blue_dwarven.json": (
        {"thaumicdyesexplor:nauglin": 16, "thaumicdyesexplor:eredluin": 16, "praecantatio": 8},
        ["minecraft:lapis_lazuli"] * 3, "minecraft:iron_ingot"),
    "me_infused_ithildin_dwarven.json": (
        {"thaumicdyesexplor:nauglin": 16, "thaumicdyesexplor:angdol": 16, "praecantatio": 8},
        ["minecraft:glowstone_dust"] * 3, "minecraft:iron_ingot"),
    "me_infused_stared_mithril.json": (
        {"thaumicdyesexplor:alfirin": 64, "thaumicdyesexplor:nauglin": 16,
         "thaumicdyesexplor:utumno": 16, "auram": 32},
        ["minecraft:nether_star"] * 9, "minecraft:gold_ingot"),
}
for fn, (aspects, comps, central) in me_fix.items():
    set_infusion(fn, central=item(central), components=[item(x) for x in comps],
                 aspects=aspects, inst=5)

# ME 极限瓶盖: 中心是星耀秘银块而不是锭
set_infusion("me_extreme_jar_cap.json",
    central=item("thaumicdyesexplor:me_storage_stared_mithril"),
    components=[item("thaumicdyesexplor:me_material_%s" % x) for x in (
        "gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven",
        "edhelmir_elven", "daedelos_galvorn", "stared_mithril", "daedelos_morgul",
        "durnaur_orc", "gulduril_uruk", "brighten_gildediron")],
    aspects={"thaumicdyesexplor:utumno": 16, "thaumicdyesexplor:alfirin": 32,
             "thaumicdyesexplor:nauglin": 16}, inst=5)

# ME 铁丘杖芯要素修正（IRONHILLS=angdol，之前误用 nauglin）
d = load(I, "me_rod_dwarf.json")
d["aspects"] = {"thaumicdyesexplor:angdol": 24, "praecantatio": 8}
save(I, "me_rod_dwarf.json", d); mark(I + "/me_rod_dwarf.json")

# ME 防扭曲戒指: 1.7.10 是注魔 (RING_warpward)
set_infusion("me_ring_warpward.json", sub=A,
    central=item("thaumcraft:mundane_ring"),
    components=[item("thaumcraft:sanity_soap")] + [item("thaumicdyesexplor:me_nugget_stared_mithril")] * 3,
    aspects={"auram": 64, "vitium": 64, "thaumicdyesexplor:ithryn": 64,
             "thaumicdyesexplor:alfirin": 64}, inst=8)

# ME 基岩粉碎焦点: 1.7.10 是注魔 (FOCUS_BEDROCK_REMOVER)
set_infusion("me_focus_bedrock_remover.json", sub=A,
    central=item("thaumcraft:focus_portable_hole"),
    components=[item("minecraft:grindstone"), item("thaumcraft:amber"),
                item("thaumicdyesexplor:me_material_stared_mithril")],
    aspects={"perditio": 64, "tenebrae": 32, "vacuos": 32,
             "thaumicdyesexplor:alfirin": 16, "thaumicdyesexplor:utumno": 16}, inst=9)

# ------------------------------------------------------------------ arcane --
def replace_arcane(name, **kw):
    # 原文件在 infusion/（之前误做成注魔），读取 research 后改写到 arcane/ 并删除旧文件
    try:
        old = load(I, name)
        research = old.get("research")
    except FileNotFoundError:
        old = load(A, name)
        research = old.get("research")
    new = arcane(kw["result"], kw["vis"], kw["pattern"], kw["key"], research)
    save(A, name, new); mark(A + "/" + name)
    p = os.path.join(I, name)
    if os.path.exists(p):
        os.remove(p); changed.append("DELETED infusion/" + name)

# TE 琥珀杖芯/嬗变杖芯: 1.7.10 是奥术合成 "  S"/" G "/"G  "
replace_arcane("amber_staff_core.json",
    result=item("thaumicdyesexplor:amber_staff_core"),
    vis={"ordo": 25},
    pattern=["  S", " G ", "G  "],
    key={"S": item("thaumcraft:thaumic_charm"), "G": item("thaumicdyesexplor:amber_core")})
replace_arcane("transmutation_staff_core.json",
    result=item("thaumicdyesexplor:transmutation_staff_core"),
    vis={"ordo": 25},
    pattern=["  S", " G ", "G  "],
    key={"S": item("thaumcraft:thaumic_charm"), "G": item("thaumicdyesexplor:transmutation_core")})

# TE 驱秽护符(捕梦网): 1.7.10 是奥术合成 GPG/PSP/FPF
replace_arcane("charm_no_taint.json",
    result=item("thaumicdyesexplor:charm_no_taint"),
    vis={"ordo": 15, "perditio": 15},
    pattern=["GPG", "PSP", "FPF"],
    key={"G": item("thaumcraft:taint_slime"), "P": item("thaumcraft:greatwood_planks"),
         "S": item("thaumcraft:taint_tendril"), "F": item("minecraft:feather")})

# TE 折扣戒指: 1.7.10 是奥术合成 F/F F/F 均衡碎片
replace_arcane("discount_ring.json",
    result=item("thaumicdyesexplor:discount_ring"),
    vis={"aer": 8, "ignis": 8, "aqua": 8, "terra": 8, "ordo": 8, "perditio": 8},
    pattern=[" F ", "F F", " F "],
    key={"F": item("thaumcraft:balanced_shard")})

# 旧的简易 crafting 版删除（已被 infusion 取代）
for fn in to_delete:
    p = os.path.join(C, fn)
    if os.path.exists(p):
        os.remove(p)
        changed.append("DELETED crafting/" + fn)

print("changed:", len(changed), "upgrades:", n_upg, "deleted:", len(to_delete))
for c in changed:
    print(" ", c)
