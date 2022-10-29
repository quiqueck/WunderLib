package org.wunder.lib.ui.layout.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.wunder.lib.ui.layout.components.render.ComponentRenderer;
import org.wunder.lib.ui.layout.components.render.TextProvider;
import org.wunder.lib.ui.layout.values.Rectangle;

@Environment(EnvType.CLIENT)
public class AbstractVanillaComponentRenderer<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> implements ComponentRenderer, TextProvider {
    V linkedComponent;

    protected V getLinkedComponent() {
        return linkedComponent;
    }

    @Override
    public void renderInBounds(
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        if (linkedComponent != null) {
            if (linkedComponent.vanillaComponent != null) {
                if (!linkedComponent.enabled) {
                    linkedComponent.vanillaComponent.setAlpha(linkedComponent.alpha / 2);
                }
                linkedComponent.vanillaComponent.render(poseStack, mouseX, mouseY, deltaTicks);
                if (!linkedComponent.enabled) {
                    linkedComponent.vanillaComponent.setAlpha(linkedComponent.alpha);
                }
            }

        }
    }
}
