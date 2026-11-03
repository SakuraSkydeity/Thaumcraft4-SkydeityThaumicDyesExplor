package com.skydeity.thaumicdyesexplor.client.model;

import java.util.Locale;

import com.skydeity.thaumicdyesexplor.item.ArmorGeoType;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * 用 {@link ArmorGeo} 烘焙出来的护甲人形模型。
 *
 * <p>1.7.10 的护甲模型直接继承 {@code ModelBiped} 并追加自定义盒体；1.21.1 对应的做法是
 * 继承 {@link HumanoidModel}，把自定义盒体作为原版部件的子部件挂上去（原版盒子本身
 * 在 {@link ArmorGeo#bake} 里置空，对应源模组的 {@code cubeList.clear()}）。</p>
 *
 * <p>源模组里按佩戴者状态显隐的部件（堡垒甲套装等级、护目镜、面具、符文 III 的整套进阶件）
 * 由 {@link ArmorPartRules} 按状态在<b>烘焙时</b>决定 —— 不同状态缓存不同的烘焙结果
 * （见 {@code ArmorGeoModels}），渲染期不再改部件可见性。</p>
 *
 * <p><b>为什么要自己按槽位裁剪（{@link #applySlotVisibility}）：</b>
 * {@code HumanoidArmorLayer.renderArmorPiece} 的顺序是
 * <i>先</i> {@code setPartVisibility(原版模型, slot)}、<i>后</i> {@code getArmorModelHook(...)}
 * 换成我们的模型，所以那段裁剪作用在<b>原版模型</b>上，对我们返回的自定义模型完全无效。
 * 不补这一步的话四个护甲槽会各自把整份几何画一遍（同一个部件被 4 张贴图重复覆盖、
 * 还互相 z-fighting），表现为「肩甲 / 裤腿 时有时无、串色」。源模组对应的是
 * {@code getArmorModel} 里那七行 {@code bipedXxx.showModel = armorSlot == N}。</p>
 *
 * <p><b>衣摆 / 披风摆动（{@link #setupAnim}）：</b>源模组的 {@code render()} 在每帧按
 * 步行摆幅动态改写 Frontcloth / ClothBack / Cloak / LegCloth 的 {@code rotateAngleX}
 * （站立时是构造器里的静态角，走路时叠加摆动）。烘焙进去的静态角只是“站立姿势”，
 * 这里覆写 {@code setupAnim} 在姿势设置完后按同一组公式逐帧改写。</p>
 */
public class GeoArmorModel extends HumanoidModel<LivingEntity> {

    private final ArmorGeo geo;
    private final ArmorGeoType geoType;
    private final ModelPart rootPart;

    public GeoArmorModel(ArmorGeo geo, ArmorGeoType type, ArmorWearerState state) {
        // bake 的 predicate 语义是「true = 隐藏」，而 ArmorPartRules.visible 是「true = 显示」，
        // 必须取反 —— 否则所有本应显示的部件被整批隐藏，穿上身后模型全空。
        this(geo.bake(part -> !ArmorPartRules.visible(type, state, part)), geo, type);
    }

    /** 旧签名兼容：全部条件部件显示。 */
    public GeoArmorModel(ArmorGeo geo) {
        this(geo.bake(null), geo, ArmorGeoType.NONE);
    }

    private GeoArmorModel(ModelPart root, ArmorGeo geo, ArmorGeoType type) {
        super(root, RenderType::entityCutoutNoCull);
        this.geo = geo;
        this.geoType = type;
        this.rootPart = root;
        // EntityModel 的构造函数把 young 初始化成 true（动物幼崽模型的默认值）。
        // 留着它的话 AgeableListModel#renderToBuffer 会走「幼年」分支：
        // 头部件缩放 ×2 再上移 1.0，身体部件缩放 ×0.5 再上移 1.5 ——
        // 头盔变成两倍大、肩甲与裤腿被缩到腰上，看起来就是「肩甲和裤腿没做出来」。
        // 护甲模型本身没有幼年形态（源模组只在 render 里按 isChild 做 GL 缩放），固定关掉。
        this.young = false;
    }

    public ArmorGeo geo() {
        return this.geo;
    }

    /**
     * 只显示该护甲槽该显示的顶层部件 —— 逐条对应源模组 {@code getArmorModel} 里的
     * {@code bipedHead.showModel = armorSlot == 0} 等七行。
     *
     * <p>只动七个顶层部件，<b>不能</b>用 {@code setAllVisible(false)}：那会把所有子部件
     * （也就是整个自定义几何）一起关掉，而之后只重开顶层，子部件再也回不来。</p>
     */
    public void applySlotVisibility(EquipmentSlot slot) {
        boolean head = slot == EquipmentSlot.HEAD;
        boolean chest = slot == EquipmentSlot.CHEST;
        boolean legs = slot == EquipmentSlot.LEGS;
        // 源模组：bipedBody 在护腿槽也显示（裙摆、腰带、腰垂都挂在 body 下）
        this.head.visible = head;
        this.hat.visible = head;
        this.body.visible = chest || legs;
        this.rightArm.visible = chest;
        this.leftArm.visible = chest;
        this.rightLeg.visible = legs || slot == EquipmentSlot.FEET;
        this.leftLeg.visible = legs || slot == EquipmentSlot.FEET;
    }

    /**
     * 姿势设置完之后，按源模组 {@code render()} 里的公式改写衣摆 / 披风的俯仰角。
     */
    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        swingClothes(limbSwing, limbSwingAmount, ageInTicks);
    }

    /**
     * 逐条对照源模组 {@code render()} 里的「TODO special rotations」段：
     * <pre>{@code
     * float a = cos(f * 0.6662) * 1.4 * f1;          // 右腿相位
     * float b = cos(f * 0.6662 + π) * 1.4 * f1;      // 左腿相位
     * float c = min(a, b);
     * }</pre>
     * 其中 f = limbSwing、f1 = limbSwingAmount。各套外观参与的部件不同：
     * 染袍 / 游侠 / 弓箭手是前后衣摆 + 后布片，骑士 / 领袖 / 圣骑士是前摆 + 三片披风
     * （领袖 / 圣骑士另有随单腿摆的腿布），符文 III 的披风改成呼吸摆动。
     */
    private void swingClothes(float limbSwing, float limbSwingAmount, float ageInTicks) {
        if (limbSwingAmount <= 0.0F && this.geoType != ArmorGeoType.ROBES_SPECIAL) {
            // 站定时不覆写 —— 烘焙进去的静态角就是源模组构造器里的站立角
            return;
        }
        float a = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float b = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        float c = Math.min(a, b);
        switch (this.geoType) {
            case ROBES, RANGER, RANGER2, ARCHER -> {
                setXRot("FrontclothR1", c - 0.1047198F);
                setXRot("FrontclothL1", c - 0.1047198F);
                setXRot("FrontclothR2", c - 0.3316126F);
                setXRot("FrontclothL2", c - 0.3316126F);
                setXRot("ClothBackR1", -c + 0.1047198F);
                setXRot("ClothBackL1", -c + 0.1047198F);
                setXRot("ClothBackR2", -c + 0.2268928F);
                setXRot("ClothBackL2", -c + 0.2268928F);
                setXRot("ClothBackR3", -c + 0.2268928F);
                setXRot("ClothBackL3", -c + 0.2268928F);
            }
            case ROBES_SPECIAL -> {
                // 源模组把 ClothBack 系列的摆动注释掉了（保持构造器静态角），披风改成呼吸摆动
                setXRot("FrontclothR1", c - 0.1047198F);
                setXRot("FrontclothL1", c - 0.1047198F);
                setXRot("FrontclothR2", c - 0.3316126F);
                setXRot("FrontclothL2", c - 0.3316126F);
                setXRot("Cloak1", 0.2F + Mth.sin(ageInTicks / 7.0F) * 0.08F);
            }
            case KNIGHT -> {
                setXRot("Frontcloth1", c - 0.1047198F);
                setXRot("Frontcloth2", c - 0.3316126F);
                setXRot("Cloak1", -c / 2.0F + 0.1396263F);
                setXRot("Cloak2", -c / 2.0F + 0.3069452F);
                setXRot("Cloak3", -c / 2.0F + 0.4465716F);
            }
            case PRAETOR, PALADIN -> {
                setXRot("LegClothR", a - 0.10472F);
                setXRot("LegClothL", b - 0.10472F);
                setXRot("Cloak1", -c / 2.0F + 0.1396263F);
                setXRot("Cloak2", -c / 2.0F + 0.3069452F);
                setXRot("Cloak3", -c / 2.0F + 0.4465716F);
            }
            default -> {
            }
        }
    }

    private void setXRot(String partName, float xRot) {
        // 沿 ArmorGeo 的父链解析部件（ModelPart 的 children 是私有的，不能直接遍历）
        ModelPart part = this.geo.resolve(this.rootPart, normalize(partName));
        if (part != null) {
            part.xRot = xRot;
        }
    }

    /** 部件名规范化（小写 + '#' 转 '_'，与 {@link ArmorGeo} 的 safeName 一致）。 */
    static String normalize(String partName) {
        return partName.toLowerCase(Locale.ROOT).replace('#', '_');
    }
}
