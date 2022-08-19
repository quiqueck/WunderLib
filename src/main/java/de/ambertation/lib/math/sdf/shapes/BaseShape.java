package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;
import de.ambertation.lib.math.sdf.interfaces.BoundedShape;
import de.ambertation.lib.math.sdf.interfaces.MaterialProvider;

public abstract class BaseShape extends SDF implements MaterialProvider, BoundedShape {
    protected Bounds bounds;
    protected int materialIndex;

    protected BaseShape(Bounds bounds, int materialIndex) {
        super(0);
        this.bounds = bounds;
        this.materialIndex = materialIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + graphIndex + "]";
    }

    public Float3 getCenter() {
        return bounds.getCenter();
    }

    public void setCenter(Float3 center) {
        bounds = bounds.moveToCenter(center);
    }

    @Override
    public Bounds getBoundingBox() {
        return bounds;
    }

    @Override
    public void setFromBoundingBox(Bounds b) {
        bounds = b;
        this.emitChangeEvent();
    }

    @Override
    public int getMaterialIndex() {
        return materialIndex;
    }

    @Override
    public void setMaterialIndex(int newMaterialIndex) {
        this.materialIndex = newMaterialIndex;
    }
}
