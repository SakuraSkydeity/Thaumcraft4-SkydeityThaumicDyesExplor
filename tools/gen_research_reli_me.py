# -*- coding: utf-8 -*-
"""
重写魔导书：RELI + ME 合并为一个分类「中土遗物学」(MERELIC)，
内容详细列出每件物品，配方页 COMPLETED_SINGLE；同时给染途学的
RUNICUPGRADE 条目补上 10 条符文升级注魔页。
运行：python tools/gen_research_reli_me.py
"""
import json
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NS = "thaumicdyesexplor"
RESEARCH = os.path.join(ROOT, "src", "main", "resources", "data", NS, "thaumcraft", "research", "default.json")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", NS)
CAT = "MERELIC"

data = json.load(open(RESEARCH, encoding="utf-8"))
# 清掉旧分类与旧条目
data["categories"] = [c for c in data["categories"]
                      if c["key"] not in (NS + ":RELI", NS + ":ME")]
data["entries"] = [e for e in data["entries"]
                   if not e["key"].split(":")[1].startswith(("RELI", "ME"))]
cat_keys = {c["key"] for c in data["categories"]}
if NS + ":" + CAT not in cat_keys:
    sort_order = max(int(c.get("sort_order", 0)) for c in data["categories"]) + 1
    data["categories"].append({"key": NS + ":" + CAT, "icon": NS + ":textures/misc/r_merelic.png",
                               "background": "thaumcraft:textures/gui/gui_researchback.png",
                               "sort_order": sort_order})

# ============ 分类图标：斜杖 + 金戒指 + 双星 ============
im = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.line([(12, 52), (36, 28)], fill=(90, 60, 35, 255), width=6)
d.line([(40, 24), (50, 14)], fill=(90, 60, 35, 255), width=6)
d.ellipse([32, 20, 44, 32], fill=(200, 170, 60, 255))
d.polygon([(50, 14), (58, 8), (56, 18), (46, 20)], fill=(140, 60, 180, 255))
d.ellipse([16, 40, 40, 60], outline=(230, 190, 70, 255), width=4)
d.ellipse([25, 46, 33, 54], fill=(120, 200, 190, 255))
d.ellipse([50, 2, 60, 12], fill=(240, 220, 120, 255))
im = im.resize((32, 32), Image.LANCZOS)
os.makedirs(os.path.join(ASSETS, "textures", "misc"), exist_ok=True)
im.save(os.path.join(ASSETS, "textures", "misc", "r_merelic.png"))
for old in ("r_relics.png", "r_me.png"):
    p = os.path.join(ASSETS, "textures", "misc", old)
    if os.path.isfile(p):
        os.remove(p)

# ============ 条目 ============
ENTRIES = [
    ("MERELIC_ROOT", 0, 0, "reli_arcanum",
     ["中土遗物学：Forgotten Relics 与 MiddleEarth-Thaumaturgy 的全部内容。",
      "所有条目默认解锁，不需要研究。每页用大白话写清每件物品是干什么的，配方直接附在页面上。",
      "法杖装配小贴士：杖柄（或杖芯）放 3×3 正中间，两个**相同**的杖端放右上角和左下角对角——其他格子必须留空。杖芯做大法杖。",
      "⚠️ 装配必须在奥术工作台（Arcane Worktable）上进行并消耗 vis，普通工作台合不出来！"],
     []),
    ("RELI_TOMES", 1, 0, "reli_fate_tome",
     ["十二本法书，全部右键施法，消耗背包里法杖的 vis：",
      "· 核怒之书：发射会爆炸的飞弹。· 绯红法咒：伤害并附加虚弱。· 咒异法咒：混沌弹。",
      "· 厉鬼头骨：怨灵弹。· 月辉之书：往视线落点降下 5 发月光弹。· 千魂咒令：怨灵追踪弹。",
      "· 深渊魔典：暗物质黑洞弹（带爆炸）。· 雷鸣：在视线处劈下闪电。· 混沌之书：随机弹。",
      "· 纷争之书：把自己闪现到 48 格内的视线处。· 位面之镜：回到重生点。",
      "· 封神之作：给自己伤害吸收 + 生命恢复 + 抗性。· 命运之书：死亡时自动免死（5 分钟冷却）。"],
     [("NORMAL_CRAFTING", "reli_missile_tome"), ("NORMAL_CRAFTING", "reli_teleportation_tome"),
      ("NORMAL_CRAFTING", "reli_apotheosis"), ("NORMAL_CRAFTING", "reli_fate_tome")]),
    ("RELI_VIS", 2, 0, "reli_arcanum",
     ["装 vis 的东西（拿法杖即可充放）：",
      "· 星云核心：250 vis。· 欧米茄核心：2500 vis。· 遗忘基石：25000 vis。",
      "· 虚空吞噬者：对着灵气节点右键，把节点 vis 抽进背包里的法杖。",
      "· 永恒智慧之书：右键把你的经验全部吸进书里，潜行右键全部取出来。"],
     [("NORMAL_CRAFTING", "reli_arcanum"), ("NORMAL_CRAFTING", "reli_omega_core"),
      ("NORMAL_CRAFTING", "reli_oblivion_stone"), ("NORMAL_CRAFTING", "reli_xp_tome"),
      ("NORMAL_CRAFTING", "reli_obelisk_drainer")]),
    ("RELI_BAUBLES", 3, 0, "reli_deific_amulet",
     ["饰品装进 Curios 槽位就生效：",
      "· 七阳之戒：着火立刻灭火。· 神性护符：持续清除负面效果、灭火、溺水时用 vis 换一口气。",
      "· 闪亮之石：原地站着不动，回血越来越快（最高再生 III）。",
      "· 叠加之戒：每 30 秒 2.5% 概率和另一个戴同款戒指的玩家交换位置（跨维度也换）。",
      "· 遗忘护符：随机给自己上负面效果（危险品），扭曲 +4。",
      "· 纷争之戒：源模组靠按键触发，暂无效果。",
      "· 采矿护符：交互距离 +2 格、挖掘速度 +50%；灵虚采矿护符：+4 格、+100%。"],
     [("NORMAL_CRAFTING", "reli_dark_sun_ring"), ("NORMAL_CRAFTING", "reli_deific_amulet"),
      ("NORMAL_CRAFTING", "reli_shiny_stone"), ("NORMAL_CRAFTING", "reli_superposition_ring"),
      ("NORMAL_CRAFTING", "reli_mining_charm"), ("NORMAL_CRAFTING", "reli_advanced_mining_charm")]),
    ("RELI_WEAPONS", 4, 0, "reli_terror_crown",
     ["· 悖论（剑）：左键打人伤害随机 0~20，差额自己吃（反噬）。面板 0 伤是正常的。",
      "· 荒芜铺展者（剑）：同材质（4 级 / 3000 耐久 / 效率 16），左键机关源码里就是空的。",
      "· 恐惧王冠（头盔）：戴上能直接看见灵气节点（同揭示护目镜），扭曲 +3。"],
     [("NORMAL_CRAFTING", "reli_paradox"), ("NORMAL_CRAFTING", "reli_wastelayer"),
      ("NORMAL_CRAFTING", "reli_terror_crown")]),
    ("ME_MATERIALS", 5, 0, "me_material_stared_mithril",
     ["十种注入锭：中心放金属锭，环上放三份对应水晶 / 材料，注魔祭坛合成。",
      "锭可以合成存储块（9 锭合 1 块），块也能拆回 9 锭。",
      "十种注魔配方见后面几页。"],
     [("INFUSION_CRAFTING", "me_infused_" + m) for m in
      ("gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven", "edhelmir_elven",
       "daedelos_galvorn", "stared_mithril", "daedelos_morgul", "durnaur_orc",
       "gulduril_uruk", "brighten_gildediron")]),
    ("ME_WANDS", 6, 0, "me_wand_core_gondor",
     ["24 个派系各有杖柄（容量 100）与杖芯（大法杖，容量 250），10 种金属杖端决定 vis 消耗"
      "（秘银 ×0.65 最省，奥克钢等 ×0.95）。",
      "装配（奥术工作台，消耗 vis）：杖柄放正中 + 两个相同杖端放右上、左下对角 = 法杖；杖芯放正中 = 大法杖（容量 250）。",
      "派系法杖每 5 秒按结盟值回 vis（当前版本固定 5 点）。杖端合成见后面几页。"],
     [("NORMAL_CRAFTING", "me_wand_cap_" + c) for c in
      ("mithril", "orcsteel", "dwarvensteel", "galvorn", "uruksteel",
       "morgulsteel", "bluedwarvensteel", "blackuruksteel", "elvensteel", "gildediron")]),
    ("ME_JARS", 7, 0, "me_extreme_jar_cap",
     ["精髓罐家族：普通右键把罐里的 vis 充进法杖，潜行右键把法杖的 vis 存进罐子。",
      "· 压缩精髓罐：2 倍容量。· 双重压缩精髓罐：4 倍。· 群星精髓罐：8 倍。",
      "罐子粘合剂（结晶 / 神秘）是合成罐子的关键材料；群星罐盖由十种注入锭注魔而成。"],
     [("NORMAL_CRAFTING", "me_jar_binder"), ("NORMAL_CRAFTING", "me_jar_binder_advanced"),
      ("NORMAL_CRAFTING", "me_compressed_essentia_jar"), ("NORMAL_CRAFTING", "me_x2_compressed_essentia_jar"),
      ("INFUSION_CRAFTING", "me_extreme_jar_cap"), ("NORMAL_CRAFTING", "me_extreme_essentia_jar")]),
    ("ME_SUMMON", 8, 0, "me_summon_focus_gondor",
     ["召唤焦点：24 个派系各一枚，配方统一 = 金锭 + 灵魂沙 + 该派系对应的注入锭。",
      "（刚铎=刚铎锭、霍比特=秘银锭、艾森加德=乌鲁克钢锭……按派系换锭即可。）",
      "源模组用它召唤派系士兵（需要结盟值），当前版本因 LOTR 前置缺失只能观赏。",
      "· 基岩粉碎者：法杖核心，右键可挖除基岩（最底层除外）。"],
     [("NORMAL_CRAFTING", "me_summon_focus_gondor"), ("NORMAL_CRAFTING", "me_summon_focus_mordor"),
      ("NORMAL_CRAFTING", "me_focus_bedrock_remover")]),
]


def page_text(key, lines):
    return [{"type": "TEXT", "value": NS + ".research_page." + key + "." + str(i + 1),
             "aspects": {}, "compound_blueprint": "", "recipe_link_policy": "DISABLED", "recipe_ids": []}
            for i in range(len(lines))]


def page_recipe(kind, rid):
    folder = "infusion" if kind == "INFUSION_CRAFTING" else "crafting"
    return {"type": kind, "value": "", "aspects": {}, "compound_blueprint": "",
            "recipe_link_policy": "COMPLETED_SINGLE", "recipe_ids": [NS + ":" + folder + "/" + rid]}


built = []
for key, col, row, icon, lines, recipes in ENTRIES:
    pages = page_text(key, lines)
    for kind, rid in recipes:
        pages.append(page_recipe(kind, rid))
    is_root = key == "MERELIC_ROOT"
    built.append({
        "key": NS + ":" + key,
        "category": NS + ":" + CAT,
        "aspects": {}, "display_column": col, "display_row": row, "complexity": 1,
        "icon": {"type": "STACK", "stack": {"id": NS + ":" + icon}},
        "flags": ["AUTO_UNLOCK", "ROUND"] if is_root else ["AUTO_UNLOCK"],
        "parents": [] if is_root else [NS + ":MERELIC_ROOT"],
        "hidden_parents": [], "siblings": [], "object_triggers": [], "entity_triggers": [],
        "aspect_triggers": [], "warp": 0, "pages": pages,
    })
data["entries"].extend(built)
json.dump(data, open(RESEARCH, "w", encoding="utf-8", newline="\n"), ensure_ascii=False, indent=2)
open(RESEARCH, "a", encoding="utf-8", newline="\n").write("\n")

# ============ 染途学 RUNICUPGRADE 补 10 条升级注魔页 ============
for e in data["entries"]:
    if e["key"] == NS + ":RUNICUPGRADE":
        e["pages"] = [p for p in e["pages"] if not p.get("recipe_ids") or
                      "runic_upgrade_" not in p["recipe_ids"][0]]
        for u in range(1, 11):
            e["pages"].append({
                "type": "INFUSION_CRAFTING", "value": "", "aspects": {}, "compound_blueprint": "",
                "recipe_link_policy": "COMPLETED_SINGLE",
                "recipe_ids": [NS + ":infusion/runic_upgrade_" + str(u) + "_runic_helmet"]})
        break
json.dump(data, open(RESEARCH, "w", encoding="utf-8", newline="\n"), ensure_ascii=False, indent=2)
open(RESEARCH, "a", encoding="utf-8", newline="\n").write("\n")

# ============ 语言键 ============
ZHN = {"MERELIC_ROOT": "中土遗物学", "RELI_TOMES": "法书施法", "RELI_VIS": "vis 的容器",
       "RELI_BAUBLES": "遗物饰品", "RELI_WEAPONS": "剑与王冠",
       "ME_MATERIALS": "注入锭", "ME_WANDS": "派系法杖", "ME_JARS": "精髓罐",
       "ME_SUMMON": "召唤焦点与杂项"}
EN = {"MERELIC_ROOT": "Middle-earth Relic Studies", "RELI_TOMES": "Spell Tomes", "RELI_VIS": "Vessels of Vis",
      "RELI_BAUBLES": "Relic Baubles", "RELI_WEAPONS": "Blades & Crown",
      "ME_MATERIALS": "Infused Ingots", "ME_WANDS": "Faction Wands", "ME_JARS": "Essentia Jars",
      "ME_SUMMON": "Summon Foci & Misc"}
EN_PAGES = {
    "MERELIC_ROOT": ["Middle-earth Relic Studies: everything from Forgotten Relics and MiddleEarth-Thaumaturgy.",
                     "All entries unlocked by default. Plain-language descriptions and recipes attached.",
                     "Wand assembly tip: rod in the very center, two IDENTICAL caps at top-right and bottom-left corners - all other slots empty. Staff core makes a staff.",
                     "ASSEMBLY REQUIRES THE ARCANE WORKTABLE and vis - it will NOT work in a plain crafting table!"],
    "RELI_TOMES": ["Twelve tomes cast on right click using wand vis:",
                   "Nuclear Fury: exploding missile. Crimson Spell: damage+weakness. Eldritch Spell: chaos bolt. Ghastly Skull: wraith bolt. Lunar Flares: 5 falling flares on target. Soul Tome: homing wraith. Void Grimoire: dark matter blast. Thunderpeal: lightning strike. Tome of Primal Chaos: random bolt. Tome of Discord: 48-block blink. Dimensional Mirror: return to spawn. Apotheosis: self buffs. Broken Fates: auto-anti-death (5 min cd)."],
    "RELI_VIS": ["Vis holders (charge with a wand): Arcanum 250, Omega Core 2500, Oblivion Keystone 25000.",
                 "Devourer of The Void: right-click a node to drain it into your wand.",
                 "Tome of Ageless Wisdom: right-click absorbs your XP, sneak-right-click releases it."],
    "RELI_BAUBLES": ["Baubles in Curios slots:",
                     "Seven Suns Ring: auto-extinguish. Deific Amulet: cures negatives, extinguishes, vis for air. Shiny Stone: stand still to heal (up to Regen III). Superposition Ring: 2.5%/30s position swap with another wearer. Oblivion Amulet: random curses, warp +4. Discord Ring: keybind item, no effect yet. Mining Charm: +2 reach +50% speed; Ethereal Charm: +4 reach +100% speed."],
    "RELI_WEAPONS": ["The Paradox: left-click deals random 0-20 damage, you take the difference (0 base damage is normal).",
                     "The Wastelayer: same material (level 4 / 3000 durability / 16 speed).",
                     "Crown of Terror: see aura nodes like goggles, warp +3."],
    "ME_MATERIALS": ["Ten infused ingots: metal core + three crystals at the infusion altar. 9 ingots craft into a storage block and back.",
                     "All ten infusion recipes follow."],
    "ME_WANDS": ["24 faction rods (capacity 100) and staff cores (250); 10 caps change vis cost (Mithril x0.65 cheapest).",
                 "Assembly AT THE ARCANE WORKTABLE with vis: rod center + two identical caps top-right & bottom-left = wand; staff core = staff (250). Faction wands regain 5 vis every 5s. Cap recipes follow."],
    "ME_JARS": ["Essentia jars: right-click fills your wand, sneak-right-click stores vis. Compressed x2, x2 Compressed x4, Extreme x8. Jar Binders are the key material; the Extreme Cap is infused from all ten ingots."],
    "ME_SUMMON": ["Summon Foci: one per faction (24 total). Recipe is always gold ingot + soul sand + that faction's infused ingot.",
                  "Source mod summons faction soldiers (needs alignment); decorative without LOTR.",
                  "Bedrock Shatterer: wand core, right-click breaks bedrock (not the bottom layer)."],
}
for lang in ("zh_cn", "en_us"):
    path = os.path.join(ASSETS, "lang", lang + ".json")
    d = json.load(open(path, encoding="utf-8"))
    names = ZHN if lang == "zh_cn" else EN
    for key, _, _, _, lines, _ in ENTRIES:
        d["tc.research_name.%s:%s" % (NS, key)] = names[key]
        d["tc.research_text.%s:%s" % (NS, key)] = lines[0]
        src = lines if lang == "zh_cn" else EN_PAGES[key]
        for i, line in enumerate(src):
            d[NS + ".research_page." + key + "." + str(i + 1)] = line
    d["tc.research_category." + NS + ":" + CAT] = "中土遗物学" if lang == "zh_cn" else "Middle-earth Relic Studies"
    json.dump(d, open(path, "w", encoding="utf-8", newline="\n"), ensure_ascii=False, indent=2, sort_keys=True)
    open(path, "a", encoding="utf-8", newline="\n").write("\n")

print("中土遗物学：%d 条目；RUNICUPGRADE 补 10 页" % len(built))
