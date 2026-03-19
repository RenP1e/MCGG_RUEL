package main.java.dreamprison;

import java.awt.*;
import java.util.List;

/**
 * CollisionHandler.java
 * Resolves AABB (axis-aligned bounding box) collisions between
 * the player and every platform in the level.
 *
 * Separation axes are handled independently (X then Y) to
 * avoid the "sticking to walls" problem common in naive implementations.
 */
public class CollisionHandler {

    /**
     * Moves the player by (dx, dy) and resolves collisions.
     * Sets player.onGround = true when landing on a platform top.
     */
    public void resolve(Player player, List<Platform> platforms) {
        player.setOnGround(false);

        // --- Horizontal pass ---
        player.setX(player.getX() + player.getVelX());
        Rectangle pBox = player.getBounds();

        for (Platform plat : platforms) {
            Rectangle platBox = plat.getBounds();
            if (!pBox.intersects(platBox)) continue;

            int overlapRight = pBox.x + pBox.width - platBox.x;
            int overlapLeft  = platBox.x + platBox.width - pBox.x;

            if (overlapRight < overlapLeft) {
                player.setX(platBox.x - pBox.width);
            } else {
                player.setX(platBox.x + platBox.width);
            }
            player.setVelX(0);
            pBox = player.getBounds();
        }

        // --- Vertical pass ---
        player.setY(player.getY() + player.getVelY());
        pBox = player.getBounds();

        for (Platform plat : platforms) {
            Rectangle platBox = plat.getBounds();
            if (!pBox.intersects(platBox)) continue;

            int overlapBottom = pBox.y + pBox.height - platBox.y;
            int overlapTop    = platBox.y + platBox.height - pBox.y;

            if (overlapBottom < overlapTop) {
                // Player landed on top of platform
                player.setY(platBox.y - pBox.height);
                player.setVelY(0);
                player.setOnGround(true);
            } else {
                // Player hit underside of platform
                player.setY(platBox.y + platBox.height);
                player.setVelY(0);
            }
            pBox = player.getBounds();
        }
    }
}
