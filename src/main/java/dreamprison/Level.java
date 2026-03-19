package main.java.dreamprison;

import java.util.ArrayList;
import java.util.List;

/**
 * Level.java
 * Defines the entire vertical tower broken into four emotional zones.
 * Platforms are specified in world-space pixels where Y=0 is the top
 * and Y increases downward.
 *
 * Zone layout (world Y ranges, world height = 6400 px):
 *   FEAR     (top):    Y   0 – 1600   → dark, hidden hazards
 *   GUILT:             Y 1600 – 3200   → tight narrow platforms
 *   ANGER:             Y 3200 – 4800   → moving / chaotic
 *   SADNESS  (bottom): Y 4800 – 6400   → wide quiet gaps
 *
 * The player starts at the BOTTOM (Sadness) and climbs upward.
 * In Java2D Y increases downward, so the player starts near Y=6200
 * and the goal is near Y=100.
 */
public class Level {

    /** Total world height in pixels. */
    public static final int WORLD_HEIGHT = 6400;
    public static final int WORLD_BOTTOM = WORLD_HEIGHT + 200; // kill plane

    /** Y boundaries for each zone (top edge). */
    public static final int SADNESS_TOP = 4800;
    public static final int ANGER_TOP   = 3200;
    public static final int GUILT_TOP   = 1600;
    public static final int FEAR_TOP    =    0;

    private final List<Platform> platforms = new ArrayList<>();

    /** Player start position (bottom of tower, Sadness zone). */
    public static final float START_X = 200;
    public static final float START_Y = 6200;

    /** Goal Y – reaching this Y wins the game. */
    public static final float GOAL_Y = 80;

    public Level() {
        buildSadness();
        buildAnger();
        buildGuilt();
        buildFear();
        buildGoalPlatform();
    }

    // -------------------------------------------------------------------------
    // Zone: SADNESS  (Y 4800–6400) — wide platforms, long quiet jumps
    // -------------------------------------------------------------------------
    private void buildSadness() {
        int z = 6400; // build upward
        // Ground floor
        plat(0,   z - 40,  480, 40, Platform.Zone.SADNESS);

        // Sparse wide platforms — long horizontal gaps, moderate vertical gaps
        plat(20,  z - 160, 180, 16, Platform.Zone.SADNESS);
        plat(280, z - 270, 160, 16, Platform.Zone.SADNESS);
        plat(60,  z - 400, 200, 16, Platform.Zone.SADNESS);
        plat(240, z - 530, 180, 16, Platform.Zone.SADNESS);
        plat(40,  z - 660, 140, 16, Platform.Zone.SADNESS);
        plat(300, z - 780, 160, 16, Platform.Zone.SADNESS);
        plat(120, z - 910, 200, 16, Platform.Zone.SADNESS);
        plat(10,  z -1040, 150, 16, Platform.Zone.SADNESS);
        plat(310, z -1160, 140, 16, Platform.Zone.SADNESS);
        plat(160, z -1280, 180, 16, Platform.Zone.SADNESS);
        plat(30,  z -1400, 160, 16, Platform.Zone.SADNESS);
        plat(270, z -1510, 180, 16, Platform.Zone.SADNESS);
        // Transition platform
    }

    // -------------------------------------------------------------------------
    // Zone: ANGER  (Y 3200–4800) — moving platforms, chaotic layout
    // -------------------------------------------------------------------------
    private void buildAnger() {
        int base = 4800;

        // Static anchors
        plat(0,   base,      150, 16, Platform.Zone.ANGER);
        plat(330, base,      150, 16, Platform.Zone.ANGER);

        // Moving platforms (range 80px, speed 1.5)
        movingPlat(80,  base - 140, 100, 14, Platform.Zone.ANGER, 80, 1.5f);
        movingPlat(260, base - 260, 90,  14, Platform.Zone.ANGER, 70, 1.8f);
        movingPlat(50,  base - 380, 110, 14, Platform.Zone.ANGER, 90, 1.3f);
        movingPlat(220, base - 490, 80,  14, Platform.Zone.ANGER, 60, 2.0f);
        movingPlat(100, base - 610, 100, 14, Platform.Zone.ANGER, 80, 1.6f);
        movingPlat(290, base - 720, 90,  14, Platform.Zone.ANGER, 70, 1.9f);
        movingPlat(40,  base - 840, 110, 14, Platform.Zone.ANGER, 85, 1.4f);
        movingPlat(200, base - 960, 95,  14, Platform.Zone.ANGER, 75, 1.7f);
        // Some static chaotic platforms interspersed
        plat(330, base - 840, 80, 14, Platform.Zone.ANGER);
        plat(10,  base - 960, 70, 14, Platform.Zone.ANGER);
        plat(180, base -1080, 120,14, Platform.Zone.ANGER);
        movingPlat(60,  base -1200, 100,14, Platform.Zone.ANGER, 90, 2.0f);
        movingPlat(300, base -1330, 80, 14, Platform.Zone.ANGER, 65, 1.8f);
        plat(150, base -1450, 200, 16, Platform.Zone.ANGER);
        // Transition

    }

    // -------------------------------------------------------------------------
    // Zone: GUILT  (Y 1600–3200) — tight narrow platforms, precision required
    // -------------------------------------------------------------------------
    private void buildGuilt() {
        int base = 3200;

        plat(0, base, 360, 20, Platform.Zone.GUILT); // entry ledge

        // Narrow platforms (32–60px wide), demanding precision
        plat(30,  base - 120,  48, 12, Platform.Zone.GUILT);
        plat(200, base - 220,  40, 12, Platform.Zone.GUILT);
        plat(380, base - 320,  48, 12, Platform.Zone.GUILT);
        plat(60,  base - 430,  36, 12, Platform.Zone.GUILT);
        plat(280, base - 530,  44, 12, Platform.Zone.GUILT);
        plat(140, base - 640,  40, 12, Platform.Zone.GUILT);
        plat(400, base - 730,  36, 12, Platform.Zone.GUILT);
        plat(20,  base - 840,  48, 12, Platform.Zone.GUILT);
        plat(240, base - 940,  40, 12, Platform.Zone.GUILT);
        plat(360, base -1040,  44, 12, Platform.Zone.GUILT);
        plat(80,  base -1150,  36, 12, Platform.Zone.GUILT);
        plat(220, base -1250,  48, 12, Platform.Zone.GUILT);
        plat(40,  base -1360,  40, 12, Platform.Zone.GUILT);
        plat(310, base -1470,  44, 12, Platform.Zone.GUILT);
        plat(160, base -1565,  60, 12, Platform.Zone.GUILT);
        // Transition

    }

    // -------------------------------------------------------------------------
    // Zone: FEAR  (Y 0–1600) — hidden/dark, moving platforms, spike-like gaps
    // -------------------------------------------------------------------------
    private void buildFear() {
        int base = 1600;

        plat(0, base, 360, 20, Platform.Zone.FEAR);

        // Tightly spaced with unpredictable movement — simulate terror
        movingPlat(100, base - 140, 70, 12, Platform.Zone.FEAR, 60, 1.2f);
        plat(330,       base - 240, 50, 12, Platform.Zone.FEAR);
        movingPlat(20,  base - 360, 65, 12, Platform.Zone.FEAR, 80, 1.5f);
        plat(280,       base - 460, 55, 12, Platform.Zone.FEAR);
        movingPlat(60,  base - 580, 60, 12, Platform.Zone.FEAR, 70, 1.3f);
        plat(360,       base - 680, 48, 12, Platform.Zone.FEAR);
        movingPlat(140, base - 790, 64, 12, Platform.Zone.FEAR, 65, 1.6f);
        plat(30,        base - 900, 52, 12, Platform.Zone.FEAR);
        movingPlat(260, base -1010, 60, 12, Platform.Zone.FEAR, 55, 1.4f);
        plat(100,       base -1120, 50, 12, Platform.Zone.FEAR);
        movingPlat(340, base -1230, 64, 12, Platform.Zone.FEAR, 60, 1.5f);
        plat(40,        base -1340, 56, 12, Platform.Zone.FEAR);
        movingPlat(200, base -1440, 70, 12, Platform.Zone.FEAR, 70, 1.7f);
        // Near-summit platform
        plat(150,       base -1540, 180, 16, Platform.Zone.FEAR);
    }

    // -------------------------------------------------------------------------
    // Goal platform at the very top
    // -------------------------------------------------------------------------
    private void buildGoalPlatform() {
        plat(100, 60, 280, 24, Platform.Zone.FEAR);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void plat(float x, float y, int w, int h, Platform.Zone zone) {
        platforms.add(new Platform(x, y, w, h, zone));
    }

    private void movingPlat(float x, float y, int w, int h, Platform.Zone zone,
                             float range, float speed) {
        platforms.add(new Platform(x, y, w, h, zone, true, range, speed));
    }

    public List<Platform> getPlatforms() { return platforms; }

    /** Determines which zone a world-Y position belongs to. */
    public static Platform.Zone getZoneAt(float worldY) {
        if (worldY < GUILT_TOP)   return Platform.Zone.FEAR;
        if (worldY < ANGER_TOP)   return Platform.Zone.GUILT;
        if (worldY < SADNESS_TOP) return Platform.Zone.ANGER;
        return Platform.Zone.SADNESS;
    }
}
