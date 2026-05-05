package com.chunkrsvp.engine;

import com.chunkrsvp.cli.CliArguments;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.cli.ui.MockViewManager;
import com.chunkrsvp.cli.ui.ViewManager;
import com.chunkrsvp.util.ConfigService;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.DefaultConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
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

    private ConfigurationManager createCm() {
        return new ConfigurationManager(configService, new CliArguments(300, 0.0, 0.0, 0, 0, false, false), new DefaultConfigProvider());
    }

    @Test
    void testRunLoop_WpmAdjustment() throws Exception {
        ConfigurationManager cm = createCm();
        MockViewManager mockView = new MockViewManager();
        RSVPEngine engine = new RSVPEngine(cm, mockView);
        List<Chunk> chunks = List.of(new Chunk("test"));

        // Simulate Up Arrow key (ESC, '[', 'A')
        byte[] input = {27, '[', 'A', 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        engine.run(chunks, is);
        assertEquals(350, cm.getConfig().wpm());
        assertTrue(mockView.setupCalls >= 1);
        assertTrue(mockView.restoreCalls >= 1);
    }

    @Test
    void testRSVPEngine_MissingTTY() {
        RSVPEngine engine = new RSVPEngine(createCm(), new MockViewManager());
        List<Chunk> chunks = List.of(new Chunk("test"));
        
        ByteArrayInputStream closedStream = new ByteArrayInputStream(new byte[0]);
        try { closedStream.close(); } catch (IOException e) {}
        
        assertDoesNotThrow(() -> engine.run(chunks, closedStream));
    }

    @Test
    void testPlayPauseTogglesStateAndUI() throws Exception {
        MockViewManager mockView = new MockViewManager();
        RSVPEngine engine = new RSVPEngine(createCm(), mockView);
        List<Chunk> chunks = List.of(new Chunk("test"));

        byte[] input = {32, 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        engine.run(chunks, is);
        assertTrue(mockView.lastPaused, "Engine should have toggled pause state");
    }

    @Test
    void testPlayPauseResumesCorrectly() throws Exception {
        MockViewManager mockView = new MockViewManager();
        RSVPEngine engine = new RSVPEngine(createCm(), mockView);
        List<Chunk> chunks = List.of(new Chunk("test"));

        // Space (32), Space (32), Ctrl+C (3)
        byte[] input = {32, 32, 3}; 
        InputStream is = new ByteArrayInputStream(input);
        
        engine.run(chunks, is);
        assertFalse(mockView.lastPaused);
    }

    @Test
    void testEngineHaltsProgressionWhenPaused() throws Exception {
        MockViewManager mockView = new MockViewManager();
        RSVPEngine engine = new RSVPEngine(createCm(), mockView);
        List<Chunk> chunks = List.of(new Chunk("test1"), new Chunk("test2"));

        // Space (32), Space (32), Ctrl+C (3)
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        
        pos.write(32); // Pause

        Thread engineThread = new Thread(() -> engine.run(chunks, pis));
        engineThread.start();
        
        Thread.sleep(100);
        
        pos.write(32); // Resume
        pos.write(3); // Exit
        engineThread.join(2000);
        assertFalse(engineThread.isAlive(), "Engine thread should have finished");
    }
}
