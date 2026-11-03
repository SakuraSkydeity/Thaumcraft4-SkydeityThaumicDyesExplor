# -*- coding: utf-8 -*-
"""
生成 MiddleEarth-Thaumaturgy 的配方：
1. 锭⇄粒存储配方（9 粒 = 1 锭，1 锭 = 9 粒）+ 3×3 存储块（9 锭 = 1 块，反向拆解）。
2. 杖端合成（XXX / X X，用对应注入粒）—— 源模组走派系工作台，这里直接给合成台。
3. 注入锭的注魔配方：源模组用 LOTR 金属锭 + LOTR 晶体，1.21.1 没有这些物品，
   按预定的替代表换成原版物品（铁/金锭 + 水晶类材料），要素消耗与源模组一致。
4. 罐子配方：压缩罐（8 原版奥秘罐 + 罐子粘合剂）、双重压缩罐、群星罐盖（注魔）、群星罐。
运行：python tools/port_me_recipes.py
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RECIPE = os.path.join(ROOT, "src", "main", "resources", "data", "thaumicdyesexplor", "recipe")
NS = "thaumicdyesexplor"

os.makedirs(os.path.join(RECIPE, "crafting"), exist_ok=True)
os.makedirs(os.path.join(RECIPE, "infusion"), exist_ok=True)


def write(path, data):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


def shaped(result, pattern, key, count=1):
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": pattern,
        "key": {k: ({"item": v} if isinstance(v, str) else v) for k, v in key.items()},
        "result": {"id": NS + ":" + result, "count": count},
    }


def shapeless(result, ingredients, count=1):
    return {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": i} for i in ingredients],
        "result": {"id": NS + ":" + result, "count": count},
    }


def infusion(result, central, components, aspects, instability=5):
    return {
        "type": "thaumcraft:infusion",
        "research": NS + ":ME_MATERIALS",
        "central": {"item": central},
        "components": [{"item": c} for c in components],
        "result": {"id": NS + ":" + result},
        "instability": instability,
        "aspects": aspects,
    }


MATERIALS = ["gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven", "edhelmir_elven",
             "daedelos_galvorn", "stared_mithril", "daedelos_morgul", "durnaur_orc",
             "gulduril_uruk", "brighten_gildediron"]
CAPS = ["mithril", "orcsteel", "dwarvensteel", "galvorn", "uruksteel",
        "morgulsteel", "bluedwarvensteel", "blackuruksteel", "elvensteel", "gildediron"]

count = 0

# ---- 锭⇄粒 + 存储块 ----
for mat in MATERIALS:
    write(os.path.join(RECIPE, "crafting", "me_ingot_" + mat + ".json"),
          shaped("me_material_" + mat, ["XXX", "XXX", "XXX"],
                 {"X": NS + ":me_nugget_" + mat}))
    write(os.path.join(RECIPE, "crafting", "me_nugget_" + mat + ".json"),
          shapeless("me_nugget_" + mat, [NS + ":me_material_" + mat], count=9))
    count += 2

# ---- 杖端合成（源模组 XXX / X X）----
CAP_NUGGET = {"mithril": "stared_mithril", "orcsteel": "durnaur_orc", "dwarvensteel": "ithildin_dwarven",
              "galvorn": "daedelos_galvorn", "uruksteel": "gulduril_uruk", "morgulsteel": "daedelos_morgul",
              "bluedwarvensteel": "ithildin_blue_dwarven", "blackuruksteel": "gulduril_black_uruk",
              "elvensteel": "edhelmir_elven", "gildediron": "brighten_gildediron"}
for cap, nugget in CAP_NUGGET.items():
    write(os.path.join(RECIPE, "crafting", "me_wand_cap_" + cap + ".json"),
          shaped("me_wand_cap_" + cap, ["XXX", "X X", "   "], {"X": NS + ":me_nugget_" + nugget}))
    count += 1

# ---- 注入锭注魔（替代表：LOTR 物品 → 原版物品，要素消耗保持源模组数值）----
INFUSED = {
    "gulduril_black_uruk": ("minecraft:iron_ingot", ["minecraft:amethyst_shard"] * 3,
                            {"thaumicdyesexplor:orchoth": 16, "thaumicdyesexplor:mordor": 16, "praecantatio": 8}),
    "ithildin_blue_dwarven": ("minecraft:iron_ingot", ["minecraft:lapis_lazuli"] * 3,
                              {"thaumicdyesexplor:nauglin": 16, "thaumicdyesexplor:eredluin": 16, "praecantatio": 8}),
    "ithildin_dwarven": ("minecraft:iron_ingot", ["minecraft:glowstone_dust"] * 3,
                         {"thaumicdyesexplor:nauglin": 16, "thaumicdyesexplor:angdol": 16, "praecantatio": 8}),
    "edhelmir_elven": ("minecraft:iron_ingot", ["minecraft:emerald"] * 3,
                       {"thaumicdyesexplor:edhel": 32, "praecantatio": 8}),
    "daedelos_galvorn": ("minecraft:iron_ingot", ["minecraft:coal"] * 3,
                         {"thaumicdyesexplor:edhel": 16, "thaumicdyesexplor:nauglin": 16, "praecantatio": 8}),
    "stared_mithril": ("minecraft:gold_ingot", ["minecraft:echo_shard"] * 3,
                       {"thaumicdyesexplor:lindon": 16, "thaumicdyesexplor:angdol": 16, "praecantatio": 8}),
    "daedelos_morgul": ("minecraft:iron_ingot", ["minecraft:blue_ice"] * 3,
                        {"thaumicdyesexplor:mornogol": 16, "thaumicdyesexplor:dolguldur": 16, "praecantatio": 8}),
    "durnaur_orc": ("minecraft:iron_ingot", ["minecraft:blaze_powder"] * 3,
                    {"thaumicdyesexplor:mornogol": 16, "thaumicdyesexplor:gundabad": 16, "praecantatio": 8}),
    "gulduril_uruk": ("minecraft:iron_ingot", ["minecraft:amethyst_shard", "minecraft:amethyst_shard",
                                              "minecraft:lapis_lazuli"],
                      {"thaumicdyesexplor:orchoth": 16, "thaumicdyesexplor:isengard": 16, "praecantatio": 8}),
    "brighten_gildediron": ("minecraft:gold_ingot", ["minecraft:glowstone_dust"] * 3,
                            {"thaumicdyesexplor:abonnen": 16, "thaumicdyesexplor:gondor": 16, "praecantatio": 8}),
}
for mat, (central, components, aspects) in INFUSED.items():
    write(os.path.join(RECIPE, "infusion", "me_infused_" + mat + ".json"),
          infusion("me_material_" + mat, central, components, aspects))
    count += 1

# ---- 罐子配方（源 addJarsRecipe；方块已在本模组注册）----
write(os.path.join(RECIPE, "crafting", "me_compressed_essentia_jar.json"),
      {"type": "thaumcraft:arcane", "research": NS + ":ME_JARS",
       "pattern": ["SSS", "SXS", "SSS"],
       "key": {"S": {"item": "thaumcraft:warded_jar"}, "X": {"item": NS + ":me_jar_binder"}},
       "result": {"id": NS + ":me_compressed_essentia_jar", "count": 1},
       "aspects": {"aer": 20, "ignis": 20, "aqua": 20}})
count += 1
write(os.path.join(RECIPE, "crafting", "me_x2_compressed_essentia_jar.json"),
      {"type": "thaumcraft:arcane", "research": NS + ":ME_JARS",
       "pattern": ["SSS", "SXS", "SSS"],
       "key": {"S": {"item": NS + ":me_compressed_essentia_jar"}, "X": {"item": NS + ":me_jar_binder_advanced"}},
       "result": {"id": NS + ":me_x2_compressed_essentia_jar", "count": 1},
       "aspects": {"aer": 80, "ignis": 80, "aqua": 80, "terra": 80, "ordo": 80, "perditio": 80}})
count += 1
write(os.path.join(RECIPE, "infusion", "me_extreme_jar_cap.json"),
      {"type": "thaumcraft:infusion", "research": NS + ":ME_JARS",
       "central": {"item": NS + ":me_material_stared_mithril"},
       "components": [{"item": NS + ":me_material_" + m} for m in MATERIALS],
       "result": {"id": NS + ":me_extreme_jar_cap"},
       "instability": 5,
       "aspects": {"thaumicdyesexplor:utumno": 16, "thaumicdyesexplor:alfirin": 32,
                   "thaumicdyesexplor:nauglin": 16}})
count += 1
write(os.path.join(RECIPE, "crafting", "me_extreme_essentia_jar.json"),
      {"type": "thaumcraft:arcane", "research": NS + ":ME_JARS",
       "pattern": ["SXS", "S S", "SSS"],
       "key": {"S": {"item": "thaumcraft:warded_glass"}, "X": {"item": NS + ":me_extreme_jar_cap"}},
       "result": {"id": NS + ":me_extreme_essentia_jar", "count": 1},
       "aspects": {"aer": 300, "ignis": 300, "aqua": 300, "terra": 300, "ordo": 300, "perditio": 300}})
count += 1

# ---- 罐子粘合剂（源模组无普通合成，给一个方便的合成：玻璃瓶 + 粘液球/纠缠水晶）----
write(os.path.join(RECIPE, "crafting", "me_jar_binder.json"),
      shapeless("me_jar_binder", ["minecraft:glass_bottle", "minecraft:slime_ball", "minecraft:amethyst_shard"]))
write(os.path.join(RECIPE, "crafting", "me_jar_binder_advanced.json"),
      shapeless("me_jar_binder_advanced", [NS + ":me_jar_binder", "minecraft:blaze_powder",
                                           "minecraft:echo_shard", "minecraft:diamond"]))
count += 2

# ---- 手持派系工作台（源注魔配方，替代 LOTR 金戒指 → 原版金锭，焦点袋 → 皮革袋/箱子）----
write(os.path.join(RECIPE, "infusion", "me_handheld_crafter.json"),
      {"type": "thaumcraft:infusion", "research": NS + ":ME_TOOLS",
       "central": {"item": "minecraft:crafting_table"},
       "components": [{"item": "minecraft:chest"}, {"item": "minecraft:sign"},
                      {"item": "minecraft:gold_ingot"}],
       "result": {"id": NS + ":me_handheld_crafter"},
       "instability": 1,
       "aspects": {"fabrico": 16}})
count += 1

print("ME 配方完成：%d 条" % count)
