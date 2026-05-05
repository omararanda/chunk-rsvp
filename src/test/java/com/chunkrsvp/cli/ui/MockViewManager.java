package com.chunkrsvp.cli.ui;

import com.chunkrsvp.model.Chunk;

public class MockViewManager implements ViewManager {
    public int setupCalls = 0;
    public int restoreCalls = 0;
    public int displayCalls = 0;
    public int lastWpm;
    public boolean lastPaused;

    @Override
    public void setup() { setupCalls++; }

    @Override
    public void restore() { restoreCalls++; }

    @Override
    public void display(Chunk chunk, int wpm, boolean isPaused, boolean forceHeader) {
        displayCalls++;
        lastWpm = wpm;
        lastPaused = isPaused;
    }
}
