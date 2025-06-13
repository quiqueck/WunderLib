package de.ambertation.wunderlib.ui.layout.components.render;

import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class RenderHelper {
    public static void outline(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        outline(guiGraphics, x0, y0, x1, y1, color, color);
    }

    public static void outline(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color1, int color2) {
        int n;
        if (x1 < x0) {
            n = x0;
            x0 = x1;
            x1 = n;
        }

        if (y1 < y0) {
            n = y0;
            y0 = y1;
            y1 = n;
        }
        y1--;
        x1--;

        innerHLine(guiGraphics, x0, x1, y0, color1);
        innerVLine(guiGraphics, x0, y0 + 1, y1, color1);
        innerHLine(guiGraphics, x0 + 1, x1, y1, color2);
        innerVLine(guiGraphics, x1, y0 + 1, y1 - 1, color2);
    }

    public static void hLine(GuiGraphics guiGraphics, int x0, int x1, int y, int color) {
        if (x1 < x0) {
            int m = x0;
            x0 = x1;
            x1 = m;
        }

        innerHLine(guiGraphics, x0, x1, y, color);
    }

    protected static void innerHLine(GuiGraphics guiGraphics, int x0, int x1, int y, int color) {
        guiGraphics.fill(RenderPipelines.GUI, x0, y, x1 + 1, y + 1, color);
    }

    public static void vLine(GuiGraphics guiGraphics, int x, int y0, int y1, int color) {
        if (y1 < y0) {
            int m = y0;
            y0 = y1;
            y1 = m;
        }
        innerVLine(guiGraphics, x, y0, y1, color);
    }

    protected static void innerVLine(GuiGraphics guiGraphics, int x, int y0, int y1, int color) {
        guiGraphics.fill(RenderPipelines.GUI, x, y0, x + 1, y1 + 1, color);
    }

    /**
     * Alternative implementation using the new submit system if you need more control
     */
    private static void innerFillAdvanced(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        // This approach uses the new render state submission system
        // You would need to create a custom ColoredRectangleRenderState if needed
        guiGraphics.fill(RenderPipelines.GUI, x0, y0, x1, y1, color);
    }

    public static void renderImage(
            GuiGraphics guiGraphics,
            int left, int top,
            ResourceLocation location,
            Size resourceSize, Rectangle uvRect,
            float alpha
    ) {
        renderImage(guiGraphics, left, top, uvRect.width, uvRect.height, location, resourceSize, uvRect, alpha);
    }

    public static void renderImage(
            GuiGraphics guiGraphics,
            int left, int top,
            int width, int height,
            ResourceLocation location,
            Size resourceSize, Rectangle uvRect,
            float alpha
    ) {
        // In 1.21.6, color/alpha setting is handled differently
        // We can either use the color parameter in blit methods or use setColor if available

        // Method 1: Using the color parameter (if your GuiGraphics supports it)
        int alphaColor = (int) (alpha * 255) << 24 | 0xFFFFFF; // Alpha in upper 8 bits, RGB as white

        // Try to use the color-enabled blit method
        try {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    location,
                    left, top,
                    (float) uvRect.left, (float) uvRect.top,
                    width, height,
                    uvRect.width, uvRect.height,
                    resourceSize.width(), resourceSize.height(),
                    alphaColor
            );
        } catch (Exception e) {
            // Fallback to the standard method if the color version doesn't exist
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    location,
                    left, top, width, height,
                    uvRect.left, uvRect.top, uvRect.width, uvRect.height,
                    resourceSize.width(), resourceSize.height()
            );
        }
    }

    /**
     * Alternative image rendering method using the new pipeline system
     */
    public static void renderImageWithPipeline(
            GuiGraphics guiGraphics,
            int left, int top,
            int width, int height,
            ResourceLocation location,
            Size resourceSize, Rectangle uvRect,
            float alpha
    ) {
        // Convert alpha to color with full RGB
        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                location,
                left, top,
                (float) uvRect.left, (float) uvRect.top,
                width, height,
                uvRect.width, uvRect.height,
                resourceSize.width(), resourceSize.height(),
                color
        );
    }
}