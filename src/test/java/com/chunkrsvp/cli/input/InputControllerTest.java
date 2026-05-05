package com.chunkrsvp.cli.input;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class InputControllerTest {
    @Test
    void testNoInputReturnsNone() throws Exception {
        InputController ic = new InputController(new ByteArrayInputStream(new byte[0]));
        assertEquals(InputAction.NONE, ic.checkInput());
    }

    @Test
    void testSpacebarReturnsPauseToggle() throws Exception {
        InputController ic = new InputController(new ByteArrayInputStream(new byte[]{32}));
        assertEquals(InputAction.PAUSE_TOGGLE, ic.checkInput());
    }

    @Test
    void testArrowKeysReturnSpeedAdjustments() throws Exception {
        InputController icUp = new InputController(new ByteArrayInputStream(new byte[]{27, '[', 'A'}));
        assertEquals(InputAction.SPEED_UP, icUp.checkInput());
        
        InputController icDown = new InputController(new ByteArrayInputStream(new byte[]{27, '[', 'B'}));
        assertEquals(InputAction.SPEED_DOWN, icDown.checkInput());
    }

    @Test
    void testArrowLeftReturnsRewind() throws Exception {
        InputController ic = new InputController(new ByteArrayInputStream(new byte[]{27, '[', 'D'}));
        assertEquals(InputAction.REWIND, ic.checkInput());
    }

    @Test
    void testCtrlCReturnsExit() throws Exception {
        InputController ic = new InputController(new ByteArrayInputStream(new byte[]{3}));
        assertEquals(InputAction.EXIT, ic.checkInput());
    }
}
