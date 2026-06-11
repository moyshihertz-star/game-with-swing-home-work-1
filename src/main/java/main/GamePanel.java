package main;

import model.Crosshair;
import model.PenaltyTarget;
import model.Target;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    private boolean isRunning;
    private boolean isPaused;
    
    private Crosshair crosshair;
    private Target target;
    private PenaltyTarget penaltyTarget;

    // Flash message shown briefly after hitting the penalty target
    private String flashMessage = "";
    private long flashUntil = 0;
    
    private int score;
    private int shotsFired;
    private int targetsHit;
    private long startTime;
    public static final int GAME_DURATION_SECONDS = 30;
    private int timeLeft;
    private boolean gameOver;
    private double recoilOffset = 0;
    private Runnable onReturnToMenu;

    public void setOnReturnToMenu(Runnable onReturnToMenu) {
        this.onReturnToMenu = onReturnToMenu;
    }

    private final int FPS = 60;
    private final long targetTime = 1000 / FPS;

    public GamePanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        requestFocusInWindow();

        crosshair = new Crosshair(width, height);
        target = new Target(width, height);
        penaltyTarget = new PenaltyTarget(width, height);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });

        // Add mouse support
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!isPaused && !gameOver) {
                    crosshair.setX(e.getX() - crosshair.getWidth() / 2);
                    crosshair.setY(e.getY() - crosshair.getHeight() / 2);
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isPaused && !gameOver) {
                    crosshair.setX(e.getX() - crosshair.getWidth() / 2);
                    crosshair.setY(e.getY() - crosshair.getHeight() / 2);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) {
                    // Click on game over screen → restart in-place (same as R key)
                    resetGame();
                    return;
                }
                if (!isPaused) {
                    shoot();
                }
            }
        });
    }

    public void startGame() {
        if (gameThread == null || !isRunning) {
            resetGame();
            gameThread = new Thread(this);
            isRunning = true;
            gameThread.start();
        }
    }

    private void resetGame() {
        score = 0;
        shotsFired = 0;
        targetsHit = 0;
        gameOver = false;
        isPaused = false;
        flashMessage = "";
        flashUntil = 0;
        timeLeft = GAME_DURATION_SECONDS;
        crosshair.setX(getPreferredSize().width / 2);
        crosshair.setY(getPreferredSize().height / 2);
        target.respawn();
        penaltyTarget.respawn();
        startTime = System.currentTimeMillis();
        // Reclaim keyboard focus — critical for R key to work after Game Over
        requestFocusInWindow();
    }

    private void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameOver) {
            if (key == KeyEvent.VK_R) {
                // ── RESTART in-place: reset all state, thread keeps running ──
                resetGame();
            } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_SPACE) {
                isRunning = false;
                if (onReturnToMenu != null) {
                    onReturnToMenu.run();
                }
            }
            return;
        }

        if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
            isPaused = !isPaused;
            return;
        }

        if (isPaused) return;

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) crosshair.setUpPressed(true);
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) crosshair.setDownPressed(true);
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) crosshair.setLeftPressed(true);
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) crosshair.setRightPressed(true);
        
        if (key == KeyEvent.VK_SPACE) {
            shoot();
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) crosshair.setUpPressed(false);
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) crosshair.setDownPressed(false);
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) crosshair.setLeftPressed(false);
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) crosshair.setRightPressed(false);
    }

    private void shoot() {
        shotsFired++;
        recoilOffset = 20; // Add recoil effect

        // Calculate center of crosshair
        double crosshairCenterX = crosshair.getX() + crosshair.getWidth() / 2.0;
        double crosshairCenterY = crosshair.getY() + crosshair.getHeight() / 2.0;

        // ── Check penalty target FIRST (drawn on top, visually prominent) ──
        double penaltyCenterX = penaltyTarget.getX() + penaltyTarget.getWidth() / 2.0;
        double penaltyCenterY = penaltyTarget.getY() + penaltyTarget.getHeight() / 2.0;
        double penaltyDist = Point2D.distance(crosshairCenterX, crosshairCenterY, penaltyCenterX, penaltyCenterY);
        if (penaltyDist <= penaltyTarget.getWidth() / 2.0) {
            // Penalty hit! Deduct 5 points
            score -= 5;
            flashMessage = "⚠ PENALTY! -5";
            flashUntil = System.currentTimeMillis() + 900;
            penaltyTarget.respawn(); // Move penalty target to a new spot
            return; // Shot consumed — don't also check the normal target
        }

        // ── Check regular target ──
        double targetCenterX = target.getX() + target.getWidth() / 2.0;
        double targetCenterY = target.getY() + target.getHeight() / 2.0;
        double distance = Point2D.distance(crosshairCenterX, crosshairCenterY, targetCenterX, targetCenterY);
        if (distance <= target.getWidth() / 2.0) {
            // Normal hit!
            targetsHit++;
            if (timeLeft <= 5) {
                score += 10;
            } else {
                score += 5;
            }
            target.respawn();
        }
    }

    @Override
    public void run() {
        long start, elapsed, wait;

        while (isRunning) {
            start = System.nanoTime();

            if (!isPaused && !gameOver) {
                update();
            }
            repaint();

            elapsed = System.nanoTime() - start;
            wait = targetTime - elapsed / 1000000;

            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void update() {
        // Update timer
        long currentTime = System.currentTimeMillis();
        int elapsedSeconds = (int) ((currentTime - startTime) / 1000);
        timeLeft = GAME_DURATION_SECONDS - elapsedSeconds;

        if (timeLeft <= 0) {
            timeLeft = 0;
            gameOver = true;
        }

        if (recoilOffset > 0) {
            recoilOffset -= 2.0; // Decay recoil smoothly
        }

        crosshair.update();
        target.update();
        penaltyTarget.update(); // advance pulse animation
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            drawGameOver(g);
        } else {
            drawBackground(g);
            target.draw(g);
            penaltyTarget.draw(g); // drawn after normal target, slightly on top
            crosshair.draw(g);
            drawFirstPersonArms(g);
            drawHUD(g);

            if (isPaused) {
                drawPauseScreen(g);
            }
        }
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();

        // Dark gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 40, 50), 0, h, new Color(15, 20, 30));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);

        // Perspective floor/wall grid
        g2d.setColor(new Color(255, 255, 255, 10)); // faint white lines
        int gridSize = 50;
        
        // Draw grid lines
        for (int i = 0; i < w; i += gridSize) {
            g2d.drawLine(i, 0, i, h);
        }
        for (int i = 0; i < h; i += gridSize) {
            g2d.drawLine(0, i, w, i);
        }

        // Draw an inner glowing frame (like the room corners)
        g2d.setColor(new Color(100, 150, 255, 40));
        g2d.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(40, 40, w - 80, h - 80, 50, 50);
        g2d.setColor(new Color(200, 220, 255, 80));
        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(40, 40, w - 80, h - 80, 50, 50);
    }

    private void drawFirstPersonArms(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        
        double crosshairX = crosshair.getX() + crosshair.getWidth() / 2.0;
        double crosshairY = crosshair.getY() + crosshair.getHeight() / 2.0;
        
        // Calculate an offset based on how far the crosshair is from the center
        // This gives the illusion of aiming up/down/left/right
        double offsetX = (crosshairX - w / 2.0) * 0.3;
        double offsetY = (crosshairY - h / 2.0) * 0.3;
        
        // Base translation point
        double baseX = w / 2.0 + offsetX;
        double baseY = h + 20 + offsetY + recoilOffset; // +20 so it sits nicely at the bottom
        
        g2d.translate(baseX, baseY);
        
        // Scale down the gun
        g2d.scale(0.6, 0.6);

        // Draw the weapon pointing "into" the screen (Doom-style centered)
        
        // Gun Base/Stock
        g2d.setColor(new Color(40, 40, 45));
        int[] basePolyX = { -80, -50, 50, 80 };
        int[] basePolyY = { 50, -100, -100, 50 };
        g2d.fillPolygon(basePolyX, basePolyY, 4);

        // Gun Barrel (tapering inwards)
        g2d.setColor(new Color(60, 60, 70));
        int[] barrelX = { -45, -30, 30, 45 };
        int[] barrelY = { -100, -250, -250, -100 };
        g2d.fillPolygon(barrelX, barrelY, 4);

        // Barrel Tip
        g2d.setColor(new Color(25, 25, 30));
        g2d.fillRoundRect(-35, -270, 70, 40, 10, 10);
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillOval(-20, -265, 40, 15); // The hole

        // Sights/Rail on top
        g2d.setColor(new Color(30, 30, 35));
        int[] railX = { -15, -10, 10, 15 };
        int[] railY = { 0, -240, -240, 0 };
        g2d.fillPolygon(railX, railY, 4);

        // Glowing center energy core
        g2d.setColor(new Color(0, 255, 255));
        int[] glowX = { -6, -3, 3, 6 };
        int[] glowY = { -20, -220, -220, -20 };
        g2d.fillPolygon(glowX, glowY, 4);
        
        // Horizontal glowing accents
        g2d.fillRect(-25, -200, 50, 4);
        g2d.fillRect(-35, -150, 70, 6);

        // --- ARMS & HANDS ---
        
        // Left Arm (Forearm coming from bottom left)
        g2d.setColor(new Color(90, 90, 100)); // Armor color
        int[] leftArmX = { -800, -80, -40, -800 };
        int[] leftArmY = { 200, -60, -20, 400 };
        g2d.fillPolygon(leftArmX, leftArmY, 4);

        // Right Arm (Forearm coming from bottom right)
        g2d.setColor(new Color(90, 90, 100));
        int[] rightArmX = { 800, 80, 40, 800 };
        int[] rightArmY = { 200, -60, -20, 400 };
        g2d.fillPolygon(rightArmX, rightArmY, 4);

        // Left Hand/Glove gripping the side
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRoundRect(-90, -90, 45, 70, 20, 20); // Palm/fingers
        g2d.setColor(new Color(50, 50, 55));
        g2d.fillOval(-55, -80, 15, 40); // Thumb knuckle showing

        // Right Hand/Glove gripping the side
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRoundRect(45, -90, 45, 70, 20, 20); // Palm/fingers
        g2d.setColor(new Color(50, 50, 55));
        g2d.fillOval(40, -80, 15, 40); // Thumb knuckle showing

        g2d.dispose(); // Cleanup
    }



    private void drawHUD(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        DecimalFormat df = new DecimalFormat("0.00");
        double accuracy = (shotsFired > 0) ? ((double) targetsHit / shotsFired) * 100.0 : 100.0;
        double elapsedSecs = GAME_DURATION_SECONDS - timeLeft;
        double killsPerSec = (elapsedSecs > 0) ? ((double) targetsHit / elapsedSecs) : 0;

        // Draw bottom left HUD
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.setColor(new Color(150, 150, 160));
        g2d.drawString("ACCURACY", 20, getHeight() - 50);
        g2d.drawString("KILLS/SEC", 20, getHeight() - 20);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(100, 150, 255));
        g2d.drawString(df.format(accuracy) + " %", 20, getHeight() - 35);
        g2d.drawString(df.format(killsPerSec), 20, getHeight() - 5);

        // Draw timer at bottom center
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(150, 150, 255));
        String timeStr = String.format("00:%02d", timeLeft);
        g2d.drawString("⏱ " + timeStr, getWidth() / 2 + 30, getHeight() - 20);
        
        // Draw score above timer just to show it
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(score), getWidth() / 2 + 30, getHeight() - 40);

        if (timeLeft <= 5 && timeLeft > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("DOUBLE POINTS!", getWidth() / 2 - 70, 30);
        }

        // Flash penalty message for ~900ms after hitting the penalty target
        if (!flashMessage.isEmpty() && System.currentTimeMillis() < flashUntil) {
            g.setColor(new Color(255, 80, 80));
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString(flashMessage, getWidth() / 2 - 80, 60);
        } else {
            flashMessage = "";
        }
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("PAUSED", getWidth() / 2 - 80, getHeight() / 2);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press P or ESC to Resume", getWidth() / 2 - 120, getHeight() / 2 + 40);
    }

    private void drawGameOver(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;

        // Dark background
        g2d.setColor(new Color(10, 10, 15));
        g2d.fillRect(0, 0, w, h);

        // "GAME OVER" in red
        g2d.setColor(new Color(220, 50, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 54));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("GAME OVER", cx - fm.stringWidth("GAME OVER") / 2, h / 2 - 80);

        // Final score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        fm = g2d.getFontMetrics();
        String scoreStr = "Final Score: " + score;
        g2d.drawString(scoreStr, cx - fm.stringWidth(scoreStr) / 2, h / 2 - 20);

        // ── Restart button (highlighted) ──
        g2d.setColor(new Color(50, 200, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        String restartStr = "[ R ] or [ CLICK ]  Restart";
        g2d.drawString(restartStr, cx - fm.stringWidth(restartStr) / 2, h / 2 + 40);

        // ── Menu option (subdued) ──
        g2d.setColor(new Color(160, 160, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();
        String menuStr = "[ ENTER / ESC ]  Return to Menu";
        g2d.drawString(menuStr, cx - fm.stringWidth(menuStr) / 2, h / 2 + 80);
    }
}
