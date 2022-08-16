package de.ambertation.lib.math.sdf;

import de.ambertation.lib.WunderLib;
import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.shapes.*;

import com.mojang.serialization.Codec;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

public abstract class SDF {
    public static final class EvaluationData {
        double dist;
        SDF source;

        public EvaluationData() {
            this(Double.MAX_VALUE, new Empty());
        }

        public EvaluationData(double dist, SDF source) {
            this.dist = dist;
            this.source = source;
        }

        public double dist() {
            return dist;
        }

        public SDF source() {
            return source;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EvaluationData) obj;
            return Double.doubleToLongBits(this.dist) == Double.doubleToLongBits(that.dist) &&
                    Objects.equals(this.source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dist, source);
        }

        @Override
        public String toString() {
            return "EvaluationData[" +
                    "dist=" + dist + ", " +
                    "source=" + source + ']';
        }
    }

    protected SDF(int inputCount) {
        inputSlots = new SDF[inputCount];
        parentSlotIndex = -1;
    }

    //---------------------- INPUT SLOTS ----------------------
    protected final SDF[] inputSlots;
    protected int graphIndex = 0;

    public int getInputSlotCount() {
        return inputSlots.length;
    }

    public boolean hasInputSlots() {
        return inputSlots.length > 0;
    }

    public SDF getSlot(int idx) {
        return inputSlots[idx];
    }

    public boolean hasInputs() {
        for (SDF inputSlot : inputSlots) {
            if (inputSlot != null && !(inputSlot instanceof Empty)) return true;
        }
        return false;
    }

    void setSlotSilent(int idx, SDF sdf) {
        if (inputSlots[idx] != sdf && inputSlots[idx] != null) {
            inputSlots[idx].setParent(null);
            inputSlots[idx].setParentSlotIndex(-1);
        }


        inputSlots[idx] = sdf == null ? new Empty() : sdf;
        inputSlots[idx].setParent(this);
        inputSlots[idx].setParentSlotIndex(idx);
        if (idx == 0) {
            inputSlots[idx].setGraphIndexRecursive(graphIndex + 1);
        } else {
            inputSlots[idx].setGraphIndexRecursive(inputSlots[idx - 1].maxGraphIndex() + 1);
        }
    }

    public void setSlot(int idx, SDF sdf) {
        setSlotSilent(idx, sdf);
        this.emitChangeEvent();
    }

    public int inputSlotIndex(SDF sdf) {
        for (int i = 0; i < inputSlots.length; i++) {
            if (inputSlots[i] == sdf) return i;
        }
        return -1;
    }

    private int setGraphIndexRecursive(int startIndex) {
        this.graphIndex = startIndex;
        for (int i = 0; i < inputSlots.length; i++) {
            startIndex = inputSlots[i].setGraphIndexRecursive(startIndex + 1);
        }
        return startIndex;
    }

    private int maxGraphIndex() {
        int idx = this.graphIndex;
        for (int i = 0; i < inputSlots.length; i++) {
            idx = Math.max(idx, inputSlots[i].maxGraphIndex());
        }
        return idx;
    }

    public int getGraphIndex() {
        return graphIndex;
    }

    public SDF getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    public SDF getChildWithGraphIndex(int gIdx) {
        if (gIdx == graphIndex) return this;

        for (int i = 0; i < inputSlots.length; i++) {
            SDF s = inputSlots[i].getChildWithGraphIndex(gIdx);
            if (s != null) return s;
        }

        return null;
    }

    //---------------------- BOUNDING BOX ----------------------
    public Bounds getBoundingBox() {
        Bounds b = Bounds.EMPTY;
        for (SDF sdf : inputSlots) {
            b = b.encapsulate(sdf.getBoundingBox());
        }
        return b;
    }

    //---------------------- CHANGE EVENTS ----------------------
    public interface OnChange {
        void didChange(SDF sdf);
    }

    private final Set<OnChange> changeEvent = new HashSet<>();

    protected void emitChangeEvent() {
        if (changeEvent != null) changeEvent.forEach(e -> e.didChange(this));
        if (parent != null) parent.emitChangeEvent();
    }

    public void addChangeListener(OnChange listener) {
        changeEvent.add(listener);
    }

    public void removeChangeListener(OnChange listener) {
        changeEvent.remove(listener);
    }


    //---------------------- PARENT HANDLING ----------------------
    private SDF parent;
    private int parentSlotIndex;

    void setParent(SDF parent) {
        this.parent = parent;
    }

    public SDF getParent() {
        return parent;
    }

    void setParentSlotIndex(int idx) {
        this.parentSlotIndex = idx;
    }

    //---------------------- EVAlUATION ----------------------
    public void dist(EvaluationData d, Float3 pos) {
        d.dist = dist(pos);
        d.source = this;
    }


    //---------------------- ABSTRACT METHODS ----------------------

    public abstract double dist(Float3 pos);
    public abstract KeyDispatchDataCodec<? extends SDF> codec();


    //---------------------- SDF REGISTRY ----------------------
    public static final MappedRegistry<Codec<? extends SDF>> SDF_REGISTRY = FabricRegistryBuilder
            .<Codec<? extends SDF>>createSimple(null, WunderLib.ID("sdf"))
            .attribute(RegistryAttribute.MODDED)
            .buildAndRegister();

    public static final Codec<SDF> CODEC = SDF_REGISTRY.byNameCodec()
                                                       .dispatch((sdf) -> sdf.codec().codec(), Function.identity());

    static void bootstrap(Registry<Codec<? extends SDF>> registry) {
        register(registry, "union", SDFUnion.CODEC);
        register(registry, "intersect", SDFIntersection.CODEC);
        register(registry, "dif", SDFDifference.CODEC);
        register(registry, "invert", SDFInvert.CODEC);
        register(registry, "move", SDFMove.CODEC);

        register(registry, "empty", Empty.CODEC);
        register(registry, "sphere", Sphere.CODEC);
        register(registry, "box", Box.CODEC);
        register(registry, "cylinder", Cylinder.CODEC);
        register(registry, "prism", Prism.CODEC);
        register(registry, "ellipsoid", Ellipsoid.CODEC);
    }

    static Codec<? extends SDF> register(
            Registry<Codec<? extends SDF>> registry,
            String name,
            KeyDispatchDataCodec<? extends SDF> codec
    ) {
        return Registry.register(registry, WunderLib.ID(name), codec.codec());
    }

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        bootstrap(SDF_REGISTRY);
    }
}
