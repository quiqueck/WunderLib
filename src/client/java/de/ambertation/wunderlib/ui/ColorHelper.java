package de.ambertation.wunderlib.ui;

import de.ambertation.wunderlib.utils.ColorUtilARGB32;

/**
 * @deprecated Use {@link ColorUtilARGB32}, which also works on a dedicated server.
 */
@Deprecated(forRemoval = true)
public class ColorHelper {
    public static final int BLACK = ColorUtilARGB32.BLACK;
    public static final int DARK_BLUE = ColorUtilARGB32.DARK_BLUE;
    public static final int DARK_GREEN = ColorUtilARGB32.DARK_GREEN;
    public static final int DARK_AQUA = ColorUtilARGB32.DARK_AQUA;
    public static final int DARK_RED = ColorUtilARGB32.DARK_RED;
    public static final int DARK_PURPLE = ColorUtilARGB32.DARK_PURPLE;
    public static final int GOLD = ColorUtilARGB32.GOLD;
    public static final int GRAY = ColorUtilARGB32.GRAY;
    public static final int DARK_GRAY = ColorUtilARGB32.DARK_GRAY;
    public static final int BLUE = ColorUtilARGB32.BLUE;
    public static final int GREEN = ColorUtilARGB32.GREEN;
    public static final int AQUA = ColorUtilARGB32.AQUA;
    public static final int RED = ColorUtilARGB32.RED;
    public static final int LIGHT_PURPLE = ColorUtilARGB32.LIGHT_PURPLE;
    public static final int YELLOW = ColorUtilARGB32.YELLOW;
    public static final int WHITE = ColorUtilARGB32.WHITE;
    public static final int DEFAULT_TEXT = ColorUtilARGB32.DEFAULT_TEXT;
    public static final int SCREEN_BACKGROUND = ColorUtilARGB32.SCREEN_BACKGROUND;
    public static final int CONTAINER_BACKGROUND = ColorUtilARGB32.CONTAINER_BACKGROUND;
    public static final int OVERLAY_BACKGROUND = ColorUtilARGB32.OVERLAY_BACKGROUND;
    public static final int OVERLAY_BORDER = ColorUtilARGB32.OVERLAY_BORDER;

    public static int color(int r, int g, int b) {
        return ColorUtilARGB32.color(r, g, b);
    }

    /**
     * Note the argument order: {@link ColorUtilARGB32#color(int, int, int, int)} takes alpha first.
     */
    public static int color(int r, int g, int b, int a) {
        return ColorUtilARGB32.color(a, r, g, b);
    }

    /**
     * Note the argument order: {@link ColorUtilARGB32#color(float, float, float, float)} takes alpha first.
     */
    public static int color(float r, float g, float b, float a) {
        return ColorUtilARGB32.color(a, r, g, b);
    }

    public static int color(String hex) {
        return ColorUtilARGB32.color(hex);
    }

    public static int[] toIntTGBA(int color) {
        return ColorUtilARGB32.toIntArrayRGBA(color);
    }

    public static float[] toFloatArrayRGBA(int color) {
        return ColorUtilARGB32.toFloatArrayRGBA(color);
    }

    public static String toRGBHex(int color) {
        return ColorUtilARGB32.toRGBHex(color);
    }

    public static boolean validHexColor(String hexColor) {
        return ColorUtilARGB32.validHexColor(hexColor);
    }

    public static int parseHex(String hexColor) {
        return ColorUtilARGB32.parseHex(hexColor);
    }

    public static int blendColors(float t, int c1, int c2) {
        return ColorUtilARGB32.lerp(c1, c2, t);
    }
}
