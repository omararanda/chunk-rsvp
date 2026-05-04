package com.rsvp.engine;

import com.rsvp.model.Chunk;
import com.rsvp.util.ConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;

public class RSVPEngineTest {
    private ConfigService configService;
    private final String tmpConfig = System.getProperty("java.io.tmpdir") + "/test_rsvp.properties";

    @BeforeEach
    void setup() {
        new File(tmpConfig).delete();
        configService = new ConfigService(tmpConfig);
    }

    @Test
    void testDelayCalculation_BaseSpeed() {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 0, configService);
        assertEquals(200, engine.calculateDelay(new Chunk("Word")));
    }

    @Test
    void testDelayCalculation_AdditiveStop() {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 50, 0, configService);
        // 200ms (1 word) + 50ms (1 stop) = 250ms
        assertEquals(250, engine.calculateDelay(new Chunk("Word.")));
    }

    @Test
    void testDelayCalculation_PercentagePause() {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 10.0, 0, 0, configService);
        // Base delay 200ms + (200ms * 0.10 * 1 pause) = 220ms
        assertEquals(220, engine.calculateDelay(new Chunk("Word,")));
    }

    @Test
    void testDelayCalculation_HybridPrecedence() {
        // Delay (30ms) overrides Perc (10%)
        RSVPEngine engine = new RSVPEngine(300, 10.0, 10.0, 30, 0, configService);
        // Base delay 200ms + 30ms (stop delay) = 230ms
        assertEquals(230, engine.calculateDelay(new Chunk("Word.")));
    }

    @Test
    void testCalculateDelay_FullStringScan() {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 10, configService);
        assertEquals(630, engine.calculateDelay(new Chunk("one, two, three,")));
    }

    @Test
    void testCalculateDelay_OnlyPunctuation() {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 10, configService);
        // Base delay 0ms (0 words) + (3 pauses * 10ms) = 30ms
        assertEquals(30, engine.calculateDelay(new Chunk(",,,")));
    }

    @Test
    void testCalculateDelay_HighLowWPM() {
        RSVPEngine slowEngine = new RSVPEngine(10, 0.0, 0.0, 0, 0, configService);
        assertEquals(6000, slowEngine.calculateDelay(new Chunk("word")));
        
        RSVPEngine fastEngine = new RSVPEngine(100000, 0.0, 0.0, 0, 0, configService);
        assertEquals(0, fastEngine.calculateDelay(new Chunk("word")));
    }
}
