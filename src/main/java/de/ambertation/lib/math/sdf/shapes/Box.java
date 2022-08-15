package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape {
    public static final Codec<Box> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Bounds.CODEC.fieldOf("bounds").forGetter(BaseShape::getBoundingBox)
            )
            .apply(instance, Box::new)
    );

    public static final KeyDispatchDataCodec<Box> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Box(Bounds b) {
        super(b);
    }

    public Box(Float3 center, Float3 size) {
        super(Bounds.ofBox(center, size));
    }

    @Override
    public double dist(Float3 pos) {
        Float3 q = pos.sub(getCenter()).abs().sub(bounds.getHalfSize());
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

    public Float3 getSize() {
        return bounds.getSize();
    }
}
