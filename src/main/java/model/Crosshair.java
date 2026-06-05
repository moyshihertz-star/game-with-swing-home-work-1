package model;

import java.awt.Color;
import java.awt.Graphics;

public class Crosshair extends GameObject {
    private int speed = 5;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private int screenWidth, screenHeight;

    public Crosshair(int screenWidth, int screenHeight) {
        super(screenWidth / 2, screenHeight / 2, 16, 16, Color.GREEN);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setUpPressed(boolean upPressed) { this.upPressed = upPressed; }
    public void setDownPressed(boolean downPressed) { this.downPressed = downPressed; }
    public void setLeftPressed(boolean leftPressed) { this.leftPressed = leftPressed; }
    public void setRightPressed(boolean rightPressed) { this.rightPressed = rightPressed; }

    @Override
    public void update() {
        if (upPressed) y -= speed;
        if (downPressed) y += speed;
        if (leftPressed) x -= speed;
        if (rightPressed) x += speed;

        // Keep within bounds
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > screenWidth) x = screenWidth - width;
        if (y + height > screenHeight) y = screenHeight - height;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        
        // Draw crosshair lines (small green cross)
        g.drawLine(centerX, y, centerX, y + height);
        g.drawLine(x, centerY, x + width, centerY);
    }
}
