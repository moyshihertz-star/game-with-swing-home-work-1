package main;

import javax.swing.*;
import java.awt.*;

public class GameApp {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Aim Practice");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            GamePanel gamePanel = new GamePanel(WINDOW_WIDTH, WINDOW_HEIGHT);
            gamePanel.setOnReturnToMenu(() -> {
                cardLayout.show(mainPanel, "MENU");
            });

            MenuPanel menuPanel = new MenuPanel(() -> {
                cardLayout.show(mainPanel, "GAME");
                gamePanel.requestFocusInWindow();
                gamePanel.startGame();
            });

            mainPanel.add(menuPanel, "MENU");
            mainPanel.add(gamePanel, "GAME");

            frame.add(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Show menu initially
            cardLayout.show(mainPanel, "MENU");
        });
    }
}
