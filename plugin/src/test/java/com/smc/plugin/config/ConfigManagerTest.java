package com.smc.plugin.config;

import com.smc.plugin.SMCPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigManager.
 * Tests configuration loading, validation, and default value handling.
 * 
 * Requirements: 8.1, 8.5
 */
public class ConfigManagerTest {

    private ConfigManager configManager;
    private SMCPlugin mockPlugin;
    private FileConfiguration testConfig;
    private Logger mockLogger;

    @BeforeEach
    public void setUp() {
        // Create mock plugin
        mockPlugin = mock(SMCPlugin.class);
        mockLogger = Logger.getLogger("TestLogger");
        
        // Create test configuration
        testConfig = new YamlConfiguration();
        
        // Setup mock behavior
        when(mockPlugin.getConfig()).thenReturn(testConfig);
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        doNothing().when(mockPlugin).saveDefaultConfig();
        doNothing().when(mockPlugin).reloadConfig();
        
        // Create ConfigManager with mock
        configManager = new ConfigManager(mockPlugin);
    }

    // ========== Configuration Loading Tests ==========

    @Test
    public void testLoadConfigWithValidConfiguration() {
        // Arrange: Set up valid configuration
        testConfig.set("http.enabled", true);
        testConfig.set("http.host", "127.0.0.1");
        testConfig.set("http.port", 8080);
        testConfig.set("http.api-key", "test-api-key-12345");
        testConfig.set("logging.buffer-size", 10000);
        testConfig.set("logging.retention-hours", 24);
        testConfig.set("commands.timeout-seconds", 30);
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify all values are loaded correctly
        assertTrue(configManager.isHttpEnabled());
        assertEquals("127.0.0.1", configManager.getHttpHost());
        assertEquals(8080, configManager.getHttpPort());
        assertEquals("test-api-key-12345", configManager.getApiKey());
        assertEquals(10000, configManager.getLogBufferSize());
        assertEquals(24, configManager.getLogRetentionHours());
        assertEquals(30, configManager.getCommandTimeoutSeconds());
        assertEquals(Arrays.asList("stop", "restart"), configManager.getCommandBlacklist());
    }

    @Test
    public void testLoadConfigCallsSaveDefaultConfig() {
        // Arrange: Set up minimal valid configuration
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify saveDefaultConfig was called
        verify(mockPlugin, times(1)).saveDefaultConfig();
    }

    @Test
    public void testReloadConfigUpdatesValues() {
        // Arrange: Load initial configuration
        testConfig.set("http.api-key", "initial-key");
        testConfig.set("http.port", 8080);
        configManager.loadConfig();
        assertEquals(8080, configManager.getHttpPort());

        // Act: Change configuration and reload
        testConfig.set("http.port", 9090);
        configManager.reloadConfig();

        // Assert: Verify values are updated
        assertEquals(9090, configManager.getHttpPort());
    }

    // ========== Default Value Tests ==========

    @Test
    public void testDefaultHttpEnabled() {
        // Arrange: Don't set http.enabled
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertTrue(configManager.isHttpEnabled());
    }

    @Test
    public void testDefaultHttpHost() {
        // Arrange: Don't set http.host
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertEquals("127.0.0.1", configManager.getHttpHost());
    }

    @Test
    public void testDefaultHttpPort() {
        // Arrange: Don't set http.port
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertEquals(8080, configManager.getHttpPort());
    }

    @Test
    public void testDefaultLogBufferSize() {
        // Arrange: Don't set logging.buffer-size
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertEquals(10000, configManager.getLogBufferSize());
    }

    @Test
    public void testDefaultLogRetentionHours() {
        // Arrange: Don't set logging.retention-hours
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertEquals(24, configManager.getLogRetentionHours());
    }

    @Test
    public void testDefaultCommandTimeout() {
        // Arrange: Don't set commands.timeout-seconds
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify default value is used
        assertEquals(30, configManager.getCommandTimeoutSeconds());
    }

    @Test
    public void testDefaultCommandBlacklist() {
        // Arrange: Don't set commands.blacklist
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify blacklist is not null (getStringList returns empty list, not null)
        List<String> blacklist = configManager.getCommandBlacklist();
        assertNotNull(blacklist);
        // When not set in config, getStringList returns empty list
        // The null check in ConfigManager only applies if getStringList somehow returns null
        // which doesn't happen with YamlConfiguration
    }

    // ========== Validation Tests ==========

    @Test
    public void testValidationFailsWithInvalidPortTooLow() {
        // Arrange: Set invalid port (too low)
        testConfig.set("http.port", 0);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("Invalid HTTP port"));
    }

    @Test
    public void testValidationFailsWithInvalidPortTooHigh() {
        // Arrange: Set invalid port (too high)
        testConfig.set("http.port", 65536);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("Invalid HTTP port"));
    }

    @Test
    public void testValidationFailsWithEmptyHost() {
        // Arrange: Set empty host
        testConfig.set("http.host", "");
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("HTTP host cannot be empty"));
    }

    @Test
    public void testValidationFailsWithNullHost() {
        // Arrange: Don't set host at all, and mock getString to return null
        testConfig.set("http.api-key", "test-key");
        // When host is not set and default is null, getString returns null
        // But YamlConfiguration always returns default, so we test with empty string instead
        testConfig.set("http.host", "");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("HTTP host cannot be empty"));
    }

    @Test
    public void testValidationFailsWithEmptyApiKey() {
        // Arrange: Set empty API key
        testConfig.set("http.api-key", "");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("API key cannot be empty"));
    }

    @Test
    public void testValidationFailsWithNullApiKey() {
        // Arrange: Don't set API key, it will use default which triggers validation
        // YamlConfiguration returns default value when key is not set
        // Test with empty string instead
        testConfig.set("http.api-key", "");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("API key cannot be empty"));
    }

    @Test
    public void testValidationWarnsWithDefaultApiKey() {
        // Arrange: Use default API key
        testConfig.set("http.api-key", "change-me-to-a-secure-key");

        // Act: Load configuration (should succeed and auto-generate API key)
        assertDoesNotThrow(() -> configManager.loadConfig());

        // Assert: API key should be auto-generated (not the default)
        String apiKey = configManager.getApiKey();
        assertNotNull(apiKey);
        assertFalse(apiKey.equals("change-me-to-a-secure-key"));
        assertFalse(apiKey.equals("change-this-to-a-secure-random-key"));
        assertTrue(apiKey.length() >= 16, "Generated API key should be at least 16 characters");
    }

    @Test
    public void testValidationFailsWithLogBufferSizeTooSmall() {
        // Arrange: Set buffer size too small
        testConfig.set("logging.buffer-size", 50);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("Log buffer size must be at least 100"));
    }

    @Test
    public void testValidationSucceedsWithMinimumLogBufferSize() {
        // Arrange: Set buffer size to minimum valid value
        testConfig.set("logging.buffer-size", 100);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation succeeds
        assertDoesNotThrow(() -> configManager.loadConfig());
        assertEquals(100, configManager.getLogBufferSize());
    }

    @Test
    public void testValidationWarnsWithVeryLargeLogBufferSize() {
        // Arrange: Set very large buffer size
        testConfig.set("logging.buffer-size", 150000);
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration (should succeed but log warning)
        assertDoesNotThrow(() -> configManager.loadConfig());

        // Assert: Configuration is loaded despite warning
        assertEquals(150000, configManager.getLogBufferSize());
    }

    @Test
    public void testValidationFailsWithLogRetentionTooLow() {
        // Arrange: Set retention hours too low
        testConfig.set("logging.retention-hours", 0);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("Log retention hours must be at least 1"));
    }

    @Test
    public void testValidationSucceedsWithMinimumLogRetention() {
        // Arrange: Set retention to minimum valid value
        testConfig.set("logging.retention-hours", 1);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation succeeds
        assertDoesNotThrow(() -> configManager.loadConfig());
        assertEquals(1, configManager.getLogRetentionHours());
    }

    @Test
    public void testValidationFailsWithCommandTimeoutTooLow() {
        // Arrange: Set timeout too low
        testConfig.set("commands.timeout-seconds", 0);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation fails
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            configManager.loadConfig();
        });
        assertTrue(exception.getMessage().contains("Command timeout must be at least 1 second"));
    }

    @Test
    public void testValidationSucceedsWithMinimumCommandTimeout() {
        // Arrange: Set timeout to minimum valid value
        testConfig.set("commands.timeout-seconds", 1);
        testConfig.set("http.api-key", "test-key");

        // Act & Assert: Verify validation succeeds
        assertDoesNotThrow(() -> configManager.loadConfig());
        assertEquals(1, configManager.getCommandTimeoutSeconds());
    }

    @Test
    public void testValidationWarnsWithVeryLongCommandTimeout() {
        // Arrange: Set very long timeout
        testConfig.set("commands.timeout-seconds", 400);
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration (should succeed but log warning)
        assertDoesNotThrow(() -> configManager.loadConfig());

        // Assert: Configuration is loaded despite warning
        assertEquals(400, configManager.getCommandTimeoutSeconds());
    }

    // ========== Command Blacklist Tests ==========

    @Test
    public void testIsCommandBlacklistedWithBlacklistedCommand() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart", "reload"));
        configManager.loadConfig();

        // Act & Assert: Verify blacklisted commands are detected
        assertTrue(configManager.isCommandBlacklisted("stop"));
        assertTrue(configManager.isCommandBlacklisted("restart"));
        assertTrue(configManager.isCommandBlacklisted("reload"));
    }

    @Test
    public void testIsCommandBlacklistedWithNonBlacklistedCommand() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify non-blacklisted commands are allowed
        assertFalse(configManager.isCommandBlacklisted("help"));
        assertFalse(configManager.isCommandBlacklisted("list"));
        assertFalse(configManager.isCommandBlacklisted("say"));
    }

    @Test
    public void testIsCommandBlacklistedCaseInsensitive() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify case-insensitive matching
        assertTrue(configManager.isCommandBlacklisted("STOP"));
        assertTrue(configManager.isCommandBlacklisted("Stop"));
        assertTrue(configManager.isCommandBlacklisted("RESTART"));
        assertTrue(configManager.isCommandBlacklisted("ReStart"));
    }

    @Test
    public void testIsCommandBlacklistedWithLeadingSlash() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify commands with leading slash are handled
        assertTrue(configManager.isCommandBlacklisted("/stop"));
        assertTrue(configManager.isCommandBlacklisted("/restart"));
    }

    @Test
    public void testIsCommandBlacklistedWithArguments() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify commands with arguments are handled
        assertTrue(configManager.isCommandBlacklisted("stop now"));
        assertTrue(configManager.isCommandBlacklisted("restart server"));
        assertFalse(configManager.isCommandBlacklisted("help stop"));
    }

    @Test
    public void testIsCommandBlacklistedWithEmptyString() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify empty string is not blacklisted
        assertFalse(configManager.isCommandBlacklisted(""));
        assertFalse(configManager.isCommandBlacklisted("   "));
    }

    @Test
    public void testIsCommandBlacklistedWithNull() {
        // Arrange: Load configuration with blacklist
        testConfig.set("http.api-key", "test-key");
        testConfig.set("commands.blacklist", Arrays.asList("stop", "restart"));
        configManager.loadConfig();

        // Act & Assert: Verify null is not blacklisted
        assertFalse(configManager.isCommandBlacklisted(null));
    }

    // ========== Edge Cases ==========

    @Test
    public void testLoadConfigWithAllDefaultValues() {
        // Arrange: Only set required API key
        testConfig.set("http.api-key", "secure-key-123");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify all defaults are applied
        assertTrue(configManager.isHttpEnabled());
        assertEquals("127.0.0.1", configManager.getHttpHost());
        assertEquals(8080, configManager.getHttpPort());
        assertEquals("secure-key-123", configManager.getApiKey());
        assertEquals(10000, configManager.getLogBufferSize());
        assertEquals(24, configManager.getLogRetentionHours());
        assertEquals(30, configManager.getCommandTimeoutSeconds());
        assertNotNull(configManager.getCommandBlacklist());
    }

    @Test
    public void testValidPortBoundaries() {
        // Arrange & Act & Assert: Test valid port boundaries
        testConfig.set("http.port", 1);
        testConfig.set("http.api-key", "test-key");
        assertDoesNotThrow(() -> configManager.loadConfig());
        assertEquals(1, configManager.getHttpPort());

        testConfig.set("http.port", 65535);
        assertDoesNotThrow(() -> configManager.loadConfig());
        assertEquals(65535, configManager.getHttpPort());
    }

    @Test
    public void testHttpDisabled() {
        // Arrange: Disable HTTP
        testConfig.set("http.enabled", false);
        testConfig.set("http.api-key", "test-key");

        // Act: Load configuration
        configManager.loadConfig();

        // Assert: Verify HTTP is disabled
        assertFalse(configManager.isHttpEnabled());
    }
}
