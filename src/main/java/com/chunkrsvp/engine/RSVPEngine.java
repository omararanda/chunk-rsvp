package com.chunkrsvp.engine;

import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.ui.ViewManager;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.RsvpConfig;
import java.io.*;
import java.util.List;

public class RSVPEngine {
    private final ConfigurationManager configManager;
    private final ViewManager view;
    private static final int MS_IN_MINUTE = 60000;

    public RSVPEngine(ConfigurationManager configManager, ViewManager view) {
        this.configManager = configManager;
        this.view = view;
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
        view.setup();
        try {
            int i = 0;
            view.display(chunks.get(0), configManager.getConfig().wpm(), isPaused, true);
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
                            view.display(chunk, configManager.getConfig().wpm(), isPaused, true);
                        } else if (b == 27) {
                            Thread.sleep(5);
                            if (ttyInput.available() > 0 && ttyInput.read() == '[') {
                                int dir = ttyInput.read();
                                if (dir == 'A') { 
                                    RsvpConfig c = configManager.getConfig();
                                    configManager.updateConfig(new RsvpConfig(c.wpm() + 50, c.stopPerc(), c.pausePerc(), c.stopDelayMs(), c.pauseDelayMs()));
                                    delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); view.display(chunk, configManager.getConfig().wpm(), isPaused, true); 
                                }
                                else if (dir == 'B') { 
                                    RsvpConfig c = configManager.getConfig();
                                    configManager.updateConfig(new RsvpConfig(Math.max(50, c.wpm() - 50), c.stopPerc(), c.pausePerc(), c.stopDelayMs(), c.pauseDelayMs()));
                                    delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); view.display(chunk, configManager.getConfig().wpm(), isPaused, true); 
                                }
                                else if (dir == 'D') { i = Math.max(0, i - 5); jumped = true; view.display(chunks.get(i), configManager.getConfig().wpm(), isPaused, true); break; }
                            }
                        } else if (b == 3) { view.restore(); return; }
                    }
                    Thread.sleep(10);
                }
                if (!jumped) { i++; if (i < chunks.size()) view.display(chunks.get(i), configManager.getConfig().wpm(), isPaused, false); }
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); Thread.currentThread().interrupt(); } finally { view.restore(); }
    }
}
