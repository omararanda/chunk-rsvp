package com.chunkrsvp.cli.input;

import java.io.IOException;
import java.io.InputStream;

public class InputController {
    private final InputStream inputStream;

    public InputController(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputAction checkInput() throws IOException, InterruptedException {
        if (inputStream.available() > 0) {
            int b = inputStream.read();
            if (b == 32) return InputAction.PAUSE_TOGGLE;
            if (b == 3) return InputAction.EXIT;
            if (b == 27) {
                // Peek ahead: read the '[' and then the directional char
                // This is a blocking read, but we checked available() >= 2 for the sequence
                // to be considered a full command.
                // Wait... if available() is 1 (the ESC), we should wait for the rest.
                long startTime = System.nanoTime();
                while (inputStream.available() < 2 && (System.nanoTime() - startTime) < 50_000_000L) { // 50ms in nanoseconds
                    Thread.sleep(5);
                }
                
                if (inputStream.available() >= 2) {
                    int bracket = inputStream.read();
                    int dir = inputStream.read();
                    if (bracket == '[') {
                        if (dir == 'A') return InputAction.SPEED_UP;
                        if (dir == 'B') return InputAction.SPEED_DOWN;
                        if (dir == 'D') return InputAction.REWIND;
                    }
                }
            }
        }
        return InputAction.NONE;
    }
}
