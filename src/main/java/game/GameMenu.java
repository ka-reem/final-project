package game;

import javax.swing.*;
import java.awt.*;

public class GameMenu extends JFrame {
    private JButton startButton;
    private JButton optionsButton;
    private JButton exitButton;

    public GameMenu() {
        setTitle("Game Name");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(3, 1, 10, 10));

        startButton = new JButton("Start Game");
        optionsButton = new JButton("Options");
        exitButton = new JButton("Exit");

        add(startButton);
        add(optionsButton);
        add(exitButton);

        startButton.addActionListener(e -> startGame());
        optionsButton.addActionListener(e -> openOptions());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void startGame() {
        this.dispose(); // Close the menu window
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.setVisible(true);
        });
    }

    private void openOptions() {
        // Change this to "choose topic to learn" or add settings to turn on and off image generation and text-to-speech
        System.out.println("Opening options"); // Debugging
    }

    public void display() {
        setVisible(true);
    }
}