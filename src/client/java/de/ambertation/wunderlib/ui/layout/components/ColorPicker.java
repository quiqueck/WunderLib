package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.utils.ColorUtilARGB32;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderlib.ui.layout.values.Value;

@Environment(EnvType.CLIENT)
public class ColorPicker extends AbstractHorizontalStack<ColorPicker> {
    ColorSwatch swatch;
    Input input;

    public ColorPicker(Value width, Value height, Component title, int color) {
        super(width, height);
        swatch = addColorSwatch(Value.fixed(20), Value.fixed(20), color);
        input = addInput(Value.fill(), Value.fit(), title, ColorUtilARGB32.toRGBHex(color));

        //input.setFilter(ColorUtil::validHexColor);
        input.setResponder(this::inputResponder);
    }

    private void inputResponder(String value) {
        if (ColorUtilARGB32.validHexColor(value)) {
            int color = ColorUtilARGB32.parseHex(value);
            swatch.setColor(color);
            swatch.setOffsetInner(false);
            swatch.setBorderColor(ColorUtilARGB32.BLACK);
        } else {
            swatch.setOffsetInner(true);
            swatch.setBorderColor(ColorUtilARGB32.RED);
        }
    }
}
