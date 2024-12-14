package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class minigame4 extends JFrame implements Minigame {
    private GroqClient groqClient;
    private String sentence;
    private String answer;
    private JTextField answerField;
    private JLabel sentenceLabel;

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);

    public minigame4() {
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
        setTitle("Fill in the Blanks");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Complete the Sentence");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sentence label
        sentenceLabel = new JLabel();
        sentenceLabel.setFont(CONTENT_FONT);
        sentenceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Answer field panel
        JPanel answerPanel = new JPanel();
        answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        answerPanel.setBackground(BACKGROUND_COLOR);
        
        answerField = new JTextField(15);
        answerField.setFont(CONTENT_FONT);
        answerField.setPreferredSize(new Dimension(200, 30));

        // Submit button
        JButton submit = new JButton("Submit");
        submit.setFont(CONTENT_FONT);
        submit.setBackground(ACCENT_COLOR);
        submit.setForeground(Color.WHITE);
        submit.setFocusPainted(false);
        submit.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        submit.setAlignmentX(Component.CENTER_ALIGNMENT);
        submit.addActionListener(e -> checkAnswer());

        answerPanel.add(answerField);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(sentenceLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(answerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(submit);

        add(mainPanel);
    }

    @Override
    public void start() {
        if (MinigameManager.getInstance().isMinigameCompleted(minigame4.class)) {
            JOptionPane.showMessageDialog(null, 
                "You have already completed this minigame!", 
                "Already Completed", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        if (!TopicManager.getInstance().hasValidTopic()) {
            JOptionPane.showMessageDialog(null, 
                "You found a landmark! Please talk to an available NPC to select a topic first.");
            dispose();
            return;
        }

        try {
            generateContent();
            setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating content: " + e.getMessage());
            dispose();
        }
    }

    private void generateContent() throws IOException {
        String topic = TopicManager.getInstance().getTopic();
        String prompt = String.format(
            "Generate a fill-in-the-blank sentence about %s. Format as JSON:\n" +
            "{\n" +
            "  \"sentence\": \"Complete sentence with ___ for blank\",\n" +
            "  \"answer\": \"correct word for blank\"\n" +
            "}\n", topic);

        String response = groqClient.generateResponse(prompt);
        // Clean the response if needed
        response = response.trim();
        parseContent(response);
        sentenceLabel.setText("<html>" + sentence + "</html>");
    }

    private void checkAnswer() {
        String userAnswer = answerField.getText().trim().toLowerCase();
        if (userAnswer.equals(answer.toLowerCase())) {
            MinigameManager.getInstance().markMinigameCompleted(minigame4.class);
            JOptionPane.showMessageDialog(this, "Correct!\nMinigame completed successfully!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect! Try again.");
            answerField.setText("");
            answerField.requestFocus();
        }
    }

    private void parseContent(String response) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response, JsonObject.class);
            sentence = json.get("sentence").getAsString();
            answer = json.get("answer").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback content
            sentence = "Complete sentence with ___ for blank";
            answer = "correct";
        }
    }
}