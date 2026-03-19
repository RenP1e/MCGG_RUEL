package main.java.dreamprison;

/**
 * Main.java
 * Entry point for the Dream Prison game.
 * Creates the window and starts the game loop.
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
