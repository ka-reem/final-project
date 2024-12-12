package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class minigame3 extends JFrame implements Minigame {
    private GroqClient groqClient;
    private HashMap<String, String> pairs;
    private JButton[] buttons;
    private String firstChoice = null;
    private JButton firstButton = null;
    private int matchesFound = 0;

    public minigame3() {
        initializeGroqClient();
        setupUI();
    }

    private void initializeGroqClient() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
                groqClient = new GroqClient(props.getProperty("groq.api.key"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Matching Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    @Override
    public void start() {
        if (!TopicManager.getInstance().hasValidTopic()) {
            JOptionPane.showMessageDialog(null, 
                "You found a landmark! Please talk to an available NPC to select a topic first.");
            dispose();
            return;
        }

        try {
            generatePairs();
            setVisible(true);
            createMatchingGame();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating matching game: " + e.getMessage());
            dispose();
        }
    }

    private void generatePairs() throws IOException {
        String topic = TopicManager.getInstance().getTopic();
        String prompt = String.format(
            "Generate 4 matching pairs about %s. Each pair should have a term and its definition. Format as JSON:\n" +
            "{\n" +
            "  \"pairs\": [\n" +
            "    {\"term\": \"term1\", \"definition\": \"definition1\"},\n" +
            "    {\"term\": \"term2\", \"definition\": \"definition2\"},\n" +
            "    {\"term\": \"term3\", \"definition\": \"definition3\"},\n" +
            "    {\"term\": \"term4\", \"definition\": \"definition4\"}\n" +
            "  ]\n" +
            "}\n", topic);

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
            javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                firstButton.setText("?");
                secondButton.setText("?");
            });
            timer.setRepeats(false);
            timer.start();
        }
        
        firstChoice = null;
        firstButton = null;
    }

    private void parsePairsContent(String response) {
        pairs = new HashMap<>();
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonArray pairsArray = json.getAsJsonArray("pairs");
            
            for (int i = 0; i < pairsArray.size(); i++) {
                JsonObject pair = pairsArray.get(i).getAsJsonObject();
                pairs.put(
                    pair.get("term").getAsString(),
                    pair.get("definition").getAsString()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback content
            pairs.put("Term 1", "Definition 1");
            pairs.put("Term 2", "Definition 2");
        }
    }

    private String loadApiKey() {
        // ...existing API key loading code...
        return "your-api-key";
    }
}