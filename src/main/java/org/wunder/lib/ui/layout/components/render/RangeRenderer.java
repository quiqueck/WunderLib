package org.wunder.lib.ui.layout.components.render;

import org.wunder.lib.ui.layout.components.AbstractVanillaComponentRenderer;
import org.wunder.lib.ui.layout.components.Range;
import org.wunder.lib.ui.vanilla.Slider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RangeRenderer<N extends Number> extends AbstractVanillaComponentRenderer<Slider<N>, Range<N>> {

}
