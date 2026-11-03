#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把 client/model/geo/*.java 的盒体几何离线渲染成 PNG，用来核对肩甲/腿甲位置。

坐标系与 Minecraft 一致：y 向下、-z 是角色正面。渲染「正面」= 相机在 -z 侧。

只画自定义部件（不画原版 7 个裸盒体），每个部件一种颜色，便于肉眼确认
Shoulder* 在肩膀、Backpanel*/Legpanel* 在腿上。
"""
import os
import re
import math
import sys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GEO = os.path.join(ROOT, 'src/main/java/com/skydeity/thaumicdyesexplor/client/model/geo')
OUT = os.path.join(ROOT, 'build/geo-preview')

# 1.21.1 HumanoidModel.createMesh 的七个顶层部件 pivot
TOP_PIVOT = {
    'head': (0.0, 0.0, 0.0), 'hat': (0.0, 0.0, 0.0), 'body': (0.0, 0.0, 0.0),
    'right_arm': (-5.0, 2.0, 0.0), 'left_arm': (5.0, 2.0, 0.0),
    'right_leg': (-1.9, 12.0, 0.0), 'left_leg': (1.9, 12.0, 0.0),
}

PALETTE = [
    (214, 80, 80), (232, 148, 60), (226, 210, 90), (140, 200, 90), (90, 200, 160),
    (90, 180, 220), (110, 130, 226), (160, 110, 220), (214, 110, 190), (230, 150, 170),
    (170, 170, 170), (120, 140, 120),
]


def parse(path):
    src = open(path, encoding='utf-8').read()
    parts = {}
    for m in re.finditer(
            r'b\.cube\("([^"]+)",\s*"([^"]+)",\s*(\d+),\s*(\d+),\s*'
            + r'(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*'
            + r'(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*'
            + r'(-?[\d.]+)F,\s*'
            + r'(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*'
            + r'(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*(-?[\d.]+)F,\s*'
            + r'(true|false)\)', src):
        g = m.groups()
        name, parent = g[0], g[1]
        u, v = int(g[2]), int(g[3])
        box = tuple(float(x) for x in g[4:10])
        inflate = float(g[10])
        rot = tuple(float(x) for x in g[11:14])
        pivot = tuple(float(x) for x in g[14:17])
        mirror = g[17] == 'true'
        parts.setdefault(name, {'parent': parent, 'cubes': []})
        parts[name]['cubes'].append((u, v, box, inflate, rot, pivot, mirror))
    return parts


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def mat_vec(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


IDENTITY = [[1.0, 0.0, 0.0], [0.0, 1.0, 0.0], [0.0, 0.0, 1.0]]


def rot_matrix(rx, ry, rz):
    cx, sx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)
    mx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    my = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    mz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    return mat_mul(mat_mul(mz, my), mx)   # 与 ModelPart.translateAndRotate 一致（Z→Y→X）


def chain(parts, name):
    """返回该部件的累计 (R, T) —— 从顶层部件一路乘下来。"""
    stack = []
    cur = name
    guard = 0
    while cur in parts and guard < 64:
        stack.append(cur)
        cur = parts[cur]['parent']
        guard += 1
    if cur not in TOP_PIVOT:
        return None, None
    R = [row[:] for row in IDENTITY]
    T = list(TOP_PIVOT[cur])
    for step in reversed(stack):
        cube0 = parts[step]['cubes'][0]
        rot, pivot = cube0[4], cube0[5]
        # 子级：先平移到 pivot，再施加父级变换
        T = [T[i] + mat_vec(R, pivot)[i] for i in range(3)]
        R = mat_mul(R, rot_matrix(*rot))
    return R, T


def box_faces(box, inflate, u, v):
    """返回每个面的 (4 个局部顶点, 4 个 UV 角)，UV 按 1.21.1 ModelPart.Cube 的标准盒式布局。"""
    x, y, z, w, h, d = box
    x0, y0, z0 = x - inflate, y - inflate, z - inflate
    x1, y1, z1 = x + w + inflate, y + h + inflate, z + d + inflate
    # 各面 UV 原点（左上角）与宽高（贴图像素，128x64 布局单位）
    faces = {
        'north': ([(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)],
                  [(u + d, v + d), (u + d + w, v + d), (u + d + w, v + d + h), (u + d, v + d + h)]),
        'south': ([(x1, y0, z1), (x0, y0, z1), (x0, y1, z1), (x1, y1, z1)],
                  [(u + d + w + d, v + d), (u + d + w + d + w, v + d),
                   (u + d + w + d + w, v + d + h), (u + d + w + d, v + d + h)]),
        'west':  ([(x0, y0, z1), (x0, y0, z0), (x0, y1, z0), (x0, y1, z1)],
                  [(u + d + w, v + d), (u + d + w + d, v + d),
                   (u + d + w + d, v + d + h), (u + d + w, v + d + h)]),
        'east':  ([(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)],
                  [(u, v + d), (u + d, v + d), (u + d, v + d + h), (u, v + d + h)]),
        'up':    ([(x0, y0, z0), (x0, y0, z1), (x1, y0, z1), (x1, y0, z0)],
                  [(u + d, v), (u + d, v + d), (u + d + w, v + d), (u + d + w, v)]),
        'down':  ([(x0, y1, z1), (x0, y1, z0), (x1, y1, z0), (x1, y1, z1)],
                  [(u + d + w, v), (u + d + w + d, v), (u + d + w + d, v + d), (u + d + w, v + d)]),
    }
    return faces


def render(parts, view, scale=7, margin=6, tex=None, tex_w=128, tex_h=64):
    """view = 'front'（-z 侧）或 'back'（+z 侧）。tex 给定时按 UV 采样贴图（256x128 实际尺寸=128x64 布局的 2 倍像素画）。"""
    W = int(34 * scale) + margin * 2
    H = int(34 * scale) + margin * 2
    img = Image.new('RGB', (W, H), (24, 24, 28))
    px = img.load()

    names = sorted(parts)
    for idx, name in enumerate(names):
        if name.startswith('Mask'):
            continue
        R, T = chain(parts, name)
        if R is None:
            continue
        for (u, v, box, inflate, _rot, _pivot, mirror) in parts[name]['cubes']:
            for face, (verts, uvs) in box_faces(box, inflate, u, v).items():
                pts = []
                for vx in verts:
                    rv = mat_vec(R, list(vx))
                    pts.append([rv[i] + T[i] for i in range(3)])
                # 投影：x → 屏幕 x，y → 屏幕 y，z → 深度
                sign = 1.0 if view == 'front' else -1.0
                poly = []
                for p, uv in zip(pts, uvs):
                    sx = margin + (p[0] * sign + 17) * scale
                    sy = margin + (p[1] + 2) * scale
                    depth = p[2] * sign
                    poly.append((sx, sy, depth, uv[0], uv[1]))
                raster(px, poly, tex, tex_w, tex_h, W, H, view)
    return img


def raster(px, poly, tex, tex_w, tex_h, W, H, view):
    """四边形逐像素光栅化（拆两个三角形，重心插值 UV），带 z-buffer 语义的简化画家排序。"""
    (ax, ay, az, au, av), (bx, by, bz, bu, bv), (cx, cy, cz, cu, cv), (dx, dy, dz, du, dv) = poly
    tris = [(poly[0], poly[1], poly[2]), (poly[0], poly[2], poly[3])]
    for t in tris:
        (x1, y1, z1, u1, v1), (x2, y2, z2, u2, v2), (x3, y3, z3, u3, v3) = t
        minx = max(int(min(x1, x2, x3)), 0)
        maxx = min(int(max(x1, x2, x3)) + 1, W - 1)
        miny = max(int(min(y1, y2, y3)), 0)
        maxy = min(int(max(y1, y2, y3)) + 1, H - 1)
        den = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3)
        if abs(den) < 1e-9:
            continue
        for yy in range(miny, maxy + 1):
            for xx in range(minx, maxx + 1):
                fx, fy = xx + 0.5, yy + 0.5
                w1 = ((y2 - y3) * (fx - x3) + (x3 - x2) * (fy - y3)) / den
                w2 = ((y3 - y1) * (fx - x3) + (x1 - x3) * (fy - y3)) / den
                w3 = 1.0 - w1 - w2
                if w1 < -1e-6 or w2 < -1e-6 or w3 < -1e-6:
                    continue
                uu = w1 * u1 + w2 * u2 + w3 * u3
                vv = w1 * v1 + w2 * v2 + w3 * v3
                # UV 是 128x64 布局单位；贴图实际 2 倍（256x128）
                # 部件 UV 偶有超出 128x64 布局的（源模组如此），按 1.7.10 的行为 clamp
                tx = max(0, min(int(uu * 2), tex_w - 1))
                ty = max(0, min(int(vv * 2), tex_h - 1))
                if tex is not None:
                    r, g, b, a = tex[tx, ty]   # PIL 像素索引是 [x, y]
                    if a < 40:
                        continue
                    px[xx, yy] = (r, g, b)
                else:
                    px[xx, yy] = (120, 160, 200)


def label(img, text):
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, img.width, 13], fill=(0, 0, 0))
    draw.text((4, 2), text, fill=(255, 240, 140))
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    files = sorted(f for f in os.listdir(GEO) if f.endswith('Geo.java'))
    tiles = []
    for fn in files:
        parts = parse(os.path.join(GEO, fn))
        front = label(render(parts, 'front'), fn[:-9] + ' front')
        back = label(render(parts, 'back'), fn[:-9] + ' back')
        pair = Image.new('RGB', (front.width + back.width, max(front.height, back.height)), (24, 24, 28))
        pair.paste(front, (0, 0))
        pair.paste(back, (front.width, 0))
        pair.save(os.path.join(OUT, fn[:-9] + '.png'))
        tiles.append(pair)
        print('%-26s parts=%d' % (fn, len(parts)))

    # 拼一张总览（每行两个模型）
    if tiles:
        cols = 2
        rows = (len(tiles) + cols - 1) // cols
        tw, th = tiles[0].width, tiles[0].height
        sheet = Image.new('RGB', (cols * tw, rows * th), (16, 16, 18))
        for i, t in enumerate(tiles):
            sheet.paste(t, ((i % cols) * tw, (i // cols) * th))
        sheet.save(os.path.join(OUT, '_all.png'))
        print('-> %s' % os.path.join(OUT, '_all.png'))
    return 0


if __name__ == '__main__':
    sys.exit(main())
