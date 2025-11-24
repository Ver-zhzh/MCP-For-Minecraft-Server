package com.smc.plugin.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogCollector.
 */
class LogCollectorTest {
    
    private LogCollector logCollector;
    
    @BeforeEach
    void setUp() {
        logCollector = new LogCollector(100, 24);
    }
    
    @Test
    void testAddLog() {
        logCollector.addLog("INFO", "TestLogger", "Test message");
        
        List<LogCollector.LogEntry> logs = logCollector.getAllLogs();
        assertEquals(1, logs.size());
        
        LogCollector.LogEntry entry = logs.get(0);
        assertEquals("INFO", entry.getLevel());
        assertEquals("TestLogger", entry.getLogger());
        assertEquals("Test message", entry.getMessage());
    }
    
    @Test
    void testAddLogWithLevel() {
        logCollector.addLog(Level.WARNING, "TestLogger", "Warning message");
        
        List<LogCollector.LogEntry> logs = logCollector.getAllLogs();
        assertEquals(1, logs.size());
        
        LogCollector.LogEntry entry = logs.get(0);
        assertEquals("WARNING", entry.getLevel());
    }
    
    @Test
    void testCircularBuffer() {
        // Add more logs than the buffer size
        for (int i = 0; i < 150; i++) {
            logCollector.addLog("INFO", "TestLogger", "Message " + i);
        }
        
        // Should only keep the last 100 entries
        assertEquals(100, logCollector.size());
        
        // The oldest entries should be removed
        List<LogCollector.LogEntry> logs = logCollector.getAllLogs();
        assertEquals("Message 149", logs.get(0).getMessage()); // Newest first
        assertEquals("Message 50", logs.get(99).getMessage()); // Oldest kept
    }
    
    @Test
    void testGetLogsWithLimit() {
        for (int i = 0; i < 50; i++) {
            logCollector.addLog("INFO", "TestLogger", "Message " + i);
        }
        
        List<LogCollector.LogEntry> logs = logCollector.getLogs(10, null, null);
        assertEquals(10, logs.size());
        
        // Should return newest first
        assertEquals("Message 49", logs.get(0).getMessage());
        assertEquals("Message 40", logs.get(9).getMessage());
    }
    
    @Test
    void testGetLogsWithTimeRange() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        
        logCollector.addLog("INFO", "TestLogger", "Message 1");
        Thread.sleep(50);
        
        long midTime = System.currentTimeMillis();
        
        logCollector.addLog("INFO", "TestLogger", "Message 2");
        Thread.sleep(50);
        
        long endTime = System.currentTimeMillis();
        
        // Get all logs in the full range
        List<LogCollector.LogEntry> logs = logCollector.getLogs(100, startTime, endTime);
        assertEquals(2, logs.size());
        
        // Get logs before midTime (should get Message 1)
        logs = logCollector.getLogs(100, startTime, midTime - 1);
        assertEquals(1, logs.size());
        assertEquals("Message 1", logs.get(0).getMessage());
        
        // Get logs after midTime (should get Message 2)
        logs = logCollector.getLogs(100, midTime, endTime);
        assertEquals(1, logs.size());
        assertEquals("Message 2", logs.get(0).getMessage());
    }
    
    @Test
    void testGetLogsByLevel() {
        logCollector.addLog("INFO", "TestLogger", "Info message");
        logCollector.addLog("ERROR", "TestLogger", "Error message");
        logCollector.addLog("WARN", "TestLogger", "Warning message");
        logCollector.addLog("INFO", "TestLogger", "Another info");
        
        List<LogCollector.LogEntry> errors = logCollector.getLogsByLevel("ERROR", null, 100);
        assertEquals(1, errors.size());
        assertEquals("Error message", errors.get(0).getMessage());
        
        List<LogCollector.LogEntry> infos = logCollector.getLogsByLevel("INFO", null, 100);
        assertEquals(2, infos.size());
    }
    
    @Test
    void testGetLogsByLevelCaseInsensitive() {
        logCollector.addLog("ERROR", "TestLogger", "Error message");
        
        List<LogCollector.LogEntry> errors = logCollector.getLogsByLevel("error", null, 100);
        assertEquals(1, errors.size());
    }
    
    @Test
    void testGetLogsByLevelWithPluginFilter() {
        logCollector.addLog("ERROR", "PluginA", "Error from A");
        logCollector.addLog("ERROR", "PluginB", "Error from B");
        logCollector.addLog("ERROR", "PluginA", "Another error from A");
        
        List<LogCollector.LogEntry> pluginAErrors = logCollector.getLogsByLevel("ERROR", "PluginA", 100);
        assertEquals(2, pluginAErrors.size());
        
        List<LogCollector.LogEntry> pluginBErrors = logCollector.getLogsByLevel("ERROR", "PluginB", 100);
        assertEquals(1, pluginBErrors.size());
    }
    
    @Test
    void testGetErrors() {
        logCollector.addLog("INFO", "TestLogger", "Info message");
        logCollector.addLog("ERROR", "TestLogger", "Error message");
        logCollector.addLog("ERROR", "OtherLogger", "Another error");
        
        List<LogCollector.LogEntry> errors = logCollector.getErrors(null, 100);
        assertEquals(2, errors.size());
        
        List<LogCollector.LogEntry> testLoggerErrors = logCollector.getErrors("TestLogger", 100);
        assertEquals(1, testLoggerErrors.size());
    }
    
    @Test
    void testGetWarnings() {
        logCollector.addLog("INFO", "TestLogger", "Info message");
        logCollector.addLog("WARN", "TestLogger", "Warning message");
        logCollector.addLog("WARNING", "TestLogger", "Another warning");
        logCollector.addLog("ERROR", "TestLogger", "Error message");
        
        List<LogCollector.LogEntry> warnings = logCollector.getWarnings(null, 100);
        assertEquals(2, warnings.size());
    }
    
    @Test
    void testGetWarningsWithPluginFilter() {
        logCollector.addLog("WARN", "PluginA", "Warning from A");
        logCollector.addLog("WARNING", "PluginB", "Warning from B");
        logCollector.addLog("WARN", "PluginA", "Another warning from A");
        
        List<LogCollector.LogEntry> pluginAWarnings = logCollector.getWarnings("PluginA", 100);
        assertEquals(2, pluginAWarnings.size());
    }
    
    @Test
    void testClearOldLogs() throws InterruptedException {
        // Create a collector with very short retention (1 millisecond)
        LogCollector shortRetention = new LogCollector(100, 0);
        
        shortRetention.addLog("INFO", "TestLogger", "Old message");
        Thread.sleep(100); // Wait for retention period to pass
        
        shortRetention.clearOldLogs();
        
        // Old logs should be removed
        assertEquals(0, shortRetention.size());
    }
    
    @Test
    void testClear() {
        for (int i = 0; i < 10; i++) {
            logCollector.addLog("INFO", "TestLogger", "Message " + i);
        }
        
        assertEquals(10, logCollector.size());
        
        logCollector.clear();
        
        assertEquals(0, logCollector.size());
    }
    
    @Test
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int logsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // Add logs from multiple threads concurrently
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < logsPerThread; i++) {
                        logCollector.addLog("INFO", "Thread" + threadId, "Message " + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Should have collected logs from all threads (up to buffer size)
        assertEquals(100, logCollector.size()); // Buffer size is 100
    }
    
    @Test
    void testLogEntryToString() {
        LogCollector.LogEntry entry = new LogCollector.LogEntry(
            1234567890L,
            "INFO",
            "TestLogger",
            "Test message"
        );
        
        String str = entry.toString();
        assertTrue(str.contains("1234567890"));
        assertTrue(str.contains("INFO"));
        assertTrue(str.contains("TestLogger"));
        assertTrue(str.contains("Test message"));
    }
    
    @Test
    void testDefaultConstructor() {
        LogCollector defaultCollector = new LogCollector();
        
        // Should use default values (10000 entries, 24 hours)
        for (int i = 0; i < 100; i++) {
            defaultCollector.addLog("INFO", "TestLogger", "Message " + i);
        }
        
        assertEquals(100, defaultCollector.size());
    }
}
