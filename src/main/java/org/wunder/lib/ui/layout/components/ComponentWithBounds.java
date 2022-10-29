package org.wunder.lib.ui.layout.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.wunder.lib.ui.layout.values.Rectangle;

@Environment(EnvType.CLIENT)
public interface ComponentWithBounds {
    Rectangle getRelativeBounds();
}
