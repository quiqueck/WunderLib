package de.ambertation.wunderlib.utils;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

/**
 * Utility class for working with ARGB32 color values.
 * This is a replacement for the formerly used FastColor class.
 * <p>
 * It is the single color helper shared by client and server. {@code ColorHelper} (WunderLib) and
 * {@code org.betterx.ui.ColorUtil} (BCLib) only forward to it.
 */
public class ColorUtilARGB32 {
    // Vanilla text colors as opaque ARGB32 values
    public static final int BLACK = TextColor.BLACK.getValue() | 0xFF000000;
    public static final int DARK_BLUE = TextColor.DARK_BLUE.getValue() | 0xFF000000;
    public static final int DARK_GREEN = TextColor.DARK_GREEN.getValue() | 0xFF000000;
    public static final int DARK_AQUA = TextColor.DARK_AQUA.getValue() | 0xFF000000;
    public static final int DARK_RED = TextColor.DARK_RED.getValue() | 0xFF000000;
    public static final int DARK_PURPLE = TextColor.DARK_PURPLE.getValue() | 0xFF000000;
    public static final int GOLD = TextColor.GOLD.getValue() | 0xFF000000;
    public static final int GRAY = TextColor.GRAY.getValue() | 0xFF000000;
    public static final int DARK_GRAY = TextColor.DARK_GRAY.getValue() | 0xFF000000;
    public static final int BLUE = TextColor.BLUE.getValue() | 0xFF000000;
    public static final int GREEN = TextColor.GREEN.getValue() | 0xFF000000;
    public static final int AQUA = TextColor.AQUA.getValue() | 0xFF000000;
    public static final int RED = TextColor.RED.getValue() | 0xFF000000;
    public static final int LIGHT_PURPLE = TextColor.LIGHT_PURPLE.getValue() | 0xFF000000;
    public static final int YELLOW = TextColor.YELLOW.getValue() | 0xFF000000;
    public static final int WHITE = TextColor.WHITE.getValue() | 0xFF000000;
    public static final int DEFAULT_TEXT = WHITE;

    // Shared UI colors
    public static final int SCREEN_BACKGROUND = 0xFF343444;
    public static final int CONTAINER_BACKGROUND = 0x77000000;
    public static final int OVERLAY_BACKGROUND = 0x97000000;
    public static final int OVERLAY_BORDER = 0xFF555566;

    /**
     * Creates a color with a different alpha value.
     *
     * @param rgb   Base Color
     * @param alpha Alpha component (0-255)
     * @return ARGB32 color value
     */
    public static int color(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Creates a color value from alpha, red, green, and blue components.
     *
     * @param alpha Alpha component (0-255)
     * @param red   Red component (0-255)
     * @param green Green component (0-255)
     * @param blue  Blue component (0-255)
     * @return ARGB32 color value
     */
    public static int color(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    /**
     * Creates a color value with full alpha (255) from red, green, and blue components.
     *
     * @param red   Red component (0-255)
     * @param green Green component (0-255)
     * @param blue  Blue component (0-255)
     * @return ARGB32 color value with alpha=255
     */
    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    /**
     * Creates a color value from alpha, red, green, and blue float components.
     * Each float value should be in the range [0.0, 1.0]
     *
     * @param alpha Alpha component (0.0-1.0)
     * @param red   Red component (0.0-1.0)
     * @param green Green component (0.0-1.0)
     * @param blue  Blue component (0.0-1.0)
     * @return ARGB32 color value
     */
    public static int color(float alpha, float red, float green, float blue) {
        return color(
                (int) (alpha * 255.0f),
                (int) (red * 255.0f),
                (int) (green * 255.0f),
                (int) (blue * 255.0f)
        );
    }

    /**
     * Creates a color value with full alpha (1.0) from red, green, and blue float components.
     * Each float value should be in the range [0.0, 1.0]
     *
     * @param red   Red component (0.0-1.0)
     * @param green Green component (0.0-1.0)
     * @param blue  Blue component (0.0-1.0)
     * @return ARGB32 color value with alpha=255
     */
    public static int color(float red, float green, float blue) {
        return color(1.0f, red, green, blue);
    }

    /**
     * Extracts the red component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Red component (0-255)
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Green component (0-255)
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Blue component (0-255)
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Extracts the alpha component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Alpha component (0-255)
     */
    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Returns a new color with a modified alpha component.
     *
     * @param color Original ARGB32 color value
     * @param alpha New alpha component (0-255)
     * @return Modified ARGB32 color with the new alpha value
     */
    public static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Returns a fully opaque version of the provided color (alpha = 255).
     *
     * @param color Original ARGB32 color value
     * @return Fully opaque version of the color
     */
    public static int opaque(int color) {
        return withAlpha(color, 255);
    }

    /**
     * Linearly interpolates between two colors by the given amount.
     * When amount = 0, returns the first color.
     * When amount = 1, returns the second color.
     * Otherwise returns a proportional blend between the two.
     *
     * @param color1 The first ARGB32 color value
     * @param color2 The second ARGB32 color value
     * @param amount The blend amount, in the range [0, 1]
     * @return The interpolated color
     */
    public static int lerp(int color1, int color2, float amount) {
        if (amount <= 0) return color1;
        if (amount >= 1) return color2;

        return color(
                Mth.lerpInt(amount, alpha(color1), alpha(color2)),
                Mth.lerpInt(amount, red(color1), red(color2)),
                Mth.lerpInt(amount, green(color1), green(color2)),
                Mth.lerpInt(amount, blue(color1), blue(color2))
        );
    }

    /**
     * Parses a color from a six digit {@code RRGGBB} hex string (no prefix).
     *
     * @param hex The hex string
     * @return Opaque ARGB32 color value
     */
    public static int color(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return color(r, g, b);
    }

    /**
     * Splits a color into its components.
     *
     * @param color ARGB32 color value
     * @return {@code [red, green, blue, alpha]} (0-255)
     */
    public static int[] toIntArrayRGBA(int color) {
        return new int[]{red(color), green(color), blue(color), alpha(color)};
    }

    /**
     * Splits a color into its components.
     *
     * @param color ARGB32 color value
     * @return {@code [alpha, red, green, blue]} (0-255)
     */
    public static int[] toIntArrayARGB(int color) {
        return new int[]{alpha(color), red(color), green(color), blue(color)};
    }

    /**
     * Splits a color into its components.
     *
     * @param color ARGB32 color value
     * @return {@code [red, green, blue, alpha]} (0.0-1.0)
     */
    public static float[] toFloatArrayRGBA(int color) {
        return new float[]{
                red(color) / 255.0F,
                green(color) / 255.0F,
                blue(color) / 255.0F,
                alpha(color) / 255.0F
        };
    }

    /**
     * Converts a color to a {@code #rrggbb} hex string. The alpha component is dropped.
     *
     * @param color ARGB32 color value
     * @return The hex string
     */
    public static String toRGBHex(int color) {
        return "#"
                + Integer.toHexString(red(color))
                + Integer.toHexString(green(color))
                + Integer.toHexString(blue(color));
    }

    /**
     * Checks if {@link #parseHex(String)} can read the string.
     *
     * @param hexColor A hex color in {@code RGB}, {@code ARGB}, {@code RRGGBB} or {@code AARRGGBB} form,
     *                 optionally prefixed with {@code #} or {@code 0x}
     * @return {@code true} if the string is a valid hex color
     */
    public static boolean validHexColor(String hexColor) {
        if (hexColor == null || hexColor.isBlank()) return false;
        return tryParseHex(hexColor) != null;
    }

    /**
     * Parses a hex color in {@code RGB}, {@code ARGB}, {@code RRGGBB} or {@code AARRGGBB} form, optionally
     * prefixed with {@code #} or {@code 0x}. Forms without alpha are opaque.
     *
     * @param hexColor The hex string
     * @return ARGB32 color value, or {@code -1} if the string is not a valid hex color
     */
    public static int parseHex(String hexColor) {
        Integer color = tryParseHex(hexColor);
        return color == null ? -1 : color;
    }

    private static Integer tryParseHex(String hexColor) {
        if (hexColor.startsWith("#")) hexColor = hexColor.substring(1);
        if (hexColor.startsWith("0x")) hexColor = hexColor.substring(2);
        int len = hexColor.length();
        if (len != 6 && len != 8 && len != 3 && len != 4) {
            return null;
        }

        if (len == 3 || len == 4) {
            StringBuilder expanded = new StringBuilder();
            for (int i = 0; i < len; i++) {
                expanded.append(hexColor.charAt(i)).append(hexColor.charAt(i));
            }
            hexColor = expanded.toString();
            len *= 2;
        }

        int color, shift;
        if (len == 6) {
            color = 0xFF000000;
            shift = 16;
        } else {
            color = 0;
            shift = 24;
        }

        try {
            for (String digit : hexColor.split("(?<=\\G.{2})")) {
                color |= Integer.valueOf(digit, 16) << shift;
                shift -= 8;
            }
        } catch (NumberFormatException ex) {
            return null;
        }

        return color;
    }

    /**
     * Converts RGB components to hue, saturation and brightness.
     *
     * @param r       Red component (0-255)
     * @param g       Green component (0-255)
     * @param b       Blue component (0-255)
     * @param hsbvals Array of at least three elements that receives the result, or {@code null} to allocate one
     * @return {@code [hue, saturation, brightness]} (0.0-1.0)
     */
    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = Math.max(Math.max(r, g), b);
        int cmin = Math.min(Math.min(r, g), b);

        brightness = ((float) cmax) / 255.0F;
        if (cmax != 0) saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else saturation = 0;
        if (saturation == 0) hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax) hue = bluec - greenc;
            else if (g == cmax) hue = 2.0F + redc - bluec;
            else hue = 4.0F + greenc - redc;
            hue = hue / 6.0F;
            if (hue < 0) hue = hue + 1.0F;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    /**
     * Converts hue, saturation and brightness to an opaque color.
     *
     * @param hue        Hue (0.0-1.0, wraps around)
     * @param saturation Saturation (0.0-1.0)
     * @param brightness Brightness (0.0-1.0)
     * @return Opaque ARGB32 color value
     */
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - (saturation * (1.0F - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 1 -> {
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                }
                case 2 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                }
                case 3 -> {
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 4 -> {
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
                }
            }
        }
        return color(r, g, b);
    }

    /**
     * Converts an ARGB32 color to the opaque ABGR layout used by {@code NativeImage}.
     *
     * @param color ARGB32 color value
     * @return Opaque ABGR color value
     */
    public static int toABGR(int color) {
        return 0xFF000000 | blue(color) << 16 | green(color) << 8 | red(color);
    }

    /**
     * Converts an ABGR color (the layout used by {@code NativeImage}) to ARGB32.
     *
     * @param color ABGR color value
     * @return ARGB32 color value
     */
    public static int ABGRtoARGB(int color) {
        int a = (color >> 24) & 255;
        int b = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int r = color & 255;
        return color(a, r, g, b);
    }

    /**
     * Changes the brightness of a color. The result is opaque.
     *
     * @param color ARGB32 color value
     * @param val   Brightness change in tenths (10 adds 1.0 to the brightness)
     * @return Opaque ARGB32 color value
     */
    public static int colorBrightness(int color, float val) {
        float[] hsb = RGBtoHSB(red(color), green(color), blue(color), null);
        hsb[2] = Mth.clamp(hsb[2] + val / 10.0F, 0.0F, 1.0F);
        return HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    /**
     * Multiplies two colors component-wise. {@code -1} acts as "no color" and returns the other color.
     *
     * @param color1 ARGB32 color value
     * @param color2 ARGB32 color value
     * @return The multiplied ARGB32 color
     */
    public static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        } else if (color2 == -1) {
            return color1;
        }

        return color(
                alpha(color1) * alpha(color2) / 0xFF,
                red(color1) * red(color2) / 0xFF,
                green(color1) * green(color2) / 0xFF,
                blue(color1) * blue(color2) / 0xFF
        );
    }

    /**
     * Multiplies a color with a tint and brightens the result.
     *
     * @param color ARGB32 color value
     * @param tint  ARGB32 tint color
     * @return Opaque ARGB32 color value
     */
    public static int applyTint(int color, int tint) {
        return colorBrightness(multiplyColor(color, tint), 1.5F);
    }

    /**
     * Squared distance between two colors in RGB space. Alpha is ignored.
     *
     * @param color1 ARGB32 color value
     * @param color2 ARGB32 color value
     * @return The squared distance
     */
    public static int colorDistance(int color1, int color2) {
        int dr = red(color1) - red(color2);
        int dg = green(color1) - green(color2);
        int db = blue(color1) - blue(color2);
        return dr * dr + dg * dg + db * db;
    }
}
