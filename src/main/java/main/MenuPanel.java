package main;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    private Runnable onStartCallback;

    public MenuPanel(Runnable onStartCallback) {
        this.onStartCallback = onStartCallback;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("AIM PRACTICE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setText(
            "Welcome to Aim Practice!\n\n" +
            "Objective:\n" +
            "Shoot as many targets as you can before the time runs out (" + GamePanel.GAME_DURATION_SECONDS + " seconds).\n" +
            "Each target is worth 5 points.\n" +
            "In the last 5 seconds, targets are worth 10 points!\n\n" +
            "Controls:\n" +
            "- Mouse: Move crosshair and Click to shoot\n" +
            "- Arrow Keys or WASD: Move the crosshair\n" +
            "- SPACEBAR: Shoot\n" +
            "- 'P' or ESC: Pause/Resume game\n"
        );
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 18));
        instructionsArea.setForeground(Color.WHITE);
        instructionsArea.setOpaque(false);
        instructionsArea.setEditable(false);
        instructionsArea.setFocusable(false);
        instructionsArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton startButton = new JButton("START GAME");
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> {
            if (this.onStartCallback != null) {
                this.onStartCallback.run();
            }
        });

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(instructionsArea);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }
}
