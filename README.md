# Thaumic Dyes Explor

**模组名**：Thaumic Dyes Explor
**作者**：skydeity
**ModID**：`thaumicdyesexplor`
**版本**：0.1.0

将两个 1.7.10 TC4 附属模组迁移到 Minecraft 1.21.1 NeoForge（Thaumcraft 4R）：
- [Thaumic-Dyes](https://github.com/KryptonCaptain/Thaumic-Dyes)（KryptonCaptain）
- [Thaumic_Exploration](https://github.com/Flaxbeard/Thaumic_Exploration)（Flaxbeard）

## 前置依赖

| 依赖 | 说明 |
| --- | --- |
| Thaumcraft 4R | `thaumcraft`（`[4.2.3.5,)`），必选前置 |
| 前置 API jar | `.none/1.21.1-neoforge-20753-dev-revealing-abilities-snapshot/` |

> 前置源码与快照在 `.none/` 目录（已 gitignore，不进版本库）。

## 构建

```bash
gradlew.bat build
```

需要 JDK 21（foojay resolver 会自动下载）。

## 迁移内容

- **86 件装备/物品**：Thaumic Dyes 的染袍/堡垒甲/骑士甲/领袖甲/游侠甲/弓箭手甲/符文护甲
  I/II/III/符文护目镜/符文旅者靴/旅者靴/古代面具/织物材料；Thaumic Exploration 的
  流星靴/彗星靴/水晶护甲/饰品/法杖核心/杖帽/封印/材料。
- **要素（Aspect）**：通过 `data/thaumicdyesexplor/thaumcraft/object_aspects/definitions.json` 数据驱动注册。
- **装备能力**：通过 `data/thaumicdyesexplor/data_maps/item/equipment_abilities.json` 声明
  （符文护盾、护目镜揭示、旅者靴掉落减免、Vis 减免）。
- **扭曲值**：`data/thaumicdyesexplor/data_maps/item/equipped_warp.json`。

## 目录结构

```
src/main/java/com/skydeity/thaumicdyesexplor/
├── ThaumicDyesExplor.java       # 主类 @Mod
├── item/                         # 物品类（护甲基类、符文护甲、旅者靴）
│   ├── SimpleArmorItem.java
│   ├── RunicArmorItem.java
│   └── TravellerBootsItem.java
└── registry/                     # 注册表
    ├── ModArmorMaterials.java    # 护甲材质
    ├── ModItems.java             # 物品注册
    └── ModTab.java               # 创造标签

src/main/resources/
├── META-INF/neoforge.mods.toml
├── assets/thaumicdyesexplor/     # 纹理、模型、语言
└── data/thaumicdyesexplor/       # 要素、装备能力、扭曲数据
```
