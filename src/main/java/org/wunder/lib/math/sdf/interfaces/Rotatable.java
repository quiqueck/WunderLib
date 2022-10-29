package org.wunder.lib.math.sdf.interfaces;

public interface Rotatable extends RootedSDF, Transformable {
    void rotate(double angle);
}
