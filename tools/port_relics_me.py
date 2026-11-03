# -*- coding: utf-8 -*-
"""
迁移 Forgotten-Relics 与 MiddleEarth-Thaumaturgy 的物品资源到本模组。

做四件事：
1. 复制两个源模组的物品贴图到 assets/thaumicdyesexplor/textures/item/，
   文件名统一改为新物品 id（reli_* / me_*，全小写）。
2. 缺失的贴图（源模组本身没有，如 BREE 派系的杖柄/杖芯/召唤焦点）
   用刚铎贴图乘上 WandType 里 BREE 的派系色生成占位。
3. 为每个新物品生成 item/generated 模型 JSON。
4. 把中英文名合并进 lang/zh_cn.json 与 lang/en_us.json
   （中文名直接读源模组 zh_CN.lang，英文名读 en_US.lang，保证与源模组叫法一致）。

运行：python tools/port_relics_me.py
"""
import json
import os
import shutil
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FR = os.path.join(ROOT, ".none", "Forgotten-Relics", "src", "main", "resources", "assets", "forgottenrelics")
ME = os.path.join(ROOT, ".none", "MiddleEarth-Thaumaturgy", "src", "main", "resources", "assets", "middleearththaumaturgy")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "thaumicdyesexplor")
ITEM_TEX = os.path.join(ASSETS, "textures", "item")
ARMOR_TEX = os.path.join(ASSETS, "textures", "models", "armor")
MODELS = os.path.join(ASSETS, "models", "item")
WAND_MODELS = os.path.join(ASSETS, "textures", "models")

os.makedirs(ITEM_TEX, exist_ok=True)
os.makedirs(ARMOR_TEX, exist_ok=True)
os.makedirs(MODELS, exist_ok=True)
os.makedirs(WAND_MODELS, exist_ok=True)

report = {"copied": [], "placeholder": [], "missing": []}


def copy(src, dst):
    shutil.copyfile(src, dst)
    report["copied"].append(os.path.basename(dst))


def tint(src, color, dst):
    """把贴图乘上一个 RGB 颜色，生成占位贴图。"""
    im = Image.open(src).convert("RGBA")
    r, g, b = [(color >> s) & 0xFF for s in (16, 8, 0)]
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            pr, pg, pb, pa = px[x, y]
            px[x, y] = (pr * r // 255, pg * g // 255, pb * b // 255, pa)
    im.save(dst)
    report["placeholder"].append(os.path.basename(dst))


def item_model(item_id):
    path = os.path.join(MODELS, item_id + ".json")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump({"parent": "minecraft:item/generated",
                   "textures": {"layer0": "thaumicdyesexplor:item/" + item_id}}, f, indent=2)
        f.write("\n")


# =====================================================================================
# Forgotten-Relics（reli_ 前缀）
# =====================================================================================
# (新物品 id, 源贴图文件名, 中文名, 源语言键或None)
FR_ITEMS = [
    ("reli_false_justice", "False_Justice", "虚妄正义", "ItemFalseJustice"),
    ("reli_deific_amulet", "Deific_Amulet", "神性护符", "ItemDeificAmulet"),
    ("reli_paradox", "Paradox", "悖论", "ItemParadox"),
    ("reli_oblivion_amulet", "Oblivion_Amulet", "遗忘护符", "ItemOblivionAmulet"),
    ("reli_arcanum", "Nebulous_Core", "星云核心", "ItemArcanum"),
    ("reli_dormant_arcanum", "Nebulous_Core_Dormant", "沉睡的星云核心", "ItemDormantArcanum"),
    ("reli_fate_tome", "Tome_of_Broken_Fates", "破碎命运之书", "ItemFateTome"),
    ("reli_dark_sun_ring", "Dark_Sun_Ring", "七阳之戒", "ItemDarkSunRing"),
    ("reli_chaos_core", "Chaos_Core", "混沌核心", "ItemChaosCore"),
    ("reli_mining_charm", "Mining_Charm", "采矿护符", "ItemMiningCharm"),
    ("reli_advanced_mining_charm", "Advanced_Mining_Charm", "灵虚采矿护符", "ItemAdvancedMiningCharm"),
    ("reli_telekinesis_tome", "Telekinesis_Tome", "预定天命之书", "ItemTelekinesisTome"),
    ("reli_ancient_aegis", "Ancient_Aegis", "远古神盾", "ItemAncientAegis"),
    ("reli_missile_tome", "Missile_Tome", "核怒之书", "ItemMissileTome"),
    ("reli_crimson_spell", "Crimson_Spell", "绯红法咒", "ItemCrimsonSpell"),
    ("reli_ghastly_skull", "Ghastly_Skull", "厉鬼头骨", None),
    ("reli_obelisk_drainer", "Obelisk_Drainer", "虚空吞噬者", "ItemObeliskDrainer"),
    ("reli_eldritch_spell", "Eldritch_Spell", "咒异法咒", "ItemEldritchSpell"),
    ("reli_lunar_flares", "Lunar_Flares", "月辉之书", "ItemLunarFlares"),
    ("reli_teleportation_tome", "Teleportation_Tome", "纷争之书", "ItemTeleportationTome"),
    ("reli_shiny_stone", "Shiny_Stone", "闪亮之石", "ItemShinyStone"),
    ("reli_superposition_ring", "Superposition_Ring", "叠加之戒", "ItemSuperpositionRing"),
    ("reli_soul_tome", "Soul_Tome", "千魂咒令", "ItemSoulTome"),
    ("reli_dimensional_mirror", "Dimensional_Mirror", "位面之镜", "ItemDimensionalMirror"),
    ("reli_weather_stone", "Weather_Stone", "符文石", "ItemWeatherStone"),
    ("reli_apotheosis", "Apotheosis", "封神之作", "ItemApotheosis"),
    ("reli_chaos_tome", "Chaos_Tome", "原初混沌之书", "ItemChaosTome"),
    ("reli_omega_core", "Omega_Core", "欧米茄核心", "ItemOmegaCore"),
    ("reli_xp_tome", "XP_Tome", "永恒智慧之书", "ItemXPTome"),
    ("reli_thunderpeal", "Thunderpeal", "雷鸣", "ItemThunderpeal"),
    ("reli_terror_crown", "Terror_Crown", "恐惧王冠", "ItemTerrorCrown"),
    ("reli_wastelayer", "Wastelayer", "荒芜铺展者", "ItemWastelayer"),
    ("reli_overthrower", "Just_Stop_Blaming_Me_For_Stealing_Calamity_Textures_Already",
     "永恒放逐咒令", "ItemOverthrower"),
    ("reli_discord_ring", "Discord_Ring", "纷争之戒", "ItemDiscordRing"),
    ("reli_void_grimoire", "Void_Grimoire", "深渊魔典", "ItemVoidGrimoire"),
    ("reli_oblivion_stone", "Oblivion_Stone", "遗忘之基石", "ItemOblivionStone"),
]


def parse_lang(path):
    d = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, _, val = line.partition("=")
            d[key.strip()] = val.strip()
    return d


FR_EN = parse_lang(os.path.join(FR, "lang", "en_US.lang"))
ME_ZH = parse_lang(os.path.join(ME, "lang", "zh_CN.lang"))
ME_EN = parse_lang(os.path.join(ME, "lang", "en_US.lang"))

fr_lang_zh, fr_lang_en = {}, {}
for item_id, tex, zh, key in FR_ITEMS:
    src = os.path.join(FR, "textures", "items", tex + ".png")
    if not os.path.isfile(src):
        report["missing"].append("FR " + tex)
        continue
    copy(src, os.path.join(ITEM_TEX, item_id + ".png"))
    mcmeta = src + ".mcmeta"
    if os.path.isfile(mcmeta):
        shutil.copyfile(mcmeta, os.path.join(ITEM_TEX, item_id + ".png.mcmeta"))
    item_model(item_id)
    fr_lang_zh["item.thaumicdyesexplor." + item_id] = zh
    fr_lang_en["item.thaumicdyesexplor." + item_id] = (
        FR_EN.get("item.%s.name" % key) if key else None) or tex.replace("_", " ")

# 恐惧王冠的护甲层贴图（源文件就是标准 64x32 人形布局）
copy(os.path.join(FR, "textures", "armor", "crown_prs.png"),
     os.path.join(ARMOR_TEX, "terror_crown_layer_1.png"))

# =====================================================================================
# MiddleEarth-Thaumaturgy（me_ 前缀）
# =====================================================================================
FACTIONS = [  # (WandType.types, WandType.typesName, 中文名后缀见 lang)
    ("GONDOR", "gondor"), ("HOBBIT", "hobbit"), ("RANGER_NORTH", "ranger_north"),
    ("BLUE_MOUNTAINS", "blue_mountains"), ("HIGH_ELF", "high_elf"), ("GUNDABAD", "gundabad"),
    ("ANGMAR", "angmar"), ("WOOD_ELF", "wood_elf"), ("DOL_GULDUR", "dol_guldur"),
    ("DALE", "dale"), ("DWARF", "dwarf"), ("GALADHRIM", "galadhrim"),
    ("DUNLAND", "dunland"), ("URUK_HAI", "uruk_hai"), ("FANGORN", "fangorn"),
    ("ROHAN", "rohan"), ("MORDOR", "mordor"), ("DORWINION", "dorwinion"),
    ("RHUN", "rhun"), ("NEAR_HARAD", "near_harad"), ("MOREDAIN", "moredain"),
    ("TAUREDAIN", "tauredain"), ("HALF_TROLL", "half_troll"), ("BREE", "bree"),
]
BREE_COLOR = 0x2B681C  # WandType.color[23]

ME_TEX = os.path.join(ME, "textures", "items")
me_lang_zh, me_lang_en = {}, {}


def me_entry(item_id, src_png, lang_key_zh, lang_key_en, fallback_en):
    src = os.path.join(ME_TEX, src_png)
    if os.path.isfile(src):
        copy(src, os.path.join(ITEM_TEX, item_id + ".png"))
        if os.path.isfile(src + ".mcmeta"):
            shutil.copyfile(src + ".mcmeta", os.path.join(ITEM_TEX, item_id + ".png.mcmeta"))
    else:
        report["missing"].append("ME " + src_png)
    item_model(item_id)
    zh = ME_ZH.get(lang_key_zh)
    en = ME_EN.get(lang_key_en)
    if zh:
        me_lang_zh["item.thaumicdyesexplor." + item_id] = zh
    else:
        report["missing"].append("lang zh " + lang_key_zh)
    if en:
        me_lang_en["item.thaumicdyesexplor." + item_id] = en
    else:
        me_lang_en["item.thaumicdyesexplor." + item_id] = fallback_en
        report["missing"].append("lang en " + lang_key_en)


# —— 派系杖柄 / 杖芯 / 召唤焦点（各 24 派系；BREE 源模组就没画贴图，用刚铎占位染色）——
for ftype, fname in FACTIONS:
    # 杖柄
    src = os.path.join(ME_TEX, "wand_rod_%s.png" % ftype)
    dst = os.path.join(ITEM_TEX, "me_wand_core_%s.png" % fname)
    if os.path.isfile(src):
        copy(src, dst)
    else:
        tint(os.path.join(ME_TEX, "wand_rod_GONDOR.png"), BREE_COLOR, dst)
        report["placeholder"].append(dst)
    item_model("me_wand_core_%s" % fname)
    me_lang_zh["item.thaumicdyesexplor.me_wand_core_" + fname] = ME_ZH["item.WandCores.%s.name" % ftype]
    me_lang_en["item.thaumicdyesexplor.me_wand_core_" + fname] = ME_EN["item.WandCores.%s.name" % ftype]

    # 杖芯
    src = os.path.join(ME_TEX, "staff_rod_%s.png" % ftype)
    dst = os.path.join(ITEM_TEX, "me_staff_core_%s.png" % fname)
    if os.path.isfile(src):
        copy(src, dst)
    else:
        tint(os.path.join(ME_TEX, "staff_rod_GONDOR.png"), BREE_COLOR, dst)
    item_model("me_staff_core_%s" % fname)
    me_lang_zh["item.thaumicdyesexplor.me_staff_core_" + fname] = ME_ZH["item.StaffCores.%s.name" % ftype]
    me_lang_en["item.thaumicdyesexplor.me_staff_core_" + fname] = ME_EN["item.StaffCores.%s.name" % ftype]

    # 召唤焦点
    src = os.path.join(ME_TEX, "focus_summon_%s.png" % ftype)
    dst = os.path.join(ITEM_TEX, "me_summon_focus_%s.png" % fname)
    if os.path.isfile(src):
        copy(src, dst)
    else:
        tint(os.path.join(ME_TEX, "focus_summon_GONDOR.png"), BREE_COLOR, dst)
    item_model("me_summon_focus_%s" % fname)
    me_lang_zh["item.thaumicdyesexplor.me_summon_focus_" + fname] = ME_ZH["item.focus_summon.%s.name" % ftype]
    me_lang_en["item.thaumicdyesexplor.me_summon_focus_" + fname] = ME_EN.get(
        "item.focus_summon.%s.name" % ftype, "Wand Focus: Summon %s" % ftype.title().replace("_", " "))

# —— 杖端（10 种，带动画 mcmeta）——
CAPS = ["mithril", "orcsteel", "dwarvensteel", "galvorn", "uruksteel",
        "morgulsteel", "bluedwarvensteel", "blackuruksteel", "elvensteel", "gildediron"]
for cap in CAPS:
    me_entry("me_wand_cap_" + cap, "wand_cap_%s.png" % cap,
             "item.WandCaps.%s.name" % cap, "item.WandCaps.%s.name" % cap, "Wand Cap: " + cap)

# —— 材料 / 粒 / 杂项（源 ItemMaterial 的 13 个子项 + 10 粒）——
MATS = ["guldurilBlackUruk", "ithildinBlueDwarven", "ithildinDwarven", "edhelmirElven",
        "daedelosGalvorn", "staredMithril", "daedelosMorgul", "durnaurOrc",
        "guldurilUruk", "brightenGildediron"]
for m in MATS:
    fid = "".join("_" + c.lower() if c.isupper() else c for c in m)
    me_entry("me_material" + fid, m + ".png",
             "item.Materials.%s.name" % m, "item.Materials.%s.name" % m, m)
    me_entry("me_nugget" + fid, "nugget_" + m + ".png",
             "item.MaterialNuggets.%s.name" % m, "item.MaterialNuggets.%s.name" % m, m)
me_entry("me_jar_binder", "jarBinder.png", "item.Materials.jarBinder.name",
         "item.Materials.jarBinder.name", "Crystallized Jar Binder")
me_entry("me_jar_binder_advanced", "jarBinderAdvanced.png", "item.Materials.jarBinderAdvanced.name",
         "item.Materials.jarBinderAdvanced.name", "Mystical Jar Binder")
me_entry("me_extreme_jar_cap", "extremeJarCap.png", "item.Materials.extremeJarCap.name",
         "item.Materials.extremeJarCap.name", "Jar Cap of Every Star")

# —— 其余单件 ——
me_entry("me_focus_bedrock_remover", "focus_bedrock_remover.png",
         "item.focus_bedrock_remover.name", "item.focus_bedrock_remover.name", "Wand Focus: Bedrock Shatterer")
me_entry("me_ring_warpward", "ItemRingWarpward.png",
         "item.ItemRingWarpward.name", "item.ItemRingWarpward.name", "Ring of Warp Fix")
me_entry("me_handheld_crafter", "handheldCrafter.png",
         "item.HandheldCrafter.name", "item.HandheldCrafter.name", "Handheld Factions Crafter")
me_entry("me_icon", "iconMiddleEarthThaumaturgy.png",
         "item.iconMiddleEarthThaumaturgy.name", "item.iconMiddleEarthThaumaturgy.name",
         "Middle-Earth Thaumaturgy Icon")

# —— 法杖模型贴图（后续做 datamap 时要用，一并搬好）——
for ftype, fname in FACTIONS:
    src = os.path.join(ME, "textures", "models", "wand_rod_%s.png" % fname)
    dst = os.path.join(WAND_MODELS, "me_wand_rod_%s.png" % fname)
    if os.path.isfile(src):
        copy(src, dst)
    else:
        tint(os.path.join(ME, "textures", "models", "wand_rod_gondor.png"), BREE_COLOR, dst)
for cap in CAPS:
    src = os.path.join(ME, "textures", "models", "wand_cap_%s.png" % cap)
    dst = os.path.join(WAND_MODELS, "me_wand_cap_%s.png" % cap)
    if os.path.isfile(src):
        copy(src, dst)

# =====================================================================================
# 语言文件合并
# =====================================================================================
for lang, extra in (("zh_cn", fr_lang_zh | me_lang_zh), ("en_us", fr_lang_en | me_lang_en)):
    path = os.path.join(ASSETS, "lang", lang + ".json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    before = len(data)
    data.update(extra)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
        f.write("\n")
    print("%s: %d -> %d keys" % (lang, before, len(data)))

print("\n贴图复制 %d 张，占位生成 %d 张，缺失 %d 项" % (
    len(report["copied"]), len(report["placeholder"]), len(report["missing"])))
for m in report["missing"]:
    print("  MISSING:", m)
total = len(FR_ITEMS) + 24 * 3 + len(CAPS) + len(MATS) * 2 + 3 + 4
print("新增物品总数: %d (FR %d + ME %d)" % (
    total, len(FR_ITEMS), 24 * 3 + len(CAPS) + len(MATS) * 2 + 3 + 4))
