package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

//based on https://iquilezles.org/articles/ellipsoids/
public class Ellipsoid extends BaseShape {
    public static final Codec<Ellipsoid> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Bounds.CODEC.fieldOf("bounds").forGetter(BaseShape::getBoundingBox)
            )
            .apply(instance, Ellipsoid::new)
    );

    public static final KeyDispatchDataCodec<Ellipsoid> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Ellipsoid(Bounds b) {
        super(b);
    }

    public Ellipsoid(Float3 center, Float3 size) {
        super(Bounds.ofBox(center, size));
    }

    @Override
    public double dist(Float3 pos) {
        Float3 size = bounds.getSize();
        pos = pos.sub(getCenter());
        double k1 = pos.div(size).length();
        double k2 = pos.div(size.square()).length();

        return k1 * (k1 - 1.0) / k2;
    }

    public Float3 getSize() {
        return bounds.getSize();
    }

}

