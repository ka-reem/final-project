package game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Player extends GameObject {
    private static final float SPEED = 5.0f;
    private float vx;
    private float vy;
    private float angle;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    public Player(float x, float y, BufferedImage sprite) {
        super(x, y, sprite);
    }

    @Override
    public void update() {
        // Reset velocity
        vx = 0;
        vy = 0;

        // Update velocity based on input
        if (upPressed) vy = -SPEED;
        if (downPressed) vy = SPEED;
        if (leftPressed) vx = -SPEED;
        if (rightPressed) vx = SPEED;

        // Calculate new position
        float newX = x + vx;
        float newY = y + vy;
        
        // Check map boundaries
        if (newX >= 0 && newX <= GameConstants.GAME_SCREEN_WIDTH * 2 - sprite.getWidth()) {
            x = newX;
        }
        if (newY >= 0 && newY <= GameConstants.GAME_SCREEN_HEIGHT * 2 - sprite.getHeight()) {
            y = newY;
        }
        
        // Update hitbox
        updateHitbox();
    }

    @Override
    public void draw(Graphics2D g2d) {
        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), sprite.getWidth() / 2.0, sprite.getHeight() / 2.0);
        g2d.drawImage(sprite, rotation, null);
    }

    // Movement controls
    public void toggleUpPressed() { upPressed = true; }
    public void toggleDownPressed() { downPressed = true; }
    public void toggleLeftPressed() { leftPressed = true; }
    public void toggleRightPressed() { rightPressed = true; }
    
    public void unToggleUpPressed() { upPressed = false; }
    public void unToggleDownPressed() { downPressed = false; }
    public void unToggleLeftPressed() { leftPressed = false; }
    public void unToggleRightPressed() { rightPressed = false; }

    public boolean isUpPressed() { return upPressed; }
    public boolean isDownPressed() { return downPressed; }
    public boolean isLeftPressed() { return leftPressed; }
    public boolean isRightPressed() { return rightPressed; }

    public void resetMovement() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        vx = 0;
        vy = 0;
    }

    // Getters and setters
    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public void setVx(float vx) { this.vx = vx; }
    public void setVy(float vy) { this.vy = vy; }
    public int getWidth() { return sprite.getWidth(); }
    public int getHeight() { return sprite.getHeight(); }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateHitbox();
    }
}
