package com.chunkrsvp.cli.ui;

import com.chunkrsvp.model.Chunk;

public class AnsiTerminalView implements ViewManager {
    private int lastDisplayedWpm = -1;

    @Override
    public void setup() {
        try { Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty raw -echo < /dev/tty && echo -n '\033[?25l' > /dev/tty"}).waitFor(); } catch (Exception e) {}
    }

    @Override
    public void restore() {
        try { System.out.print("\n\r"); System.out.flush(); Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty sane < /dev/tty && echo -n '\033[?25h' > /dev/tty"}).waitFor(); } catch (Exception e) {}
    }

    @Override
    public void display(Chunk chunk, int wpm, boolean isPaused, boolean forceHeader) {
        if (wpm != lastDisplayedWpm || forceHeader) {
            System.out.print("\033[H");
            System.out.print("\033[1G\033[KControls: [↑] +50 WPM | [↓] -50 WPM | [←] Back 5 chunks | [SPACE] Pause/Resume\n");
            System.out.print("\033[1G\033[K\n");
            System.out.print("\033[1G\033[K");
            if (isPaused) {
                System.out.printf("Speed: [PAUSED] %d WPM", wpm);
            } else {
                System.out.printf("Speed: %d WPM", wpm);
            }
            System.out.print("\033[K\n"); 
            lastDisplayedWpm = wpm;
        }
        System.out.print("\033[4;1H\033[J");
        System.out.print("\033[1G\n\n          " + chunk.getText() + "\n\n\n");
        System.out.flush();
    }
}
