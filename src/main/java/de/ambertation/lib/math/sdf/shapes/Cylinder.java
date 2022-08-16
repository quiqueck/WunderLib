package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float2;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

// https://iquilezles.org/articles/distfunctions/
public class Cylinder extends BaseShape {
    public static final Codec<Cylinder> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Bounds.CODEC.fieldOf("bounds").forGetter(BaseShape::getBoundingBox),
                    Codec.INT.fieldOf("material").orElse(0).forGetter(BaseShape::getMaterialIndex)
            )
            .apply(instance, Cylinder::new)
    );

    public static final KeyDispatchDataCodec<Cylinder> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    public Cylinder(Bounds b, int matIndex) {
        super(b, matIndex);
    }

    public Cylinder(Bounds b) {
        this(b, 0);
    }


    public Cylinder(Float3 center, double height, double radius) {
        super(Bounds.ofCylinder(center, height, radius), 0);
    }

    public double getHeight() {
        return bounds.getHalfSize().y;
    }


    public double getRadius() {
        return bounds.getHalfSize().x;
    }


    @Override
    public double dist(Float3 p) {
        Float2 d = Float2.of(p.xz().length(), p.y).abs().sub(bounds.getHalfSize().zx());
        return Math.min(d.maxComp(), 0.0) + d.max(0.0).length();
    }
}