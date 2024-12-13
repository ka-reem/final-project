package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class minigame3 extends JFrame implements Minigame {
    private GroqClient groqClient;
    private HashMap<String, String> pairs;
    private JButton[] buttons;
    private String firstChoice = null;
    private JButton firstButton = null;
    private int matchesFound = 0;
    private boolean isProcessing = false;

    public minigame3() {
        pairs = new HashMap<>();
        initializeGroqClient();
        setupUI();
    }

    private void setupUI() {
        setTitle("Matching Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 240, 240));
    }

    private void createMatchingGame() {
        getContentPane().removeAll();
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel(new GridLayout(3, 4, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and shuffle cards
        ArrayList<String> allItems = new ArrayList<>();
        pairs.forEach((term, def) -> {
            allItems.add(term);
            allItems.add(def);
        });
        Collections.shuffle(allItems);

        // Create buttons with improved styling
        buttons = new JButton[allItems.size()];
        for (int i = 0; i < allItems.size(); i++) {
            buttons[i] = createStyledButton(allItems.get(i));
            mainPanel.add(buttons[i]);
        }

        // Add panel to scroll pane for better handling of long content
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane);

        // Update display
        revalidate();
        repaint();
    }

    private JButton createStyledButton(String value) {
        JButton button = new JButton("?");
        button.putClientProperty("value", value);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 120));
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setFocusPainted(false);
        
        button.addActionListener(e -> {
            if (!isProcessing && button.getText().equals("?")) {
                handleButtonClick(button);
            }
        });
        
        return button;
    }

    private void handleButtonClick(JButton button) {
        if (isProcessing) return;
        
        String value = (String) button.getClientProperty("value");
        button.setText("<html><center>" + value + "</center></html>");
        
        if (firstChoice == null) {
            firstChoice = value;
            firstButton = button;
            button.setBackground(new Color(200, 200, 255)); // Light blue for selected
        } else {
            isProcessing = true;
            
            if ((pairs.containsKey(firstChoice) && pairs.get(firstChoice).equals(value)) ||
                (pairs.containsKey(value) && pairs.get(value).equals(firstChoice))) {
                // Match found
                matchFound(button);
            } else {
                // No match
                noMatch(button);
            }
        }
    }

    private void matchFound(JButton secondButton) {
        matchesFound++;
        firstButton.setBackground(new Color(144, 238, 144)); // Light green
        secondButton.setBackground(new Color(144, 238, 144));
        
        javax.swing.Timer timer = new javax.swing.Timer(800, e -> {
            firstButton.setEnabled(false);
            secondButton.setEnabled(false);
            firstChoice = null;
            firstButton = null;
            isProcessing = false;
            
            if (matchesFound == pairs.size()) {
                gameComplete();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void noMatch(JButton secondButton) {
        secondButton.setBackground(new Color(255, 200, 200)); // Light red
        firstButton.setBackground(new Color(255, 200, 200));
        
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            firstButton.setText("?");
            secondButton.setText("?");
            firstButton.setBackground(Color.WHITE);
            secondButton.setBackground(Color.WHITE);
            firstChoice = null;
            firstButton = null;
            isProcessing = false;
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void gameComplete() {
        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            MinigameManager.getInstance().markMinigameCompleted(minigame3.class);
            JOptionPane.showMessageDialog(this, 
                "Congratulations! You've matched all pairs!\nMinigame completed successfully!", 
                "Game Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
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

    private void generatePairs() throws IOException {
        String topic = TopicManager.getInstance().getTopic();
        String prompt = String.format(
            "Generate 6 pairs of matching terms and definitions about %s. Format each pair as:\nterm|definition", 
            topic);

        String response = groqClient.generateResponse(prompt);
        parsePairsContent(response);
    }

    private void parsePairsContent(String response) {
        pairs.clear();
        String[] lines = response.split("\n");
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) {
                pairs.put(parts[0].trim(), parts[1].trim());
            }
        }
        
        // Add fallback pair if no valid pairs were parsed
        if (pairs.isEmpty()) {
            pairs.put("Term 1", "Definition 1");
            pairs.put("Term 2", "Definition 2");
            pairs.put("Term 3", "Definition 3");
        }
    }

    @Override
    public void start() {
        if (MinigameManager.getInstance().isMinigameCompleted(minigame3.class)) {
            JOptionPane.showMessageDialog(this, 
                "You have already completed this minigame!", 
                "Already Completed", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        if (!TopicManager.getInstance().hasValidTopic()) {
            JOptionPane.showMessageDialog(this, 
                "Please select a topic first!", 
                "No Topic Selected", 
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        try {
            generatePairs();
            createMatchingGame();
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error starting game: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}