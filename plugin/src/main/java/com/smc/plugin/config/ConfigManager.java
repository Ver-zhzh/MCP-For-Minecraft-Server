package com.smc.plugin.config;

import com.smc.plugin.SMCPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Manages plugin configuration loading, validation, and hot-reloading.
 * 
 * This class handles all configuration-related operations including:
 * - Loading configuration from config.yml
 * - Validating configuration values
 * - Providing type-safe access to configuration values
 * - Hot-reloading configuration without server restart
 */
public class ConfigManager {
    
    private final SMCPlugin plugin;
    private FileConfiguration config;
    
    // HTTP Configuration
    private boolean httpEnabled;
    private String httpHost;
    private int httpPort;
    private String apiKey;
    
    // Logging Configuration
    private int logBufferSize;
    private int logRetentionHours;
    
    // Command Configuration
    private int commandTimeoutSeconds;
    private List<String> commandBlacklist;
    
    /**
     * Creates a new configuration manager.
     * 
     * @param plugin The plugin instance
     */
    public ConfigManager(SMCPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads the configuration from config.yml.
     * Creates a default configuration file if it doesn't exist.
     * 
     * @throws IllegalStateException if configuration validation fails
     */
    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Reload config from disk
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Generate API key if using default
        generateApiKeyIfNeeded();
        
        // Load and validate all configuration values
        loadHttpConfig();
        loadLoggingConfig();
        loadCommandConfig();
        
        // Validate the configuration
        validateConfig();
        
        plugin.getLogger().info("Configuration loaded and validated successfully");
    }
    
    /**
     * Generates a secure random API key if the current one is the default value.
     * The generated key is automatically saved to the config file.
     */
    private void generateApiKeyIfNeeded() {
        String currentKey = config.getString("http.api-key", "change-this-to-a-secure-random-key");
        
        // Check if it's a default/insecure key
        if (currentKey.equals("change-this-to-a-secure-random-key") || 
            currentKey.equals("change-me-to-a-secure-key") ||
            currentKey.equals("your-secure-api-key-here")) {
            
            // Generate a secure random API key
            String newApiKey = generateSecureApiKey();
            
            // Save it to config
            config.set("http.api-key", newApiKey);
            plugin.saveConfig();
            
            plugin.getLogger().info("=================================");
            plugin.getLogger().info("Generated new API key!");
            plugin.getLogger().info("API Key: " + newApiKey);
            plugin.getLogger().info("");
            plugin.getLogger().info("Please use this key in your MCP server configuration:");
            plugin.getLogger().info("MINECRAFT_API_KEY=" + newApiKey);
            plugin.getLogger().info("=================================");
        }
    }
    
    /**
     * Generates a secure random API key using SecureRandom and Base64 encoding.
     * 
     * @return A 32-character secure random API key
     */
    private String generateSecureApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 24 bytes = 32 base64 characters
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Reloads the configuration from disk.
     * This allows configuration changes without restarting the server.
     * 
     * @throws IllegalStateException if configuration validation fails
     */
    public void reloadConfig() {
        plugin.getLogger().info("Reloading configuration...");
        loadConfig();
        plugin.getLogger().info("Configuration reloaded successfully");
    }
    
    /**
     * Loads HTTP server configuration.
     */
    private void loadHttpConfig() {
        httpEnabled = config.getBoolean("http.enabled", true);
        httpHost = config.getString("http.host", "127.0.0.1");
        httpPort = config.getInt("http.port", 8080);
        apiKey = config.getString("http.api-key", "change-me-to-a-secure-key");
    }
    
    /**
     * Loads logging configuration.
     */
    private void loadLoggingConfig() {
        logBufferSize = config.getInt("logging.buffer-size", 10000);
        logRetentionHours = config.getInt("logging.retention-hours", 24);
    }
    
    /**
     * Loads command execution configuration.
     */
    private void loadCommandConfig() {
        commandTimeoutSeconds = config.getInt("commands.timeout-seconds", 30);
        commandBlacklist = config.getStringList("commands.blacklist");
        
        // Ensure blacklist is never null
        if (commandBlacklist == null) {
            commandBlacklist = java.util.Arrays.asList("stop", "restart");
        }
    }
    
    /**
     * Validates all configuration values.
     * 
     * @throws IllegalStateException if any configuration value is invalid
     */
    private void validateConfig() {
        // Validate HTTP configuration
        if (httpPort < 1 || httpPort > 65535) {
            throw new IllegalStateException("Invalid HTTP port: " + httpPort + ". Must be between 1 and 65535.");
        }
        
        if (httpHost == null || httpHost.trim().isEmpty()) {
            throw new IllegalStateException("HTTP host cannot be empty");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("API key cannot be empty");
        }
        
        // Check for weak/default API keys (should not happen after auto-generation)
        if (apiKey.length() < 16) {
            plugin.getLogger().warning("=================================");
            plugin.getLogger().warning("WARNING: API key is too short!");
            plugin.getLogger().warning("Please use a longer, more secure key.");
            plugin.getLogger().warning("=================================");
        }
        
        // Validate logging configuration
        if (logBufferSize < 100) {
            throw new IllegalStateException("Log buffer size must be at least 100. Current: " + logBufferSize);
        }
        
        if (logBufferSize > 100000) {
            plugin.getLogger().warning("Log buffer size is very large (" + logBufferSize + "). This may use significant memory.");
        }
        
        if (logRetentionHours < 1) {
            throw new IllegalStateException("Log retention hours must be at least 1. Current: " + logRetentionHours);
        }
        
        // Validate command configuration
        if (commandTimeoutSeconds < 1) {
            throw new IllegalStateException("Command timeout must be at least 1 second. Current: " + commandTimeoutSeconds);
        }
        
        if (commandTimeoutSeconds > 300) {
            plugin.getLogger().warning("Command timeout is very long (" + commandTimeoutSeconds + " seconds). Consider reducing it.");
        }
    }
    
    // Getters for HTTP configuration
    
    public boolean isHttpEnabled() {
        return httpEnabled;
    }
    
    public String getHttpHost() {
        return httpHost;
    }
    
    public int getHttpPort() {
        return httpPort;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    // Getters for logging configuration
    
    public int getLogBufferSize() {
        return logBufferSize;
    }
    
    public int getLogRetentionHours() {
        return logRetentionHours;
    }
    
    // Getters for command configuration
    
    public int getCommandTimeoutSeconds() {
        return commandTimeoutSeconds;
    }
    
    public List<String> getCommandBlacklist() {
        return commandBlacklist;
    }
    
    /**
     * Checks if a command is blacklisted.
     * 
     * @param command The command to check (without leading slash)
     * @return true if the command is blacklisted, false otherwise
     */
    public boolean isCommandBlacklisted(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        // Remove leading slash if present
        String cleanCommand = command.trim();
        if (cleanCommand.startsWith("/")) {
            cleanCommand = cleanCommand.substring(1);
        }
        
        // Extract the base command (first word)
        String baseCommand = cleanCommand.split("\\s+")[0].toLowerCase();
        
        // Check if it's in the blacklist
        for (String blacklisted : commandBlacklist) {
            if (blacklisted.equalsIgnoreCase(baseCommand)) {
                return true;
            }
        }
        
        return false;
    }
}
