package com.smc.plugin.version;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Version adapter for Minecraft 1.8 - 1.16.
 * Handles API differences in older Minecraft versions.
 */
public class LegacyAdapter implements VersionAdapter {
    
    @Override
    public String getServerVersion() {
        return Bukkit.getServer().getBukkitVersion();
    }
    
    @Override
    public List<Player> getOnlinePlayers() {
        Server server = Bukkit.getServer();
        
        try {
            // Try the modern API first (1.13+)
            Method getOnlinePlayersMethod = server.getClass().getMethod("getOnlinePlayers");
            Object result = getOnlinePlayersMethod.invoke(server);
            
            // Check if it returns a Collection (1.13+) or array (1.8-1.12)
            if (result instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<? extends Player> players = (Collection<? extends Player>) result;
                return new ArrayList<>(players);
            } else if (result instanceof Player[]) {
                // 1.8-1.12 returns Player[]
                Player[] players = (Player[]) result;
                List<Player> playerList = new ArrayList<>();
                for (Player player : players) {
                    playerList.add(player);
                }
                return playerList;
            }
        } catch (Exception e) {
            // Fallback: try direct cast
            try {
                Collection<? extends Player> players = server.getOnlinePlayers();
                return new ArrayList<>(players);
            } catch (Exception ex) {
                // Last resort: return empty list
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
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
