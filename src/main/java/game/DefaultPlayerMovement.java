package game;

public class DefaultPlayerMovement implements PlayerMovementStrategy {
    private static final float MOVE_SPEED = 2.0f;

    @Override
    public void move(Player player) {
        if (player.isUpPressed()) {
            player.setVy(-MOVE_SPEED);
        }
        if (player.isDownPressed()) {
            player.setVy(MOVE_SPEED);
        }
        if (player.isLeftPressed()) {
            player.setVx(-MOVE_SPEED);
        }
        if (player.isRightPressed()) {
            player.setVx(MOVE_SPEED);
        }
        
        player.setX(player.getX() + player.getVx());
        player.setY(player.getY() + player.getVy());
    }
}