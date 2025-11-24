package com.smc.plugin.version;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Interface for handling version-specific API differences.
 * Will be implemented in task 3.
 */
public interface VersionAdapter {
    
    /**
     * Get the server version string.
     */
    String getServerVersion();
    
    /**
     * Get list of online players (handles API differences between versions).
     */
    List<Player> getOnlinePlayers();
    
    /**
     * Execute a command and return the result.
     */
    String executeCommand(String command);
    
    /**
     * Check if a plugin is enabled.
     */
    boolean isPluginEnabled(Plugin plugin);
}
