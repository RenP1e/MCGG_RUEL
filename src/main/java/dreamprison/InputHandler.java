package main.java.dreamprison;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * InputHandler.java
 * Listens for keyboard events and tracks which keys are currently held.
 * The Player polls this each update tick.
 */
public class InputHandler extends KeyAdapter {

    private final boolean[] keys = new boolean[256];

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }

    public boolean isLeft()  { return keys[KeyEvent.VK_LEFT]  || keys[KeyEvent.VK_A]; }
    public boolean isRight() { return keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]; }
    public boolean isJump()  { return keys[KeyEvent.VK_SPACE] || keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W]; }
}
