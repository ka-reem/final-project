package game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Landmark {
    private float x;
    private float y;
    private BufferedImage img;
    private Rectangle hitbox;
    private static final int INTERACTION_DISTANCE = 50;

    public Landmark(float x, float y, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.hitbox = new Rectangle((int)x, (int)y, img.getWidth(), img.getHeight());
    }

    public boolean isPlayerInRange(Player player) {
        double distance = Math.sqrt(
            Math.pow(player.getX() - x, 2) + 
            Math.pow(player.getY() - y, 2)
        );
        return distance < INTERACTION_DISTANCE;
    }

    public void activateMinigame() throws InterruptedException {
        try {
            // Key sometimes gets stuck
            Thread.sleep(100); // Prevent the last key pressed from getting stuck
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, Failed to complete operation");
        }
        minigame1 game = new minigame1();
        game.startGame();
    }

    public void drawImage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, (int)x, (int)y, null);
        
        // Debug hitbox
        g2d.setColor(Color.GREEN);
        g2d.drawRect((int)x, (int)y, this.img.getWidth(), this.img.getHeight());
    }
}