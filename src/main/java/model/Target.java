package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class Target extends GameObject {
    private Random random;
    private int screenWidth;
    private int screenHeight;

    public Target(int screenWidth, int screenHeight) {
        // Default color red, size 40x40
        super(0, 0, 40, 40, Color.RED);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        respawn();
    }

    public void respawn() {
        // Ensure target doesn't spawn exactly on the edges
        int margin = 50;
        if (screenWidth > 2 * margin && screenHeight > 2 * margin) {
            this.x = margin + random.nextInt(screenWidth - 2 * margin - width);
            this.y = margin + random.nextInt(screenHeight - 2 * margin - height);
        } else {
            this.x = random.nextInt(Math.max(1, screenWidth - width));
            this.y = random.nextInt(Math.max(1, screenHeight - height));
        }
    }

    @Override
    public void update() {
        // For now, the target doesn't move on its own. 
        // It only moves when hit (respawn called externally).
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw translucent orange background
        g2d.setColor(new Color(255, 140, 0, 150));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        
        // Draw orange grid lines
        g2d.setColor(new Color(255, 140, 0, 200));
        g2d.setStroke(new BasicStroke(1));
        int spacing = width / 4;
        for (int i = 1; i < 4; i++) {
            g2d.drawLine(x + i * spacing, y, x + i * spacing, y + height);
            g2d.drawLine(x, y + i * spacing, x + width, y + i * spacing);
        }

        // Draw solid orange border
        g2d.setColor(new Color(255, 140, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 10, 10);
        
        // Draw bright orange center square
        g2d.setColor(new Color(255, 165, 0));
        int centerSize = width / 5;
        g2d.fillRect(x + width / 2 - centerSize / 2, y + height / 2 - centerSize / 2, centerSize, centerSize);
    }
}
