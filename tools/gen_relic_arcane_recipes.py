#!/usr/bin/env python3
"""把 36 件遗物的「无序合成」改写成奥术工作台配方（第 21 轮）。

设计要点
--------
1. **形状统一**：3×3 里四个角固定放神秘锭，剩下三格放原来的三种材料：

       T A T
       B C B      A/B/C = 原有三种材料（A=原第 1 种，B=第 2 种，C=第 3 种）
       T . T       T = thaumcraft:thaumium_ingot

   这样既保留了「遗物是奥术造物」的味道，也让每条配方都需要魔力布匹之外的
   神秘锭作为门槛。

2. **要素消耗分层**：按遗物的强度分三档，写在 VIS 表里（单位 vis，不是 centivis）：
   普通 30~40、强力 50~70、顶级 90~120。要素按物品的主题选（火焰书 = ignis，
   灵魂/虚空 = perditio + aqua，传送/风 = aer，防护 = terra + ordo）。

3. **研究门槛**：配方挂在对应研究条目上（RELI_TOMES / RELI_BAUBLES /
   RELI_WEAPONS / RELI_ODDITIES），这些都是 AUTO_UNLOCK，不会卡玩家。

用法：``python tools/gen_relic_arcane_recipes.py``（幂等，会删掉旧的 crafting 版本）
"""
from __future__ import annotations

import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data/thaumicdyesexplor/recipe")
CRAFTING = os.path.join(DATA, "crafting")
ARCANE = os.path.join(DATA, "arcane")
NS = "thaumicdyesexplor"
THAUMIUM = "thaumcraft:thaumium_ingot"

# 遗物 -> (研究条目, 要素消耗)
# 要素名必须是六元之一：aer / terra / ignis / aqua / ordo / perditio
PLAN = {
    # ---------------- 施法遗物（书 / 咒） ----------------
    "reli_missile_tome": ("RELI_TOMES", {"ignis": 30, "aer": 10}),
    "reli_crimson_spell": ("RELI_TOMES", {"ignis": 30, "perditio": 10}),
    "reli_eldritch_spell": ("RELI_TOMES", {"perditio": 40, "aqua": 10}),
    "reli_ghastly_skull": ("RELI_TOMES", {"perditio": 30, "aqua": 15}),
    "reli_lunar_flares": ("RELI_TOMES", {"aer": 25, "ordo": 15}),
    "reli_chaos_tome": ("RELI_TOMES", {"perditio": 35, "ignis": 15}),
    "reli_soul_tome": ("RELI_TOMES", {"perditio": 30, "aqua": 20}),
    "reli_void_grimoire": ("RELI_TOMES", {"perditio": 50, "ordo": 20}),
    "reli_thunderpeal": ("RELI_TOMES", {"aer": 40, "ignis": 20}),
    "reli_teleportation_tome": ("RELI_TOMES", {"aer": 30, "ordo": 20}),
    "reli_dimensional_mirror": ("RELI_TOMES", {"aer": 40, "perditio": 20}),
    "reli_apotheosis": ("RELI_TOMES", {"ordo": 40, "terra": 20}),
    "reli_obelisk_drainer": ("RELI_TOMES", {"aqua": 30, "perditio": 20}),
    "reli_telekinesis_tome": ("RELI_TOMES", {"aer": 30, "ordo": 10}),
    "reli_weather_stone": ("RELI_ODDITIES", {"aer": 30, "aqua": 30}),
    "reli_overthrower": ("RELI_ODDITIES", {"perditio": 40, "terra": 20}),
    "reli_discord_ring": ("RELI_ODDITIES", {"aer": 30, "perditio": 20}),
    "reli_dormant_arcanum": ("RELI_ODDITIES", {"ordo": 30, "perditio": 25}),
    "reli_fate_tome": ("RELI_TOMES", {"ordo": 60, "aqua": 30, "perditio": 30}),
    "reli_xp_tome": ("RELI_WEAPONS", {"ordo": 25, "terra": 15}),
    # ---------------- vis 储具 ----------------
    "reli_arcanum": ("RELI_VIS", {"ordo": 60, "aqua": 40, "terra": 40}),
    "reli_omega_core": ("RELI_VIS", {"ordo": 80, "perditio": 40, "ignis": 40}),
    "reli_oblivion_stone": ("RELI_VIS", {"perditio": 70, "aqua": 40}),
    # ---------------- 饰品 ----------------
    "reli_dark_sun_ring": ("RELI_BAUBLES", {"ignis": 30, "aer": 10}),
    "reli_superposition_ring": ("RELI_BAUBLES", {"aer": 30, "ordo": 20}),
    "reli_shiny_stone": ("RELI_BAUBLES", {"ordo": 25, "terra": 15}),
    "reli_deific_amulet": ("RELI_BAUBLES", {"ordo": 50, "aqua": 30}),
    "reli_oblivion_amulet": ("RELI_BAUBLES", {"perditio": 40, "aqua": 30}),
    "reli_mining_charm": ("RELI_BAUBLES", {"terra": 20, "ordo": 10}),
    "reli_advanced_mining_charm": ("RELI_BAUBLES", {"terra": 40, "ordo": 20}),
    "reli_ancient_aegis": ("RELI_BAUBLES", {"terra": 40, "ordo": 20}),
    "reli_chaos_core": ("RELI_ODDITIES", {"perditio": 40, "ignis": 20}),
    # ---------------- 武器与护甲 ----------------
    "reli_paradox": ("RELI_WEAPONS", {"perditio": 50, "ignis": 30}),
    "reli_wastelayer": ("RELI_WEAPONS", {"terra": 40, "perditio": 30}),
    "reli_false_justice": ("RELI_WEAPONS", {"ordo": 40, "ignis": 30}),
    "reli_terror_crown": ("RELI_WEAPONS", {"perditio": 40, "aer": 20}),
}

PATTERN = ["TAT", "BCB", "T T"]

# 中土两件：基岩粉碎者（原普通合成版删除）与扭曲修正之戒（原本没有配方）
EXTRA = {
    "me_ring_warpward": {
        "type": "thaumcraft:arcane_shaped",
        "research": "%s:RELI_BAUBLES" % NS,
        "pattern": [" T ", "GQG", " T "],
        "key": {
            "T": {"item": THAUMIUM},
            "G": {"item": "minecraft:gold_ingot"},
            "Q": {"item": "thaumcraft:amber"},
        },
        "result": {"id": "%s:me_ring_warpward" % NS},
        "vis": {"ordo": 40, "aqua": 20},
    },
    "me_focus_bedrock_remover": {
        "type": "thaumcraft:arcane_shaped",
        "research": "%s:MERELIC_ROOT" % NS,
        "pattern": ["ITI", "QEQ", " T "],
        "key": {
            "T": {"item": THAUMIUM},
            "I": {"item": "minecraft:iron_ingot"},
            "Q": {"item": "minecraft:quartz"},
            "E": {"item": "thaumcraft:thaumic_charm"},
        },
        "result": {"id": "%s:me_focus_bedrock_remover" % NS},
        "vis": {"ignis": 15, "terra": 15, "ordo": 5, "perditio": 25},
    },
}


def stack(entry):
    """把 crafting 配方里的 ingredient 原样转成 arcane key 条目。"""
    if not isinstance(entry, dict):
        raise SystemExit("非法 ingredient: %r" % (entry,))
    if "item" in entry:
        return {"item": entry["item"]}
    if "tag" in entry:
        return {"tag": entry["tag"]}
    raise SystemExit("不支持的 ingredient 写法: %r" % (entry,))


def main():
    missing = []
    written = []
    for relic, (research, vis) in PLAN.items():
        src = os.path.join(CRAFTING, relic + ".json")
        if not os.path.exists(src):
            # 幂等：上一次已经转完（crafting 版被删掉），目标存在就跳过
            if not os.path.exists(os.path.join(ARCANE, relic + ".json")):
                missing.append(relic)
            continue
        old = json.load(open(src, encoding="utf-8"))
        if "shaped" in old["type"]:
            raise SystemExit("%s 是有序配方，脚本只处理无序合成" % relic)
        ingredients = [stack(x) for x in old["ingredients"]]
        while len(ingredients) < 3:
            ingredients.append({"item": THAUMIUM})
        key = {
            "T": {"item": THAUMIUM},
            "A": ingredients[0],
            "B": ingredients[1],
            "C": ingredients[2],
        }
        recipe = {
            "type": "thaumcraft:arcane_shaped",
            "research": "%s:%s" % (NS, research),
            "pattern": PATTERN,
            "key": key,
            "result": {"id": "%s:%s" % (NS, relic)},
            "vis": vis,
        }
        out = os.path.join(ARCANE, relic + ".json")
        with open(out, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(recipe, fh, ensure_ascii=False, indent=2)
            fh.write("\n")
        written.append(relic)

    # 旧的普通合成版必须删掉，否则工作台里还能直接拼出来
    for relic in list(PLAN) + list(EXTRA):
        old = os.path.join(CRAFTING, relic + ".json")
        if os.path.exists(old):
            os.remove(old)

    if missing:
        print("缺少原配方，未生成：", missing)

    # 中土的两件原本没有 crafting 配方（基岩粉碎者旧的普通合成版会被删掉），这里单独处理
    extra = EXTRA
    for name, recipe in extra.items():
        out = os.path.join(ARCANE, name + ".json")
        with open(out, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(recipe, fh, ensure_ascii=False, indent=2)
            fh.write("\n")
        written.append(name)

    print("生成奥术配方 %d 条（遗物 %d + 中土 %d）" % (len(written), len(written) - len(extra), len(extra)))
    for name in written:
        print("  arcane/" + name + ".json")


if __name__ == "__main__":
    main()
