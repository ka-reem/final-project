package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

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
        BufferedImage newSprite = loadLandmarkSprite();
        if (newSprite != null && !newSprite.equals(this.sprite)) {
            this.sprite = newSprite;
            this.hitbox = new Rectangle((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
            // Use GameWorld instead of GamePanel for repainting
            GameWorld.getInstance().repaint();
        }
    }

    private static BufferedImage loadLandmarkSprite() {
        try {
            String resourcePath = "/landmark.png";
            InputStream is = Landmark.class.getResourceAsStream(resourcePath);
            if (is != null) {
                BufferedImage originalImage = ImageIO.read(is);
                is.close();
                return scaleImage(originalImage);
            }
            return defaultSprite;
        } catch (IOException e) {
            System.err.println("Error loading landmark sprite: " + e.getMessage());
            return defaultSprite;
        }
    }

    private static BufferedImage scaleImage(BufferedImage original) {
        int newWidth = 128;
        int newHeight = 128;
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }

    public static void updateLandmark(String imagePath) {
        try {
            // Read the new image
            BufferedImage newImage = ImageIO.read(new File(imagePath));
            if (newImage != null) {
                BufferedImage scaledImage = scaleImage(newImage);
                
                // Save to resources directory
                File resourceFile = new File("src/main/resources/landmark.png");
                ImageIO.write(scaledImage, "PNG", resourceFile);
                
                // Force reload sprite and trigger game world update
                defaultSprite = scaledImage;
                GameWorld.getInstance().reloadGameObjects();
                
                System.out.println("Landmark image updated successfully");
            }
        } catch (IOException e) {
            System.err.println("Error updating landmark sprite: " + e.getMessage());
        }
    }

    public void forceReload() {
        this.sprite = loadLandmarkSprite();
        this.hitbox = new Rectangle((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
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