package org.wunder.lib.math.sdf.interfaces;

import org.wunder.lib.math.Bounds;
import org.wunder.lib.math.Float3;
import org.wunder.lib.math.Matrix4;
import org.wunder.lib.math.Transform;

public interface Transformable extends RootedSDF {
    Float3[] getCornersInWorldSpace(boolean blockAligned, Transform localTransform);
    Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned, Transform localTransform);
    Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Transform transform);
    Transform getLocalTransform();
    void setLocalTransform(Transform t);
    Matrix4 getParentTransformMatrix();

    default Float3[] getCornersInWorldSpace(boolean blockAligned) {
        return getCornersInWorldSpace(blockAligned, getLocalTransform());
    }

    default Float3[] getCornersAndCenterInWorldSpace(boolean blockAligned) {
        return getCornersAndCenterInWorldSpace(blockAligned, getLocalTransform());
    }

    default Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned) {
        return getCornerInWorldSpace(corner, blockAligned, getLocalTransform());
    }

    default Matrix4 getWorldTransformMatrix() {
        return getParentTransformMatrix().mul(getLocalTransform().asMatrix());
    }

    boolean isOperation();
}
