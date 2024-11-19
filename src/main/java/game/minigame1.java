package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class minigame1 extends JFrame implements Minigame {
    private int score = 0;
    private JLabel scoreLabel;
    private JButton button;

    public minigame1() {
        setTitle("Minigame 1");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        scoreLabel = new JLabel("Score: " + score);
        add(scoreLabel);

        button = new JButton("Click me!");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                score++;
                scoreLabel.setText("Score: " + score);
            }
        });
        add(button);
    }

    @Override
    public void start() {
        startGame();
    }

    public void startGame() {
        setVisible(true);
    }
}
