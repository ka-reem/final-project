package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Landmark extends InteractiveObject {
    private Minigame minigame;
    private static BufferedImage defaultSprite;

    static {
        // Create a default 32x32 placeholder sprite
        defaultSprite = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = defaultSprite.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, 32, 32);
        g.dispose();
    }

    public Landmark(float x, float y, BufferedImage sprite, Minigame minigame) {
        super(x, y, defaultSprite); // Start with default sprite
        this.minigame = minigame;
    }

    @Override
    public void update() {
        // Try to load the landmark image if it exists
        try {
            BufferedImage newSprite = loadLandmarkSprite();
            if (newSprite != null && newSprite != this.sprite) {
                this.sprite = newSprite;
                // Update hitbox for new sprite size
                this.hitbox = new Rectangle((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
            }
        } catch (Exception e) {
            // If image isn't available yet, keep using default sprite
        }
    }

    private static BufferedImage loadLandmarkSprite() {
        try {
            BufferedImage originalImage = ImageIO.read(Landmark.class.getResourceAsStream("/landmark.png"));
            if (originalImage == null) return null;

            int newWidth = 128;
            int newHeight = 128;
            
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            return scaledImage;
        } catch (IOException e) {
            return null; // Return null instead of throwing exception
        }
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
        player.resetMovement();
        minigame.start();
    }

    public void activateMinigame(Player player) {
        interact(player);
    }
}