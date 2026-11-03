# -*- coding: utf-8 -*-
"""
为 MiddleEarth-Thaumaturgy 的 24 派系法杖部件补合成表。

源模组（1.7.10）的做法：
  * 杖柄 me_wand_core_<派系>  —— 注魔（infusion），中央是「银树杖柄」(itemWandRod meta 2)，
    要素 MAGIC 8 + 派系要素 24，不稳定度 6，三个派系材料做组件；
  * 杖芯 me_staff_core_<派系> —— 奥术工作台，pattern "  S"/" W "/"W  "，
    S = 元始魔力(thaumic_charm)，W = 同派系杖柄，消耗 6 种元要素各 80 vis。

1.21.1 没有 LOTR，组件一律换成同主题的原版 / 本前置物品（见 COMPONENTS 表）。

输出：
  data/thaumicdyesexplor/recipe/infusion/me_rod_<派系>.json
  data/thaumicdyesexplor/recipe/arcane/me_staff_core_<派系>.json
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "thaumicdyesexplor", "recipe")
NS = "thaumicdyesexplor"

# 与 tools/port_me_aspects.py 的 FACTION_ASPECT 保持一致
FACTION_ASPECT = {
    "gondor": "gondor", "hobbit": "perian", "ranger_north": "dunedain",
    "blue_mountains": "eredluin", "high_elf": "lindon", "gundabad": "gundabad",
    "angmar": "angmar", "wood_elf": "druardh", "dol_guldur": "dolguldur",
    "dale": "dale", "dwarf": "nauglin", "galadhrim": "lothlorien",
    "dunland": "dunland", "uruk_hai": "isengard", "fangorn": "fangorn",
    "rohan": "rohan", "mordor": "mordor", "dorwinion": "dorwinion",
    "rhun": "rhudel", "near_harad": "harad", "moredain": "morwaith",
    "tauredain": "taurethrim", "half_troll": "pertorog", "bree": "bree",
}

# 派系 → 三个注魔组件（源模组是 LOTR 的旗帜 / 石块 / 宝石，这里换同主题原版物）
COMPONENTS = {
    "gondor":         ["minecraft:white_banner", "minecraft:stone_bricks", "minecraft:diamond"],
    "hobbit":         ["minecraft:green_banner", "minecraft:oak_log", "thaumcraft:amber"],
    "ranger_north":   ["minecraft:gray_banner", "minecraft:bow", "minecraft:emerald"],
    "blue_mountains": ["minecraft:blue_banner", "minecraft:cobbled_deepslate", "minecraft:lapis_lazuli"],
    "high_elf":       ["minecraft:light_blue_banner", "minecraft:emerald", "minecraft:emerald"],
    "gundabad":       ["minecraft:light_gray_banner", "minecraft:bow", "minecraft:amethyst_shard"],
    "angmar":         ["minecraft:black_banner", "minecraft:amethyst_shard", "minecraft:amethyst_shard"],
    "wood_elf":       ["minecraft:lime_banner", "minecraft:oak_leaves", "minecraft:glowstone_dust"],
    "dol_guldur":     ["minecraft:purple_banner", "minecraft:amethyst_shard", "minecraft:deepslate_bricks"],
    "dale":           ["minecraft:yellow_banner", "minecraft:bread", "minecraft:redstone"],
    "dwarf":          ["minecraft:gray_banner", "minecraft:stone_bricks", "minecraft:iron_ingot"],
    "galadhrim":      ["minecraft:lime_banner", "minecraft:oak_log", "minecraft:emerald"],
    "dunland":        ["minecraft:brown_banner", "minecraft:trident", "minecraft:lapis_lazuli"],
    "uruk_hai":       ["minecraft:white_banner", "minecraft:iron_ingot", "minecraft:prismarine_crystals"],
    "fangorn":        ["minecraft:vine", "minecraft:oak_leaves", "minecraft:brown_mushroom"],
    "rohan":          ["minecraft:green_banner", "minecraft:hay_block", "minecraft:prismarine_crystals"],
    "mordor":         ["minecraft:black_banner", "minecraft:blackstone", "minecraft:amethyst_shard"],
    "dorwinion":      ["minecraft:purple_banner", "minecraft:red_terracotta", "minecraft:honey_bottle"],
    "rhun":           ["minecraft:orange_banner", "minecraft:sandstone", "minecraft:redstone"],
    "near_harad":     ["minecraft:orange_banner", "minecraft:cut_sandstone", "minecraft:amethyst_shard"],
    "moredain":       ["minecraft:brown_banner", "minecraft:mud_bricks", "minecraft:leather"],
    "tauredain":      ["minecraft:cyan_banner", "minecraft:mud_bricks", "minecraft:cocoa_beans"],
    "half_troll":     ["minecraft:light_gray_banner", "minecraft:iron_axe", "minecraft:glowstone_dust"],
    "bree":           ["minecraft:brown_banner", "minecraft:cobblestone", "thaumcraft:amber"],
}

ROD_BASE = "thaumcraft:silverwood_wand_rod"   # 源 itemWandRod meta 2（容量 100，与派系杖柄同档）
RESEARCH = NS + ":ME_WANDS"
PRIMALS = ["aer", "terra", "ignis", "aqua", "ordo", "perditio"]


def write(path, payload):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(payload, fh, ensure_ascii=False, indent=2)
        fh.write("\n")


def rod_recipe(faction):
    comps = COMPONENTS[faction]
    assert len(comps) == 3, faction
    return {
        "type": "thaumcraft:infusion",
        "research": RESEARCH,
        "central": {"item": ROD_BASE},
        "components": [{"item": item} for item in comps],
        "result": {"id": "%s:me_wand_core_%s" % (NS, faction)},
        "instability": 6,
        "aspects": {
            "%s:%s" % (NS, FACTION_ASPECT[faction]): 24,
            "praecantatio": 8,
        },
    }


def staff_recipe(faction):
    return {
        "type": "thaumcraft:arcane_shaped",
        "research": RESEARCH,
        "pattern": ["  S", " W ", "W  "],
        "key": {
            "S": {"item": "thaumcraft:thaumic_charm"},
            "W": {"item": "%s:me_wand_core_%s" % (NS, faction)},
        },
        "result": {"id": "%s:me_staff_core_%s" % (NS, faction)},
        "vis": {channel: 80 for channel in PRIMALS},
    }


# 布理的杖柄 / 杖芯已按用户要求删除（第 20 轮），不再生成配方
SKIP = {"bree"}


def main():
    factions = [f for f in sorted(FACTION_ASPECT) if f not in SKIP]
    assert len(factions) == 23, factions
    for faction in factions:
        write(os.path.join(DATA, "infusion", "me_rod_%s.json" % faction), rod_recipe(faction))
        write(os.path.join(DATA, "arcane", "me_staff_core_%s.json" % faction), staff_recipe(faction))
    print("写入 %d 条杖柄注魔配方 + %d 条杖芯奥术配方" % (len(factions), len(factions)))


if __name__ == "__main__":
    main()
