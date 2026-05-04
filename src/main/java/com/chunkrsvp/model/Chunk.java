package com.chunkrsvp.model;

/**
 * Teacher's Notes:
 * This model represents a single "flash" or "chunk" of text to be displayed.
 * Encapsulating the text allows for easier expansion if we want to add 
 * metadata (like per-chunk speed overrides) later.
 */
public class Chunk {
    private final String text;

    public Chunk(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getWordCount() {
        if (text == null || text.isBlank()) return 0;
        // Split by any sequence of non-alphanumeric characters or whitespace
        String[] words = text.trim().split("[^a-zA-Z0-9]+");
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return text;
    }
}
