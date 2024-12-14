package game;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class GameObject {
    protected float x;
    protected float y;
    protected BufferedImage sprite;
    protected Rectangle hitbox;

    public GameObject(float x, float y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.hitbox = new Rectangle((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2d);
    
    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { 
        this.x = x;
        updateHitbox();
    }
    public void setY(float y) { 
        this.y = y;
        updateHitbox();
    }
    
    public Rectangle getHitbox() { 
        return hitbox; 
    }

    protected void updateHitbox() {
        // Update hitbox to exactly match the object's position
        hitbox.setLocation((int)x, (int)y);
    }

    public int getWidth() {
        return sprite.getWidth();
    }

    public int getHeight() {
        return sprite.getHeight();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateHitbox();
    }
}