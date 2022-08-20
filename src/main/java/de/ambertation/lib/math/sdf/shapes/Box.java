package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Quaternion;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape {
    public static final Codec<Box> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Bounds.CODEC.fieldOf("bounds").forGetter(BaseShape::getBoundingBox),
                    Codec.INT.fieldOf("material").orElse(0).forGetter(BaseShape::getMaterialIndex)
            )
            .apply(instance, Box::new)
    );

    public static final KeyDispatchDataCodec<Box> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Box(Bounds b, int matIndex) {
        super(b, matIndex);
    }

    public Box(Bounds b) {
        this(b, 0);
    }

    public Box(Float3 center, Float3 size) {
        super(Bounds.ofBox(center, size), 0);
    }

    public static double angle = 0;

    @Override
    public double dist(Float3 pos) {
        Quaternion rot = Quaternion.ofAxisAngle(Float3.Y_AXIS, angle);
        Float3 q = pos.sub(getCenter()).rotate(rot).abs().sub(bounds.getHalfSize());
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

    public Float3 getSize() {
        return bounds.getSize();
    }

    @Override
    public Bounds getBoundingBox() {
        Quaternion rot = Quaternion.ofAxisAngle(Float3.Y_AXIS, angle);
        Bounds local = super.getBoundingBox().moveToCenter(Float3.ZERO);
        Bounds fresh = Bounds.of(Float3.ZERO, Float3.ZERO);
        for (Bounds.Interpolate i : Bounds.Interpolate.CORNERS) {
            fresh = fresh.encapsulate(local.get(i).rotate(rot).conservative());
        }
        return fresh.moveToCenter(super.getBoundingBox().getCenter());
        //return super.getBoundingBox();
    }
}
