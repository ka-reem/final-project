package game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Landmark extends InteractiveObject {
    private Minigame minigame;

    public Landmark(float x, float y, BufferedImage sprite, Minigame minigame) {
        super(x, y, sprite);
        this.minigame = minigame;
    }

    @Override
    public void update() {
        // Landmark behavior updates
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.drawImage(sprite, (int)x, (int)y, null);
        // Debug hitbox
        g2d.setColor(Color.GREEN);
        g2d.drawRect((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
    }

    @Override
    public void interact() {
        minigame.start();
    }

    // Rename activateMinigame to interact to match interface
    public void activateMinigame() {
        interact();
    }
}