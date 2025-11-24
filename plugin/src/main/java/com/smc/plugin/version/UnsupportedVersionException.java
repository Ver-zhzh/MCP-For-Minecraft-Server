package com.smc.plugin.version;

/**
 * Exception thrown when an unsupported Minecraft version is detected.
 */
public class UnsupportedVersionException extends Exception {
    
    public UnsupportedVersionException(String message) {
        super(message);
    }
    
    public UnsupportedVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}
