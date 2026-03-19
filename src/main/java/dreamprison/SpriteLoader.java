package main.java.dreamprison;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * SpriteLoader.java
 * Loads the player sprite sheet from /assets/player.png and slices it into frames.
 *
 * Expected sprite sheet layout (each cell is 32×32):
 *   Row 0, Cols 0–3 → Walk cycle (facing right)
 *   Row 0, Col  4   → Airborne / jump frame
 *
 * If the image cannot be found the game still runs using the fallback
 * rectangle renderer in Player.render().
 */
public class SpriteLoader {

    private BufferedImage sheet;
    private final int FRAME_W = 32;
    private final int FRAME_H = 32;
    private boolean loaded = false;

    public SpriteLoader() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/player.png");
            if (is != null) {
                sheet = ImageIO.read(is);
                loaded = true;
                System.out.println("[SpriteLoader] player.png loaded successfully.");
            } else {
                System.out.println("[SpriteLoader] player.png not found – using fallback renderer.");
            }
        } catch (Exception e) {
            System.out.println("[SpriteLoader] Failed to load player.png: " + e.getMessage());
        }
    }

    /**
     * Returns the sprite image for the given animation frame.
     * @param frameIndex 0–3 walk, 4 airborne
     * @param facingRight if false, the image is flipped horizontally
     */
    public Image getPlayerFrame(int frameIndex, boolean facingRight) {
        if (!loaded) return null;

        int col = Math.min(frameIndex, (sheet.getWidth() / FRAME_W) - 1);
        BufferedImage frame = sheet.getSubimage(col * FRAME_W, 0, FRAME_W, FRAME_H);

        if (!facingRight) {
            // Flip horizontally
            BufferedImage flipped = new BufferedImage(FRAME_W, FRAME_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = flipped.createGraphics();
            g.drawImage(frame, FRAME_W, 0, -FRAME_W, FRAME_H, null);
            g.dispose();
            return flipped;
        }
        return frame;
    }
}
