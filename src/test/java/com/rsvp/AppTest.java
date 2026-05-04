package com.rsvp;

import com.rsvp.engine.RSVPEngine;
import com.rsvp.model.Chunk;
import com.rsvp.util.ConfigService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;

public class AppTest {

    @Test
    public void testDelayCalculation() {
        // Base WPM: 300 -> 200ms per word
        // Create a temporary mock config service
        ConfigService mockConfig = new ConfigService(System.getProperty("java.io.tmpdir") + "/test_config.properties");
        RSVPEngine engine = new RSVPEngine(300, 2.0, 1.5, 50, 20, mockConfig);
        
        // Single word chunk -> 200ms + 0 = 200ms
        Chunk word = new Chunk("Test");
        assertEquals(200, engine.calculateDelay(word));
    }
}
