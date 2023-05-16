package de.ambertation.wunderlib.ui.layout.components.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderlib.ui.layout.values.Rectangle;

@Environment(EnvType.CLIENT)
public interface ComponentRenderer {
    void renderInBounds(
            PoseStack stack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    );
}
