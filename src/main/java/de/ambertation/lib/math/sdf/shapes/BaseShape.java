package de.ambertation.lib.math.sdf.shapes;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.sdf.SDF;

public abstract class BaseShape extends SDF {
    protected Bounds bounds;

    public BaseShape(Bounds bounds) {
        super(0);
        this.bounds = bounds;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public Float3 getCenter() {
        return bounds.getCenter();
    }

    public void setCenter(Float3 center) {
        bounds = bounds.moveToCenter(center);
    }

    @Override
    public final Bounds getBoundingBox() {
        return bounds;
    }

    public void setFromBoundingBox(Bounds b) {
        bounds = b;
        this.emitChangeEvent();
    }
}
