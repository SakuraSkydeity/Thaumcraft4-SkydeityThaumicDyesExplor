# -*- coding: utf-8 -*-
"""
重画三个精髓罐的贴图与方块模型。

原来的做法是「把一张正面画的罐子图标贴到六面上」（cube_all），进游戏就是一个
每面都画着小罐子的方块，玩家看起来就是「贴图有问题」。

现在改成两段式的罐子模型：罐身 + 罐盖，贴图是一张 32×32 的小图集，四个象限：
  (0,0)-(16,16)   玻璃罐身（侧面）
  (16,0)-(32,16)  罐盖
  (0,16)-(16,32)  罐口（俯视，液面）

模型里的 uv 一律按 0~16 归一化（32×32 的贴图也是 0~16），所以象限 1 = [0,0,8,8]、
象限 2 = [8,0,16,8]、象限 3 = [0,8,8,16]。
"""
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "thaumicdyesexplor")
TEX = os.path.join(ASSETS, "textures", "block")
MODELS = os.path.join(ASSETS, "models", "block")

# (贴图名, 框色, 玻璃色, 液体色, 盖色)
JARS = [
    ("jar_1", (0x3A, 0x46, 0x50), (0xC8, 0xDB, 0xE6), (0x4A, 0x86, 0xC8), (0x6B, 0x78, 0x83)),
    ("jar_2", (0x35, 0x49, 0x2F), (0xD6, 0xE7, 0xCD), (0x4F, 0x9A, 0x4A), (0x7D, 0x8F, 0x5A)),
    ("jar_3", (0x1C, 0x1C, 0x20), (0xAA, 0xB0, 0xBD), (0x5A, 0x5F, 0x8A), (0x33, 0x33, 0x3A)),
]

MODEL = {
    "textures": {
        "particle": "thaumicdyesexplor:block/{tex}",
        "jar": "thaumicdyesexplor:block/{tex}",
    },
    "elements": [
        {
            "name": "body",
            "from": [3, 0, 3],
            "to": [13, 12, 13],
            "faces": {
                "north": {"uv": [0, 0, 8, 8], "texture": "#jar"},
                "east": {"uv": [0, 0, 8, 8], "texture": "#jar"},
                "south": {"uv": [0, 0, 8, 8], "texture": "#jar"},
                "west": {"uv": [0, 0, 8, 8], "texture": "#jar"},
                "up": {"uv": [0, 8, 8, 16], "texture": "#jar"},
                "down": {"uv": [0, 8, 8, 16], "texture": "#jar"},
            },
        },
        {
            "name": "lid",
            "from": [2, 12, 2],
            "to": [14, 14, 14],
            "faces": {
                "north": {"uv": [8, 0, 16, 8], "texture": "#jar"},
                "east": {"uv": [8, 0, 16, 8], "texture": "#jar"},
                "south": {"uv": [8, 0, 16, 8], "texture": "#jar"},
                "west": {"uv": [8, 0, 16, 8], "texture": "#jar"},
                "up": {"uv": [8, 0, 16, 8], "texture": "#jar"},
                "down": {"uv": [8, 0, 16, 8], "texture": "#jar"},
            },
        },
    ],
}


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def build_texture(frame, glass, liquid, lid):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()

    # --- 象限 1：罐身侧面（玻璃 + 底部液体带） ---
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15)
            color = frame if edge else glass
            # 左侧高光
            if x in (2, 3) and 1 <= y <= 13:
                color = mix(glass, (255, 255, 255), 0.55)
            # 右侧阴影
            if x in (13, 14) and 1 <= y <= 13:
                color = mix(glass, frame, 0.35)
            # 罐底液体
            if y >= 10 and not edge:
                color = mix(liquid, (0, 0, 0), 0.10 if y >= 13 else 0.0)
                if y == 10:
                    color = mix(liquid, (255, 255, 255), 0.35)
            # 上下框
            if y == 0 or y == 15:
                color = frame
            px[x, y] = color + (255,)

    # --- 象限 2：罐盖 ---
    for y in range(16):
        for x in range(16, 32):
            local = x - 16
            color = lid
            if local in (0, 15) or y in (0, 15):
                color = mix(lid, (0, 0, 0), 0.45)
            elif y in (1, 2):
                color = mix(lid, (255, 255, 255), 0.30)
            elif y in (13, 14):
                color = mix(lid, (0, 0, 0), 0.30)
            elif local in (2, 3):
                color = mix(lid, (255, 255, 255), 0.18)
            px[x, y] = color + (255,)

    # --- 象限 3：罐口 / 液面（俯视） ---
    for y in range(16, 32):
        for x in range(0, 16):
            ly = y - 16
            color = liquid
            if x in (0, 15) or ly in (0, 15):
                color = frame
            elif x in (1, 14) or ly in (1, 14):
                color = mix(liquid, (0, 0, 0), 0.25)
            elif 5 <= x <= 8 and 4 <= ly <= 6:
                color = mix(liquid, (255, 255, 255), 0.55)
            px[x, y] = color + (255,)

    # --- 象限 4：留空（填盖色，避免被误采样时出黑边） ---
    for y in range(16, 32):
        for x in range(16, 32):
            px[x, y] = mix(lid, (0, 0, 0), 0.15) + (255,)

    return img


def main():
    for name, frame, glass, liquid, lid in JARS:
        build_texture(frame, glass, liquid, lid).save(os.path.join(TEX, name + ".png"))
        for target in ("me_compressed_essentia_jar" if name == "jar_1" else
                       "me_x2_compressed_essentia_jar" if name == "jar_2" else
                       "me_extreme_essentia_jar",):
            payload = json.loads(json.dumps(MODEL).replace("{tex}", name))
            with open(os.path.join(MODELS, target + ".json"), "w", encoding="utf-8", newline="\n") as fh:
                json.dump(payload, fh, ensure_ascii=False, indent=2)
                fh.write("\n")
        print("jar", name, "ok")
    # 物品模型要重新指向方块模型（这是标准做法，保持不变，只提示）
    print("三个罐子贴图 + 模型已更新")


if __name__ == "__main__":
    main()
