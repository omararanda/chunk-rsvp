package com.chunkrsvp.cli;

import com.chunkrsvp.engine.RSVPEngine;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.util.ConfigService;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.DefaultConfigProvider;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkRSVPTest {

    @Test
    public void testDelayCalculation() {
        CliArguments cli = new CliArguments(300, 2.0, 1.5, 50, 20, false, false, null);
        ConfigurationManager cm = new ConfigurationManager(
            new ConfigService(System.getProperty("java.io.tmpdir") + "/test_config.properties"), cli, new DefaultConfigProvider());
        
        RSVPEngine engine = new RSVPEngine(cm, new com.chunkrsvp.cli.ui.MockViewManager());
        
        // Single word chunk -> 200ms + 0 = 200ms
        Chunk word = new Chunk("Test");
        assertEquals(200, engine.calculateDelay(word));
    }
}
