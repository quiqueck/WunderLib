package de.ambertation.lib.ui.layout.components.render;

import de.ambertation.lib.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
