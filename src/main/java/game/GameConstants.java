package game;

import java.awt.Toolkit;
import java.awt.Dimension;

public class GameConstants {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // Make window 85% of screen size 
    public static final int GAME_SCREEN_WIDTH = (int) (screenSize.getWidth() * 0.85);
    public static final int GAME_SCREEN_HEIGHT = (int) (screenSize.getHeight() * 0.85);
}
