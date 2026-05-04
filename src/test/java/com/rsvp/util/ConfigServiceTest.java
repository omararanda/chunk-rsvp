package com.rsvp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigServiceTest {
    private String tmpPath;
    private ConfigService configService;

    @BeforeEach
    void setup() {
        tmpPath = System.getProperty("java.io.tmpdir") + "/test_config.properties";
        new File(tmpPath).delete();
        configService = new ConfigService(tmpPath);
    }

    @Test
    void testConfigLoading_MissingFile() {
        Properties props = configService.load();
        assertTrue(props.isEmpty());
    }

    @Test
    void testConfigLoading_Valid() throws IOException {
        Properties p = new Properties();
        p.setProperty("wpm", "500");
        try (FileOutputStream fos = new FileOutputStream(tmpPath)) { p.store(fos, null); }
        
        Properties loaded = configService.load();
        assertEquals("500", loaded.getProperty("wpm"));
    }

    @Test
    void testConfigSaving_Persistence() {
        Properties p = new Properties();
        p.setProperty("wpm", "400");
        configService.save(p);
        
        Properties loaded = configService.load();
        assertEquals("400", loaded.getProperty("wpm"));
    }
    
    @Test
    void testConfigLoading_PartialFile() throws IOException {
        Properties p = new Properties();
        p.setProperty("wpm", "250");
        try (FileOutputStream fos = new FileOutputStream(tmpPath)) { p.store(fos, null); }
        
        Properties loaded = configService.load();
        assertEquals("250", loaded.getProperty("wpm"));
        assertNull(loaded.getProperty("delay.stop"));
    }
}
