package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Landmark extends InteractiveObject {
    private Minigame minigame;

    public Landmark(float x, float y, BufferedImage sprite, Minigame minigame) {
        super(x, y, loadLandmarkSprite());
        this.minigame = minigame;
    }

    private static BufferedImage loadLandmarkSprite() {
        try {
            BufferedImage originalImage = ImageIO.read(Landmark.class.getResourceAsStream("/landmark.png"));

            int newWidth = 128;
            int newHeight = 128;
            
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            return scaledImage;
        } catch (IOException e) {
            System.err.println("Error loading landmark image: " + e.getMessage());
            throw new RuntimeException("Failed to load landmark image", e);
        }
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
    public void interact(Player player) {
        player.resetMovement();
        minigame.start();
    }

    public void activateMinigame(Player player) {
        interact(player);
    }
}