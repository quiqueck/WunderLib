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
public class HLine extends CustomRenderComponent {
    private int color = ColorHelper.DEFAULT_TEXT;

    public HLine(Value width, Value height) {
        super(width, height);
        this.vAlign = Alignment.CENTER;
        this.hAlign = Alignment.CENTER;
    }

    public HLine setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        int top = bounds.height - getContentHeight();
        if (vAlign == Alignment.CENTER) top /= 2;
        else if (vAlign == Alignment.MIN) top = 0;
        RenderHelper.hLine(stack, 0, bounds.width, top, color);
    }

    @Override
    public int getContentWidth() {
        return 0;
    }

    @Override
    public int getContentHeight() {
        return 1;
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
