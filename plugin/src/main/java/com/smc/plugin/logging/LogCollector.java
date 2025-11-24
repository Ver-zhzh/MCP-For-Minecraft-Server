package com.smc.plugin.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * Thread-safe log collector that maintains a circular buffer of recent log entries.
 * 
 * This class collects log messages from the Minecraft server and stores them in memory
 * for later retrieval via the HTTP API. It uses a circular buffer to limit memory usage
 * and provides filtering capabilities for querying logs.
 */
public class LogCollector {
    
    /**
     * Represents a single log entry with timestamp, level, logger name, and message.
     */
    public static class LogEntry {
        private final long timestamp;
        private final String level;
        private final String logger;
        private final String message;
        
        /**
         * Creates a new log entry.
         * 
         * @param timestamp Unix timestamp in milliseconds
         * @param level Log level (INFO, WARN, ERROR, etc.)
         * @param logger Logger name (often the plugin or class name)
         * @param message Log message content
         */
        public LogEntry(long timestamp, String level, String logger, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.logger = logger;
            this.message = message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getLogger() {
            return logger;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("[%d] [%s] [%s] %s", timestamp, level, logger, message);
        }
    }
    
    private final int maxSize;
    private final ConcurrentLinkedQueue<LogEntry> logBuffer;
    private final ReadWriteLock lock;
    private final long retentionMillis;
    
    /**
     * Creates a new LogCollector with the specified buffer size and retention period.
     * 
     * @param maxSize Maximum number of log entries to store
     * @param retentionHours Number of hours to retain logs (for automatic cleanup)
     */
    public LogCollector(int maxSize, int retentionHours) {
        this.maxSize = maxSize;
        this.logBuffer = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantReadWriteLock();
        this.retentionMillis = retentionHours * 60L * 60L * 1000L;
    }
    
    /**
     * Creates a new LogCollector with default settings (10000 entries, 24 hours retention).
     */
    public LogCollector() {
        this(10000, 24);
    }
    
    /**
     * Adds a log entry to the buffer.
     * This method is thread-safe and can be called from multiple threads.
     * If the buffer is full, the oldest entry is removed.
     * 
     * @param level Log level
     * @param logger Logger name
     * @param message Log message
     */
    public void addLog(String level, String logger, String message) {
        long timestamp = System.currentTimeMillis();
        LogEntry entry = new LogEntry(timestamp, level, logger, message);
        
        lock.writeLock().lock();
        try {
            // Add the new entry
            logBuffer.offer(entry);
            
            // Remove oldest entries if buffer is full
            while (logBuffer.size() > maxSize) {
                logBuffer.poll();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Adds a log entry using Java's logging Level enum.
     * 
     * @param level Java logging Level
     * @param logger Logger name
     * @param message Log message
     */
    public void addLog(Level level, String logger, String message) {
        addLog(level.getName(), logger, message);
    }
    
    /**
     * Retrieves all log entries from the buffer.
     * 
     * @return List of all log entries (newest first)
     */
    public List<LogEntry> getAllLogs() {
        return getLogs(Integer.MAX_VALUE, null, null);
    }
    
    /**
     * Retrieves log entries with optional filtering.
     * 
     * @param limit Maximum number of entries to return (most recent)
     * @param startTime Start of time range (Unix timestamp in ms), null for no lower bound
     * @param endTime End of time range (Unix timestamp in ms), null for no upper bound
     * @return List of filtered log entries (newest first)
     */
    public List<LogEntry> getLogs(int limit, Long startTime, Long endTime) {
        lock.readLock().lock();
        try {
            List<LogEntry> result = new ArrayList<>();
            
            // Convert queue to list for easier processing
            List<LogEntry> allLogs = new ArrayList<>(logBuffer);
            
            // Reverse to get newest first
            Collections.reverse(allLogs);
            
            // Filter by time range and collect up to limit
            for (LogEntry entry : allLogs) {
                if (result.size() >= limit) {
                    break;
                }
                
                // Check time range
                if (startTime != null && entry.getTimestamp() < startTime) {
                    continue;
                }
                if (endTime != null && entry.getTimestamp() > endTime) {
                    continue;
                }
                
                result.add(entry);
            }
            
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retrieves log entries filtered by log level.
     * 
     * @param level Log level to filter by (e.g., "ERROR", "WARN")
     * @param pluginName Optional plugin name to filter by (null for all plugins)
     * @param limit Maximum number of entries to return
     * @return List of filtered log entries (newest first)
     */
    public List<LogEntry> getLogsByLevel(String level, String pluginName, int limit) {
        lock.readLock().lock();
        try {
            List<LogEntry> result = new ArrayList<>();
            
            // Convert queue to list for easier processing
            List<LogEntry> allLogs = new ArrayList<>(logBuffer);
            
            // Reverse to get newest first
            Collections.reverse(allLogs);
            
            // Filter by level and optional plugin name
            for (LogEntry entry : allLogs) {
                if (result.size() >= limit) {
                    break;
                }
                
                // Check level (case-insensitive)
                if (!entry.getLevel().equalsIgnoreCase(level)) {
                    continue;
                }
                
                // Check plugin name if specified
                if (pluginName != null && !entry.getLogger().contains(pluginName)) {
                    continue;
                }
                
                result.add(entry);
            }
            
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retrieves error logs (ERROR level).
     * 
     * @param pluginName Optional plugin name to filter by
     * @param limit Maximum number of entries to return
     * @return List of error log entries (newest first)
     */
    public List<LogEntry> getErrors(String pluginName, int limit) {
        return getLogsByLevel("ERROR", pluginName, limit);
    }
    
    /**
     * Retrieves warning logs (WARN or WARNING level).
     * 
     * @param pluginName Optional plugin name to filter by
     * @param limit Maximum number of entries to return
     * @return List of warning log entries (newest first)
     */
    public List<LogEntry> getWarnings(String pluginName, int limit) {
        lock.readLock().lock();
        try {
            List<LogEntry> result = new ArrayList<>();
            
            // Convert queue to list for easier processing
            List<LogEntry> allLogs = new ArrayList<>(logBuffer);
            
            // Reverse to get newest first
            Collections.reverse(allLogs);
            
            // Filter by WARN or WARNING level and optional plugin name
            for (LogEntry entry : allLogs) {
                if (result.size() >= limit) {
                    break;
                }
                
                // Check level (WARN or WARNING)
                String entryLevel = entry.getLevel().toUpperCase();
                if (!entryLevel.equals("WARN") && !entryLevel.equals("WARNING")) {
                    continue;
                }
                
                // Check plugin name if specified
                if (pluginName != null && !entry.getLogger().contains(pluginName)) {
                    continue;
                }
                
                result.add(entry);
            }
            
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Removes log entries older than the retention period.
     * This method should be called periodically to prevent memory buildup.
     */
    public void clearOldLogs() {
        long cutoffTime = System.currentTimeMillis() - retentionMillis;
        
        lock.writeLock().lock();
        try {
            // Remove entries older than cutoff time
            logBuffer.removeIf(entry -> entry.getTimestamp() < cutoffTime);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Clears all log entries from the buffer.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            logBuffer.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Gets the current number of log entries in the buffer.
     * 
     * @return Number of log entries
     */
    public int size() {
        return logBuffer.size();
    }
}
