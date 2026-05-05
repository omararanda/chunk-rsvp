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

    private ConfigurationManager createCmEmptyCli(ConfigService cs) {
        return new ConfigurationManager(cs, new CliArguments(null, null, null, null, null, false, false), new DefaultConfigProvider());
    }

    private ConfigurationManager createCmWithCli(int wpm, double sm, double pm, int sd, int pd, ConfigService cs) {
        return new ConfigurationManager(cs, new CliArguments(wpm, sm, pm, sd, pd, false, false), new DefaultConfigProvider());
    }

    @Test
    void testConfigResolutionPriority() {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        Properties props = new Properties();
        props.setProperty("wpm", "200");
        cs.save(props);

        ConfigurationManager cm = createCmWithCli(400, 0.0, 0.0, 0, 0, cs);
        assertEquals(400, cm.getConfig().wpm(), "CLI WPM should override properties");
    }

    @Test
    void testUpdateWpmPersistsState() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("wpm", "300");
        cs.save(p);
        
        ConfigurationManager cm = createCmEmptyCli(cs);

        RsvpConfig cfg = cm.getConfig();
        cm.updateConfig(new RsvpConfig(500, cfg.stopPerc(), cfg.pausePerc(), cfg.stopDelayMs(), cfg.pauseDelayMs()));
        
        java.util.Properties reloaded = cs.load();
        assertEquals("500", reloaded.getProperty("wpm"));
    }

    @Test
    void testPersistOnlyUpdatesExistingKeys_WPM() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("wpm", "200");
        cs.save(p);
        
        ConfigurationManager cm = createCmEmptyCli(cs);
        cm.updateConfig(new RsvpConfig(500, 2.0, 1.5, 30, 10));

        java.util.Properties reloaded = cs.load();
        assertEquals("500", reloaded.getProperty("wpm"));
        assertNull(reloaded.getProperty("delay.stop"), "delay.stop should not be added to file");
    }

    @Test
    void testPersistUpdatesDelayProperties() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("wpm", "300");
        p.setProperty("delay.stop", "50");
        p.setProperty("delay.pause", "20");
        cs.save(p);
        
        ConfigurationManager cm = createCmEmptyCli(cs);
        cm.updateConfig(new RsvpConfig(300, 0.0, 0.0, 60, 30));

        java.util.Properties reloaded = cs.load();
        assertEquals("60", reloaded.getProperty("delay.stop"));
        assertEquals("30", reloaded.getProperty("delay.pause"));
        assertEquals("300", reloaded.getProperty("wpm"));
    }

    @Test
    void testPersistUpdatesPercentageProperties() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("perc.stop", "2.0");
        p.setProperty("perc.pause", "1.5");
        cs.save(p);
        
        ConfigurationManager cm = createCmEmptyCli(cs);
        cm.updateConfig(new RsvpConfig(300, 5.0, 4.0, 30, 10));

        java.util.Properties reloaded = cs.load();
        assertEquals("5.0", reloaded.getProperty("perc.stop"));
        assertEquals("4.0", reloaded.getProperty("perc.pause"));
    }

    @Test
    void testPersistDoesNotUpdateMissingProperties() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("wpm", "200");
        cs.save(p);
        
        ConfigurationManager cm = createCmEmptyCli(cs);
        cm.updateConfig(new RsvpConfig(500, 0.0, 0.0, 100, 50));

        java.util.Properties reloaded = cs.load();
        assertEquals("500", reloaded.getProperty("wpm"));
        assertNull(reloaded.getProperty("delay.stop"));
        assertNull(reloaded.getProperty("delay.pause"));
    }

    @Test
    void testCliFlagsAreNeverPersisted() throws Exception {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        
        java.util.Properties p = new java.util.Properties();
        p.setProperty("wpm", "200");
        cs.save(p);
        
        // CLI flag sets WPM to 400 (transient)
        ConfigurationManager cm = new ConfigurationManager(cs, new CliArguments(400, null, null, null, null, false, false), new DefaultConfigProvider());
        
        // Update WPM interactively to 500
        RsvpConfig cfg = cm.getConfig();
        cm.updateConfig(new RsvpConfig(500, cfg.stopPerc(), cfg.pausePerc(), cfg.stopDelayMs(), cfg.pauseDelayMs()));
        
        // WPM was initially set by CLI, so it should NOT be persisted
        java.util.Properties reloaded = cs.load();
        assertEquals("200", reloaded.getProperty("wpm"), "WPM should remain 200 in file because it was a CLI override");
    }

    @Test
    void testInitAndHelpFlags() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        ConfigurationManager cm = new ConfigurationManager(cs, new CliArguments(null, null, null, null, null, true, true), new DefaultConfigProvider());

        assertTrue(cm.isHelp());
        assertTrue(cm.isInit());
    }
}
