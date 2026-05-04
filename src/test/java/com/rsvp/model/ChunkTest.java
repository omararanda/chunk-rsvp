package com.rsvp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkTest {

    @Test
    void testGetWordCount_IrregularWhitespace() {
        assertEquals(2, new Chunk("hello\tworld").getWordCount());
        assertEquals(2, new Chunk("  hello    world  ").getWordCount());
        assertEquals(2, new Chunk("hello\nworld").getWordCount());
    }

    @Test
    void testGetWordCount_ComplexText() {
        assertEquals(3, new Chunk("hyphen-ated word").getWordCount());
        assertEquals(3, new Chunk("alpha123 beta456 gamma789").getWordCount());
        assertEquals(1, new Chunk("12345").getWordCount());
    }
}
