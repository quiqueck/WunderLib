package org.wunder.lib.ui.layout.components;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.wunder.lib.ui.ColorHelper;
import org.wunder.lib.ui.layout.components.render.RenderHelper;
import org.wunder.lib.ui.layout.values.Alignment;
import org.wunder.lib.ui.layout.values.Rectangle;
import org.wunder.lib.ui.layout.values.Value;

@Environment(EnvType.CLIENT)
public class VLine extends CustomRenderComponent {
    private int color = ColorHelper.DEFAULT_TEXT;

    public VLine(Value width, Value height) {
        super(width, height);
        this.vAlign = Alignment.CENTER;
        this.hAlign = Alignment.CENTER;
    }

    public VLine setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        int left = bounds.height - getContentHeight();
        if (hAlign == Alignment.CENTER) left /= 2;
        else if (hAlign == Alignment.MIN) left = 0;
        RenderHelper.vLine(stack, left, 0, bounds.height, color);
    }

    @Override
    public int getContentWidth() {
        return 1;
    }

    @Override
    public int getContentHeight() {
        return 0;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }

    private boolean focused;

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean bl) {
        focused = bl;
    }
}
