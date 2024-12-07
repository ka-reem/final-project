package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class minigame1 extends JFrame implements Minigame {
    private int score = 0;
    private JLabel scoreLabel;
    private JLabel questionLabel;
    private JTextField answerField;
    private JButton submitButton;
    private GroqClient groqClient;
    private String currentAnswer;
    private int wrongAttempts = 0;
    private static final int MAX_ATTEMPTS = 4;
    private String[] hints;
    private String maskedAnswer;  // Add this field
    private boolean[] revealedLetters;  // Add this field
    private String currentQuestion; // Add this field

    public minigame1() {
        initializeGroqClient();
        setupUI();
        // Don't generate question in constructor
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
        setTitle("Quiz: " + TopicManager.getInstance().getTopic());
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout());
        answerField = new JTextField(20);
        submitButton = new JButton("Submit");

        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));

        inputPanel.add(answerField);
        inputPanel.add(submitButton);

        mainPanel.add(scoreLabel, BorderLayout.NORTH);
        mainPanel.add(questionLabel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        add(mainPanel);

        submitButton.addActionListener(e -> checkAnswer());
        answerField.addActionListener(e -> checkAnswer());

        setLocationRelativeTo(null);
    }

    private void generateQuestion() {
        try {
            String topic = TopicManager.getInstance().getTopic();
            System.out.println("DEBUG - Current Topic: " + topic); // Debug print
            
            if (!TopicManager.getInstance().hasValidTopic()) {
                questionLabel.setText("Error: Please select a topic in the chat first.");
                return;
            }
            
            String prompt = String.format(
                "Generate a fun question and its one or two-word answer about %s, " +
                "along with 3 descriptive and progressive hints to answer the question. Each hint should be more revealing than the last. " +
                "For the question, only write the question and nothing else. " +
                "Format: question|answer|hint1|hint2|hint3", topic);
            
            String response = groqClient.generateResponse(prompt);
            
            String[] parts = response.split("\\|");
            if (parts.length == 5) {  // Now expecting 5 parts: question, answer, and 3 hints
                currentQuestion = parts[0].trim(); // Store the question
                questionLabel.setText("<html>" + currentQuestion + "<br><br>Answer: " + maskedAnswer + "</html>");
                currentAnswer = parts[1].trim().toLowerCase();
                hints = new String[]{parts[2].trim(), parts[3].trim(), parts[4].trim()};
                wrongAttempts = 0;  // Reset wrong attempts for new question
                
                // Initialize masked answer
                revealedLetters = new boolean[currentAnswer.length()];
                updateMaskedAnswer();
                
                // Update the display with both question and masked answer
                updateQuestionDisplay();
                
                System.out.println("DEBUG - Question: " + currentQuestion);
                System.out.println("DEBUG - Correct Answer: " + currentAnswer);
                System.out.println("DEBUG - Hints: " + String.join(" -> ", hints));
            }
        } catch (IOException e) {
            e.printStackTrace();
            questionLabel.setText("Error generating question");
        }
    }

    private void updateMaskedAnswer() {
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < currentAnswer.length(); i++) {
            if (revealedLetters[i]) {
                masked.append(currentAnswer.charAt(i));
            } else {
                masked.append(currentAnswer.charAt(i) == ' ' ? ' ' : '_');
            }
            masked.append(' '); // Add space between characters
        }
        maskedAnswer = masked.toString();
    }

    private void updateQuestionDisplay() {
        questionLabel.setText("<html>" + currentQuestion + "<br><br>Answer: " + maskedAnswer + "</html>");
    }

    private void revealRandomLetter() {
        // Find unrevealed letters
        ArrayList<Integer> unrevealed = new ArrayList<>();
        for (int i = 0; i < revealedLetters.length; i++) {
            if (!revealedLetters[i] && currentAnswer.charAt(i) != ' ') {
                unrevealed.add(i);
            }
        }
        
        if (!unrevealed.isEmpty()) {
            // Reveal a random letter
            int randomIndex = unrevealed.get(new Random().nextInt(unrevealed.size()));
            revealedLetters[randomIndex] = true;
            updateMaskedAnswer();
            updateQuestionDisplay(); // Use the new method instead of direct setText
        }
    }

    private void checkAnswer() {
        String userAnswer = answerField.getText().trim().toLowerCase();
        // Add debug print for user input
        System.out.println("DEBUG - User Answer: " + userAnswer);
        System.out.println("DEBUG - Expected Answer: " + currentAnswer);
        
        if (!userAnswer.isEmpty()) {
            if (userAnswer.equals(currentAnswer)) {
                score += 10;
                scoreLabel.setText("Score: " + score);
                JOptionPane.showMessageDialog(this, "Correct!");
                dispose();
            } else {
                wrongAttempts++;
                try {
                    if (wrongAttempts < MAX_ATTEMPTS) {
                        revealRandomLetter();
                        String hint = hints[wrongAttempts - 1];
                        JOptionPane.showMessageDialog(this, 
                            String.format("Incorrect! Here's a hint: %s\n(%d attempts remaining)", 
                            hint, MAX_ATTEMPTS - wrongAttempts));
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "The correct answer was: " + currentAnswer + "\nLet's try a new question!");
                        dispose();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Error accessing hint: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Incorrect! Try again.");
                }
            }
            answerField.setText("");
            answerField.requestFocus();
        }
    }

    @Override
    public void start() {
        // Add retry mechanism
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            if (TopicManager.getInstance().hasValidTopic()) {
                setVisible(true);
                generateQuestion();
                answerField.requestFocus();
                return;
            }
            try {
                Thread.sleep(1000); // Wait 1 second between retries
                retryCount++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // If still no topic after retries
        JOptionPane.showMessageDialog(null, 
            "You found a lankmark! Please talk to an available NPC to select a topic first.");
        dispose();
    }
}
