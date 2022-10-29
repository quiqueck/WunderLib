package org.wunder.lib.ui.layout.components.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.wunder.lib.ui.layout.values.Rectangle;

@Environment(EnvType.CLIENT)
public class NullRenderer implements ComponentRenderer {
    @Override
    public void renderInBounds(
            PoseStack stack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {

    }
}
