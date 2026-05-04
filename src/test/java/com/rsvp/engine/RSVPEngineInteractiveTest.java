package com.rsvp.engine;

import com.rsvp.model.Chunk;
import com.rsvp.util.ConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RSVPEngineInteractiveTest {
    private ConfigService configService;
    private final String tmpConfig = System.getProperty("java.io.tmpdir") + "/test_interactive.properties";

    @BeforeEach
    void setup() {
        new File(tmpConfig).delete();
        configService = new ConfigService(tmpConfig);
    }

    @Test
    void testRunLoop_WpmAdjustment() throws Exception {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 0, configService);
        List<Chunk> chunks = List.of(new Chunk("test"));

        // Simulate Up Arrow key (ESC, '[', 'A')
        byte[] input = {27, '[', 'A', 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        // This will run and update the baseWpm internally
        engine.run(chunks, is);
        assertEquals(350, engine.getBaseWpm());
    }

    @Test
    void testRSVPEngine_MissingTTY() {
        // Verify that the engine doesn't crash if passed an invalid InputStream
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 0, configService);
        List<Chunk> chunks = List.of(new Chunk("test"));
        
        // Pass a closed input stream to simulate missing/inaccessible TTY
        ByteArrayInputStream closedStream = new ByteArrayInputStream(new byte[0]);
        try {
            closedStream.close();
        } catch (IOException e) {
            // Ignore
        }
        
        assertDoesNotThrow(() -> engine.run(chunks, closedStream));
    }

    @Test
    void testPlayPauseTogglesStateAndUI() throws Exception {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 0, configService);
        List<Chunk> chunks = List.of(new Chunk("test"));

        // Simulate Spacebar (32) and then Ctrl+C (3)
        byte[] input = {32, 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        PrintStream originalOut = System.out;
        System.setOut(ps);
        
        try {
            engine.run(chunks, is);
            String output = out.toString();
            assertTrue(output.contains("[PAUSED]"), "Output should contain [PAUSED] indicator");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testPlayPauseResumesCorrectly() throws Exception {
        RSVPEngine engine = new RSVPEngine(300, 0.0, 0.0, 0, 0, configService);
        List<Chunk> chunks = List.of(new Chunk("test"));

        // Space (32), Space (32), Ctrl+C (3)
        byte[] input = {32, 32, 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        PrintStream originalOut = System.out;
        System.setOut(ps);
        
        try {
            engine.run(chunks, is);
            String output = out.toString();
            // Should show [PAUSED] and then revert to regular speed string
            assertTrue(output.contains("[PAUSED]"));
            assertTrue(output.contains("Speed: 300 WPM"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testEngineHaltsProgressionWhenPaused() throws Exception {
        // Mocking complexity here is high, but we can verify that if we send Spacebar
        // the engine does not move to the next chunk in the time it should have.
        // This is a placeholder that will fail until the logic is implemented.
        assertTrue(false, "Test not yet implemented - this will fail as expected.");
    }
}
