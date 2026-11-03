# -*- coding: utf-8 -*-
"""校验配方 JSON：语法 + 物品 ID 存在性（thaumcraft 对照 TC4R lang，本模组对照注册代码）。"""
import json, glob, os, sys, re

ROOT = r"D:/.codepy/trea/slash/SkydeityThaumicDyesExplo_1.21"
R = ROOT + "/src/main/resources/data/thaumicdyesexplor/recipe"
sys.stdout.reconfigure(encoding="utf-8")

# TC4R 物品/方块 id 集
tc4r = set()
import zipfile
jar = ROOT + "/.none/1.21.1-neoforge-20759-dev/thaumcraft-neoforge-4.2.3.5-1.21.1-port.0.1.0-20759.jar"
with zipfile.ZipFile(jar) as z:
    lang = json.loads(z.read("assets/thaumcraft/lang/en_us.json").decode("utf8"))
for k in lang:
    m = re.match(r"(item|block)\.thaumcraft\.([a-z0-9_]+)$", k)
    if m:
        tc4r.add(m.group(2))

# 本模组注册的 id：从 java 源码里抠字符串
port_items = set()
src = ROOT + "/src/main/java"
for dirpath, _, files in os.walk(src):
    for fn in files:
        if fn.endswith(".java"):
            txt = open(os.path.join(dirpath, fn), encoding="utf8", errors="ignore").read()
            port_items |= set(re.findall(r'"([a-z0-9_]+)"\s*\)', txt))  # 粗略
            port_items |= set(re.findall(r'string\b.*?"([a-z0-9_]+)"', txt))
# 更可靠：从 lang 文件收集
plang = ROOT + "/src/main/resources/assets/thaumicdyesexplor/lang/en_us.json"
pl = json.load(open(plang, encoding="utf8"))
for k in pl:
    m = re.match(r"item\.thaumicdyesexplor\.([a-z0-9_]+)$", k)
    if m:
        port_items.add(m.group(1))
    m = re.match(r"block\.thaumicdyesexplor\.([a-z0-9_]+)$", k)
    if m:
        port_items.add(m.group(1))

def check_item(iid, where, errs):
    ns, _, name = iid.partition(":")
    if ns == "minecraft" or ns == "c":
        return
    if ns == "thaumcraft":
        if name not in tc4r:
            errs.append("%s: unknown thaumcraft item %s" % (where, iid))
    elif ns == "thaumicdyesexplor":
        if name not in port_items:
            errs.append("%s: unknown mod item %s" % (where, iid))
    else:
        errs.append("%s: unknown namespace %s" % (where, iid))

def walk(x, where, errs):
    if isinstance(x, dict):
        if "item" in x and isinstance(x["item"], str):
            check_item(x["item"], where, errs)
        for v in x.values():
            walk(v, where, errs)
    elif isinstance(x, list):
        for v in x:
            walk(v, where, errs)

errs = []
n = 0
for f in glob.glob(R + "/*/*.json"):
    n += 1
    where = os.path.relpath(f, R)
    try:
        d = json.load(open(f, encoding="utf8"))
    except Exception as e:
        errs.append("%s: JSON parse error %s" % (where, e))
        continue
    walk(d, where, errs)
    # infusion 必须字段
    if d.get("type") == "thaumcraft:infusion":
        for k in ("research", "central", "components", "instability", "aspects"):
            if k not in d:
                errs.append("%s: infusion missing %s" % (where, k))
        # TC4R 的 infusion 有四种互斥的输出模式（见 InfusionRecipe 构造器）：
        # result / component_patch / input_patch / input_transformation，必须恰好一种。
        modes = [k for k in ("result", "component_patch", "input_patch", "input_transformation") if k in d]
        if len(modes) != 1:
            errs.append("%s: infusion needs exactly one of result/component_patch/input_patch/"
                        "input_transformation (got %s)" % (where, modes or "none"))
        # input_patch 的键必须是本模组注册过的扩展键（否则解码时抛未注册）
        for key in (d.get("input_patch") or {}):
            if ":" not in key:
                errs.append("%s: input_patch key must be namespaced: %s" % (where, key))
            elif key.startswith(("minecraft:", "thaumcraft:")):
                errs.append("%s: input_patch key uses reserved namespace: %s" % (where, key))
            elif key != "thaumicdyesexplor:runic_upgrade":
                errs.append("%s: unregistered input_patch key: %s" % (where, key))
    if d.get("type") == "thaumcraft:arcane_shaped":
        for k in ("research", "vis", "pattern", "key", "result"):
            if k not in d:
                errs.append("%s: arcane missing %s" % (where, k))
    # 1.21 schema: result 必须用 "id"（写成 "item" 会在 RecipeManager 解析失败）
    res = d.get("result")
    if isinstance(res, dict):
        if "item" in res and "id" not in res:
            errs.append("%s: result uses 'item' instead of 'id'" % where)
        elif "id" not in res:
            errs.append("%s: result missing id" % where)

# 注魔配方唯一性：TC4 的注魔按「中心 + 配料多重集」匹配，两条完全一样的配方
# 只会命中其中一条，另一条永远做不出来（第 30 轮踩过：乌鲁克 / 黑乌鲁克钢）。
def _norm(x):
    if isinstance(x, dict):
        if "item" in x:
            return "i:" + str(x["item"])
        if "tag" in x:
            return "t:" + str(x["tag"])
        return "?" + json.dumps(x, sort_keys=True, ensure_ascii=False)
    return "?" + json.dumps(x, sort_keys=True, ensure_ascii=False)

_sigs = {}
for f in glob.glob(R + "/infusion/*.json"):
    where = os.path.relpath(f, R)
    try:
        d = json.load(open(f, encoding="utf8"))
    except Exception:
        continue
    if d.get("type") != "thaumcraft:infusion":
        continue
    sig = (_norm(d.get("central")), tuple(sorted(_norm(c) for c in d.get("components") or [])))
    _sigs.setdefault(sig, []).append(where)
for sig, files in _sigs.items():
    if len(files) > 1:
        errs.append("%s: duplicate infusion recipe (same central + components): %s"
                    % ("infusion", ", ".join(sorted(files))))

# 附魔 JSON 的 min_cost / max_cost 是 LevelBasedValue：要么是裸数字（Constant），
# 要么是 {"base": x, "per_level_above_first": y}（Linear），两个字段缺一个整份注册表
# 解析失败、进不去世界（第 33 轮踩过：night_vision 只写了 base）。
ENCH = os.path.join(R, "..", "enchantment")
for f in glob.glob(os.path.join(ENCH, "*.json")):
    where = os.path.relpath(f, ENCH)
    try:
        d = json.load(open(f, encoding="utf8"))
    except Exception as e:
        errs.append("%s: enchantment syntax error: %s" % (where, e))
        continue
    for key in ("min_cost", "max_cost"):
        v = d.get(key)
        if isinstance(v, dict) and not ({"base", "per_level_above_first"} <= set(v)):
            errs.append("%s: %s needs both base and per_level_above_first (or a bare number)" % (where, key))

print("checked", n, "files;", len(errs), "problems")
for e in errs:
    print(" ", e)
