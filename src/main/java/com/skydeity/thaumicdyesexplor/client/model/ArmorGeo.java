package com.skydeity.thaumicdyesexplor.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 1.7.10 {@code ModelRenderer} 几何到 1.21.1 {@link ModelPart} 的最小等价映射。
 *
 * <p>源模组的护甲模型是在 {@code ModelBiped} 基础上追加自定义 {@code ModelRenderer}
 * 盒体（UV 按 128×64 的“模型单位”计算，贴图实际为 256×128）。1.21.1 的
 * {@link HumanoidModel#createMesh(CubeDeformation, float)} 提供同样的原版盒体布局，
 * 只要<b>用 (texWidth, texHeight) 烘焙</b>，UV 就会落在与 1.7.10 相同的像素位置。</p>
 *
 * <p>每个 {@code ModelRenderer} 对应一个 {@link ModelPart}：盒体坐标相对该部件的
 * rotationPoint，旋转绕该点进行 —— 与 1.7.10 的 {@code setRotationPoint} /
 * {@code addBox} 语义一致。</p>
 */
public final class ArmorGeo {

    /** 原版人形模型自带的七个顶层部件名（{@code HumanoidModel.createMesh} 里的键）。 */
    private static final Set<String> TOP_LEVEL = Set.of(
            "head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg");

    /** 一个盒体定义（对应 1.7.10 的一次 {@code addBox}）。 */
    public record Cube(String part, String parent, int u, int v,
                       float x, float y, float z, float w, float h, float d, float inflate,
                       float rx, float ry, float rz,
                       float px, float py, float pz,
                       boolean mirror) {
    }

    private final int texWidth;
    private final int texHeight;
    private final float grow;
    private final List<Cube> cubes;

    private ArmorGeo(int texWidth, int texHeight, float grow, List<Cube> cubes) {
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.grow = grow;
        this.cubes = cubes;
    }

    public int texWidth() {
        return this.texWidth;
    }

    public int texHeight() {
        return this.texHeight;
    }

    /**
     * 烘焙成 {@code HumanoidModel} 需要的根部件。
     *
     * @param hidden 需要隐藏的部件名判定（源模组里由 NBT 开关的护目镜/面具/卷轴等）；
     *               传 {@code null} 表示全部显示
     * @return 根 {@link ModelPart}（已按本几何的纹理尺寸烘焙）
     */
    public ModelPart bake(Predicate<String> hidden) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(this.grow), 0.0F);
        PartDefinition root = mesh.getRoot();

        // 按父级名称分组，逐层挂载：先建父子关系，再统一填充盒体
        Map<String, PartDefinition> created = new HashMap<>();
        // 父 -> 子列表，用于按依赖顺序创建
        Map<String, List<Cube>> byPart = new LinkedHashMap<>();
        Map<String, String> parentOf = new HashMap<>();
        Map<String, PartPose> poseOf = new HashMap<>();
        for (Cube cube : this.cubes) {
            byPart.computeIfAbsent(cube.part(), k -> new ArrayList<>()).add(cube);
            parentOf.putIfAbsent(cube.part(), cube.parent());
            poseOf.putIfAbsent(cube.part(), PartPose.offsetAndRotation(
                    cube.px(), cube.py(), cube.pz(), cube.rx(), cube.ry(), cube.rz()));
        }

        int guard = 0;
        while (created.size() < byPart.size() && guard++ < 512) {
            for (Map.Entry<String, List<Cube>> entry : byPart.entrySet()) {
                String name = entry.getKey();
                if (created.containsKey(name)) {
                    continue;
                }
                String parentName = parentOf.get(name);
                PartDefinition parent;
                if (created.containsKey(parentName)) {
                    parent = created.get(parentName);
                } else if (!byPart.containsKey(parentName)) {
                    // 原版部件（head / hat / body / right_arm ...）
                    parent = root.getChild(parentName);
                } else {
                    continue; // 等父级先建好
                }
                if (parent == null) {
                    continue;
                }
                CubeListBuilder builder = CubeListBuilder.create();
                for (Cube cube : entry.getValue()) {
                    builder.texOffs(cube.u(), cube.v());
                    if (cube.mirror()) {
                        builder.mirror();
                    }
                    builder.addBox(cube.x(), cube.y(), cube.z(), cube.w(), cube.h(), cube.d(),
                            new CubeDeformation(cube.inflate()));
                    if (cube.mirror()) {
                        builder.mirror(false);
                    }
                }
                created.put(name, parent.addOrReplaceChild(safeName(name), builder, poseOf.get(name)));
            }
        }
        ModelPart baked = root.bake(this.texWidth, this.texHeight);

        // 烘焙后再按名字把“条件显示”部件关掉：ModelPart 的 children 是私有的，
        // 只能顺着 parent 链一路 getChild 下去定位。
        if (hidden != null) {
            for (String name : byPart.keySet()) {
                if (!hidden.test(name)) {
                    continue;
                }
                ModelPart part = resolve(baked, name, parentOf);
                if (part != null) {
                    part.visible = false;
                }
            }
        }
        return baked;
    }

    /** 沿父级链解析出烘焙后的 {@link ModelPart}；找不到返回 {@code null}。 */
    private static ModelPart resolve(ModelPart baked, String name, Map<String, String> parentOf) {
        List<String> path = new ArrayList<>();
        String current = name;
        int guard = 0;
        while (current != null && !TOP_LEVEL.contains(current) && guard++ < 64) {
            path.add(0, safeName(current));
            current = parentOf.get(current);
        }
        if (current == null) {
            return null;
        }
        try {
            ModelPart part = baked.getChild(current);
            for (String step : path) {
                part = part.getChild(step);
            }
            return part;
        } catch (RuntimeException missing) {
            return null;
        }
    }

    /** 部件名里含 {@code #} 等非法字符时替换为下划线。 */
    private static String safeName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            sb.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
        }
        return sb.toString();
    }

    /** 几何构建器（由 {@code tools/port_armor_models.py} 生成代码调用）。 */
    public static final class Builder {

        private final int texWidth;
        private final int texHeight;
        private final float grow;
        private final List<Cube> cubes = new ArrayList<>();

        public Builder(int texWidth, int texHeight, float grow) {
            this.texWidth = texWidth;
            this.texHeight = texHeight;
            this.grow = grow;
        }

        /**
         * 追加一个盒体。
         *
         * @param part    部件名（对应源模组 ModelRenderer 字段名）
         * @param parent  父部件（head/hat/body/right_arm/left_arm/right_leg/left_leg 或另一部件名）
         */
        public Builder cube(String part, String parent, int u, int v,
                            float x, float y, float z, float w, float h, float d, float inflate,
                            float rx, float ry, float rz,
                            float px, float py, float pz,
                            boolean mirror) {
            this.cubes.add(new Cube(part, parent, u, v, x, y, z, w, h, d, inflate,
                    rx, ry, rz, px, py, pz, mirror));
            return this;
        }

        /** 没有盒体、仅作为父节点的部件。 */
        public Builder empty(String part, String parent, float rx, float ry, float rz) {
            return this;
        }

        public ArmorGeo build() {
            return new ArmorGeo(this.texWidth, this.texHeight, this.grow, List.copyOf(this.cubes));
        }
    }
}
