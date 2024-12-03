package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;

public class minigame1 extends JFrame implements Minigame {
    private int score = 0;
    private JLabel scoreLabel;
    private JLabel questionLabel;
    private JTextField answerField;
    private JButton submitButton;
    private GroqClient groqClient;
    private String currentAnswer;
    private ArrayList<String> questions;
    private int currentQuestionIndex = 0;

    public minigame1() {
        initializeGroqClient();
        setupUI();
        generateQuestions();
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
        setTitle("Fill in the Blank Challenge");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create panels
        JPanel topPanel = new JPanel(new FlowLayout());
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JPanel bottomPanel = new JPanel(new FlowLayout());

        // Initialize components
        scoreLabel = new JLabel("Score: " + score);
        questionLabel = new JLabel("Loading question...");
        answerField = new JTextField(20);
        submitButton = new JButton("Submit Answer");

        // Style components
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Add components to panels
        topPanel.add(scoreLabel);
        centerPanel.add(questionLabel);
        centerPanel.add(answerField);
        bottomPanel.add(submitButton);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add action listener
        submitButton.addActionListener(e -> checkAnswer());
        answerField.addActionListener(e -> checkAnswer());

        // Center the frame
        setLocationRelativeTo(null);
    }

    private void generateQuestions() {
        questions = new ArrayList<>();
        try {
            // TODO:
            // Create when starting the game a topic you'd like to learn and store it globally so 
            // it can be used in the minigames.
            String prompt = "Generate a fill-in-the-blank question about math. Format: Question|Answer";
            String response = groqClient.generateResponse(prompt);
            String[] parts = response.split("\\|");
            if (parts.length == 2) {
                questions.add(parts[0].trim());
                currentAnswer = parts[1].trim();
                questionLabel.setText(questions.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            questionLabel.setText("Error generating question");
        }
    }

    private void checkAnswer() {
        String userAnswer = answerField.getText().trim().toLowerCase();
        if (userAnswer.equals(currentAnswer.toLowerCase())) {
            score += 10;
            scoreLabel.setText("Score: " + score);
            JOptionPane.showMessageDialog(this, "Correct! +10 points");
            
            // Generate next question or end game
            currentQuestionIndex++;
            if (currentQuestionIndex < 5) { // Limit to 5 questions
                generateQuestions();
                answerField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Game Complete! Final Score: " + score);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect! Try again!");
        }
    }

    @Override
    public void start() {
        setVisible(true);
        answerField.requestFocus();
    }
}
