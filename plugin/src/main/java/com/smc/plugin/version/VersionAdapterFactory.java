package com.smc.plugin.version;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * Factory for creating version-specific adapters.
 * Detects the server version and returns the appropriate adapter implementation.
 */
public class VersionAdapterFactory {
    
    /**
     * Creates a version adapter based on the current server version.
     * 
     * @return The appropriate VersionAdapter implementation
     * @throws UnsupportedVersionException if the version is not supported
     */
    public static VersionAdapter createAdapter() throws UnsupportedVersionException {
        String version = detectServerVersion();
        
        // Reject 1.17.x versions explicitly
        if (isVersion17(version)) {
            throw new UnsupportedVersionException(
                "Minecraft 1.17.x is not supported. Please use 1.8-1.16 or 1.18+. Current version: " + version
            );
        }
        
        // Determine which adapter to use based on version
        if (isLegacyVersion(version)) {
            return new LegacyAdapter();
        } else if (isModernVersion(version)) {
            return new ModernAdapter();
        } else {
            throw new UnsupportedVersionException(
                "Unsupported Minecraft version: " + version + ". Supported versions: 1.8-1.16, 1.18-1.21"
            );
        }
    }
    
    /**
     * Detects the server version from Bukkit API.
     * 
     * @return Version string (e.g., "1.16.5", "1.20.4")
     */
    public static String detectServerVersion() {
        Server server = Bukkit.getServer();
        String bukkitVersion = server.getBukkitVersion(); // Format: "1.16.5-R0.1-SNAPSHOT"
        
        // Extract version number (e.g., "1.16.5" from "1.16.5-R0.1-SNAPSHOT")
        if (bukkitVersion.contains("-")) {
            return bukkitVersion.split("-")[0];
        }
        
        return bukkitVersion;
    }
    
    /**
     * Checks if the version is in the legacy range (1.8 - 1.16).
     * 
     * @param version Version string (e.g., "1.16.5")
     * @return true if version is between 1.8 and 1.16
     */
    private static boolean isLegacyVersion(String version) {
        return isVersionBetween(version, "1.8", "1.16");
    }
    
    /**
     * Checks if the version is in the modern range (1.18 - 1.21).
     * 
     * @param version Version string (e.g., "1.20.4")
     * @return true if version is between 1.18 and 1.21
     */
    private static boolean isModernVersion(String version) {
        return isVersionBetween(version, "1.18", "1.21");
    }
    
    /**
     * Checks if the version is 1.17.x.
     * 
     * @param version Version string (e.g., "1.17.1")
     * @return true if version is 1.17.x
     */
    private static boolean isVersion17(String version) {
        String[] parts = version.split("\\.");
        if (parts.length >= 2) {
            try {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                return major == 1 && minor == 17;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Checks if a version is between two version bounds (inclusive).
     * 
     * @param version Version to check (e.g., "1.16.5")
     * @param minVersion Minimum version (e.g., "1.8")
     * @param maxVersion Maximum version (e.g., "1.16")
     * @return true if version is within the range
     */
    private static boolean isVersionBetween(String version, String minVersion, String maxVersion) {
        try {
            int[] versionParts = parseVersion(version);
            int[] minParts = parseVersion(minVersion);
            int[] maxParts = parseVersion(maxVersion);
            
            int comparison = compareVersions(versionParts, minParts);
            if (comparison < 0) {
                return false; // version < minVersion
            }
            
            comparison = compareVersions(versionParts, maxParts);
            return comparison <= 0; // version <= maxVersion
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Parses a version string into an array of integers.
     * 
     * @param version Version string (e.g., "1.16.5")
     * @return Array of version parts [1, 16, 5]
     */
    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[3];
        
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        
        return result;
    }
    
    /**
     * Compares two version arrays.
     * 
     * @param v1 First version array
     * @param v2 Second version array
     * @return -1 if v1 < v2, 0 if v1 == v2, 1 if v1 > v2
     */
    private static int compareVersions(int[] v1, int[] v2) {
        for (int i = 0; i < 3; i++) {
            if (v1[i] < v2[i]) {
                return -1;
            } else if (v1[i] > v2[i]) {
                return 1;
            }
        }
        return 0;
    }
}
