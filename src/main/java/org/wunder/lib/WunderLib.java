package org.wunder.lib;

import org.wunder.lib.general.Logger;

import net.minecraft.resources.ResourceLocation;

public class WunderLib {
    public static final String MOD_ID = "wunderlib";
    public static final Logger LOGGER = new Logger();

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
