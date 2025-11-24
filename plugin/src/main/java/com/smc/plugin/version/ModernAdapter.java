package com.smc.plugin.version;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Version adapter for Minecraft 1.18 - 1.21.
 * Uses modern Bukkit API methods.
 */
public class ModernAdapter implements VersionAdapter {
    
    @Override
    public String getServerVersion() {
        return Bukkit.getServer().getBukkitVersion();
    }
    
    @Override
    public List<Player> getOnlinePlayers() {
        // Modern versions (1.13+) return Collection<? extends Player>
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        return new ArrayList<>(players);
    }
    
    @Override
    public String executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "Error: Command cannot be empty";
        }
        
        try {
            Server server = Bukkit.getServer();
            ConsoleCommandSender console = server.getConsoleSender();
            
            // Execute the command directly
            // Note: Output capture would require complex implementation
            // For now, we execute and return success status
            boolean success = server.dispatchCommand(console, command);
            
            if (success) {
                return "Command executed successfully";
            } else {
                return "Command execution failed";
            }
            
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
    
    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        if (plugin == null) {
            return false;
        }
        return plugin.isEnabled();
    }
}
