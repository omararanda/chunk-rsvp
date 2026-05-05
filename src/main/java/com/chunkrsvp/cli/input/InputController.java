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
                Thread.sleep(5);
                if (inputStream.available() > 0 && inputStream.read() == '[') {
                    int dir = inputStream.read();
                    if (dir == 'A') return InputAction.SPEED_UP;
                    if (dir == 'B') return InputAction.SPEED_DOWN;
                    if (dir == 'D') return InputAction.REWIND;
                }
            }
        }
        return InputAction.NONE;
    }
}
