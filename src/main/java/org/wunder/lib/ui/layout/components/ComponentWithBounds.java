package org.wunder.lib.ui.layout.components;

import org.wunder.lib.ui.layout.values.Rectangle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ComponentWithBounds {
    Rectangle getRelativeBounds();
}
