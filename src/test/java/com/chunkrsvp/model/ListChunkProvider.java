package com.chunkrsvp.model;

import java.util.ArrayList;
import java.util.List;

public class ListChunkProvider implements ChunkProvider {
    private final List<Chunk> chunks;
    private int currentIndex = -1;

    public ListChunkProvider(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < chunks.size() - 1;
    }

    @Override
    public Chunk next() {
        if (hasNext()) {
            currentIndex++;
            return chunks.get(currentIndex);
        }
        return null;
    }

    @Override
    public Chunk current() {
        if (currentIndex >= 0 && currentIndex < chunks.size()) {
            return chunks.get(currentIndex);
        }
        return null;
    }

    @Override
    public void rewind(int steps) {
        currentIndex = Math.max(0, currentIndex - steps);
    }
}
