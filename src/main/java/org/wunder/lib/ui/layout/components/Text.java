package org.wunder.lib.ui.layout.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

import org.wunder.lib.ui.ColorHelper;
import org.wunder.lib.ui.layout.components.render.ComponentRenderer;
import org.wunder.lib.ui.layout.components.render.TextProvider;
import org.wunder.lib.ui.layout.values.Alignment;
import org.wunder.lib.ui.layout.values.Rectangle;
import org.wunder.lib.ui.layout.values.Value;

public class Text extends LayoutComponent<Text.TextRenderer, Text> {
    net.minecraft.network.chat.Component text;
    int color = ColorHelper.DEFAULT_TEXT;

    public Text(
            Value width,
            Value height,
            net.minecraft.network.chat.Component text
    ) {
        super(width, height, new TextRenderer());
        vAlign = Alignment.CENTER;
        renderer.linkedComponent = this;
        this.text = text;
    }

    public Text setColor(int cl) {
        this.color = cl;
        return this;
    }

    public Text setText(Component text) {
        this.text = text;
        return this;
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(text);
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(text);
    }

    public static class TextRenderer implements ComponentRenderer, TextProvider {
        Text linkedComponent;

        @Override
        public int getWidth(net.minecraft.network.chat.Component c) {
            return getFont().width(c.getVisualOrderText());
        }

        @Override
        public int getHeight(net.minecraft.network.chat.Component c) {
            return TextProvider.super.getLineHeight(c);
        }

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
                int left = bounds.width - getWidth(linkedComponent.text);
                if (linkedComponent.hAlign == Alignment.MIN) left = 0;
                if (linkedComponent.hAlign == Alignment.CENTER) left /= 2;

                int top = bounds.height - getLineHeight(linkedComponent.text);
                if (linkedComponent.vAlign == Alignment.MIN) top = 0;
                if (linkedComponent.vAlign == Alignment.CENTER) top = top / 2 + 1;

                GuiComponent.drawString(stack, getFont(), linkedComponent.text, left, top, linkedComponent.color);
            }
        }
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }
}
