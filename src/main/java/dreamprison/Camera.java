package main.java.dreamprison;

/**
 * Camera.java
 * Tracks the player's vertical position and smoothly scrolls the world.
 * Converts world-space coordinates to screen-space for rendering.
 */
public class Camera {

    private float offsetY = 0;
    private static final float LERP = 0.1f; // smoothing factor

    // Keep player ~35% from the top of the screen
    private static final float TARGET_RATIO = 0.35f;

    public void update(Player player) {
        float targetOffsetY = player.getY() - GameWindow.HEIGHT * TARGET_RATIO;
        offsetY += (targetOffsetY - offsetY) * LERP;
    }

    /** Convert world Y to screen Y. */
    public float toScreenY(float worldY) {
        return worldY - offsetY;
    }

    /** Convert world X to screen X (no horizontal scrolling). */
    public float toScreenX(float worldX) {
        return worldX;
    }

    public float getOffsetY() { return offsetY; }

    /** Snap camera instantly (used on reset). */
    public void snapTo(Player player) {
        offsetY = player.getY() - GameWindow.HEIGHT * TARGET_RATIO;
    }
}
