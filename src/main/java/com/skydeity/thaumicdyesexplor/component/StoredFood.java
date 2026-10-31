package com.skydeity.thaumicdyesexplor.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 营养护符里囤着的口粮。
 *
 * <p>对应 1.7.10 版本往物品 NBT 里写的 {@code food}/{@code saturation} 两个 float。
 * 两者各自封顶 {@value #CAP}，喂食时按「先填饥饿、再填饱和」的顺序取出。</p>
 *
 * @param food       食物点（等价于饥饿值点数）
 * @param saturation 饱和点
 */
public record StoredFood(int food, float saturation) {

    /** 食物点与饱和点各自的存储上限。 */
    public static final int CAP = 100;

    public static final StoredFood EMPTY = new StoredFood(0, 0.0F);

    public static final Codec<StoredFood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("food").forGetter(StoredFood::food),
            Codec.FLOAT.fieldOf("saturation").forGetter(StoredFood::saturation)
    ).apply(instance, StoredFood::new));

    public static final StreamCodec<ByteBuf, StoredFood> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StoredFood::food,
            ByteBufCodecs.FLOAT, StoredFood::saturation,
            StoredFood::new);

    /** 是否已经吃满，吃满后不再吸收快捷栏里的食物。 */
    public boolean isFull() {
        return this.food >= CAP && this.saturation >= CAP;
    }

    /** 存入一份口粮，两个数值各自封顶。 */
    public StoredFood plus(int foodPoints, float saturationPoints) {
        int nextFood = Math.min(CAP, this.food + Math.max(0, foodPoints));
        float nextSaturation = Math.min(CAP, this.saturation + Math.max(0.0F, saturationPoints));
        return new StoredFood(nextFood, nextSaturation);
    }

    /** 取走指定量的食物点，返回剩余口粮与真正取出的量。 */
    public StoredFood minusFood(int amount) {
        int taken = Math.min(this.food, Math.max(0, amount));
        return new StoredFood(this.food - taken, this.saturation);
    }

    /** 取走指定量的饱和点。 */
    public StoredFood minusSaturation(float amount) {
        float taken = Math.min(this.saturation, Math.max(0.0F, amount));
        return new StoredFood(this.food, this.saturation - taken);
    }
}
