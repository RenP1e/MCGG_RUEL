package main.java.dreamprison;

import java.awt.*;

/**
 * Player.java
 * Handles all player physics, input response, animation state, and death/reset logic.
 *
 * Physics model inspired by Celeste / Jump King:
 *  - Fixed gravity applied every tick
 *  - Horizontal movement with friction
 *  - Jump buffering (coyote time) for forgiving feel
 *  - Variable jump height (hold SPACE to jump higher)
 *  - Fall penalty: if falling speed exceeds FALL_DEATH_VEL the player resets to checkpoint
 */
public class Player {

    // --- Dimensions ---
    public static final int W = 28;
    public static final int H = 32;

    // --- Physics constants ---
    private static final float GRAVITY          =  0.55f;
    private static final float JUMP_VEL         = -13.5f;
    private static final float JUMP_HOLD_BOOST  =  0.35f; // extra upward force per tick while holding jump
    private static final int   JUMP_HOLD_TICKS  =  10;    // max ticks jump-hold applies
    private static final float H_ACCEL          =  1.6f;
    private static final float H_MAX            =  5.0f;
    private static final float FRICTION         =  0.78f;
    private static final float FALL_DEATH_VEL   =  22f;   // falling faster than this → reset

    // --- Coyote time / jump buffer ---
    private static final int COYOTE_TICKS  = 6;
    private static final int JUMP_BUFFER_TICKS = 8;

    // --- State ---
    private float x, y;
    private float velX, velY;
    private boolean onGround;
    private boolean alive = true;
    private boolean wasJumpHeld = false;
    private int jumpHoldTimer   = 0;
    private int coyoteTimer     = 0;
    private int jumpBufferTimer = 0;

    // --- Checkpoint (last safe ground position) ---
    private float checkpointX, checkpointY;

    // --- Visual feedback ---
    private int deathFlashTimer = 0;
    private static final int DEATH_FLASH_DURATION = 45; // ~0.75 s

    // --- Sprite / animation ---
    private int animFrame   = 0;
    private int animTimer   = 0;
    private boolean facingRight = true;

    private final SpriteLoader spriteLoader;
    private final InputHandler input;

    public Player(float startX, float startY, InputHandler input, SpriteLoader spriteLoader) {
        this.x = startX;
        this.y = startY;
        this.checkpointX = startX;
        this.checkpointY = startY;
        this.input = input;
        this.spriteLoader = spriteLoader;
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------
    public void update() {
        if (!alive) {
            deathFlashTimer--;
            if (deathFlashTimer <= 0) respawn();
            return;
        }

        handleHorizontal();
        handleJump();
        applyGravity();
        checkFallDeath();
        updateAnimation();
    }

    private void handleHorizontal() {
        if (input.isLeft()) {
            velX -= H_ACCEL;
            facingRight = false;
        } else if (input.isRight()) {
            velX += H_ACCEL;
            facingRight = true;
        } else {
            velX *= FRICTION;
            if (Math.abs(velX) < 0.1f) velX = 0;
        }
        velX = Math.max(-H_MAX, Math.min(H_MAX, velX));

        // World bounds clamp (left / right walls)
        if (x < 0) { x = 0; velX = 0; }
        if (x + W > GameWindow.WIDTH) { x = GameWindow.WIDTH - W; velX = 0; }
    }

    private void handleJump() {
        // Coyote time countdown
        if (onGround) {
            coyoteTimer = COYOTE_TICKS;
        } else if (coyoteTimer > 0) {
            coyoteTimer--;
        }

        // Jump buffer: store intent even if pressed slightly before landing
        if (input.isJump()) {
            jumpBufferTimer = JUMP_BUFFER_TICKS;
        } else if (jumpBufferTimer > 0) {
            jumpBufferTimer--;
        }

        boolean canJump = coyoteTimer > 0;
        boolean wantsJump = jumpBufferTimer > 0;

        if (canJump && wantsJump && !wasJumpHeld) {
            velY = JUMP_VEL;
            coyoteTimer = 0;
            jumpBufferTimer = 0;
            jumpHoldTimer = JUMP_HOLD_TICKS;
            onGround = false;
        }

        // Variable jump height: hold SPACE for higher jump
        if (input.isJump() && jumpHoldTimer > 0 && velY < 0) {
            velY -= JUMP_HOLD_BOOST;
            jumpHoldTimer--;
        } else if (!input.isJump()) {
            jumpHoldTimer = 0;
        }

        wasJumpHeld = input.isJump();
    }

    private void applyGravity() {
        velY += GRAVITY;
        // Terminal velocity
        if (velY > 28f) velY = 28f;
    }

    private void checkFallDeath() {
        if (velY >= FALL_DEATH_VEL) {
            die();
        }
        // Fell off the bottom of the world
        if (y > Level.WORLD_BOTTOM) {
            die();
        }
    }

    private void die() {
        alive = false;
        deathFlashTimer = DEATH_FLASH_DURATION;
        velX = 0;
        velY = 0;
    }

    private void respawn() {
        x = checkpointX;
        y = checkpointY;
        velX = 0;
        velY = 0;
        alive = true;
    }

    /** Call when safely standing still on a platform to save checkpoint. */
    public void saveCheckpoint() {
        checkpointX = x;
        checkpointY = y;
    }

    private void updateAnimation() {
        animTimer++;
        if (animTimer >= 8) {
            animTimer = 0;
            if (onGround && Math.abs(velX) > 0.5f) {
                animFrame = (animFrame + 1) % 4; // walk cycle frames 0-3
            } else if (!onGround) {
                animFrame = 4; // airborne frame
            } else {
                animFrame = 0; // idle
            }
        }
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------
    public void render(Graphics2D g, Camera camera) {
        int sx = (int) camera.toScreenX(x);
        int sy = (int) camera.toScreenY(y);

        if (!alive) {
            // Death flash effect
            if ((deathFlashTimer / 4) % 2 == 0) return;
        }

        Image sprite = spriteLoader.getPlayerFrame(animFrame, facingRight);
        if (sprite != null) {
            g.drawImage(sprite, sx, sy, W, H, null);
        } else {
            // Fallback: draw a colored rectangle
            g.setColor(alive ? Color.WHITE : Color.RED);
            g.fillRect(sx, sy, W, H);
            // eyes
            g.setColor(Color.BLACK);
            int eyeX = facingRight ? sx + 18 : sx + 6;
            g.fillOval(eyeX, sy + 8, 4, 4);
        }
    }

    // -------------------------------------------------------------------------
    // Getters / Setters (used by CollisionHandler and GamePanel)
    // -------------------------------------------------------------------------
    public float getX()   { return x; }
    public float getY()   { return y; }
    public float getVelX(){ return velX; }
    public float getVelY(){ return velY; }
    public boolean isOnGround() { return onGround; }
    public boolean isAlive()    { return alive; }

    public void setX(float x)        { this.x = x; }
    public void setY(float y)        { this.y = y; }
    public void setVelX(float v)     { this.velX = v; }
    public void setVelY(float v)     { this.velY = v; }
    public void setOnGround(boolean b){ this.onGround = b; }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, W, H);
    }
}
