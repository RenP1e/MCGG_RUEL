package main.java.dreamprison;

import javax.swing.*;
import java.awt.*;

/**
 * GamePanel.java
 * The heart of the game: contains the fixed-timestep game loop,
 * delegates updates to each subsystem, and renders every frame.
 *
 * Target: 60 FPS via Thread.sleep timing.
 */
public class GamePanel extends JPanel implements Runnable {

    // ---- Timing ----
    private static final int  TARGET_FPS  = 60;
    private static final long NANOSEC_PER_TICK = 1_000_000_000L / TARGET_FPS;

    // ---- Systems ----
    private final InputHandler    input;
    private final SpriteLoader    sprites;
    private final Level           level;
    private final Camera          camera;
    private final CollisionHandler collision;
    private       Player          player;

    // ---- Loop state ----
    private Thread gameThread;
    private boolean running = false;

    // ---- Game state ----
    private enum State { PLAYING, WIN, DEAD }
    private State state = State.PLAYING;

    // ---- Checkpoint timer ----
    private int checkpointTimer = 0; // ticks player must stand still to save

    // ---- UI Fonts ----
    private Font hudFont;
    private Font bigFont;

    public GamePanel() {
        setPreferredSize(new Dimension(GameWindow.WIDTH, GameWindow.HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        input    = new InputHandler();
        sprites  = new SpriteLoader();
        level    = new Level();
        camera   = new Camera();
        collision= new CollisionHandler();

        player = new Player(Level.START_X, Level.START_Y, input, sprites);
        camera.snapTo(player);

        addKeyListener(input);

        hudFont = new Font("Monospaced", Font.BOLD, 14);
        bigFont = new Font("Monospaced", Font.BOLD, 32);
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------
    public void startGame() {
        requestFocusInWindow();
        running = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long lag = 0;

        while (running) {
            long now  = System.nanoTime();
            lag += now - lastTime;
            lastTime = now;

            while (lag >= NANOSEC_PER_TICK) {
                update();
                lag -= NANOSEC_PER_TICK;
            }

            repaint();

            // Sleep to avoid busy-waiting
            long sleepNs = NANOSEC_PER_TICK - (System.nanoTime() - now);
            if (sleepNs > 0) {
                try { Thread.sleep(sleepNs / 1_000_000, (int)(sleepNs % 1_000_000)); }
                catch (InterruptedException ignored) {}
            }
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------
    private void update() {
        if (state != State.PLAYING) return;

        // Update moving platforms
        for (Platform p : level.getPlatforms()) p.update();

        // Update player (physics + input)
        player.update();

        // Resolve collisions
        collision.resolve(player, level.getPlatforms());

        // Update camera
        camera.update(player);

        // Checkpoint: save when standing still on ground for 1 second
        if (player.isOnGround() && Math.abs(player.getVelX()) < 0.5f) {
            checkpointTimer++;
            if (checkpointTimer >= TARGET_FPS) {
                player.saveCheckpoint();
                checkpointTimer = 0;
            }
        } else {
            checkpointTimer = 0;
        }

        // Win condition
        if (player.getY() <= Level.GOAL_Y) {
            state = State.WIN;
        }
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background gradient based on current zone
        drawBackground(g2);

        // Platforms
        drawPlatforms(g2);

        // Goal marker
        drawGoal(g2);

        // Player
        player.render(g2, camera);

        // HUD
        drawHUD(g2);

        // Overlays
        if (state == State.WIN)  drawWinScreen(g2);
    }

    // ---- Background ----
    private void drawBackground(Graphics2D g) {
        float playerWorldY = player.getY();
        Platform.Zone zone = Level.getZoneAt(playerWorldY);

        Color top, bot;
        switch (zone) {
            case SADNESS -> { top = new Color(10,  20,  60);  bot = new Color(20,  40, 100); }
            case ANGER   -> { top = new Color(60,  10,  10);  bot = new Color(120, 30,  20); }
            case GUILT   -> { top = new Color(20,  30,  20);  bot = new Color(35,  55,  35); }
            case FEAR    -> { top = new Color(5,   5,   15);  bot = new Color(10,  10,  30); }
            default      -> { top = Color.BLACK; bot = Color.BLACK; }
        }

        GradientPaint gp = new GradientPaint(0, 0, top, 0, GameWindow.HEIGHT, bot);
        g.setPaint(gp);
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
    }

    // ---- Platforms ----
    private void drawPlatforms(Graphics2D g) {
        for (Platform p : level.getPlatforms()) {
            int sx = (int) camera.toScreenX(p.x);
            int sy = (int) camera.toScreenY(p.y);

            // Skip if off-screen
            if (sy > GameWindow.HEIGHT + 20 || sy + p.height < -20) continue;

            Color fill, edge;
            switch (p.zone) {
                case SADNESS -> { fill = new Color(60,  80, 140); edge = new Color(90, 120, 200); }
                case ANGER   -> { fill = new Color(150, 40,  30); edge = new Color(220, 80,  50); }
                case GUILT   -> { fill = new Color(50,  80,  50); edge = new Color(80, 130,  80); }
                case FEAR    -> { fill = new Color(25,  25,  50); edge = new Color(45,  45,  90); }
                default      -> { fill = Color.GRAY; edge = Color.WHITE; }
            }

            // Fear zone: reduce alpha (dark/hidden feel)
            if (p.zone == Platform.Zone.FEAR) {
                float alpha = 0.55f;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }

            g.setColor(fill);
            g.fillRect(sx, sy, p.width, p.height);
            g.setColor(edge);
            g.drawRect(sx, sy, p.width, p.height);

            // Top highlight line
            g.setColor(edge.brighter());
            g.drawLine(sx + 1, sy, sx + p.width - 1, sy);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // Moving platform indicator (pulsing dot)
            if (p.isMoving()) {
                g.setColor(new Color(255, 200, 50, 180));
                g.fillOval(sx + p.width / 2 - 3, sy - 5, 6, 6);
            }
        }
    }

    // ---- Goal ----
    private void drawGoal(Graphics2D g) {
        int sx = (int) camera.toScreenX(100);
        int sy = (int) camera.toScreenY(Level.GOAL_Y - 60);
        if (sy < -80 || sy > GameWindow.HEIGHT + 80) return;

        // Glowing ring
        g.setColor(new Color(255, 255, 150, 100));
        g.fillOval(sx + 60, sy, 160, 60);
        g.setColor(new Color(255, 255, 200));
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("✦ AWAKEN ✦", sx + 65, sy + 38);
    }

    // ---- HUD ----
    private void drawHUD(Graphics2D g) {
        g.setFont(hudFont);
        Platform.Zone zone = Level.getZoneAt(player.getY());

        // Zone label
        String zoneLabel = switch (zone) {
            case SADNESS -> "I. LOSS  (Sadness)";
            case ANGER   -> "II. BETRAYAL  (Anger)";
            case GUILT   -> "III. FAILURE  (Guilt)";
            case FEAR    -> "IV. ISOLATION  (Fear)";
        };
        g.setColor(new Color(255, 255, 255, 160));
        g.drawString(zoneLabel, 12, 22);

        // Progress bar
        float progress = 1f - (player.getY() / Level.WORLD_HEIGHT);
        progress = Math.max(0, Math.min(1, progress));
        int barW = 120;
        int barH = 8;
        int bx = GameWindow.WIDTH - barW - 12;
        int by = 12;
        g.setColor(new Color(50, 50, 80));
        g.fillRect(bx, by, barW, barH);
        g.setColor(new Color(180, 220, 255));
        g.fillRect(bx, by, (int)(barW * progress), barH);
        g.setColor(new Color(120, 160, 220));
        g.drawRect(bx, by, barW, barH);
        g.drawString("Ascent", bx - 52, by + 8);

        // Controls hint (bottom left, small)
        g.setColor(new Color(255, 255, 255, 80));
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString("A/D or ←/→  Move     Space  Jump", 10, GameWindow.HEIGHT - 10);
    }

    // ---- Win screen ----
    private void drawWinScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);

        g.setFont(bigFont);
        g.setColor(new Color(255, 240, 150));
        drawCenteredString(g, "YOU AWAKENED", GameWindow.HEIGHT / 2 - 60);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 255));
        drawCenteredString(g, "The Dream Prison has shattered.", GameWindow.HEIGHT / 2 - 10);
        drawCenteredString(g, "You carry the weight of what was,", GameWindow.HEIGHT / 2 + 20);
        drawCenteredString(g, "but you are no longer its prisoner.", GameWindow.HEIGHT / 2 + 45);

        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        drawCenteredString(g, "[ Close the window to exit ]", GameWindow.HEIGHT / 2 + 110);
    }

    private void drawCenteredString(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (GameWindow.WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}
