package de.ambertation.lib.math.sdf;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;

public class SDFMove extends SDFOperation {
    public static final Codec<SDFMove> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SDF.CODEC.fieldOf("sdf").forGetter(b -> b.getFirst()),
                    Float3.CODEC.fieldOf("offset").forGetter(b -> b.getOffset())
            )
            .apply(instance, SDFMove::new)
    );

    public static final KeyDispatchDataCodec<SDFMove> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    private Float3 offset;

    public SDFMove(SDF sdf, Float3 offset) {
        super(sdf);
        this.offset = offset;
    }

    public Float3 getOffset() {
        return offset;
    }

    public void setOffset(Float3 c) {
        this.offset = c;
        this.emitChangeEvent();
    }

    @Override
    public double dist(Float3 pos) {
        return getFirst().dist(pos.sub(offset));
    }

    @Override
    public Bounds getBoundingBox() {
        return getFirst().getBoundingBox().move(offset);
    }
}
