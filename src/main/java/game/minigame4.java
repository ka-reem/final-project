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
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        sentenceLabel = new JLabel();
        answerField = new JTextField(20);
        JButton submit = new JButton("Submit");
        
        submit.addActionListener(e -> checkAnswer());
        
        panel.add(sentenceLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(answerField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(submit);

        add(panel);
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