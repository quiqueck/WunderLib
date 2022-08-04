package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;

public class Empty extends SDF {
    public static final Empty INSTANCE = new Empty();
    public static final Codec<Empty> DIRECT_CODEC = Codec.unit(() -> INSTANCE);
    public static final KeyDispatchDataCodec<Empty> CODEC = KeyDispatchDataCodec.of(DIRECT_CODEC);

    protected Empty() {
        super(0);
    }

    @Override
    public KeyDispatchDataCodec<? extends SDF> codec() {
        return CODEC;
    }


    //-------------------------------------------------------------------------------
    @Override
    public double dist(Float3 pos) {
        return Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "Empty";
    }
}
