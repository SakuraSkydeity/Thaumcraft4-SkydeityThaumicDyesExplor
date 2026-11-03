# -*- coding: utf-8 -*-
"""
迁移 MiddleEarth-Thaumaturgy 的要素系统到本模组（数据包驱动）。

做四件事：
1. 生成 38 个中洲要素定义 JSON 到 data/thaumicdyesexplor/thaumcraft/aspects/<latin>.json
   （前置的重载监听目录是 "thaumcraft/aspects"，文件 id 即要素 id）。
   组合要素引用原版 TC4 要素用拉丁 id（GREED=lucrum 等），引用中洲要素用本模组命名空间。
2. 复制要素贴图（taurethrim 的贴图在源模组叫 tauredain.png，JSON 里显式写 texture）。
3. 合并要素名语言键（aspect.thaumicdyesexplor.<path>）。
4. 生成 ME 物品的要素分配 object_aspects/me_items.json
   （杖柄/杖芯带派系要素，材料带 metallum，召唤焦点带派系+咒术等）。

运行：python tools/port_me_aspects.py
"""
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ME_ASPECT_TEX = os.path.join(ROOT, ".none", "MiddleEarth-Thaumaturgy", "src", "main", "resources",
                             "assets", "middleearththaumaturgy", "textures", "aspects")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "thaumicdyesexplor", "thaumcraft")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "thaumicdyesexplor")
NS = "thaumicdyesexplor"

# (拉丁 id, 颜色, 组合要素, 自然生成权重, 中文名, 英文名, 贴图覆盖或 None)
# 组合要素里没有冒号的是 TC4 原版拉丁 id，加 ME: 前缀的是中洲要素（展开成本模组命名空间）。
TC = "thaumcraft"
ME = NS + ":"
ASPECTS = [
    # —— 种族要素 ——
    ("alfirin",    "#4bf4f4", [TC + ":permutatio", TC + ":praecantatio"], 1, "阿尔费林（不朽）", "Alfirin", None),
    ("abonnen",    "#f2d18e", [TC + ":humanus", TC + ":lucrum"], 1, "阿邦嫩（人类）", "Abonnen", None),
    ("edhel",      "#eac0e5", [TC + ":humanus", ME + "alfirin"], 1, "埃达（精灵）", "Edhel", None),
    ("nauglin",    "#afb9c9", [TC + ":humanus", TC + ":perfodio"], 1, "瑙格林（矮人）", "Nauglin", None),
    ("orchoth",    "#47775a", [ME + "edhel", TC + ":mortuus"], 771, "奥克辛（奥克）", "Orchoth", None),
    ("perian",     "#ed6136", [TC + ":humanus", TC + ":sensus"], 1, "佩里安（霍比特）", "Perian", None),
    ("mornogol",   "#726e6d", [ME + "orchoth", TC + ":humanus"], 771, "魔诺戈（乌鲁克）", "Mornogol", None),
    ("torog",      "#492b22", [ME + "orchoth", TC + ":tenebrae"], 771, "托罗格（食人妖）", "Torog", None),
    ("onodrim",    "#17661c", [TC + ":arbor", TC + ":motus"], 1, "欧诺德林（树人）", "Onodrim", None),
    ("uhorm",      "#ba8723", [TC + ":arbor", TC + ":bestia"], 1, "乌霍尔（树胡）", "Uhorm", None),
    ("draugol",    "#605341", [TC + ":bestia", TC + ":telum"], 771, "德劳戈（座狼）", "Draugol", None),
    ("valaraukar", "#aa0606", [ME + "alfirin", TC + ":alienis"], 771, "瓦拉劳卡（炎魔）", "Valaraukar", None),
    ("ithryn",     "#dddddd", [ME + "alfirin", TC + ":praecantatio"], 1, "伊斯林（巫师）", "Ithryn", None),
    # —— 派系要素 ——
    ("shire",      "#51ff57", [ME + "perian", TC + ":messis"], 1, "夏尔", "Shire", None),
    ("dunedain",   "#2b4f2c", [ME + "abonnen", TC + ":volatus"], 1, "杜内丹", "Dunedain", None),
    ("eredluin",   "#4794cc", [ME + "nauglin", TC + ":gelum"], 1, "埃瑞德路因", "Ered Luin", None),
    ("lindon",     "#adf4f4", [ME + "edhel", TC + ":aqua"], 1, "林顿", "Lindon", None),
    ("gundabad",   "#a87443", [ME + "orchoth", TC + ":venenum"], 1, "贡达巴德", "Gundabad", None),
    ("angmar",     "#729176", [ME + "orchoth", TC + ":vitium"], 1, "安格玛", "Angmar", None),
    ("druardh",    "#1ca52c", [ME + "edhel", TC + ":arbor"], 1, "德鲁阿德（森林精灵）", "Druardh", None),
    ("dolguldur",  "#12122d", [ME + "orchoth", TC + ":spiritus"], 1, "多古尔都", "Dol Guldur", None),
    ("dale",       "#db9655", [ME + "abonnen", TC + ":permutatio"], 1, "河谷邦", "Dale", None),
    ("angdol",     "#3f5887", [ME + "nauglin", TC + ":perfodio"], 1, "安格多（铁丘）", "Angdol", None),
    ("lothlorien", "#f2e479", [ME + "edhel", TC + ":auram"], 1, "罗斯洛立安", "Lothlorien", None),
    ("dunland",    "#af9f99", [ME + "abonnen", TC + ":perditio"], 1, "黑蛮地", "Dunland", None),
    ("isengard",   "#353434", [ME + "mornogol", TC + ":praecantatio"], 1, "艾森加德", "Isengard", None),
    ("fangorn",    "#7dea59", [ME + "onodrim", TC + ":arbor"], 1, "范贡", "Fangorn", None),
    ("rohan",      "#bc7709", [ME + "abonnen", TC + ":bestia"], 1, "洛汗", "Rohan", None),
    ("gondor",     "#e8e6e3", [ME + "abonnen", TC + ":ordo"], 1, "刚铎", "Gondor", None),
    ("mordor",     "#2b1501", [ME + "orchoth", TC + ":telum"], 1, "魔多", "Mordor", None),
    ("dorwinion",  "#751d63", [ME + "abonnen", ME + "edhel"], 1, "多温尼安", "Dorwinion", None),
    ("rhudel",     "#ceab5f", [ME + "abonnen", TC + ":fames"], 1, "鲁德耳", "Rhudel", None),
    ("harad",      "#ed310b", [ME + "abonnen", TC + ":ignis"], 1, "哈拉德", "Near Harad", None),
    ("morwaith",   "#e0b869", [ME + "abonnen", TC + ":venenum"], 1, "墨怀斯", "Morwaith", None),
    ("taurethrim", "#1d6038", [ME + "abonnen", TC + ":arbor"], 1, "陶瑞斯", "Taurethrim", "tauredain.png"),
    ("pertorog",   "#8c7f5b", [ME + "abonnen", ME + "torog"], 1, "佩尔托罗格（半食人妖）", "Pertorog", None),
    ("utumno",     "#2d0404", [ME + "valaraukar", TC + ":alienis"], 1, "乌图姆诺", "Utumno", None),
    ("nazgul",     "#073513", [ME + "angmar", TC + ":spiritus"], 1, "戒灵", "Nazgul", None),
    ("bree",       "#2b681c", [ME + "abonnen", ME + "perian"], 1, "布理", "Bree", None),
]

os.makedirs(os.path.join(DATA, "aspects"), exist_ok=True)
os.makedirs(os.path.join(ASSETS, "textures", "aspects"), exist_ok=True)

copied = 0
lang_zh, lang_en = {}, {}
for latin, color, components, weight, zh, en, tex in ASPECTS:
    definition = {
        "id": NS + ":" + latin,
        "color": color,
        "components": components,
        "natural_generation_weight": weight,
    }
    if tex:
        definition["texture"] = NS + ":textures/aspects/" + tex
    with open(os.path.join(DATA, "aspects", latin + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(definition, f, ensure_ascii=False, indent=2)
        f.write("\n")
    # 贴图
    tex_src = os.path.join(ME_ASPECT_TEX, (tex or latin + ".png"))
    tex_dst = os.path.join(ASSETS, "textures", "aspects", (tex or latin + ".png"))
    if os.path.isfile(tex_src):
        shutil.copyfile(tex_src, tex_dst)
        copied += 1
    lang_zh["aspect.%s.%s" % (NS, latin)] = zh
    lang_en["aspect.%s.%s" % (NS, latin)] = en

# =====================================================================================
# ME 物品的要素分配（object_aspects）
# =====================================================================================
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
MATERIALS = ["gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven", "edhelmir_elven",
             "daedelos_galvorn", "stared_mithril", "daedelos_morgul", "durnaur_orc",
             "gulduril_uruk", "brighten_gildediron"]
CAPS = ["mithril", "orcsteel", "dwarvensteel", "galvorn", "uruksteel",
        "morgulsteel", "bluedwarvensteel", "blackuruksteel", "elvensteel", "gildediron"]

direct = {}
for faction, aspect in FACTION_ASPECT.items():
    direct[NS + ":me_wand_core_" + faction] = {ME + aspect: 4, TC + ":arbor": 4}
    direct[NS + ":me_staff_core_" + faction] = {ME + aspect: 8, TC + ":arbor": 8}
    direct[NS + ":me_summon_focus_" + faction] = {ME + aspect: 8, TC + ":praecantatio": 4, TC + ":telum": 2}
for cap in CAPS:
    direct[NS + ":me_wand_cap_" + cap] = {TC + ":metallum": 4, TC + ":tutamen": 2}
for mat in MATERIALS:
    faction_aspect = None
    for faction, aspect in FACTION_ASPECT.items():
        if faction in mat:
            faction_aspect = aspect
            break
    entry = {TC + ":metallum": 4}
    if faction_aspect:
        entry[ME + faction_aspect] = 2
    direct[NS + ":me_material_" + mat] = entry
    direct[NS + ":me_nugget_" + mat] = {TC + ":metallum": 1}
direct[NS + ":me_jar_binder"] = {TC + ":vitium": 2, TC + ":vitreus": 2}
direct[NS + ":me_jar_binder_advanced"] = {TC + ":vitium": 4, TC + ":vitreus": 4, TC + ":praecantatio": 2}
direct[NS + ":me_extreme_jar_cap"] = {TC + ":vitreus": 4, TC + ":vitium": 2, TC + ":ordo": 2}
direct[NS + ":me_focus_bedrock_remover"] = {TC + ":perfodio": 8, TC + ":terra": 4, TC + ":potentia": 4}
direct[NS + ":me_ring_warpward"] = {TC + ":vinculum": 4, TC + ":vitium": 2, TC + ":praecantatio": 2}
direct[NS + ":me_handheld_crafter"] = {TC + ":fabrico": 6, TC + ":instrumentum": 4, TC + ":humanus": 2}
direct[NS + ":me_icon"] = {TC + ":praecantatio": 2}

with open(os.path.join(DATA, "object_aspects", "me_items.json"), "w", encoding="utf-8", newline="\n") as f:
    json.dump({"direct": direct}, f, ensure_ascii=False, indent=2)
    f.write("\n")

# =====================================================================================
# 语言键
# =====================================================================================
for lang, extra in (("zh_cn", lang_zh), ("en_us", lang_en)):
    path = os.path.join(ASSETS, "lang", lang + ".json")
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data.update(extra)
    # 顺手补上 Dyes 头盔开关的提示文本
    if lang == "zh_cn":
        data.update({
            "msg.thaumicdyesexplor.mask.none": "面具：无（潜行右键切换）",
            "msg.thaumicdyesexplor.mask.0": "面具 I",
            "msg.thaumicdyesexplor.mask.1": "面具 II",
            "msg.thaumicdyesexplor.mask.2": "面具 III",
            "msg.thaumicdyesexplor.goggles.on": "护目镜：开（右键切换）",
            "msg.thaumicdyesexplor.goggles.off": "护目镜：关（右键切换）",
        })
    else:
        data.update({
            "msg.thaumicdyesexplor.mask.none": "Mask: none (sneak-right-click to cycle)",
            "msg.thaumicdyesexplor.mask.0": "Mask I",
            "msg.thaumicdyesexplor.mask.1": "Mask II",
            "msg.thaumicdyesexplor.mask.2": "Mask III",
            "msg.thaumicdyesexplor.goggles.on": "Goggles: on (right-click to toggle)",
            "msg.thaumicdyesexplor.goggles.off": "Goggles: off (right-click to toggle)",
        })
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
        f.write("\n")

print("要素 %d 个，贴图 %d 张，物品要素分配 %d 条" % (len(ASPECTS), copied, len(direct)))
