package com.chunkrsvp.model;

public interface ChunkProvider {
    boolean hasNext();
    Chunk next();
    Chunk current();
    void rewind(int steps);
}
