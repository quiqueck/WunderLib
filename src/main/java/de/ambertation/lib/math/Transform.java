package de.ambertation.lib.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public class Transform {
    public static final Transform IDENTITY = new Transform(Float3.ZERO, Float3.IDENTITY, Quaternion.IDENTITY);
    public static final Codec<Transform> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Float3.CODEC.fieldOf("center").orElse(Float3.ZERO).forGetter(o -> o.center),
                    Float3.CODEC.fieldOf("size").orElse(Float3.IDENTITY).forGetter(o -> o.size),
                    Quaternion.CODEC.fieldOf("rotation").orElse(Quaternion.IDENTITY).forGetter(o -> o.rotation)
            )
            .apply(instance, Transform::new)
    );

    public final Float3 center;
    public final Float3 size;
    public final Quaternion rotation;

    protected Transform(Float3 center, Float3 size, Quaternion rotation) {
        this.center = center;
        this.size = size;
        this.rotation = rotation;
    }

    public static Transform of(Float3 center, Float3 size, Quaternion rotation) {
        return new Transform(center, size, rotation);
    }

    public static Transform of(Float3 center, Float3 size) {
        return new Transform(center, size, Quaternion.IDENTITY);
    }

    public static Transform of(Bounds b) {
        return new Transform(b.getCenter(), b.getSize(), Quaternion.IDENTITY);
    }

    public Bounds getBoundingBoxWorldSpace() {
        return Bounds.ofBox(center, size).rotate(rotation);
    }

    public Bounds getBoundingBoxUnrotated() {
        return Bounds.ofBox(center, size);
    }

    public Float3[] getCornersInWorldSpace() {
        return getCornersInWorldSpace(false);
    }

    public Float3[] getCornersInWorldSpace(boolean blockAligned) {
        Float3[] corners = new Float3[Bounds.Interpolate.CORNERS.length];
        Bounds localBounds = Bounds.ofBox(Float3.ZERO, size);
        for (int i = 0; i < Bounds.Interpolate.CORNERS.length; i++) {
            Bounds.Interpolate p = Bounds.Interpolate.CORNERS[i];
            corners[i] = localBounds.get(p).rotate(rotation).add(center);
            if (blockAligned) corners[i] = corners[i].blockAligned();
        }

        return corners;
    }


    public Transform translate(Float3 offset) {
        if (offset == null) return this;
        return new Transform(center.add(offset), size, rotation);
    }

    public Transform translate(Vec3 offset) {
        if (offset == null) return this;
        return new Transform(center.add(offset), size, rotation);
    }

    public Transform translateInverted(Float3 offset) {
        if (offset == null) return this;
        return new Transform(center.sub(offset), size, rotation);
    }

    public Transform translateInverted(Vec3 offset) {
        if (offset == null) return this;
        return new Transform(center.sub(offset), size, rotation);
    }

    public Transform scale(Float3 scale) {
        if (scale == null) return this;
        return new Transform(center, size.mul(scale), rotation);
    }

    public Transform rotate(Quaternion rotation) {
        if (rotation == null) return this;
        return new Transform(center, size, this.rotation.mul(rotation));
    }

    public Transform transformBy(Transform t) {
        if (t == null) return t;
        return new Transform(center.add(t.center), size.mul(t.size), this.rotation.mul(t.rotation));
    }

    public Float3 transform(Float3 p) {
        return p.mul(size).rotate(rotation).add(center);
    }

    public Float3 unTransform(Float3 p) {
        return p.sub(center).unRotate(rotation).div(size);
    }

    public Transform addScale(double sx, double sy, double sz) {
        return new Transform(center, size.add(sx, sy, sz), rotation);
    }

    @Override
    public String toString() {
        return "Transform{" +
                "c=" + center +
                ", s=" + size +
                ", r=" + rotation +
                '}';
    }
}
