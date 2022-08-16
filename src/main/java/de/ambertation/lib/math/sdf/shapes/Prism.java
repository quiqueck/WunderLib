package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Prism extends BaseShape {
    public static final Codec<Prism> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Bounds.CODEC.fieldOf("bounds").forGetter(BaseShape::getBoundingBox),
                    Codec.INT.fieldOf("material").orElse(0).forGetter(BaseShape::getMaterialIndex)
            )
            .apply(instance, Prism::new)
    );

    public static final KeyDispatchDataCodec<Prism> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Prism(Bounds b, int matIndex) {
        super(b, matIndex);
    }

    public Prism(Bounds b) {
        this(b, 0);
    }

    public Prism(Float3 center, double sx, double sy) {
        super(Bounds.ofBox(center, new Float3(sx, sy, sx)), 0);
    }

    @Override
    public double dist(Float3 p) {
        Float3 size = bounds.getSize();
        Float3 q = p.sub(getCenter()).abs();
        return Math.max(q.z - size.y, Math.max(q.x * 0.866025 + p.y * 0.5, -p.y) - size.x * 0.5);
    }

    public Float2 getSize() {
        return bounds.getSize().xy();
    }

}
