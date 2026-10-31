#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
把 Thaumic Dyes (1.7.10) 的 Tabula 生成模型转成 1.21.1 可用的几何描述。

输入：.none/Thaumic-Dyes/src/main/java/thaumicdyes/client/models/Model*.java
输出：
  src/main/java/com/skydeity/thaumicdyesexplor/client/model/geo/<Name>Geo.java

每个 ModelRenderer 变成一个 PartSpec：
  name, parent, texOffs(u,v), boxes[(x,y,z,w,h,d,inflate)], rot(x,y,z), mirror, pivot(x,y,z)

用法：python tools/port_armor_models.py
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, '.none/Thaumic-Dyes/src/main/java/thaumicdyes/client/models')
OUT = os.path.join(ROOT, 'src/main/java/com/skydeity/thaumicdyesexplor/client/model/geo')

PARENT_PARTS = {
    'bipedHead': 'head', 'bipedHeadwear': 'hat', 'bipedBody': 'body',
    'bipedRightArm': 'right_arm', 'bipedLeftArm': 'left_arm',
    'bipedRightLeg': 'right_leg', 'bipedLeftLeg': 'left_leg',
}

num = r'(-?(?:\d+\.?\d*|\.\d+))[FfDd]?'

def clean(text):
    # 去掉注释
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    text = re.sub(r'//[^\n]*', '', text)
    return text


def parse(path):
    raw = open(path, encoding='utf-8', errors='replace').read()
    src = clean(raw)
    m = re.search(r'super\(\s*f\s*,\s*' + num + r'\s*,\s*' + r'(\d+)\s*,\s*(\d+)\s*\)', src)
    tex_w, tex_h = (int(m.group(2)), int(m.group(3))) if m else (128, 64)

    # 记录变量声明（区分数组）
    arrays = set(re.findall(r'ModelRenderer\[\]\s+(\w+)', src))

    # 变量 -> 当前 texOffs
    parts = {}

    def part(name):
        if name not in parts:
            parts[name] = {'name': name, 'u': 0, 'v': 0, 'boxes': [], 'rot': [0.0, 0.0, 0.0],
                           'mirror': False, 'pivot': [0.0, 0.0, 0.0], 'parent': None, 'order': len(parts)}
        return parts[name]

    # this.X[i] = new ModelRenderer(this, u, v);
    for mm in re.finditer(r'this\.(\w+)(\[(\d+)\])?\s*=\s*new ModelRenderer\(this,\s*(\d+),\s*(\d+)\)', src):
        key = mm.group(1) + ('#' + mm.group(3) if mm.group(3) else '')
        p = part(key)
        p['u'], p['v'] = int(mm.group(4)), int(mm.group(5))

    # 语句级扫描：this.X(.或[i]) . 调用
    stmt = re.compile(r'this\.(\w+)(?:\[(\d+)\])?\s*\.?\s*')
    # 直接按行处理链式调用
    for line in src.split('\n'):
        line = line.strip()
        if not line.startswith('this.'):
            continue
        # mirror
        for mm in re.finditer(r'this\.(\w+)(?:\[(\d+)\])?\.mirror\s*=\s*true', line):
            part(mm.group(1) + ('#' + mm.group(2) if mm.group(2) else ''))['mirror'] = True
        # addBox
        for mm in re.finditer(r'this\.(\w+)(?:\[(\d+)\])?\.addBox\(\s*' + r',\s*'.join([num] * 6) + r'(?:\s*,\s*' + num + r')?\s*\)', line):
            key = mm.group(1) + ('#' + mm.group(2) if mm.group(2) else '')
            vals = [float(x) for x in mm.groups()[2:] if x is not None]
            box = vals[:6]
            inflate = vals[6] if len(vals) > 6 else 0.0
            part(key)['boxes'].append(box + [inflate])
        # setRotationPoint
        for mm in re.finditer(r'this\.(\w+)(?:\[(\d+)\])?\.setRotationPoint\(\s*' + num + r'\s*,\s*' + num + r'\s*,\s*' + num + r'\s*\)', line):
            key = mm.group(1) + ('#' + mm.group(2) if mm.group(2) else '')
            part(key)['pivot'] = [float(mm.group(3)), float(mm.group(4)), float(mm.group(5))]
        # setRotation(this.X, x, y, z)
        for mm in re.finditer(r'setRotation\(\s*this\.(\w+)(?:\[(\d+)\])?\s*,\s*' + num + r'\s*,\s*' + num + r'\s*,\s*' + num + r'\s*\)', line):
            key = mm.group(1) + ('#' + mm.group(2) if mm.group(2) else '')
            part(key)['rot'] = [float(mm.group(3)), float(mm.group(4)), float(mm.group(5))]
        # 直接赋值 rotateAngle
        for mm in re.finditer(r'this\.(\w+)(?:\[(\d+)\])?\.rotateAngle([XYZ])\s*=\s*' + num, line):
            key = mm.group(1) + ('#' + mm.group(2) if mm.group(2) else '')
            idx = 'XYZ'.index(mm.group(3))
            part(key)['rot'][idx] = float(mm.group(4))
        # addChild
        for mm in re.finditer(r'this\.(biped\w+|(\w+)(?:\[(\d+)\])?)\.addChild\(\s*this\.(\w+)(?:\[(\d+)\])?\s*\)', line):
            parent_raw = mm.group(1)
            child = mm.group(4) + ('#' + mm.group(5) if mm.group(5) else '')
            parent = PARENT_PARTS.get(parent_raw, parent_raw + ('#' + mm.group(3) if mm.group(3) else ''))
            if child in parts:
                part(child)['parent'] = parent
            else:
                p = part(child)
                p['parent'] = parent

    # 没有 parent 的部件挂到 body
    for p in parts.values():
        if not p['parent']:
            p['parent'] = 'body'
    return tex_w, tex_h, parts, arrays


def fmt(f):
    s = ('%.4f' % f).rstrip('0').rstrip('.')
    return s + 'F' if '.' in s or 'E' in s else s + 'F'


def emit(name, tex_w, tex_h, parts, arrays, desc):
    lines = []
    lines.append('package com.skydeity.thaumicdyesexplor.client.model.geo;')
    lines.append('')
    lines.append('import com.skydeity.thaumicdyesexplor.client.model.ArmorGeo;')
    lines.append('')
    lines.append('/**')
    lines.append(' * %s' % desc)
    lines.append(' *')
    lines.append(' * <p>由 {@code tools/port_armor_models.py} 从 1.7.10 源模组同名 Model 类自动转换生成，')
    lines.append(' * 请勿手工修改；要改请改脚本或源模组数据。</p>')
    lines.append(' */')
    lines.append('public final class %sGeo {' % name)
    lines.append('')
    lines.append('    public static final int TEX_WIDTH = %d;' % tex_w)
    lines.append('    public static final int TEX_HEIGHT = %d;' % tex_h)
    lines.append('')
    lines.append('    private %sGeo() {' % name)
    lines.append('    }')
    lines.append('')
    lines.append('    public static ArmorGeo build(float grow) {')
    lines.append('        ArmorGeo.Builder b = new ArmorGeo.Builder(TEX_WIDTH, TEX_HEIGHT, grow);')
    # 保证父部件先创建
    ordered = sorted(parts.values(), key=lambda p: (0 if p['parent'] in PARENT_PARTS.values() else 1, p['order']))
    for p in ordered:
        parent = p['parent']
        for box in p['boxes']:
            args = [fmt(box[i]) for i in range(7)]
            args += [fmt(p['rot'][i]) for i in range(3)]
            args += [fmt(p['pivot'][i]) for i in range(3)]
            args += ['true' if p['mirror'] else 'false']
            lines.append('        b.cube("%s", "%s", %d, %d, %s);' % (
                p['name'], parent, p['u'], p['v'], ', '.join(args)))
        if not p['boxes']:
            lines.append('        b.empty("%s", "%s", %s, %s, %s);' % (
                p['name'], parent, fmt(p['rot'][0]), fmt(p['rot'][1]), fmt(p['rot'][2])))
    lines.append('        return b.build();')
    lines.append('    }')
    lines.append('}')
    out = os.path.join(OUT, name + 'Geo.java')
    os.makedirs(OUT, exist_ok=True)
    open(out, 'w', encoding='utf-8').write('\n'.join(lines) + '\n')
    print('  -> %sGeo.java  parts=%d cubes=%d' % (name, len(parts), sum(len(p['boxes']) for p in parts.values())))
    return out


DESCRIPTIONS = {
    'ModelRobes': '染袍（Cultist / Thaumium / Void Robe）系列护甲几何，纹理 128x64 UV（贴图 256x128）。',
    'ModelRobesSpecial': '符文护甲 III（Runic Primal）护甲几何，纹理 128x64 UV（贴图 256x128）。',
    'ModelKnight': '骑士甲（Cultist Knight / Sun Knight / Inhabited Plate）护甲几何。',
    'ModelPraetor': '领袖甲（Cultist Leader / Praetor）护甲几何。',
    'ModelRanger': '游侠甲（已染色）护甲几何。',
    'ModelRanger2': '游侠甲（未染色）护甲几何。',
    'ModelArcher': '弓箭手甲护甲几何。',
    'ModelFortress': '堡垒甲（Thaumium Fortress）护甲几何。',
    'ModelIronFortress': '铁堡垒甲护甲几何。',
    'ModelPaladin': '圣骑士头盔护甲几何。',
}


def main():
    if not os.path.isdir(SRC):
        print('missing source dir: %s' % SRC)
        return 1
    print('parsing %s' % SRC)
    for fn in sorted(os.listdir(SRC)):
        if not fn.endswith('.java'):
            continue
        base = fn[:-5]
        if base not in DESCRIPTIONS:
            continue
        tex_w, tex_h, parts, arrays = parse(os.path.join(SRC, fn))
        emit(base, tex_w, tex_h, parts, arrays, DESCRIPTIONS[base])
    return 0


if __name__ == '__main__':
    sys.exit(main())
