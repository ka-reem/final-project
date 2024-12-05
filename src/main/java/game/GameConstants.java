package game;

import java.awt.Toolkit;
import java.awt.Dimension;

public class GameConstants {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int GAME_SCREEN_WIDTH = (int) screenSize.getWidth();
    public static final int GAME_SCREEN_HEIGHT = (int) screenSize.getHeight();
}
