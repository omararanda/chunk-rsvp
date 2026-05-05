package com.chunkrsvp.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class StreamChunkProvider implements ChunkProvider {
    private final BufferedReader reader;
    // LinkedList handles removal from the front efficiently O(1)
    private final LinkedList<Chunk> buffer = new LinkedList<>();
    private final int maxHistory = 50;
    // Global absolute index of the current chunk we are pointing to
    private int currentAbsoluteIndex = -1;
    // Index of the first element currently in the buffer (relative to absolute indices)
    private int bufferStartAbsoluteIndex = 0;
    private boolean eof = false;

    public StreamChunkProvider(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasNext() {
        if (currentAbsoluteIndex < bufferStartAbsoluteIndex + buffer.size() - 1) return true;
        if (eof) return false;
        
        try {
            String line = reader.readLine();
            while (line != null && line.isBlank()) {
                line = reader.readLine();
            }
            if (line == null) {
                eof = true;
                return false;
            }
            
            Chunk chunk = new Chunk(line);
            buffer.add(chunk);
            
            if (buffer.size() > maxHistory) {
                buffer.removeFirst();
                bufferStartAbsoluteIndex++;
            }
            return true;
        } catch (Exception e) {
            eof = true;
            return false;
        }
    }

    @Override
    public Chunk next() {
        if (hasNext()) {
            currentAbsoluteIndex++;
            return getFromBuffer(currentAbsoluteIndex);
        }
        return null;
    }

    @Override
    public Chunk current() {
        return getFromBuffer(currentAbsoluteIndex);
    }

    @Override
    public void rewind(int steps) {
        currentAbsoluteIndex = Math.max(bufferStartAbsoluteIndex, currentAbsoluteIndex - steps);
    }

    private Chunk getFromBuffer(int absoluteIndex) {
        int relativeIndex = absoluteIndex - bufferStartAbsoluteIndex;
        if (relativeIndex >= 0 && relativeIndex < buffer.size()) {
            return buffer.get(relativeIndex);
        }
        return null;
    }
}
