package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.ui.layout.components.render.ScrollerRenderer;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class VanillaScrollerRenderer implements ScrollerRenderer {
    public static final VanillaScrollerRenderer DEFAULT = new VanillaScrollerRenderer();

    @Override
    public void renderScrollBar(GuiGraphicsExtractor guiGraphics, Rectangle b, int pickerOffset, int pickerSize, float zIndex) {
        b = this.getScrollerBounds(b);
        Rectangle p = this.getPickerBounds(b, pickerOffset, pickerSize);

        // Use the new GuiGraphicsExtractor fill method with RenderPipelines
        // This is much simpler than the old BufferBuilder approach

        // Scroller background (black)
        guiGraphics.fill(RenderPipelines.GUI, b.left, b.top, b.right(), b.bottom(), 0xFF000000);

        // Scroll widget shadow (dark gray)
        guiGraphics.fill(RenderPipelines.GUI, p.left, p.top, p.right(), p.bottom(), 0xFF808080);

        // Scroll widget (light gray) - slightly smaller to show shadow effect
        guiGraphics.fill(RenderPipelines.GUI, p.left, p.top, p.right() - 1, p.bottom() - 1, 0xFFC0C0C0);
    }
}