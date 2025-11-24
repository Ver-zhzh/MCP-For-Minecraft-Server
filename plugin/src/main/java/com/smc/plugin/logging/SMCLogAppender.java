package com.smc.plugin.logging;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

/**
 * Custom Log4j2 Appender that forwards log messages to the LogCollector.
 * 
 * This appender intercepts all log messages from the Minecraft server and plugins,
 * allowing them to be stored in memory and retrieved via the HTTP API.
 * 
 * This implementation is compatible with Log4j2, which is used in Minecraft 1.12+.
 */
@Plugin(name = "SMCLogAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class SMCLogAppender extends AbstractAppender {
    
    private static LogCollector logCollector;
    
    /**
     * Sets the LogCollector instance that will receive log messages.
     * This must be called before the appender starts receiving logs.
     * 
     * @param collector The LogCollector instance
     */
    public static void setLogCollector(LogCollector collector) {
        logCollector = collector;
    }
    
    /**
     * Gets the current LogCollector instance.
     * 
     * @return The LogCollector instance, or null if not set
     */
    public static LogCollector getLogCollector() {
        return logCollector;
    }
    
    /**
     * Creates a new SMCLogAppender.
     * 
     * @param name Appender name
     * @param filter Optional filter
     */
    protected SMCLogAppender(String name, Filter filter) {
        super(name, filter, null, true);
    }
    
    /**
     * Factory method for creating the appender via Log4j2 configuration.
     * 
     * @param name Appender name
     * @param filter Optional filter
     * @return New appender instance
     */
    @PluginFactory
    public static SMCLogAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for SMCLogAppender");
            return null;
        }
        return new SMCLogAppender(name, filter);
    }
    
    /**
     * Appends a log event to the LogCollector.
     * This method is called by Log4j2 for each log message.
     * 
     * @param event The log event to append
     */
    @Override
    public void append(LogEvent event) {
        if (logCollector == null) {
            // LogCollector not initialized yet, skip
            return;
        }
        
        try {
            String level = event.getLevel().name();
            String logger = event.getLoggerName();
            String message = event.getMessage().getFormattedMessage();
            
            // Add throwable information if present
            Throwable throwable = event.getThrown();
            if (throwable != null) {
                message = message + "\n" + getStackTrace(throwable);
            }
            
            logCollector.addLog(level, logger, message);
        } catch (Exception e) {
            // Don't let logging errors break the appender
            LOGGER.error("Error in SMCLogAppender", e);
        }
    }
    
    /**
     * Converts a throwable to a string representation.
     * 
     * @param throwable The throwable to convert
     * @return String representation of the stack trace
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage());
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\n    at ").append(element.toString());
        }
        
        // Include cause if present
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            sb.append("\nCaused by: ").append(getStackTrace(cause));
        }
        
        return sb.toString();
    }
}
