# -*- coding: utf-8 -*-
"""
生成 ME 方块的资产：blockstates、方块/物品模型、战利品表、程序化贴图、语言键。
（物品模型由 DeferredRegister.Blocks#registerBlockItem 注册，模型走 parent=方块模型。）
运行：python tools/port_me_blocks_assets.py
"""
import json
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "thaumicdyesexplor")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "thaumicdyesexplor")
NS = "thaumicdyesexplor"

BLOCKS = {
    # id: (贴图色, 中文名, 英文名)
    "me_compressed_essentia_jar": ("jar_1", "压缩精髓罐", "Compressed Essentia Jar"),
    "me_x2_compressed_essentia_jar": ("jar_2", "双重压缩精髓罐", "x2 Compressed Essentia Jar"),
    "me_extreme_essentia_jar": ("jar_3", "群星精髓罐", "Extreme Essentia Jar"),
    "me_storage_block": ("storage", "中洲秘银存储块", "Middle-earth Storage Block"),
    "me_inventarium_bookshelf": ("bookshelf", "清单书架", "Inventarium Bookshelf"),
    "me_crafting_table_basic": ("table_basic", "基础派系工作台", "Basic Faction Crafting Table"),
    "me_crafting_table_advanced": ("table_advanced", "进阶派系工作台", "Advanced Faction Crafting Table"),
    "me_crafting_table_redstone": ("table_redstone", "红敏派系工作台", "Redstone Faction Crafting Table"),
}

TEX_DIR = os.path.join(ASSETS, "textures", "block")


def gen_texture(name, kind):
    os.makedirs(TEX_DIR, exist_ok=True)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    if kind.startswith("jar"):
        tier = int(kind[-1])
        # 玻璃罐体 + 蓝色精髓液面 + 金属箍
        body = (205, 225, 235, 255)
        liquid = [(70, 150, 200, 255), (50, 120, 190, 255), (30, 90, 170, 255)][tier - 1]
        band = [(180, 180, 190, 255), (150, 160, 175, 255), (120, 130, 150, 255)][tier - 1]
        d.rectangle([2, 1, 13, 14], fill=body)
        d.rectangle([3, 5 + tier, 12, 13], fill=liquid)
        d.rectangle([1, 0, 14, 1], fill=band)
        d.rectangle([1, 14, 14, 14], fill=band)
        d.rectangle([2, 4, 13, 4], fill=(140, 200, 230, 255))
    elif kind == "storage":
        d.rectangle([0, 0, 15, 15], fill=(110, 120, 135, 255))
        d.rectangle([1, 1, 14, 14], fill=(140, 150, 165, 255))
        for x in range(3, 14, 4):
            d.line([(x, 2), (x, 13)], fill=(90, 100, 115, 255))
        d.rectangle([6, 6, 9, 9], fill=(210, 215, 225, 255))
    elif kind == "bookshelf":
        d.rectangle([0, 0, 15, 15], fill=(140, 105, 60, 255))
        d.rectangle([1, 1, 14, 14], fill=(100, 70, 40, 255))
        colors = [(160, 40, 40), (40, 90, 160), (40, 130, 60), (160, 130, 40)]
        for row in (2, 9):
            x = 2
            for c in colors:
                d.rectangle([x, row, x + 2, row + 4], fill=c + (255,))
                x += 3
    else:
        base = {"table_basic": (150, 110, 60), "table_advanced": (110, 80, 130),
                "table_redstone": (130, 70, 60)}[kind]
        d.rectangle([0, 0, 15, 15], fill=base + (255,))
        d.rectangle([1, 1, 14, 14], fill=tuple(min(255, c + 25) for c in base) + (255,))
        d.rectangle([6, 6, 9, 9], fill=(30, 30, 30, 255))
        d.rectangle([7, 7, 8, 8], fill=(230, 210, 120, 255))
    im.save(os.path.join(TEX_DIR, name + ".png"))


for block_id, (tex, zh, en) in BLOCKS.items():
    gen_texture(tex, tex)

    # blockstate
    bs = {"variants": {"": {"model": NS + ":block/" + block_id}}}
    with open(os.path.join(ASSETS, "blockstates", block_id + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(bs, f, indent=2)
        f.write("\n")

    # 方块模型 + 物品模型
    model = {"parent": "minecraft:block/cube_all", "textures": {"all": NS + ":block/" + tex}}
    with open(os.path.join(ASSETS, "models", "block", block_id + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(model, f, indent=2)
        f.write("\n")
    item_model = {"parent": NS + ":block/" + block_id}
    with open(os.path.join(ASSETS, "models", "item", block_id + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(item_model, f, indent=2)
        f.write("\n")

    # 战利品表（掉自身）
    loot = {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": NS + ":" + block_id}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]
        }]
    }
    loot_path = os.path.join(DATA, "loot_table", "blocks", block_id + ".json")
    os.makedirs(os.path.dirname(loot_path), exist_ok=True)
    with open(loot_path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(loot, f, indent=2)
        f.write("\n")

# 语言键
lang_zh = {("block." + NS + "." + b): v[1] for b, v in BLOCKS.items()}
lang_en = {("block." + NS + "." + b): v[2] for b, v in BLOCKS.items()}
lang_zh.update({
    "container.thaumicdyesexplor.me_crafting_basic": "基础派系工作台",
    "container.thaumicdyesexplor.me_crafting_advanced": "进阶派系工作台",
    "container.thaumicdyesexplor.me_crafting_redstone": "红敏派系工作台",
    "tooltip.thaumicdyesexplor.me_jar.hint": "右键：法杖取 vis；潜行右键：存入 vis",
})
lang_en.update({
    "container.thaumicdyesexplor.me_crafting_basic": "Basic Faction Crafting Table",
    "container.thaumicdyesexplor.me_crafting_advanced": "Advanced Faction Crafting Table",
    "container.thaumicdyesexplor.me_crafting_redstone": "Redstone Faction Crafting Table",
    "tooltip.thaumicdyesexplor.me_jar.hint": "Right-click: fill wand; Sneak-right-click: store vis",
})
for lang, extra in (("zh_cn", lang_zh), ("en_us", lang_en)):
    path = os.path.join(ASSETS, "lang", lang + ".json")
    data = json.load(open(path, encoding="utf-8"))
    data.update(extra)
    json.dump(data, open(path, "w", encoding="utf-8", newline="\n"), ensure_ascii=False, indent=2, sort_keys=True)
    open(path, "a", encoding="utf-8", newline="\n").write("\n")

print("方块资产完成：%d 个方块" % len(BLOCKS))
