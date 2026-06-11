package model;

import java.awt.*;
import java.util.Random;

/**
 * PenaltyTarget — מטרה מסוכנת.
 * פגיעה בה מורידה 5 נקודות מהניקוד.
 * מראה: ירוק רעיל עם X אדום — סימן "אל תירה!"
 */
public class PenaltyTarget extends GameObject {

    private Random random;
    private int screenWidth;
    private int screenHeight;

    // Pulsing animation state
    private float pulseAngle = 0f;

    public PenaltyTarget(int screenWidth, int screenHeight) {
        super(0, 0, 44, 44, new Color(0, 220, 80));
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        respawn();
    }

    /** Moves the penalty target to a new random position. */
    public void respawn() {
        int margin = 60;
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
        // Advance the pulse animation
        pulseAngle += 0.12f;
        if (pulseAngle > Math.PI * 2) pulseAngle -= (float)(Math.PI * 2);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Pulsing glow radius (outer warning halo)
        float pulse = (float)(0.5 + 0.5 * Math.sin(pulseAngle));
        int glowSize = (int)(8 + 8 * pulse);

        // Outer danger glow — red/orange halo pulsing
        g2d.setColor(new Color(220, 40, 40, 60 + (int)(60 * pulse)));
        g2d.fillOval(x - glowSize, y - glowSize, width + glowSize * 2, height + glowSize * 2);

        // Translucent dark-green fill body
        g2d.setColor(new Color(10, 160, 60, 180));
        g2d.fillRoundRect(x, y, width, height, 12, 12);

        // Bright green border
        g2d.setColor(new Color(0, 255, 100));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(x, y, width, height, 12, 12);

        // Red X mark — the "do NOT shoot" symbol
        g2d.setColor(new Color(255, 50, 50));
        g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pad = 10;
        g2d.drawLine(x + pad, y + pad, x + width - pad, y + height - pad);
        g2d.drawLine(x + width - pad, y + pad, x + pad, y + height - pad);

        // Small "−5" label at top-left corner
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(255, 200, 200));
        g2d.drawString("-5", x + 3, y + 12);
    }
}
