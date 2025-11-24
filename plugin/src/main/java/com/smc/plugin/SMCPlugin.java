package com.smc.plugin;

import com.smc.plugin.api.HttpApiServer;
import com.smc.plugin.command.CommandExecutor;
import com.smc.plugin.config.ConfigManager;
import com.smc.plugin.logging.LogAppenderManager;
import com.smc.plugin.logging.LogCollector;
import com.smc.plugin.version.VersionAdapter;
import com.smc.plugin.version.VersionAdapterFactory;
import com.smc.plugin.version.UnsupportedVersionException;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for SMC (Server Management via MCP).
 * Provides HTTP API for Minecraft server management.
 * 
 * This plugin exposes a REST API that allows external tools (like MCP servers)
 * to interact with the Minecraft server for monitoring and management purposes.
 */
public class SMCPlugin extends JavaPlugin {
    
    private static SMCPlugin instance;
    private ConfigManager configManager;
    private VersionAdapter versionAdapter;
    private LogCollector logCollector;
    private LogAppenderManager logAppenderManager;
    private CommandExecutor commandExecutor;
    private HttpApiServer httpApiServer;
    
    /**
     * Gets the singleton instance of the plugin.
     * 
     * @return The plugin instance
     */
    public static SMCPlugin getInstance() {
        return instance;
    }
    
    /**
     * Gets the configuration manager.
     * 
     * @return The configuration manager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the version adapter.
     * 
     * @return The version adapter instance
     */
    public VersionAdapter getVersionAdapter() {
        return versionAdapter;
    }
    
    /**
     * Gets the log collector.
     * 
     * @return The log collector instance
     */
    public LogCollector getLogCollector() {
        return logCollector;
    }
    
    /**
     * Gets the command executor.
     * 
     * @return The command executor instance
     */
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * Called when the plugin is enabled.
     * Initializes all components and starts services.
     */
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("=================================");
        getLogger().info("SMC Plugin v" + getDescription().getVersion());
        getLogger().info("Starting initialization...");
        getLogger().info("=================================");
        
        try {
            // Initialize configuration manager
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            getLogger().info("Configuration loaded successfully");
            
            // Log server version information
            String serverVersion = getServer().getVersion();
            String bukkitVersion = getServer().getBukkitVersion();
            getLogger().info("Server Version: " + serverVersion);
            getLogger().info("Bukkit Version: " + bukkitVersion);
            
            // Initialize version adapter
            try {
                versionAdapter = VersionAdapterFactory.createAdapter();
                String detectedVersion = VersionAdapterFactory.detectServerVersion();
                getLogger().info("Version adapter initialized for Minecraft " + detectedVersion);
                getLogger().info("Using adapter: " + versionAdapter.getClass().getSimpleName());
            } catch (UnsupportedVersionException e) {
                getLogger().severe("Unsupported Minecraft version detected!");
                getLogger().severe(e.getMessage());
                throw e;
            }
            
            // Initialize log collector
            int bufferSize = configManager.getLogBufferSize();
            int retentionHours = configManager.getLogRetentionHours();
            logCollector = new LogCollector(bufferSize, retentionHours);
            getLogger().info("Log collector initialized (buffer: " + bufferSize + ", retention: " + retentionHours + "h)");
            
            // Register log appender
            logAppenderManager = new LogAppenderManager(this, logCollector);
            if (logAppenderManager.register()) {
                getLogger().info("Log appender registered - now collecting server logs");
            } else {
                getLogger().warning("Failed to register log appender - log collection may not work");
            }
            
            // Initialize command executor
            commandExecutor = new CommandExecutor(this);
            getLogger().info("Command executor initialized (timeout: " + configManager.getCommandTimeoutSeconds() + "s)");
            
            // Initialize and start HTTP API server
            if (configManager.isHttpEnabled()) {
                httpApiServer = new HttpApiServer(this, configManager.getHttpHost(), configManager.getHttpPort());
                if (httpApiServer.startServer()) {
                    getLogger().info("HTTP API server started on " + configManager.getHttpHost() + ":" + configManager.getHttpPort());
                } else {
                    getLogger().warning("Failed to start HTTP API server - API will not be available");
                }
            } else {
                getLogger().info("HTTP API server is disabled in configuration");
            }
            
            getLogger().info("=================================");
            getLogger().info("SMC Plugin enabled successfully!");
            getLogger().info("=================================");
            
        } catch (Exception e) {
            getLogger().severe("=================================");
            getLogger().severe("Failed to enable SMC Plugin!");
            getLogger().severe("Error: " + e.getMessage());
            getLogger().severe("=================================");
            e.printStackTrace();
            
            // Disable the plugin if initialization fails
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Called when the plugin is disabled.
     * Cleans up resources and stops services.
     */
    @Override
    public void onDisable() {
        getLogger().info("=================================");
        getLogger().info("SMC Plugin shutting down...");
        getLogger().info("=================================");
        
        try {
            // Stop HTTP API server
            if (httpApiServer != null && httpApiServer.isRunning()) {
                httpApiServer.stopServer();
            }
            
            // Unregister log appender
            if (logAppenderManager != null) {
                logAppenderManager.unregister();
            }
            
            // Clear log collector
            if (logCollector != null) {
                logCollector.clear();
            }
            
            getLogger().info("SMC Plugin disabled successfully");
            
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        } finally {
            instance = null;
        }
        
        getLogger().info("=================================");
    }
}
