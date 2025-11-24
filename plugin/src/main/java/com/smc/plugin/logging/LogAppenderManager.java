package com.smc.plugin.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.plugin.Plugin;

/**
 * Manages the registration and lifecycle of the custom Log4j appender.
 * 
 * This class handles the dynamic registration of the SMCLogAppender with the
 * Log4j2 logging system, allowing the plugin to intercept all server logs.
 */
public class LogAppenderManager {
    
    private final Plugin plugin;
    private final LogCollector logCollector;
    private SMCLogAppender appender;
    private boolean registered = false;
    
    /**
     * Creates a new LogAppenderManager.
     * 
     * @param plugin The plugin instance
     * @param logCollector The LogCollector to receive log messages
     */
    public LogAppenderManager(Plugin plugin, LogCollector logCollector) {
        this.plugin = plugin;
        this.logCollector = logCollector;
    }
    
    /**
     * Registers the custom appender with Log4j2.
     * This method attempts to add the appender to the root logger.
     * 
     * @return true if registration was successful, false otherwise
     */
    public boolean register() {
        if (registered) {
            plugin.getLogger().warning("Log appender already registered");
            return true;
        }
        
        try {
            // Set the LogCollector instance for the appender
            SMCLogAppender.setLogCollector(logCollector);
            
            // Get the Log4j2 LoggerContext
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            
            // Create the appender
            appender = SMCLogAppender.createAppender("SMCLogAppender", null);
            
            if (appender == null) {
                plugin.getLogger().severe("Failed to create SMCLogAppender");
                return false;
            }
            
            // Start the appender
            appender.start();
            
            // Add the appender to the root logger
            Logger rootLogger = context.getRootLogger();
            rootLogger.addAppender(appender);
            
            registered = true;
            plugin.getLogger().info("Log appender registered successfully");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register log appender: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Unregisters the custom appender from Log4j2.
     * This should be called when the plugin is disabled.
     */
    public void unregister() {
        if (!registered || appender == null) {
            return;
        }
        
        try {
            // Get the Log4j2 LoggerContext
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            
            // Remove the appender from the root logger
            Logger rootLogger = context.getRootLogger();
            rootLogger.removeAppender(appender);
            
            // Stop the appender
            appender.stop();
            
            // Clear the LogCollector reference
            SMCLogAppender.setLogCollector(null);
            
            registered = false;
            plugin.getLogger().info("Log appender unregistered successfully");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to unregister log appender: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if the appender is currently registered.
     * 
     * @return true if registered, false otherwise
     */
    public boolean isRegistered() {
        return registered;
    }
}
