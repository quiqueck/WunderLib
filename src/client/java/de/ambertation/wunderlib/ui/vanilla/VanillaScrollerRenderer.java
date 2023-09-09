package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.ui.layout.components.render.ScrollerRenderer;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public class VanillaScrollerRenderer implements ScrollerRenderer {
    public static final VanillaScrollerRenderer DEFAULT = new VanillaScrollerRenderer();

    @Override
    public void renderScrollBar(Rectangle b, int pickerOffset, int pickerSize, float zIndex) {
        b = this.getScrollerBounds(b);
        Rectangle p = this.getPickerBounds(b, pickerOffset, pickerSize);


        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        //scroller background
        bufferBuilder.vertex(b.left, b.bottom(), zIndex).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.right(), b.bottom(), zIndex).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.right(), b.top, zIndex).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.left, b.top, zIndex).color(0, 0, 0, 255).endVertex();

        //scroll widget shadow
        bufferBuilder.vertex(p.left, p.bottom(), zIndex).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.right(), p.bottom(), zIndex).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.right(), p.top, zIndex).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.left, p.top, zIndex).color(128, 128, 128, 255).endVertex();

        //scroll widget
        bufferBuilder.vertex(p.left, p.bottom() - 1, zIndex)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(p.right() - 1, p.bottom() - 1, zIndex)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(p.right() - 1, p.top, zIndex).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(p.left, p.top, zIndex).color(192, 192, 192, 255).endVertex();

        tesselator.end();
    }
}
