package de.ambertation.lib.math.sdf.interfaces;

import de.ambertation.lib.math.Bounds;
import de.ambertation.lib.math.Float3;
import de.ambertation.lib.math.Matrix4;
import de.ambertation.lib.math.Transform;

public interface Transformable {
    Float3[] getCornersInWorldSpace(boolean blockAligned, Transform localTransform);
    Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned, Transform transform);
    Transform getLocalTransform();
    Matrix4 getParentTransformMatrix();

    default Float3[] getCornersInWorldSpace(boolean blockAligned) {
        return getCornersInWorldSpace(blockAligned, getLocalTransform());
    }

    default Float3 getCornerInWorldSpace(Bounds.Interpolate corner, boolean blockAligned) {
        return getCornerInWorldSpace(corner, blockAligned, getLocalTransform());
    }

    default Matrix4 getWorldTransformMatrix() {
        return getParentTransformMatrix().mul(getLocalTransform().asMatrix());
    }

}
