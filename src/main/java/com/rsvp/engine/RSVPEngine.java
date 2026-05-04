package com.rsvp.engine;

import com.rsvp.model.Chunk;
import com.rsvp.util.ConfigService;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class RSVPEngine {
    private int baseWpm;
    private double stopPerc = 0.0;
    private double pausePerc = 0.0;
    private int stopDelayMs = 30;
    private int pauseDelayMs = 10;
    private static final int MS_IN_MINUTE = 60000;
    private final ConfigService configService;

    public RSVPEngine(int baseWpm, Double cliStopPerc, Double cliPausePerc, Integer cliStopAdd, Integer cliPauseAdd, ConfigService configService) {
        this.baseWpm = baseWpm;
        this.configService = configService;
        loadConfig();
        if (cliStopPerc != null) this.stopPerc = cliStopPerc;
        if (cliPausePerc != null) this.pausePerc = cliPausePerc;
        if (cliStopAdd != null) this.stopDelayMs = cliStopAdd;
        if (cliPauseAdd != null) this.pauseDelayMs = cliPauseAdd;
    }

    private void loadConfig() {
        Properties props = configService.load();
        stopPerc = Double.parseDouble(props.getProperty("perc.stop", String.valueOf(stopPerc)));
        pausePerc = Double.parseDouble(props.getProperty("perc.pause", String.valueOf(pausePerc)));
        stopDelayMs = Integer.parseInt(props.getProperty("delay.stop", String.valueOf(stopDelayMs)));
        pauseDelayMs = Integer.parseInt(props.getProperty("delay.pause", String.valueOf(pauseDelayMs)));
        baseWpm = Integer.parseInt(props.getProperty("wpm", String.valueOf(baseWpm)));
    }

    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("perc.stop", String.valueOf(stopPerc));
        props.setProperty("perc.pause", String.valueOf(pausePerc));
        props.setProperty("delay.stop", String.valueOf(stopDelayMs));
        props.setProperty("delay.pause", String.valueOf(pauseDelayMs));
        props.setProperty("wpm", String.valueOf(baseWpm));
        configService.save(props);
    }

    public int getBaseWpm() {
        return baseWpm;
    }

    public long calculateDelay(Chunk chunk) {
        String text = chunk.getText();
        long baseDelay = (long) (chunk.getWordCount() * ((double) MS_IN_MINUTE / baseWpm));
        long stopCount = text.chars().filter(ch -> ch == '.' || ch == '?' || ch == '!').count();
        long pauseCount = text.chars().filter(ch -> ch == ',' || ch == ';' || ch == ':').count();
        long totalDelay = baseDelay;
        if (stopDelayMs > 0) totalDelay += (stopCount * stopDelayMs);
        else if (stopPerc > 0) totalDelay += (long) (baseDelay * (stopPerc / 100.0) * stopCount);
        if (pauseDelayMs > 0) totalDelay += (pauseCount * pauseDelayMs);
        else if (pausePerc > 0) totalDelay += (long) (baseDelay * (pausePerc / 100.0) * pauseCount);
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
                                if (dir == 'A') { baseWpm += 50; saveConfig(); delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); displayChunk(chunk, true); }
                                else if (dir == 'B') { baseWpm = Math.max(50, baseWpm - 50); saveConfig(); delay = calculateDelay(chunk); remainingDelay = delay; lastLoopTime = System.currentTimeMillis(); displayChunk(chunk, true); }
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
        if (baseWpm != lastDisplayedWpm || force) {
            System.out.print("\033[H");
            System.out.print("\033[1G\033[KControls: [↑] +50 WPM | [↓] -50 WPM | [←] Back 5 chunks\n");
            System.out.print("\033[1G\033[K\n");
            System.out.print("\033[1G\033[K");
            System.out.printf("Speed: %d WPM", baseWpm);
            System.out.print("\033[K\n"); 
            lastDisplayedWpm = baseWpm;
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
