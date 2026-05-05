package com.chunkrsvp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class InputResolutionServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void testPrioritizesFilePath() throws Exception {
        Path file = tempDir.resolve("test.txt");
        new java.io.FileOutputStream(file.toFile()).close();
        
        InputResolutionService irs = new InputResolutionService();
        InputStream result = irs.resolve(file.toString(), new ByteArrayInputStream(new byte[0]));
        assertTrue(result instanceof java.io.FileInputStream);
    }

    @Test
    void testFallsBackToStdin() throws Exception {
        InputResolutionService irs = new InputResolutionService();
        InputStream stdIn = new ByteArrayInputStream(new byte[0]);
        InputStream result = irs.resolve(null, stdIn);
        assertEquals(stdIn, result);
    }

    @Test
    void testThrowsIfFileNotFound() {
        InputResolutionService irs = new InputResolutionService();
        assertThrows(FileNotFoundException.class, () -> irs.resolve("nonexistent.txt", null));
    }
}
