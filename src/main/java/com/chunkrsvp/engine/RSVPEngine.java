package com.chunkrsvp.engine;

import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.RsvpConfig;
import java.io.*;
import java.util.List;

public class RSVPEngine {
    private final ConfigurationManager configManager;
    private static final int MS_IN_MINUTE = 60000;

    public RSVPEngine(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    public long calculateDelay(Chunk chunk) {
        RsvpConfig cfg = configManager.getConfig();
        String text = chunk.getText();
        long baseDelay = (long) (chunk.getWordCount() * ((double) MS_IN_MINUTE / cfg.wpm()));
        long stopCount = text.chars().filter(ch -> ch == '.' || ch == '?' || ch == '!').count();
        long pauseCount = text.chars().filter(ch -> ch == ',' || ch == ';' || ch == ':').count();
        long totalDelay = baseDelay;
        if (cfg.stopDelayMs() > 0) totalDelay += (stopCount * cfg.stopDelayMs());
        else if (cfg.stopPerc() > 0) totalDelay += (long) (baseDelay * (cfg.stopPerc() / 100.0) * stopCount);
        if (cfg.pauseDelayMs() > 0) totalDelay += (pauseCount * cfg.pauseDelayMs());
        else if (cfg.pausePerc() > 0) totalDelay += (long) (baseDelay * (cfg.pausePerc() / 100.0) * pauseCount);
        return totalDelay;
    }

    private boolean isPaused = false;

    public void run(List<Chunk> chunks, InputStream ttyInput) {
        setupTerminal();
        try {
            int i = 0;
            displayChunk(chunks.get(0), true);
            while (i < chunks.size()) {
                Chunk chunk = chunks.get(i);
                long delay = calculateDelay(chunk);
                long remainingDelay = delay;
                long lastLoopTime = System.currentTimeMillis();
                boolean jumped = false;

                while (remainingDelay > 0 || isPaused) {
                    long currentTime = System.currentTimeMillis();
                    long elapsed = currentTime - lastLoopTime;
                    lastLoopTime = currentTime;

                    if (!isPaused) {
                        remainingDelay -= elapsed;
                    }

                    if (ttyInput.available() > 0) {
                        int b = ttyInput.read();
                        if (b == 32) { // Space
                            isPaused = !isPaused;
                            displayChunk(chunk, true);
                        } else if (b == 27) {
                            Thread.sleep(5);
                            if (ttyInput.available() > 0 && ttyInput.read() == '[') {
                                int dir = ttyInput.read();
                                if (dir == 'A') { 
                                    configManager.updateWpm(configManager.getConfig().wpm() + 50);
                                    delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); displayChunk(chunk, true); 
                                }
                                else if (dir == 'B') { 
                                    configManager.updateWpm(Math.max(50, configManager.getConfig().wpm() - 50));
                                    delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); displayChunk(chunk, true); 
                                }
                                else if (dir == 'D') { i = Math.max(0, i - 5); jumped = true; displayChunk(chunks.get(i), true); break; }
                            }
                        } else if (b == 3) { restoreTerminal(); return; }
                    }
                    Thread.sleep(10);
                }
                if (!jumped) { i++; if (i < chunks.size()) displayChunk(chunks.get(i), false); }
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); Thread.currentThread().interrupt(); } finally { restoreTerminal(); }
    }

    private int lastDisplayedWpm = -1;
    private void displayHeader(boolean force) {
        int currentWpm = configManager.getConfig().wpm();
        if (currentWpm != lastDisplayedWpm || force) {
            System.out.print("\033[H");
            System.out.print("\033[1G\033[KControls: [↑] +50 WPM | [↓] -50 WPM | [←] Back 5 chunks | [SPACE] Pause/Resume\n");
            System.out.print("\033[1G\033[K\n");
            System.out.print("\033[1G\033[K");
            if (isPaused) {
                System.out.printf("Speed: [PAUSED] %d WPM", currentWpm);
            } else {
                System.out.printf("Speed: %d WPM", currentWpm);
            }
            System.out.print("\033[K\n"); 
            lastDisplayedWpm = currentWpm;
        }
    }
    private void displayChunk(Chunk chunk, boolean forceHeader) {
        displayHeader(forceHeader);
        System.out.print("\033[4;1H\033[J");
        System.out.print("\033[1G\n\n          " + chunk.getText() + "\n\n\n");
        System.out.flush();
    }
    private void setupTerminal() {
        try { Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty raw -echo < /dev/tty && echo -n '\033[?25l' > /dev/tty"}).waitFor(); } catch (Exception e) {}
    }
    private void restoreTerminal() {
        try { System.out.print("\n\r"); System.out.flush(); Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty sane < /dev/tty && echo -n '\033[?25h' > /dev/tty"}).waitFor(); } catch (Exception e) {}
    }
}
