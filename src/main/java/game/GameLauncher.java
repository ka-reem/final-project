package game;

import javax.swing.SwingUtilities;

public class GameLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameMenu gameMenu = new GameMenu();
            gameMenu.display();
        });
    }
}