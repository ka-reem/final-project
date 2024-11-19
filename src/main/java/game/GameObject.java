
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
    
    protected void updateHitbox() {
        hitbox.setLocation((int)x, (int)y);
    }
}