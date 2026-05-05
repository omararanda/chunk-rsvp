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
        return new ConfigurationManager(cs, new CliArguments(null, null, null, null, null, false, false, null), new DefaultConfigProvider());
    }

    private ConfigurationManager createCmWithCli(int wpm, double sm, double pm, int sd, int pd, ConfigService cs) {
        return new ConfigurationManager(cs, new CliArguments(wpm, sm, pm, sd, pd, false, false, null), new DefaultConfigProvider());
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
        cm.shutdown();
        
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
        cm.shutdown();

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
        cm.shutdown();

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
        cm.shutdown();

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
        cm.shutdown();

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
        ConfigurationManager cm = new ConfigurationManager(cs, new CliArguments(400, null, null, null, null, false, false, null), new DefaultConfigProvider());
        
        // Update WPM interactively to 500
        RsvpConfig cfg = cm.getConfig();
        cm.updateConfig(new RsvpConfig(500, cfg.stopPerc(), cfg.pausePerc(), cfg.stopDelayMs(), cfg.pauseDelayMs()));
        cm.shutdown();
        
        // WPM was initially set by CLI, so it should NOT be persisted
        java.util.Properties reloaded = cs.load();
        assertEquals("200", reloaded.getProperty("wpm"), "WPM should remain 200 in file because it was a CLI override");
    }

    @Test
    void testInitAndHelpFlags() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        ConfigurationManager cm = new ConfigurationManager(cs, new CliArguments(null, null, null, null, null, true, true, null), new DefaultConfigProvider());

        assertTrue(cm.isHelp());
        assertTrue(cm.isInit());
    }

    @Test
    void testNoControlsConfig() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        CliArguments cli = new CliArguments(null, null, null, null, null, false, false, null, true);
        ConfigurationManager cm = new ConfigurationManager(cs, cli, new DefaultConfigProvider());
        
        assertTrue(cm.getConfig().noControls(), "noControls flag should be propagated to RsvpConfig");
    }

    @Test
    void testNoControlsConfigDefaultIsFalse() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        CliArguments cli = new CliArguments(null, null, null, null, null, false, false, null);
        ConfigurationManager cm = new ConfigurationManager(cs, cli, new DefaultConfigProvider());
        
        assertFalse(cm.getConfig().noControls(), "noControls should be false by default in RsvpConfig");
    }

    @Test
    void testUpdateConfigIsDebounced() throws Exception {
        MockConfigService mcs = new MockConfigService(true);
        ConfigurationManager cm = new ConfigurationManager(mcs, new CliArguments(null, null, null, null, null, false, false, null), new DefaultConfigProvider());

        // Rapid updates
        RsvpConfig base = cm.getConfig();
        for (int i = 0; i < 5; i++) {
            cm.updateConfig(new RsvpConfig(300 + i, base.stopPerc(), base.pausePerc(), base.stopDelayMs(), base.pauseDelayMs()));
        }

        // Immediately after updates, saveCount should be 0 because it's async and debounced
        assertEquals(0, mcs.saveCount.get(), "Save should be debounced and not happen immediately");

        // Wait for debounce (scheduled for 500ms in plan)
        Thread.sleep(800);
        assertEquals(1, mcs.saveCount.get(), "Multiple updates should be debounced into a single save");
    }

    @Test
    void testShutdownFlushesPendingSave() throws Exception {
        MockConfigService mcs = new MockConfigService(true);
        ConfigurationManager cm = new ConfigurationManager(mcs, new CliArguments(null, null, null, null, null, false, false, null), new DefaultConfigProvider());

        RsvpConfig base = cm.getConfig();
        cm.updateConfig(new RsvpConfig(500, base.stopPerc(), base.pausePerc(), base.stopDelayMs(), base.pauseDelayMs()));
        
        assertEquals(0, mcs.saveCount.get());
        
        cm.shutdown();
        assertEquals(1, mcs.saveCount.get(), "Shutdown should flush pending save");
    }
}
