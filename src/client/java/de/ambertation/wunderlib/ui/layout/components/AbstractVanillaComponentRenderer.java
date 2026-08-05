package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.ComponentRenderer;
import de.ambertation.wunderlib.ui.layout.components.render.TextProvider;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AbstractVanillaComponentRenderer<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> implements ComponentRenderer, TextProvider {
    V linkedComponent;

    protected V getLinkedComponent() {
        return linkedComponent;
    }

    @Override
    public void renderInBounds(
            GuiGraphicsExtractor guiGraphics,
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
                // AbstractWidget.extractRenderState() hit-tests hover via
                // guiGraphics.containsPointInScissor(), which compares against the scissor
                // stack in absolute screen space, while the vanilla component's own x/y stay
                // at (0, 0) so the local-space mouse handlers (mouseClicked/isMouseOver/...)
                // keep working. extractRenderState also draws using getX()/getY() combined
                // with whatever pose is currently active, and by this point the pose has
                // already been translated to this component's absolute screen position - so
                // we can't just move the widget to absolute coords (that double-translates
                // the draw). Instead we temporarily cancel the pose translation and use
                // absolute coords for both the widget position and the mouse, matching
                // vanilla's own assumption that pose is screen space and getX()/getY() are
                // absolute; then we restore both afterwards.
                guiGraphics.pose().translate(-bounds.left, -bounds.top);
                linkedComponent.vanillaComponent.setPosition(bounds.left, bounds.top);
                linkedComponent.vanillaComponent.extractRenderState(guiGraphics, bounds.left + mouseX, bounds.top + mouseY, deltaTicks);
                linkedComponent.vanillaComponent.setPosition(0, 0);
                guiGraphics.pose().translate(bounds.left, bounds.top);
                if (!linkedComponent.enabled) {
                    linkedComponent.vanillaComponent.setAlpha(linkedComponent.alpha);
                }
            }

        }
    }
}
