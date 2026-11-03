# -*- coding: utf-8 -*-
"""
重写「中土遗物学」章节的魔导书内容 + 遗物用法 tooltip + 提示语。

第 20 轮起的原则（用户要求）：
  * 书里**只写物品的功能**，一句话一条，不讲来历、不讲材料、不讲怎么合成；
  * 杖柄 / 杖端只写「容量 / 消耗 / 附带回 vis」这类功能，合成那句一律不要；
  * 修掉「书里显示 tc.research_name.xxx 这种原始键」的问题：
    - 补全每个条目的 tc.research_name / tc.research_text；
    - 删掉没有任何条目引用的孤儿页键。

脚本跑完会做一致性校验：
  * 研究页引用的配方 id 必须存在；
  * RelicItem 用到的 tooltip 键在中英两份 lang 里都必须存在；
  * 不允许出现孤儿页键 / 缺 name-text 键。
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
NS = "thaumicdyesexplo" + "r"  # 避免被某些编辑器拼写检查改坏
DATA = os.path.join(RES, "data", NS, "thaumcraft", "research", "default.json")
LANG_ZH = os.path.join(RES, "assets", NS, "lang", "zh_cn.json")
LANG_EN = os.path.join(RES, "assets", NS, "lang", "en_us.json")

TEXT = "TEXT"


def page(kind, value=""):
    return {
        "type": kind,
        "value": value,
        "aspects": {},
        "compound_blueprint": "",
        "recipe_link_policy": "DISABLED",
        "recipe_ids": [],
    }


# =====================================================================================
# 中文正文：一条物品一行，只讲功能
# =====================================================================================
ZH = {}

ZH["MERELIC_ROOT"] = [
    (TEXT, "这一章只讲「这东西有什么用」。每件一行，不多废话。\n"
           "法书、咒令这类要拿在手上右键；戒指 / 护符 / 腰带要装进饰品栏；"
           "写着「放背包」的放在身上就生效。"),
]

ZH["RELI_TOMES"] = [
    (TEXT, "【核怒之书】右键发射会爆炸的飞弹。\n"
           "【绯红法咒】右键发射法球，命中伤害并使目标虚弱。\n"
           "【咒异法咒】右键发射混沌法球，落点范围伤害。\n"
           "【厉鬼头骨】右键发射吸血弹。"),
    (TEXT, "【月辉之书】右键在准星落点上方降下 5 发月辉弹。\n"
           "【原初混沌之书】右键发射会乱飞的混沌弹。\n"
           "【千魂咒令】右键发射会追踪的弹丸。\n"
           "【深渊魔典】右键发射黑洞弹，命中爆炸。"),
    (TEXT, "【雷鸣】右键在准星处劈下闪电。\n"
           "【纷争之书】右键瞬移到准星落点（48 格内）。\n"
           "【位面之镜】右键回到重生点。\n"
           "【封神之作】右键给自己上护盾、回血、抗性。"),
    (TEXT, "【破碎命运之书】放背包里就生效：死亡时自动免死一次，冷却 5 分钟。"),
]

ZH["RELI_VIS"] = [
    (TEXT, "【星云核心】装项链位，存 250 vis；拿在手上右键可与法杖互相充能，潜行右键反向。\n"
           "【欧米茄核心】同上，容量 2500 vis。\n"
           "【遗忘之基石】同上，容量 25000 vis。"),
    (TEXT, "【虚空吞噬者】对灵气节点右键，把节点的 vis 抽进法杖。\n"
           "【永恒智慧之书】右键把经验存进书里，潜行右键全部取出。"),
]

ZH["RELI_BAUBLES"] = [
    (TEXT, "【七阳之戒】戒指位：火焰 / 岩浆伤害变成治疗，超过 12 点的伤害免疫。\n"
           "【叠加之戒】戒指位：偶尔和另一个戴同款戒指的玩家交换位置。"),
    (TEXT, "【闪亮之石】项链位：站着不动会持续回血，站越久回越快。\n"
           "【神性护符】项链位：自动清负面效果、灭火，溺水时用 vis 换气。"),
    (TEXT, "【遗忘护符】项链位：随机给自己上负面效果（危险品）。\n"
           "【纷争之戒】戒指位；拿在手上右键可以闪现（背包里要有纷争之书）。"),
    (TEXT, "【采矿护符】戒指位：交互距离 +2 格，挖掘速度 +50%。\n"
           "【灵虚采矿护符】戒指位：交互距离 +4 格，挖掘速度 +100%。"),
]

ZH["RELI_WEAPONS"] = [
    (TEXT, "【悖论】左键伤害在 0~20 之间随机，差额反噬自己。\n"
           "【荒芜铺展者】和悖论同材质的剑，没有额外效果。\n"
           "【恐惧王冠】戴在头盔位：能看见灵气节点，准星 32 格内的生物持续中失明 / 凋零 / 弱化。"),
]

ZH["RELI_ODDITIES"] = [
    (TEXT, "【虚妄正义】放背包里：你造成的伤害和你受到的伤害都翻倍。\n"
           "【混沌核心】放背包里：受到的伤害随机变成 0~2 倍，偶尔随机中一个药水效果。"),
    (TEXT, "【预定天命之书】右键把准星 16 格内的生物拽到身前。\n"
           "【远古神盾】腰带位：免疫击退，每秒回 1 点血。"),
    (TEXT, "【符文石】下雨时右键，让天气放晴。\n"
           "【永恒放逐咒令】右键把准星处的生物扔进地狱。\n"
           "【沉睡的星云核心】右键唤醒，变成星云核心。"),
]

ZH["ME_WANDS"] = [
    (TEXT, "【杖柄】决定法杖容量。普通杖柄 100，杖芯（大法杖）250。\n"
           "【派系杖柄】拿在手上或放背包，每 5 秒自动回 5 点 vis。"),
    (TEXT, "【杖端】决定每次施法要花多少 vis：\n"
           "秘银 ×0.65（最省）｜加尔沃恩、魔古尔 ×0.8｜黑乌鲁克、精灵钢 ×0.9｜其余 ×0.95。"),
    (TEXT, "【大法杖】用杖芯装成，容量 250，要两只手拿。"),
]

ZH["ME_MATERIALS"] = [
    (TEXT, "【注入锭】十种金属锭，注魔台产物。\n"
           "【存储块】9 锭压成 1 块，也能拆回 9 锭。\n"
           "【粒】1 锭拆成 9 粒。"),
]

ZH["ME_JARS"] = [
    (TEXT, "【压缩精髓罐】存 2 倍 vis。\n"
           "【双重压缩精髓罐】存 4 倍。\n"
           "【群星精髓罐】存 8 倍。"),
    (TEXT, "三个罐子用法一样：普通右键把罐里的 vis 充进法杖，潜行右键把法杖的 vis 存进罐子。"),
]

ZH["ME_SUMMON"] = [
    (TEXT, "【基岩粉碎者】拿在手上对基岩右键：扣 10 气 / 10 土 / 5 秩序 / 20 混沌 vis，"
           "挖掉一格基岩。世界最底层的基岩挖不动。\n"
           "【扭曲修正之戒】装戒指位：一直带扭曲抗性，扭曲事件不再触发，法杖 vis 消耗 -5%。"),
]

# =====================================================================================
# English：同样只写功能
# =====================================================================================
EN = {
    "MERELIC_ROOT": [
        (TEXT, "This chapter only says what each item does. One line per item. Casting relics: "
               "hold and right-click. Accessories: put them in a Curios slot. \"Inventory\" means it "
               "works while carried."),
    ],
    "RELI_TOMES": [
        (TEXT, "[Rageous Missiles] Right-click: fires an explosive missile.\n"
               "[Crimson Spell] Right-click: orb that damages and weakens.\n"
               "[Eldritch Spell] Right-click: chaotic orb with an area hit.\n"
               "[Ghastly Skull] Right-click: life-stealing orb."),
        (TEXT, "[Lunar Flares] Right-click: drops 5 lunar flares at your crosshair.\n"
               "[Chaos Tome] Right-click: chaotic orb that veers off.\n"
               "[Soul Tome] Right-click: homing orb.\n"
               "[Void Grimoire] Right-click: dark matter orb that explodes."),
        (TEXT, "[Thunderpeal] Right-click: lightning at your crosshair.\n"
               "[Tome of Discord] Right-click: blink up to 48 blocks.\n"
               "[Dimensional Mirror] Right-click: return to your respawn point.\n"
               "[Apotheosis] Right-click: absorption, regeneration, resistance."),
        (TEXT, "[Fate Tome] Inventory: cancels one death, 5 minute cooldown."),
    ],
    "RELI_VIS": [
        (TEXT, "[Nebulous Core] Necklace, 250 vis. Right-click in hand to move vis between it and your "
               "wand, sneak to reverse.\n[Omega Core] Same, 2500 vis.\n[Oblivion Stone] Same, 25000 vis."),
        (TEXT, "[Void Devourer] Right-click an aura node to drain it into your wand.\n"
               "[Tome of Eternal Wisdom] Right-click stores XP, sneak + right-click gives it back."),
    ],
    "RELI_BAUBLES": [
        (TEXT, "[Ring of Seven Suns] Ring: fire and lava damage heals you, hits above 12 are ignored.\n"
               "[Superposition Ring] Ring: sometimes swaps places with another wearer."),
        (TEXT, "[Shiny Stone] Necklace: standing still regenerates you, faster the longer you stay.\n"
               "[Deific Amulet] Necklace: clears debuffs, puts out fire, vis for air when drowning."),
        (TEXT, "[Oblivion Amulet] Necklace: randomly curses you.\n"
               "[Ring of Discord] Ring; right-click in hand to blink (needs Tome of Discord)."),
        (TEXT, "[Mining Charm] Ring: +2 reach, +50% mining speed.\n"
               "[Advanced Mining Charm] Ring: +4 reach, +100% mining speed."),
    ],
    "RELI_WEAPONS": [
        (TEXT, "[Paradox] Left-click: 0~20 random damage, the excess hits you back.\n"
               "[Wastelayer] Same tier sword, no extra effect.\n"
               "[Terror Crown] Helmet: reveals aura nodes."),
    ],
    "RELI_ODDITIES": [
        (TEXT, "[False Justice] Inventory: all damage you deal and take is doubled.\n"
               "[Chaos Core] Inventory: damage taken becomes 0~2x random, random potion effects."),
        (TEXT, "[Tome of Predestiny] Right-click: pulls the creature under your crosshair to you.\n"
               "[Ancient Aegis] Belt: knockback immunity, 1 HP per second."),
        (TEXT, "[Runic Stone] Right-click while raining to clear the weather.\n"
               "[Edict of Eternal Banishment] Right-click a creature to banish it to the Nether.\n"
               "[Dormant Nebulous Core] Right-click to awaken it into a Nebulous Core."),
    ],
    "ME_WANDS": [
        (TEXT, "[Rod] Sets capacity. Normal rod 100, staff core 250.\n"
               "[Faction rod] Held or carried: regains 5 vis every 5 seconds."),
        (TEXT, "[Cap] Sets the vis cost multiplier: mithril 0.65 (cheapest), galvorn/morgul 0.8, "
               "black uruk/elven 0.9, the rest 0.95."),
        (TEXT, "[Staff] Built on a staff core: capacity 250, two-handed."),
    ],
    "ME_MATERIALS": [
        (TEXT, "[Infused ingot] Ten kinds of metal ingot from the infusion altar.\n"
               "[Storage block] 9 ingots to 1 block, and back.\n"
               "[Nugget] 1 ingot to 9 nuggets."),
    ],
    "ME_JARS": [
        (TEXT, "[Compressed jar] 2x vis.\n[Double compressed jar] 4x.\n[Extremis jar] 8x."),
        (TEXT, "All three work the same: right-click pours the jar into your wand, sneak + right-click "
               "stores your wand into the jar."),
    ],
    "ME_SUMMON": [
        (TEXT, "[Bedrock remover] Hold it and right-click bedrock: costs 10 aer / 10 terra / 5 ordo / "
               "20 perditio vis and removes one bedrock. The bottom layer stays.\n"
               "[Ring of Warp Fix] Ring slot: permanent Warp Ward (no warp events) and -5% vis cost."),
    ],
}

# 条目显示名与一句话简介（tc.research_name / tc.research_text）
ENTRY_NAME = {
    "MERELIC_ROOT": ("遗物速查", "Quick Reference"),
    "RELI_TOMES": ("施法遗物", "Casting Relics"),
    "RELI_VIS": ("vis 储具", "Vis Storage"),
    "RELI_BAUBLES": ("饰品遗物", "Relic Accessories"),
    "RELI_WEAPONS": ("武器与护甲", "Weapons and Armour"),
    "RELI_ODDITIES": ("奇异遗物", "Odd Relics"),
    "ME_WANDS": ("中土法杖", "Middle-earth Wands"),
    "ME_MATERIALS": ("注入金属", "Infused Metals"),
    "ME_JARS": ("精髓罐", "Essentia Jars"),
    "ME_SUMMON": ("焦点与饰品", "Foci and Trinkets"),
}
ENTRY_TEXT = {
    "MERELIC_ROOT": ("36 件遗物与中土法杖的用法速查", "What every relic and wand part does"),
    "RELI_TOMES": ("拿在手上右键施法的 13 件遗物", "13 relics you right-click"),
    "RELI_VIS": ("存 vis 的遗物", "Relics that store vis"),
    "RELI_BAUBLES": ("装进 Curios 饰品栏才生效的遗物", "Relics that need a Curios slot"),
    "RELI_WEAPONS": ("两把剑和一顶头盔", "Two swords and a helmet"),
    "RELI_ODDITIES": ("用法最不直观的几件遗物", "The least obvious relics"),
    "ME_WANDS": ("杖柄、杖端、大法杖各管什么", "What rods, caps and staves do"),
    "ME_MATERIALS": ("注入锭 / 存储块 / 粒", "Infused ingots, blocks, nuggets"),
    "ME_JARS": ("三种精髓罐的容量与用法", "The three essentia jars"),
    "ME_SUMMON": ("基岩粉碎者与扭曲修正之戒", "Bedrock remover and warp ring"),
}

# =====================================================================================
# tooltip 用法摘要
# =====================================================================================
ZH_USAGE = {
    "reli_false_justice": "放背包即可：你和敌人的伤害都翻倍",
    "reli_chaos_core": "放背包即可：受伤 0~2 倍随机，偶尔随机药水",
    "reli_dormant_arcanum": "右键唤醒成星云核心",
    "reli_telekinesis_tome": "右键把准星处的生物拽到身前",
    "reli_weather_stone": "下雨时右键放晴",
    "reli_overthrower": "右键把准星处的生物扔进地狱",
    "reli_discord_ring": "戒指位；右键闪现（需带纷争之书）",
    "reli_ancient_aegis": "腰带位：免疫击退 + 持续回血",
    "reli_shiny_stone": "项链位：站着不动会回血",
    "reli_deific_amulet": "项链位：清负面、灭火、溺水换气",
    "reli_oblivion_amulet": "项链位：随机给自己挂负面",
    "reli_mining_charm": "戒指位：交互距离 +2、挖掘 +50%",
    "reli_advanced_mining_charm": "戒指位：交互距离 +4、挖掘 +100%",
    "reli_dark_sun_ring": "戒指位：火伤变治疗，重击免疫",
    "reli_superposition_ring": "戒指位：偶尔与其他佩戴者换位",
    "reli_missile_tome": "右键发射爆炸飞弹",
    "reli_crimson_spell": "右键发射法球，伤害并虚弱",
    "reli_eldritch_spell": "右键发射混沌法球",
    "reli_ghastly_skull": "右键发射吸血弹",
    "reli_lunar_flares": "右键在准星处降下 5 发月辉弹",
    "reli_chaos_tome": "右键发射会乱飞的混沌弹",
    "reli_soul_tome": "右键发射追踪弹",
    "reli_void_grimoire": "右键发射黑洞弹，会爆炸",
    "reli_thunderpeal": "右键在准星处劈闪电",
    "reli_teleportation_tome": "右键瞬移到准星处",
    "reli_dimensional_mirror": "右键回到重生点",
    "reli_apotheosis": "右键加护盾、回血、抗性",
    "reli_fate_tome": "放背包即可：死亡时自动免死",
    "reli_obelisk_drainer": "右键灵气节点，把 vis 吸进法杖",
    "reli_xp_tome": "右键存经验，潜行右键取出",
    "reli_paradox": "左键伤害 0~20 随机，差额反噬自己",
    "reli_wastelayer": "与悖论同材质的剑，无额外效果",
    "reli_terror_crown": "戴头上：看见灵气节点，并诅咒准星 32 格内的生物",
}

EN_USAGE = {
    "reli_false_justice": "Inventory: all damage you deal and take is doubled",
    "reli_chaos_core": "Inventory: damage taken becomes 0~2x random",
    "reli_dormant_arcanum": "Right-click to awaken it",
    "reli_telekinesis_tome": "Right-click: pull the creature under your crosshair",
    "reli_weather_stone": "Right-click while raining to clear the sky",
    "reli_overthrower": "Right-click a creature to banish it",
    "reli_discord_ring": "Ring; right-click to blink (needs Tome of Discord)",
    "reli_ancient_aegis": "Belt: knockback immunity + regeneration",
    "reli_shiny_stone": "Necklace: stand still to regenerate",
    "reli_deific_amulet": "Necklace: clears debuffs and fire, vis for air",
    "reli_oblivion_amulet": "Necklace: randomly curses you",
    "reli_mining_charm": "Ring: +2 reach, +50% mining",
    "reli_advanced_mining_charm": "Ring: +4 reach, +100% mining",
    "reli_dark_sun_ring": "Ring: fire damage heals you",
    "reli_superposition_ring": "Ring: sometimes swaps with another wearer",
    "reli_missile_tome": "Right-click: explosive missile",
    "reli_crimson_spell": "Right-click: orb, damage + weakness",
    "reli_eldritch_spell": "Right-click: chaotic orb",
    "reli_ghastly_skull": "Right-click: life-stealing orb",
    "reli_lunar_flares": "Right-click: 5 lunar flares at your crosshair",
    "reli_chaos_tome": "Right-click: chaotic orb that veers off",
    "reli_soul_tome": "Right-click: homing orb",
    "reli_void_grimoire": "Right-click: dark matter orb that explodes",
    "reli_thunderpeal": "Right-click: lightning at your crosshair",
    "reli_teleportation_tome": "Right-click: blink to your crosshair",
    "reli_dimensional_mirror": "Right-click: return to your respawn point",
    "reli_apotheosis": "Right-click: absorption, regeneration, resistance",
    "reli_fate_tome": "Inventory: cancels one death",
    "reli_obelisk_drainer": "Right-click an aura node to drain it into your wand",
    "reli_xp_tome": "Right-click stores XP, sneak to release",
    "reli_paradox": "Left-click: 0~20 random damage, the excess hits you",
    "reli_wastelayer": "Same tier as Paradox, no extra effect",
    "reli_terror_crown": "Helmet: reveals aura, curses mobs you look at",
}

MSG_ZH = {
    "msg.thaumicdyesexplor.relics.no_wand": "背包里没有能用的法杖",
    "msg.thaumicdyesexplor.relics.not_enough_vis": "法杖里的 Vis 不足",
    "msg.thaumicdyesexplor.relics.no_target": "准星没有对准生物",
    "msg.thaumicdyesexplor.arcanum.awakened": "星云核心苏醒了",
    "msg.thaumicdyesexplor.weather_stone.no_rain": "现在没有下雨",
    "msg.thaumicdyesexplor.overthrower.banished": "%s 已被放逐到地狱",
    "msg.thaumicdyesexplor.discord_ring.no_tome": "背包里需要有纷争之书",
}
MSG_EN = {
    "msg.thaumicdyesexplor.relics.no_wand": "No usable wand in your inventory",
    "msg.thaumicdyesexplor.relics.not_enough_vis": "Not enough Vis in your wand",
    "msg.thaumicdyesexplor.relics.no_target": "No creature under your crosshair",
    "msg.thaumicdyesexplor.arcanum.awakened": "The Nebulous Core awakens",
    "msg.thaumicdyesexplor.weather_stone.no_rain": "It is not raining",
    "msg.thaumicdyesexplor.overthrower.banished": "%s has been banished to the Nether",
    "msg.thaumicdyesexplor.discord_ring.no_tome": "You need a Tome of Discord in your inventory",
}

TAB_ZH = "神秘时代：中土遗物"
TAB_EN = "Thaumcraft: Middle-earth Relics"

NEW_ENTRY = {
    "key": "%s:RELI_ODDITIES" % NS,
    "category": "%s:MERELIC" % NS,
    "aspects": {},
    "display_column": 1,
    "display_row": 2,
    "complexity": 1,
    "icon": {"type": "STACK", "stack": {"id": "%s:reli_false_justice" % NS}},
    "flags": ["AUTO_UNLOCK"],
    "parents": ["%s:MERELIC_ROOT" % NS],
    "hidden_parents": [],
    "siblings": [],
    "object_triggers": [],
    "entity_triggers": [],
    "aspect_triggers": [],
    "warp": 0,
    "pages": [],
}


def build_pages(entry, spec):
    pages = []
    index = 0
    for kind, _value in spec:
        if kind == TEXT:
            index += 1
            pages.append(page(TEXT, "%s.research_page.%s.%d" % (NS, entry, index)))
        else:
            pages.append(page(kind))
    return pages, index


# 书里能用、且任何字体都画得出来的替换（第 23 轮）：
#   —— → ，    → → ->     −（真减号）→ -     ｜（全角竖线）→ ，
# 这几类符号在部分字体包里会画成方框，看起来就是「乱码」。
GLYPHS = (("——", "，"), ("→", "->"), ("−", "-"), ("｜", "，"))


def tidy_page(text, item_prefix):
    """统一符号，并让「每条物品」之间空一行（只是多一点点间距，不换页）。"""
    for bad, good in GLYPHS:
        text = text.replace(bad, good)
    text = re.sub(r"\n{2,}", "\n", text)              # 先归一，保证可重复执行
    text = re.sub(r"\n(?=" + re.escape(item_prefix) + ")", "\n\n", text)
    return text


def main():
    with open(DATA, encoding="utf-8") as fh:
        research = json.load(fh)
    with open(LANG_ZH, encoding="utf-8") as fh:
        zh = json.load(fh)
    with open(LANG_EN, encoding="utf-8") as fh:
        en = json.load(fh)

    entries = {e["key"].split(":")[1]: e for e in research["entries"]}

    if "RELI_ODDITIES" not in entries:
        entry = dict(NEW_ENTRY)
        entry["pages"] = []
        research["entries"].append(entry)
        entries["RELI_ODDITIES"] = entry

    for key in list(ZH):
        entry = entries[key]
        pages, count = build_pages(key, ZH[key])
        entry["pages"] = pages
        _, en_count = build_pages(key, EN[key])
        assert count == en_count, (key, count, en_count)

    with open(DATA, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(research, fh, ensure_ascii=False, indent=2)
        fh.write("\n")

    # 重新生成页文本：先清掉本脚本负责的条目，再写；孤儿键天然被清掉
    prefixes = tuple("%s.research_page.%s" % (NS, key) for key in ZH)
    for table in (zh, en):
        for key in [k for k in table if k.startswith(prefixes)]:
            del table[key]

    for key, spec in ZH.items():
        index = 0
        for kind, value in spec:
            if kind == TEXT:
                index += 1
                zh["%s.research_page.%s.%d" % (NS, key, index)] = value
    for key, spec in EN.items():
        index = 0
        for kind, value in spec:
            if kind == TEXT:
                index += 1
                en["%s.research_page.%s.%d" % (NS, key, index)] = value

    # 条目名 / 简介（书里标题和副标题），缺一个就会在界面上显示原始键
    for key, (nz, ne) in ENTRY_NAME.items():
        full = "%s:%s" % (NS, key)
        zh["tc.research_name." + full] = nz
        en["tc.research_name." + full] = ne
        zh["tc.research_text." + full] = ENTRY_TEXT[key][0]
        en["tc.research_text." + full] = ENTRY_TEXT[key][1]

    for item, text in ZH_USAGE.items():
        zh["tooltip.%s.usage.%s" % (NS, item)] = text
    for item, text in EN_USAGE.items():
        en["tooltip.%s.usage.%s" % (NS, item)] = text
    for key, text in MSG_ZH.items():
        zh[key] = text
    for key, text in MSG_EN.items():
        en[key] = text

    zh["itemGroup.%s.middle_earth" % NS] = TAB_ZH
    en["itemGroup.%s.middle_earth" % NS] = TAB_EN

    # ---- 孤儿键清理：任何 tc.research_*/页面键只要没有对应条目就删掉 ----
    live = {"%s:%s" % (NS, e["key"].split(":")[1]) for e in research["entries"]}
    cats = {c["key"] for c in research["categories"]}
    for table in (zh, en):
        for key in list(table):
            for prefix in ("tc.research_name.", "tc.research_text."):
                if key.startswith(prefix):
                    target = key[len(prefix):]
                    if target not in live and target not in cats:
                        del table[key]
                    break
            # 分类键：分类在第 16 轮合并过，旧的 ME / RELI 分类名必须一起清掉
            prefix = "tc.research_category."
            if key.startswith(prefix) and key[len(prefix):] not in cats:
                del table[key]
    used = set()
    for e in research["entries"]:
        for pg in e["pages"]:
            if pg["type"] == TEXT and pg["value"]:
                used.add(pg["value"])
    for table in (zh, en):
        for key in [k for k in table if k.startswith("tc.research_page.")]:
            if key not in used:
                del table[key]

    # ---------------- 排版整理：符号 + 条目间距 ----------------
    for table, prefix in ((zh, "【"), (en, "[")):
        for key in [k for k in table if k.startswith("thaumicdyesexplor.research_page.")]:
            table[key] = tidy_page(table[key], prefix)

    for path, table in ((LANG_ZH, zh), (LANG_EN, en)):
        ordered = dict(sorted(table.items()))
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(ordered, fh, ensure_ascii=False, indent=2)
            fh.write("\n")

    # ---------------- 校验 ----------------
    problems = []
    recipe_dirs = [os.path.join(RES, "data", NS, "recipe", sub)
                   for sub in ("crafting", "infusion", "arcane")]
    existing = set()
    for folder in recipe_dirs:
        for name in os.listdir(folder):
            existing.add("%s:%s/%s" % (NS, os.path.basename(folder), name[:-5]))
    for entry in research["entries"]:
        for pg in entry["pages"]:
            for rid in pg.get("recipe_ids") or []:
                if rid not in existing:
                    problems.append("缺少配方 %s（被 %s 引用）" % (rid, entry["key"]))
    for entry in research["entries"]:
        for prefix in ("tc.research_name.", "tc.research_text."):
            k = prefix + entry["key"]
            if k not in zh:
                problems.append("缺中文键 " + k)
            if k not in en:
                problems.append("缺英文键 " + k)
    for item in ZH_USAGE:
        for table, tag in ((zh, "zh"), (en, "en")):
            if ("tooltip.%s.usage.%s" % (NS, item)) not in table:
                problems.append("缺 %s tooltip 键 %s" % (tag, item))

    pages = sum(len(e["pages"]) for e in research["entries"])
    print("条目 %d 个 / 页面 %d 页 / 配方 %d 条 / tooltip %d 条"
          % (len(research["entries"]), pages, len(existing), len(ZH_USAGE)))
    if problems:
        print("\n".join(problems))
        raise SystemExit(1)
    print("校验通过")


if __name__ == "__main__":
    main()
