
package game;

import java.awt.image.BufferedImage;

public abstract class InteractiveObject extends GameObject {
    protected static final int INTERACTION_DISTANCE = 50;

    public InteractiveObject(float x, float y, BufferedImage sprite) {
        super(x, y, sprite);
    }

    public boolean isPlayerInRange(Player player) {
        double distance = Math.sqrt(
            Math.pow(player.getX() - x, 2) + 
            Math.pow(player.getY() - y, 2)
        );
        return distance < INTERACTION_DISTANCE;
    }

    public abstract void interact();
}