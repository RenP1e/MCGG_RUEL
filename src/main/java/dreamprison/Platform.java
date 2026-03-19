package main.java.dreamprison;

import java.awt.*;

/**
 * Platform.java
 * Represents a single solid platform tile in the world.
 * Stores position, size, zone type, and optional horizontal movement for Anger zone.
 */
public class Platform {

    public enum Zone { SADNESS, ANGER, GUILT, FEAR }

    // World-space position (pixels)
    public float x, y;
    public final int width, height;
    public final Zone zone;

    // Moving platform support (Anger zone)
    private final boolean moving;
    private final float moveRange;
    private final float moveSpeed;
    private float originX;
    private float moveDir = 1;

    public Platform(float x, float y, int width, int height, Zone zone) {
        this(x, y, width, height, zone, false, 0, 0);
    }

    public Platform(float x, float y, int width, int height, Zone zone,
                    boolean moving, float moveRange, float moveSpeed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zone   = zone;
        this.moving = moving;
        this.moveRange = moveRange;
        this.moveSpeed = moveSpeed;
        this.originX = x;
    }

    public void update() {
        if (!moving) return;
        x += moveSpeed * moveDir;
        if (x > originX + moveRange || x < originX - moveRange) {
            moveDir *= -1;
        }
    }

    /** Returns axis-aligned bounding box in world space. */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean isMoving() { return moving; }
}
