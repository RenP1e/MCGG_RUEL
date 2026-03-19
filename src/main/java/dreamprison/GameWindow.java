package main.java.dreamprison;

import javax.swing.*;

/**
 * GameWindow.java
 * Creates and configures the main JFrame window.
 * Embeds the GamePanel inside and locks the window size.
 */
public class GameWindow extends JFrame {

    public static final int WIDTH  = 480;
    public static final int HEIGHT = 720;
    public static final String TITLE = "Dream Prison";

    private final GamePanel gamePanel;

    public GameWindow() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);

        gamePanel.startGame();
    }
}
