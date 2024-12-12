package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class minigame3 implements Minigame {
    private GroqClient groqClient;
    private HashMap<String, String> pairs;
    private JButton[] buttons;
    private String firstChoice = null;
    private JButton firstButton = null;
    private int matchesFound = 0;

    public minigame3() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
    }

    @Override
    public void start() {
        String topic = TopicManager.getInstance().getTopic();
        try {
            generatePairs(topic);
            createMatchingGame();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating matching game: " + e.getMessage());
        }
    }

    private void generatePairs(String topic) throws IOException {
        String prompt = String.format(
            "Create 4 matching pairs about %s. Format as JSON:\n" +
            "{\n" +
            "  \"pairs\": [\n" +
            "    {\"term\": \"term1\", \"definition\": \"definition1\"},\n" +
            "    {\"term\": \"term2\", \"definition\": \"definition2\"},\n" +
            "    ...\n" +
            "  ]\n" +
            "}\n" +
            "Make pairs educational and relevant to %s.",
            topic, topic
        );

        String response = groqClient.generateResponse(prompt);
        parsePairsContent(response);
    }

    private void createMatchingGame() {
        JFrame frame = new JFrame("Matching Game - " + TopicManager.getInstance().getTopic());
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        ArrayList<String> allItems = new ArrayList<>();
        pairs.forEach((term, def) -> {
            allItems.add(term);
            allItems.add(def);
        });
        Collections.shuffle(allItems);

        buttons = new JButton[allItems.size()];
        for (int i = 0; i < allItems.size(); i++) {
            final int index = i;
            buttons[i] = new JButton("?");
            buttons[i].putClientProperty("value", allItems.get(i));
            buttons[i].addActionListener(e -> handleButtonClick(buttons[index]));
            mainPanel.add(buttons[i]);
        }

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void handleButtonClick(JButton button) {
        if (button.getText().equals("?")) {
            button.setText((String) button.getClientProperty("value"));
            
            if (firstChoice == null) {
                firstChoice = (String) button.getClientProperty("value");
                firstButton = button;
            } else {
                checkMatch(button);
            }
        }
    }

    private void checkMatch(JButton secondButton) {
        String secondChoice = (String) secondButton.getClientProperty("value");
        
        if ((pairs.containsKey(firstChoice) && pairs.get(firstChoice).equals(secondChoice)) ||
            (pairs.containsKey(secondChoice) && pairs.get(secondChoice).equals(firstChoice))) {
            matchesFound++;
            if (matchesFound == pairs.size()) {
                JOptionPane.showMessageDialog(null, "Congratulations! You found all matches!");
            }
        } else {
            Timer timer = new Timer(1000, e -> {
                firstButton.setText("?");
                secondButton.setText("?");
            });
            timer.setRepeats(false);
            timer.start();
        }
        
        firstChoice = null;
        firstButton = null;
    }

    private String loadApiKey() {
        // ...existing API key loading code...
        return "your-api-key";
    }
}