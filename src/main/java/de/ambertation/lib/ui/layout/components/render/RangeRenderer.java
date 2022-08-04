package de.ambertation.lib.ui.layout.components.render;

import de.ambertation.lib.ui.layout.components.AbstractVanillaComponentRenderer;
import de.ambertation.lib.ui.layout.components.Range;
import de.ambertation.lib.ui.vanilla.Slider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RangeRenderer<N extends Number> extends AbstractVanillaComponentRenderer<Slider<N>, Range<N>> {

}
