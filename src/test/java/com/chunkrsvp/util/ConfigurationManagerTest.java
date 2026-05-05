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

    private ConfigurationManager createCm(int wpm, double sm, double pm, int sd, int pd, ConfigService cs) {
        return new ConfigurationManager(cs, new CliArguments(wpm, sm, pm, sd, pd, false, false), new DefaultConfigProvider());
    }

    @Test
    void testConfigResolutionPriority() {
        Path configFile = tempDir.resolve("config.properties");
        ConfigService cs = new ConfigService(configFile.toString());
        Properties props = new Properties();
        props.setProperty("wpm", "200");
        cs.save(props);

        ConfigurationManager cm = createCm(400, 0.0, 0.0, 0, 0, cs);
        assertEquals(400, cm.getConfig().wpm(), "CLI WPM should override properties");
    }
@Test
void testUpdateWpmPersistsState() throws Exception {
    Path configFile = tempDir.resolve("config.properties");
    ConfigService cs = new ConfigService(configFile.toString());

    java.util.Properties p = new java.util.Properties();
    p.setProperty("wpm", "300");
    p.setProperty("perc.stop", "0.0");
    p.setProperty("perc.pause", "0.0");
    p.setProperty("delay.stop", "30");
    p.setProperty("delay.pause", "10");
    cs.save(p);

    ConfigurationManager cm = createCm(300, 0.0, 0.0, 0, 0, cs);

    RsvpConfig cfg = cm.getConfig();
    RsvpConfig newCfg = new RsvpConfig(500, cfg.stopPerc(), cfg.pausePerc(), cfg.stopDelayMs(), cfg.pauseDelayMs());
    cm.updateConfig(newCfg);

    // Verify manager updated
    assertEquals(500, cm.getConfig().wpm());

    // Verify persistence
    java.util.Properties reloaded = cs.load();
    assertEquals("500", reloaded.getProperty("wpm"));
}

    @Test
    void testInitAndHelpFlags() {
        ConfigService cs = new ConfigService(tempDir.resolve("config.properties").toString());
        ConfigurationManager cm = new ConfigurationManager(cs, new CliArguments(null, null, null, null, null, true, true), new DefaultConfigProvider());

        assertTrue(cm.isHelp());
        assertTrue(cm.isInit());
    }
}
