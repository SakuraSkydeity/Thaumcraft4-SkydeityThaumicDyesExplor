# -*- coding: utf-8 -*-
"""Dump all recipe JSONs in compact one-line form for 1.7.10 comparison."""
import json, glob, os, sys

R = r"D:/.codepy/trea/slash/SkydeityThaumicDyesExplo_1.21/src/main/resources/data/thaumicdyesexplor/recipe"
sys.stdout.reconfigure(encoding="utf-8")

def ing(x):
    if "item" in x:
        return x["item"] + ("x%d" % x.get("count", 1) if x.get("count", 1) != 1 else "")
    if "tag" in x:
        return "#" + x["tag"]
    return json.dumps(x)

for sub in ["crafting", "arcane", "infusion"]:
    files = sorted(glob.glob(R + "/" + sub + "/*.json"))
    print("=" * 20, sub, len(files))
    for f in files:
        d = json.load(open(f, encoding="utf8"))
        t = d["type"].split(":")[-1]
        if "result" in d:
            res = d["result"].get("id", "?") + "x" + str(d["result"].get("count", 1))
        else:
            res = "?"
        if "ingredients" in d:
            comps = [ing(i) for i in d["ingredients"]]
        elif "pattern" in d:
            comps = [k + "=" + ing(v) for k, v in d.get("key", {}).items()]
            comps += ["pattern:" + "/".join(d.get("pattern", []))]
        else:
            comps = []
        extra = ""
        if "central" in d:
            extra = "| central=" + ing(d["central"])
        if "components" in d:
            extra += " || " + ", ".join(ing(c) for c in d["components"])
        if "aspects" in d:
            extra += "| " + ",".join("%s %d" % (k, v) for k, v in d["aspects"].items())
        if "instability" in d:
            extra += "| inst=%d" % d["instability"]
        print(os.path.basename(f), "|", t, "|", res, "|", ", ".join(comps), extra)
