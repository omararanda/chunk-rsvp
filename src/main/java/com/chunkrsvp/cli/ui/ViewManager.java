package com.chunkrsvp.cli.ui;

import com.chunkrsvp.model.Chunk;

public interface ViewManager {
    void setup();
    void restore();
    void display(Chunk chunk, int wpm, boolean isPaused, boolean forceHeader);
}
