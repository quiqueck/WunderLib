package de.ambertation.wunderlib.utils;


import de.ambertation.wunderlib.WunderLib;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling semantic versioning in Minecraft mods.
 * This class provides methods for parsing, comparing, and converting version strings
 * in the format "major.minor.patch".
 */
public class Version {
    /** Constant representing version 0.0.0 */
    public static final Version ZERO = new Version(0, 0, 0);

    /**
     * Interface for mods that provide version information.
     * Implementing classes can provide mod version, ID, and utility methods for namespaces.
     */
    public interface ModVersionProvider {
        /**
         * Gets the version of the mod.
         * @return The version object
         */
        Version getModVersion();
        
        /**
         * Gets the ID of the mod.
         * @return The mod ID string
         */
        String getModID();

        /**
         * Gets the namespace for the mod, defaults to the mod ID.
         * @return The namespace string
         */
        default String getNamespace() {
            return getModID();
        }
        
        /**
         * Creates a ResourceLocation with the mod's namespace and the given path.
         * @param key The path part of the ResourceLocation
         * @return A ResourceLocation with the mod's namespace and the given path
         */
        default ResourceLocation mk(String key) {
            return ResourceLocation.fromNamespaceAndPath(getModID(), key);
        }
    }

    /** The version string in "major.minor.patch" format */
    public final String version;

    /**
     * Creates a new Version from a string.
     * If the input is "${version}" (a placeholder), it will default to "0.0.0".
     * 
     * @param version The version string in "major.minor.patch" format
     */
    public Version(String version) {
        if ("${version}".equals(version)) version = "0.0.0";
        this.version = version.trim();
    }

    /**
     * Creates a new Version from major, minor, and patch components.
     * 
     * @param major The major version number
     * @param minor The minor version number
     * @param patch The patch version number
     */
    public Version(int major, int minor, int patch) {
        this(String.format(Locale.ROOT, "%d.%d.%d", major, minor, patch));
    }

    /** Number of bits reserved for each of the three version components. */
    private static final int COMPONENT_BITS = 16;
    /** Bit mask for a single version component. */
    private static final long COMPONENT_MASK = (1L << COMPONENT_BITS) - 1;
    /** Largest value a single version component can hold without being clamped. */
    public static final int MAX_COMPONENT = (int) COMPONENT_MASK;

    /**
     * Clamps a single version component into the representable range.
     * <p>
     * Components are packed into {@value #COMPONENT_BITS} bits each, so anything outside
     * {@code [0, }{@link #MAX_COMPONENT}{@code ]} cannot be represented. Clamping (rather than
     * masking) keeps the ordering of {@link #toLong()} monotonic: an out-of-range component
     * saturates at the maximum instead of wrapping around to a small value.
     *
     * @param value The raw component value
     * @return The component, clamped to {@code [0, }{@link #MAX_COMPONENT}{@code ]}
     */
    private static long clampComponent(int value) {
        if (value < 0) return 0;
        return Math.min(value, MAX_COMPONENT);
    }

    /**
     * Converts a version string to a long representation.
     * <p>
     * The three components are packed into {@value #COMPONENT_BITS} bits each
     * ({@code major << 32 | minor << 16 | patch}), so the numeric value orders identically to
     * the semantic version. This replaces the former 8-bit-per-component packing, which
     * overflowed for any component above 255 (e.g. the {@code 26.200.x} / {@code 26.300.x}
     * mod versions) and made newer versions compare as older.
     *
     * @param version The version string to convert
     * @return A long representation of the version
     */
    private static long toLong(String version) {
        if (version == null || version.isEmpty()) return 0;

        try {
            final String semanticVersionPattern = "(\\d+)\\.(\\d+)(\\.(\\d+))?\\D*";
            final Matcher matcher = Pattern.compile(semanticVersionPattern).matcher(version);

            int major = 0;
            int minor = 0;
            int patch = 0;
            if (matcher.find()) {
                if (matcher.groupCount() > 0)
                    major = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1));
                if (matcher.groupCount() > 1)
                    minor = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
                if (matcher.groupCount() > 3)
                    patch = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
            }

            return (clampComponent(major) << (2 * COMPONENT_BITS))
                    | (clampComponent(minor) << COMPONENT_BITS)
                    | clampComponent(patch);
        } catch (Exception e) {
            WunderLib.LOGGER.error("Failed to parse Version '" + version + "'.");
            return 0;
        }
    }

    /**
     * Extracts the major version component from a long representation.
     *
     * @param version The long representation of the version
     * @return The major version number
     */
    public static int major(long version) {
        return (int) ((version >>> (2 * COMPONENT_BITS)) & COMPONENT_MASK);
    }

    /**
     * Extracts the minor version component from a long representation.
     *
     * @param version The long representation of the version
     * @return The minor version number
     */
    public static int minor(long version) {
        return (int) ((version >>> COMPONENT_BITS) & COMPONENT_MASK);
    }

    /**
     * Extracts the patch version component from a long representation.
     *
     * @param version The long representation of the version
     * @return The patch version number
     */
    public static int patch(long version) {
        return (int) (version & COMPONENT_MASK);
    }

    /**
     * Creates a Version object from a long representation.
     *
     * @param version The long representation of the version
     * @return A Version object
     */
    public static Version fromLong(long version) {
        return new Version(major(version), minor(version), patch(version));
    }

    /**
     * Converts this Version object to a long representation.
     *
     * @return A long representation of this version
     */
    public long toLong() {
        return toLong(version);
    }

    /**
     * Checks if this version is larger than the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is larger, false otherwise
     */
    public boolean isLargerThan(Version v2) {
        return toLong() > v2.toLong();
    }

    /**
     * Checks if this version is larger than or equal to the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is larger or equal, false otherwise
     */
    public boolean isLargerOrEqualVersion(Version v2) {
        return toLong() >= v2.toLong();
    }

    /**
     * Checks if this version is larger than the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is larger, false otherwise
     */
    public boolean isLargerThan(String v2) {
        return toLong() > toLong(v2);
    }

    /**
     * Checks if this version is larger than or equal to the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is larger or equal, false otherwise
     */
    public boolean isLargerOrEqualVersion(String v2) {
        return toLong() >= toLong(v2);
    }

    /**
     * Checks if this version is less than the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is less, false otherwise
     */
    public boolean isLessThan(Version v2) {
        return toLong() < v2.toLong();
    }

    /**
     * Checks if this version is less than or equal to the given version.
     * 
     * @param v2 The version to compare against
     * @return True if this version is less or equal, false otherwise
     */
    public boolean isLessOrEqualVersion(Version v2) {
        return toLong() <= v2.toLong();
    }

    /**
     * Checks if this version is less than the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is less, false otherwise
     */
    public boolean isLessThan(String v2) {
        return toLong() < toLong(v2);
    }

    /**
     * Checks if this version is less than or equal to the given version string.
     * 
     * @param v2 The version string to compare against
     * @return True if this version is less or equal, false otherwise
     */
    public boolean isLessOrEqualVersion(String v2) {
        return toLong() <= toLong(v2);
    }

    /**
     * Checks if this version is equal to another object.
     * If the object is a string, it compares the trimmed version string.
     * If the object is a Version, it compares the version strings.
     * 
     * @param o The object to compare against
     * @return True if the versions are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String s) return version.equals(s.trim());
        if (!(o instanceof Version version1)) return false;
        return Objects.equals(version, version1.version);
    }

    /**
     * Computes the hash code for this version.
     * 
     * @return The hash code of the version string
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(version);
    }

    /**
     * Returns the string representation of this version.
     * 
     * @return The version string
     */
    @Override
    public String toString() {
        return version;
    }
}
