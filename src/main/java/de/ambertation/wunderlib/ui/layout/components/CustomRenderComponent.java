package de.ambertation.wunderlib.ui.layout.components;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderlib.ui.layout.components.render.ComponentRenderer;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Value;

@Environment(EnvType.CLIENT)
public abstract class CustomRenderComponent<C extends CustomRenderComponent<C>> extends LayoutComponent<CustomRenderComponent.CustomRenderRenderer<C>, C> {
    public CustomRenderComponent(
            Value width,
            Value height
    ) {
        super(width, height, new CustomRenderRenderer<>());
        renderer.linkedComponent = (C) this;
    }

    protected abstract void customRender(
            PoseStack stack,
            int x,
            int y,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    );

    protected static class CustomRenderRenderer<C extends CustomRenderComponent<C>> implements ComponentRenderer {
        C linkedComponent;

        @Override
        public void renderInBounds(
                PoseStack stack,
                int mouseX,
                int mouseY,
                float deltaTicks,
                Rectangle bounds,
                Rectangle clipRect
        ) {
            if (linkedComponent != null) {
                linkedComponent.customRender(stack, mouseX, mouseY, deltaTicks, bounds, clipRect);
            }
        }
    }
}
