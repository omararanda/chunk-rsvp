package com.chunkrsvp.engine;

import com.chunkrsvp.cli.CliArguments;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.util.ConfigService;
import com.chunkrsvp.cli.ui.MockViewManager;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.DefaultConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;

public class RSVPEngineTest {
    private ConfigService configService;
    private final String tmpConfig = System.getProperty("java.io.tmpdir") + "/test_rsvp.properties";
    private final MockViewManager mockView = new MockViewManager();

    @BeforeEach
    void setup() {
        new File(tmpConfig).delete();
        configService = new ConfigService(tmpConfig);
    }

    private ConfigurationManager createCm(int wpm, double sm, double pm, int sd, int pd) {
        return new ConfigurationManager(configService, new CliArguments(wpm, sm, pm, sd, pd, false, false, null), new DefaultConfigProvider());
    }

    @Test
    void testDelayCalculation_BaseSpeed() {
        RSVPEngine engine = new RSVPEngine(createCm(300, 0.0, 0.0, 0, 0), mockView);
        assertEquals(200, engine.calculateDelay(new Chunk("Word")));
    }

    @Test
    void testDelayCalculation_AdditiveStop() {
        RSVPEngine engine = new RSVPEngine(createCm(300, 0.0, 0.0, 50, 0), mockView);
        // 200ms (1 word) + 50ms (1 stop) = 250ms
        assertEquals(250, engine.calculateDelay(new Chunk("Word.")));
    }

    @Test
    void testDelayCalculation_PercentagePause() {
        RSVPEngine engine = new RSVPEngine(createCm(300, 0.0, 10.0, 0, 0), mockView);
        // Base delay 200ms + (200ms * 0.10 * 1 pause) = 220ms
        assertEquals(220, engine.calculateDelay(new Chunk("Word,")));
    }

    @Test
    void testDelayCalculation_HybridPrecedence() {
        // Delay (30ms) overrides Perc (10%)
        RSVPEngine engine = new RSVPEngine(createCm(300, 10.0, 10.0, 30, 0), mockView);
        // Base delay 200ms + 30ms (stop delay) = 230ms
        assertEquals(230, engine.calculateDelay(new Chunk("Word.")));
    }

    @Test
    void testCalculateDelay_FullStringScan() {
        RSVPEngine engine = new RSVPEngine(createCm(300, 0.0, 0.0, 0, 10), mockView);
        assertEquals(630, engine.calculateDelay(new Chunk("one, two, three,")));
    }

    @Test
    void testCalculateDelay_OnlyPunctuation() {
        RSVPEngine engine = new RSVPEngine(createCm(300, 0.0, 0.0, 0, 10), mockView);
        // Base delay 0ms (0 words) + (3 pauses * 10ms) = 30ms
        assertEquals(30, engine.calculateDelay(new Chunk(",,,")));
    }

    @Test
    void testCalculateDelay_HighLowWPM() {
        RSVPEngine slowEngine = new RSVPEngine(createCm(10, 0.0, 0.0, 0, 0), mockView);
        assertEquals(6000, slowEngine.calculateDelay(new Chunk("word")));
        
        RSVPEngine fastEngine = new RSVPEngine(createCm(100000, 0.0, 0.0, 0, 0), mockView);
        assertEquals(0, fastEngine.calculateDelay(new Chunk("word")));
    }

    @Test
    void testNoControlsIgnoresInput() throws Exception {
        ConfigurationManager cm = new ConfigurationManager(configService, 
            new CliArguments(300, null, null, null, null, false, false, null, true), 
            new DefaultConfigProvider());
        RSVPEngine engine = new RSVPEngine(cm, mockView);
        
        // Mock input for SPEED_UP (Arrow Up: ESC [ A)
        java.io.InputStream bais = new java.io.ByteArrayInputStream(new byte[]{27, '[', 'A'});
        com.chunkrsvp.cli.input.InputController ic = new com.chunkrsvp.cli.input.InputController(bais);
        
        com.chunkrsvp.model.ChunkProvider provider = new com.chunkrsvp.model.ListChunkProvider(java.util.List.of(new Chunk("test")));
        
        // We run in a separate thread because run() is blocking, 
        // or we just check if calculateDelay changes after one loop.
        // Actually, run() will process the chunk and wait.
        // Since we only have one chunk, it will finish quickly.
        
        engine.run(provider, ic);
        
        assertEquals(300, cm.getConfig().wpm(), "WPM should NOT have changed in no-controls mode");
    }
}
