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
            "Generate a multiple choice quiz about %s with 3 questions. Format as JSON:\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    {\"question\": \"Q1\", \"options\": [\"A1\", \"B1\", \"C1\"], \"correct\": 0},\n" +
            "    {\"question\": \"Q2\", \"options\": [\"A2\", \"B2\", \"C2\"], \"correct\": 1},\n" +
            "    {\"question\": \"Q3\", \"options\": [\"A3\", \"B3\", \"C3\"], \"correct\": 2}\n" +
            "  ]\n" +
            "}\n", topic);

        String response = groqClient.generateResponse(prompt);
        // Clean the response if needed
        response = response.trim();
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
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response, JsonObject.class);
            JsonArray questionsArray = json.getAsJsonArray("questions");
            
            for (int i = 0; i < questionsArray.size(); i++) {
                JsonObject q = questionsArray.get(i).getAsJsonObject();
                questions.add(q.get("question").getAsString());
                
                JsonArray opts = q.getAsJsonArray("options");
                String[] optionsArray = new String[3];
                for (int j = 0; j < opts.size(); j++) {
                    optionsArray[j] = opts.get(j).getAsString();
                }
                options.add(optionsArray);
                
                answers.add(q.get("correct").getAsInt());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback content
            questions.add("Sample question?");
            options.add(new String[]{"Option A", "Option B", "Option C"});
            answers.add(0);
        }
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