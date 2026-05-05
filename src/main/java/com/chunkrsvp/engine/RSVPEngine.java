package com.chunkrsvp.engine;

import com.chunkrsvp.cli.input.InputAction;
import com.chunkrsvp.cli.input.InputController;
import com.chunkrsvp.model.Chunk;
import com.chunkrsvp.cli.ui.ViewManager;
import com.chunkrsvp.util.ConfigurationManager;
import com.chunkrsvp.util.RsvpConfig;
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

    public void run(com.chunkrsvp.model.ChunkProvider provider, InputController input) {
        view.setup();
        try {
            if (!provider.hasNext()) return;
            provider.next();
            view.display(provider.current(), configManager.getConfig().wpm(), isPaused, true);
            
            boolean running = true;
            while (running) {
                Chunk chunk = provider.current();
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

                    InputAction action = input.checkInput();
                    if (action == InputAction.PAUSE_TOGGLE) {
                        isPaused = !isPaused;
                        view.display(chunk, configManager.getConfig().wpm(), isPaused, true);
                    } else if (action == InputAction.SPEED_UP) {
                        RsvpConfig c = configManager.getConfig();
                        configManager.updateConfig(new RsvpConfig(c.wpm() + 50, c.stopPerc(), c.pausePerc(), c.stopDelayMs(), c.pauseDelayMs()));
                        delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); view.display(chunk, configManager.getConfig().wpm(), isPaused, true); 
                    } else if (action == InputAction.SPEED_DOWN) {
                        RsvpConfig c = configManager.getConfig();
                        configManager.updateConfig(new RsvpConfig(Math.max(50, c.wpm() - 50), c.stopPerc(), c.pausePerc(), c.stopDelayMs(), c.pauseDelayMs()));
                        delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); view.display(chunk, configManager.getConfig().wpm(), isPaused, true); 
                    } else if (action == InputAction.REWIND) {
                        provider.rewind(5); jumped = true; view.display(provider.current(), configManager.getConfig().wpm(), isPaused, true); break;
                    } else if (action == InputAction.EXIT) {
                        view.restore(); return;
                    }
                    Thread.sleep(10);
                }
                if (!jumped) {
                    if (provider.hasNext()) {
                        provider.next();
                        view.display(provider.current(), configManager.getConfig().wpm(), isPaused, false);
                    } else {
                        running = false;
                    }
                }
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); Thread.currentThread().interrupt(); } finally { view.restore(); }
    }
}
