package com.smc.plugin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test for SMCPlugin.
 * More tests will be added in later tasks.
 */
public class SMCPluginTest {

    @Test
    public void testPluginClassExists() {
        // Verify that the plugin class exists and can be loaded
        assertNotNull(SMCPlugin.class);
        assertEquals("SMCPlugin", SMCPlugin.class.getSimpleName());
    }
}
