package com.chunkrsvp.util;

public record RsvpConfig(int wpm, double stopPerc, double pausePerc, int stopDelayMs, int pauseDelayMs, boolean noControls) {
    public RsvpConfig(int wpm, double stopPerc, double pausePerc, int stopDelayMs, int pauseDelayMs) {
        this(wpm, stopPerc, pausePerc, stopDelayMs, pauseDelayMs, false);
    }
}
