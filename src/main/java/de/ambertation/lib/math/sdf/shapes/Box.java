package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Quaternion;
import de.ambertation.lib.math.Transform;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

import org.jetbrains.annotations.NotNull;

// https://iquilezles.org/articles/distfunctions/
public class Box extends BaseShape {
    public static final Codec<Box> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Transform.CODEC.fieldOf("transform").orElse(Transform.IDENTITY).forGetter(o -> o.transform),
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
    public Box(Transform t, int matIndex) {
        super(t.getBoundingBoxUnrotated(), matIndex);
        this.transform = t;
    }

    public Box(Bounds b) {
        this(Transform.of(b), 0);
    }

    public Box(Float3 center, Float3 size) {
        this(Transform.of(center, size), 0);
    }

    @NotNull
    public Transform transform;

    public void rotate(double angle) {
        transform = transform.rotate(Quaternion.ofAxisAngle(Float3.Y_AXIS, angle));
    }

    @Override
    public double dist(Float3 pos) {
        Float3 q = pos.sub(getCenter()).unRotate(transform.rotation).abs().sub(getSize().sub(1).div(2));
        return q.max(0.0).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), 0.0);
    }

    public Float3 getSize() {
        return transform.size;
    }


    @Override
    public Float3 getCenter() {
        return transform.center.blockAligned();
    }

    @Override
    public Bounds getBoundingBox() {
        return transform.getBoundingBoxWorldSpace();
    }

    @Override
    public void setFromBoundingBox(Bounds b) {
        System.out.println("new Bounds: " + b);
        System.out.println("         -> " + b.rotate(transform.rotation.inverted()));
        //super.setFromBoundingBox(b.rotate(rotation.inverted()));
    }
}
