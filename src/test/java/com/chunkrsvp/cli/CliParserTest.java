package com.chunkrsvp.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CliParserTest {
    @Test
    void testParseWithFilePath() {
        String[] args = {"-wpm=400", "my_book.txt"};
        CliArguments cli = CliParser.parse(args);
        assertEquals("my_book.txt", cli.getFilePath());
        assertEquals(400, cli.getWpm());
    }

    @Test
    void testParseWithoutFilePath() {
        String[] args = {"-wpm=400"};
        CliArguments cli = CliParser.parse(args);
        assertNull(cli.getFilePath());
        assertEquals(400, cli.getWpm());
    }

    @Test
    void testNoControlsFlag() {
        String[] args = {"--no-controls"};
        CliArguments cli = CliParser.parse(args);
        assertTrue(cli.isNoControls(), "--no-controls flag should be recognized");
    }
}
