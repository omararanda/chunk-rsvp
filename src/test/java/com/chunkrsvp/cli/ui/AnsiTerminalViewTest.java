package com.chunkrsvp.cli.ui;

import com.chunkrsvp.model.Chunk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnsiTerminalViewTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final AnsiTerminalView view = new AnsiTerminalView();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void testDisplayShowsControlsWhenEnabled() {
        Chunk chunk = new Chunk("Test");
        view.display(chunk, 300, false, true, true);
        
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Controls:"), "Output should contain 'Controls:' string when enabled");
    }

    @Test
    void testDisplayHidesControlsWhenDisabled() {
        Chunk chunk = new Chunk("Test");
        view.display(chunk, 300, false, true, false);
        
        String output = outputStreamCaptor.toString();
        assertFalse(output.contains("Controls:"), "Output should NOT contain 'Controls:' string when disabled");
    }
}
