package org.wunder.lib;

import net.minecraft.resources.ResourceLocation;

import org.wunder.lib.general.Logger;

public class WunderLib {
    public static final String MOD_ID = "wunderlib";
    public static final Logger LOGGER = new Logger();

    public static ResourceLocation ID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
