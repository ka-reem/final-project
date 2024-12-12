package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class minigame2 implements Minigame {
    private GroqClient groqClient;
    private ArrayList<String> questions;
    private ArrayList<String[]> options;
    private ArrayList<Integer> answers;
    private int currentQuestion = 0;
    private int score = 0;

    public minigame2() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
    }

    @Override
    public void start() {
        String topic = TopicManager.getInstance().getTopic();
        try {
            generateQuizContent(topic);
            showQuiz();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating quiz: " + e.getMessage());
        }
    }

    private void generateQuizContent(String topic) throws IOException {
        String prompt = String.format(
            "Create a multiple choice quiz about %s with 3 questions. Format as JSON:\n" +
            "{\n" +
            "  \"questions\": [\"Q1\", \"Q2\", \"Q3\"],\n" +
            "  \"options\": [[\"A1\", \"B1\", \"C1\"], [\"A2\", \"B2\", \"C2\"], [\"A3\", \"B3\", \"C3\"]],\n" +
            "  \"correct_answers\": [0, 1, or 2 for each question]\n" +
            "}\n" +
            "Make questions challenging but appropriate for learning %s.",
            topic, topic
        );

        String response = groqClient.generateResponse(prompt);
        parseQuizContent(response);
    }

    private void showQuiz() {
        JFrame frame = new JFrame("Quiz about " + TopicManager.getInstance().getTopic());
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel questionLabel = new JLabel(questions.get(currentQuestion));
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] radioButtons = new JRadioButton[3];
        
        for (int i = 0; i < 3; i++) {
            radioButtons[i] = new JRadioButton(options.get(currentQuestion)[i]);
            group.add(radioButtons[i]);
            mainPanel.add(radioButtons[i]);
        }

        JButton submitButton = new JButton("Submit Answer");
        submitButton.addActionListener(e -> {
            for (int i = 0; i < 3; i++) {
                if (radioButtons[i].isSelected()) {
                    checkAnswer(i, frame);
                    break;
                }
            }
        });

        mainPanel.add(questionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(submitButton);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void checkAnswer(int selectedAnswer, JFrame frame) {
        if (selectedAnswer == answers.get(currentQuestion)) {
            score++;
        }
        
        currentQuestion++;
        if (currentQuestion < questions.size()) {
            frame.dispose();
            showQuiz();
        } else {
            frame.dispose();
            JOptionPane.showMessageDialog(null, 
                String.format("Quiz completed! Score: %d/%d", score, questions.size()));
        }
    }

    private String loadApiKey() {
        // ...existing API key loading code...
        return "your-api-key";
    }
}