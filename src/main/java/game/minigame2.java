package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class minigame2 extends JFrame implements Minigame {
    private GroqClient groqClient;
    private ArrayList<String> questions;
    private ArrayList<String[]> options;
    private ArrayList<Integer> answers;
    private int currentQuestion = 0;
    private int score = 0;
    private JLabel questionLabel;
    private JRadioButton[] radioButtons;
    private JPanel mainPanel;
    private JButton submitButton;
    private ButtonGroup buttonGroup;

    public minigame2() {
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
        setTitle("Multiple Choice Quiz");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Question panel
        questionLabel = new JLabel();
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Radio buttons panel
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        
        radioButtons = new JRadioButton[3];
        buttonGroup = new ButtonGroup();
        
        for (int i = 0; i < 3; i++) {
            radioButtons[i] = new JRadioButton();
            buttonGroup.add(radioButtons[i]);
            radioPanel.add(radioButtons[i]);
            radioPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Submit button
        submitButton = new JButton("Submit Answer");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> handleSubmit());

        contentPanel.add(questionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(radioPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(submitButton);

        add(contentPanel);
    }

    @Override
    public void start() {
        if (!TopicManager.getInstance().hasValidTopic()) {
            JOptionPane.showMessageDialog(null, 
                "You found a landmark! Please talk to an available NPC to select a topic first.");
            return;
        }
        
        try {
            generateQuizContent();
            showCurrentQuestion();
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error starting quiz: " + e.getMessage());
        }
    }

    private void generateQuizContent() throws IOException {
        String topic = TopicManager.getInstance().getTopic();
        String prompt = String.format(
            "Create a multiple choice quiz about %s with 3 questions.\n" +
            "Keep answers short and clear. Format exactly as:\n" +
            "Q1|A1|B1|C1|0\n" +
            "Q2|A2|B2|C2|1\n" +
            "Q3|A3|B3|C3|2\n" +
            "Where Q is the question, A/B/C are options, and the last number is the correct answer (0=A, 1=B, 2=C)", 
            topic);

        String response = groqClient.generateResponse(prompt);
        parseQuizContent(response);
    }

    private String loadApiKey() {
        // ...existing API key loading code...
        return "your-api-key";
    }

    private void showCurrentQuestion() {
        try {
            questionLabel.setText("<html><body style='width: 300px'>" + 
                questions.get(currentQuestion) + "</body></html>");
            
            buttonGroup.clearSelection();
            String[] currentOptions = options.get(currentQuestion);
            
            for (int i = 0; i < radioButtons.length; i++) {
                radioButtons[i].setText(currentOptions[i]);
                radioButtons[i].setSelected(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    private void parseQuizContent(String response) {
        questions = new ArrayList<>();
        options = new ArrayList<>();
        answers = new ArrayList<>();

        try {
            String[] lines = response.trim().split("\n");
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    questions.add(parts[0].trim());
                    options.add(new String[]{
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim()
                    });
                    answers.add(Integer.parseInt(parts[4].trim()));
                }
            }

            // Add fallback if no valid questions were parsed
            if (questions.isEmpty()) {
                addFallbackQuestion();
            }
        } catch (Exception e) {
            e.printStackTrace();
            addFallbackQuestion();
        }
    }

    private void addFallbackQuestion() {
        String topic = TopicManager.getInstance().getTopic();
        questions.add("What is a key concept of " + topic + "?");
        options.add(new String[]{"Option A", "Option B", "Option C"});
        answers.add(0);
    }

    private void handleSubmit() {
        boolean answered = false;
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                answered = true;
                if (i == answers.get(currentQuestion)) {
                    score++;
                }
                break;
            }
        }

        if (!answered) {
            JOptionPane.showMessageDialog(this, "Please select an answer!");
            return;
        }

        currentQuestion++;
        if (currentQuestion < questions.size()) {
            showCurrentQuestion();
        } else {
            JOptionPane.showMessageDialog(this, 
                String.format("Quiz completed! Score: %d/%d", score, questions.size()));
            dispose();
        }
    }
}