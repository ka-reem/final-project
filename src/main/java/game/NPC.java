package game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class NPC extends InteractiveObject {
    private String dialog;

    public NPC(float x, float y, BufferedImage sprite, String dialog) {
        super(x, y, sprite);
        this.dialog = dialog;
    }

    @Override
    public void update() {
        // NPC behavior updates
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.drawImage(sprite, (int)x, (int)y, null);
        // Debug hitbox
        g2d.setColor(Color.GREEN);
        g2d.drawRect((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
    }

    @Override
    public void interact(Player player) {
        // Trigger dialog
    }
}