package com.skydeity.thaumicdyesexplor.compat;

import com.skydeity.thaumicdyesexplor.ThaumicDyesExplor;
import com.skydeity.thaumicdyesexplor.component.RunicUpgrades;
import com.skydeity.thaumicdyesexplor.item.RunicUpgradeHolder;
import com.skydeity.thaumicdyesexplor.registry.ModDataComponents;

import dev.tc4port.thaumcraft.api.recipe.InfusionInputPatch;
import dev.tc4port.thaumcraft.api.recipe.InfusionInputPatchApi;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 符文升级的注魔写入端（{@code input_patch} 扩展键 {@code thaumicdyesexplor:runic_upgrade}）。
 *
 * <h2>为什么不是 {@code result}</h2>
 * <p>源模组 {@code ConfigRecipes} 里每条升级注魔都是
 * {@code addInfusionCraftingRecipe("TD.RUNICARMORUPGRADES", new Object[]{"upgrade", (byte)n}, ...)}——
 * 最后那个 {@code Object[]} 是<b>打到中央物品上的 NBT 补丁</b>，物品本体原样保留
 * （耐久、染色、别的槽位上的升级都还在）。1.21.1 里对应的就是 {@code input_patch}：</p>
 * <ul>
 *   <li>{@code component_patch} 是<b>静态</b>补丁，写不出「在已有列表后面追加一项」；</li>
 *   <li>{@code result} 会<b>整个换掉</b>中央物品（新物品、满耐久、丢掉染色与其它升级），
 *       与源模组行为不符，也是升级装上后护甲“变新”的原因；</li>
 *   <li>{@code input_patch} 收到的是一份中央物品的<b>拷贝</b>，可以按配方里的整数参数
 *       就地改组件 —— 正是源模组 NBT 补丁的语义。</li>
 * </ul>
 *
 * <p>注册必须早于数据包配方解析（见 {@code ThaumicDyesExplor#commonSetup}），
 * 否则 {@code InfusionInputPatchApi.validate} 会在解码阶段抛
 * “Unregistered infusion input patch”。</p>
 */
public final class RunicInfusionPatch implements InfusionInputPatch {

    /** 扩展键 id：配方里写 {@code "input_patch": {"thaumicdyesexplor:runic_upgrade": 2}}。 */
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ThaumicDyesExplor.MOD_ID, "runic_upgrade");

    /**
     * 配方解码期的静态校验：参数必须是 1~10 的升级 id。
     *
     * <p>这里不校验「这件护甲能不能吃这个升级」—— 配方用的是标签（任意符文甲），
     * 具体哪件在 {@link #apply} 里按槽位处理，与源模组「装到没槽的甲上就覆盖第一格」一致。</p>
     */
    @Override
    public void validate(int value, int componentCount) {
        if (value < 1 || value > RunicUpgrades.MAX_UPGRADE) {
            throw new IllegalArgumentException("runic upgrade id out of range: " + value);
        }
        if (componentCount < 1) {
            throw new IllegalArgumentException("runic upgrade infusion needs pedestal components");
        }
    }

    /** 把升级写进中央护甲的升级槽：先填空槽，槽满覆盖第一格（{@link RunicUpgrades#with}）。 */
    @Override
    public void apply(ItemStack output, int value, HolderLookup.Provider registries) {
        int slots = slotsOf(output);
        if (slots <= 0) {
            return; // 标签里塞了非符文甲（数据包作者改标签）时静默跳过，不让注魔崩
        }
        output.set(ModDataComponents.RUNIC_UPGRADES, RunicUpgrades.of(output).with(value, slots));
    }

    /** 该物品的升级槽数（非符文甲返回 0）。 */
    public static int slotsOf(ItemStack stack) {
        if (stack.getItem() instanceof RunicUpgradeHolder holder) {
            return holder.upgradeSlots();
        }
        return 0;
    }

    /** 在模组构造期（common setup）注册，两个物理侧都要装。 */
    public static void register() {
        InfusionInputPatchApi.register(ID, new RunicInfusionPatch());
        ThaumicDyesExplor.LOGGER.info("[TDE] 符文升级注魔补丁已注册：{}", ID);
    }

    private RunicInfusionPatch() {
    }
}
