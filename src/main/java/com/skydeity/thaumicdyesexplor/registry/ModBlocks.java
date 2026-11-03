package com.skydeity.thaumicdyesexplor.registry;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.block.CrucibleOfSoulsBlock;
import com.skydeity.thaumicdyesexplor.block.CrucibleOfSoulsBlockEntity;
import com.skydeity.thaumicdyesexplor.block.EverfullUrnBlock;
import com.skydeity.thaumicdyesexplor.block.EverfullUrnBlockEntity;
import com.skydeity.thaumicdyesexplor.block.MeEssentiaJarBlock;
import com.skydeity.thaumicdyesexplor.block.OblivionJarBlock;
import com.skydeity.thaumicdyesexplor.block.OblivionJarBlockEntity;
import com.skydeity.thaumicdyesexplor.block.ReplicatorBlock;
import com.skydeity.thaumicdyesexplor.block.ReplicatorBlockEntity;
import com.skydeity.thaumicdyesexplor.block.ThinkTankBlock;
import com.skydeity.thaumicdyesexplor.block.ThinkTankBlockEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * MiddleEarth-Thaumaturgy 的方块：压缩精髓罐 ×3（压缩 / 双重压缩 / 群星）与 10 种注入金属存储块。
 *
 * <p>精髓罐容量以“原版奥秘罐 8 vis”为基准（见 {@code MeEssentiaJarBlockEntity#BASE_CENTIVIS}）：
 * 压缩 ×2、双重压缩 ×4、群星罐 ×8。</p>
 *
 * <p>源模组的基础 / 进阶 / 红敏「派系工作台」已在第 21 轮整组删除：它们只是换皮的 3×3 合成台，
 * 真正的派系配方来自 LOTR 的 {@code LOTRRecipes.*}，1.21.1 没有 LOTR 就没有内容可放。</p>
 *
 * <p>第 23 轮再删两个：{@code me_storage_block}（第 13 轮的通用存储块，早被 10 种注入金属存储块
 * 取代，没有配方也没人用）与 {@code me_inventarium_bookshelf}（源模组是 Automagy 的 Inventarium
 * 槽位加成，1.21.1 没有 Automagy，只能当装饰，且没有配方）。</p>
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ThaumicDyesExplor.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,
                    ThaumicDyesExplor.MOD_ID);

    // ============================================================================================
    // 精髓罐
    // ============================================================================================
    public static final DeferredBlock<Block> COMPRESSED_ESSENTIA_JAR =
            BLOCKS.registerBlock("me_compressed_essentia_jar",
                    properties -> new MeEssentiaJarBlock(2, properties), jarProperties());
    public static final DeferredBlock<Block> X2_COMPRESSED_ESSENTIA_JAR =
            BLOCKS.registerBlock("me_x2_compressed_essentia_jar",
                    properties -> new MeEssentiaJarBlock(4, properties), jarProperties());
    public static final DeferredBlock<Block> EXTREME_ESSENTIA_JAR =
            BLOCKS.registerBlock("me_extreme_essentia_jar",
                    properties -> new MeEssentiaJarBlock(8, properties), jarProperties());

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<com.skydeity.thaumicdyesexplor.block.MeEssentiaJarBlockEntity>> ME_ESSENTIA_JAR =
            BLOCK_ENTITIES.register("me_essentia_jar", () -> BlockEntityType.Builder
                    .of(com.skydeity.thaumicdyesexplor.block.MeEssentiaJarBlockEntity::new,
                            COMPRESSED_ESSENTIA_JAR.get(), X2_COMPRESSED_ESSENTIA_JAR.get(),
                            EXTREME_ESSENTIA_JAR.get())
                    .build(null));
    // ============================================================================================
    // Thaumic Exploration —— 五个功能方块（第 33 轮，贴图取自源模组 resources/）
    // ============================================================================================
    public static final DeferredBlock<Block> EVERFULL_URN =
            BLOCKS.registerBlock("everfull_urn", EverfullUrnBlock::new, urnProperties());
    public static final DeferredBlock<Block> OBLIVION_JAR =
            BLOCKS.registerBlock("oblivion_jar", OblivionJarBlock::new, jarProperties());
    public static final DeferredBlock<Block> THINK_TANK =
            BLOCKS.registerBlock("think_tank", ThinkTankBlock::new, jarProperties());
    public static final DeferredBlock<Block> CRUCIBLE_OF_SOULS =
            BLOCKS.registerBlock("crucible_of_souls", CrucibleOfSoulsBlock::new, machineProperties());
    public static final DeferredBlock<Block> REPLICATOR =
            BLOCKS.registerBlock("replicator", ReplicatorBlock::new, machineProperties());

    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<EverfullUrnBlockEntity>> EVERFULL_URN_BE =
            BLOCK_ENTITIES.register("everfull_urn", () -> BlockEntityType.Builder
                    .of(EverfullUrnBlockEntity::new, EVERFULL_URN.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<OblivionJarBlockEntity>> OBLIVION_JAR_BE =
            BLOCK_ENTITIES.register("oblivion_jar", () -> BlockEntityType.Builder
                    .of(OblivionJarBlockEntity::new, OBLIVION_JAR.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<ThinkTankBlockEntity>> THINK_TANK_BE =
            BLOCK_ENTITIES.register("think_tank", () -> BlockEntityType.Builder
                    .of(ThinkTankBlockEntity::new, THINK_TANK.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleOfSoulsBlockEntity>> CRUCIBLE_OF_SOULS_BE =
            BLOCK_ENTITIES.register("crucible_of_souls", () -> BlockEntityType.Builder
                    .of(CrucibleOfSoulsBlockEntity::new, CRUCIBLE_OF_SOULS.get()).build(null));
    public static final net.neoforged.neoforge.registries.DeferredHolder<BlockEntityType<?>, BlockEntityType<ReplicatorBlockEntity>> REPLICATOR_BE =
            BLOCK_ENTITIES.register("replicator", () -> BlockEntityType.Builder
                    .of(ReplicatorBlockEntity::new, REPLICATOR.get()).build(null));

    private static BlockBehaviour.Properties urnProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(1.5F)
                .sound(net.minecraft.world.level.block.SoundType.STONE)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.0F, 6.0F)
                .sound(net.minecraft.world.level.block.SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties jarProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(0.3F)
                .sound(net.minecraft.world.level.block.SoundType.GLASS)
                .noOcclusion();
    }


    // ============================================================================================
    // 中洲存储块（10 种注入锭各一块）：9 锭合 1 块，1 块拆 9 锭
    // ============================================================================================
    public static final java.util.List<DeferredBlock<Block>> STORAGE_VARIANTS = new java.util.ArrayList<>();
    public static final String[] STORAGE_MATERIALS = {
            "gulduril_black_uruk", "ithildin_blue_dwarven", "ithildin_dwarven", "edhelmir_elven",
            "daedelos_galvorn", "stared_mithril", "daedelos_morgul", "durnaur_orc",
            "gulduril_uruk", "brighten_gildediron"};

    static {
        for (String mat : STORAGE_MATERIALS) {
            STORAGE_VARIANTS.add(BLOCKS.registerBlock("me_storage_" + mat,
                    Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
        }
        // 每个方块都要有物品形态——没有 BlockItem 的话战利品表 / 创造栏 / 配方全会炸
        STORAGE_VARIANTS.forEach(ModItems.ITEMS::registerSimpleBlockItem);
        ModItems.ITEMS.registerSimpleBlockItem(EVERFULL_URN);
        ModItems.ITEMS.registerSimpleBlockItem(OBLIVION_JAR);
        ModItems.ITEMS.registerSimpleBlockItem(THINK_TANK);
        ModItems.ITEMS.registerSimpleBlockItem(CRUCIBLE_OF_SOULS);
        ModItems.ITEMS.registerSimpleBlockItem(REPLICATOR);
        ModItems.ITEMS.registerSimpleBlockItem(COMPRESSED_ESSENTIA_JAR);
        ModItems.ITEMS.registerSimpleBlockItem(X2_COMPRESSED_ESSENTIA_JAR);
        ModItems.ITEMS.registerSimpleBlockItem(EXTREME_ESSENTIA_JAR);
    }

    private ModBlocks() {
    }
}
