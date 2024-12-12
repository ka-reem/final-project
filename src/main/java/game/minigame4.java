package game;

import javax.swing.*;
import java.awt.*;

public class minigame4 implements Minigame {
    @Override
    public void start() {  // Changed from start(Player player)
        JFrame frame = new JFrame("Fill in the Blanks");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel instruction = new JLabel("Complete the sentence about " + 
                                      TopicManager.getInstance().getTopic());
        JTextField answer = new JTextField(20);
        JButton submit = new JButton("Submit");
        
        submit.addActionListener(e -> {
            // Validate answer
            frame.dispose();
        });

        panel.add(instruction);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(answer);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(submit);

        frame.add(panel);
        frame.setVisible(true);
    }
}