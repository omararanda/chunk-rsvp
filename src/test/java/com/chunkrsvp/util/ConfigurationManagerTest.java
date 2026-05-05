package com.chunkrsvp.util;

import com.chunkrsvp.cli.CliArguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void testConfigResolutionPriority() {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        Properties props = new Properties();
        props.setProperty("wpm", "200");
        cs.save(props);

        CliArguments cli = new CliArguments(400, null, null, null, null, false, false);
        ConfigurationManager cm = new ConfigurationManager(cs, cli);

        assertEquals(400, cm.getConfig().wpm(), "CLI WPM should override properties");
    }

    @Test
    void testUpdateWpmPersistsState() {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        CliArguments cli = new CliArguments(300, null, null, null, null, false, false);
        ConfigurationManager cm = new ConfigurationManager(cs, cli);

        cm.updateWpm(500);
        
        assertEquals(500, cm.getConfig().wpm());
        
        // Verify persistence
        Properties reloaded = cs.load();
        assertEquals("500", reloaded.getProperty("wpm"));
    }

    @Test
    void testInitAndHelpFlags() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        CliArguments cli = new CliArguments(null, null, null, null, null, true, true);
        ConfigurationManager cm = new ConfigurationManager(cs, cli);

        assertTrue(cm.isHelp());
        assertTrue(cm.isInit());
    }
}
