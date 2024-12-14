package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.imageio.ImageIO;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.text.*;
import javax.sound.sampled.*;
import javazoom.jl.player.Player;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ChatPanel extends JPanel {
    private static final String VENV_PATH = new File("venv").getAbsolutePath();
    private static final String AUDIO_DIR = "audio/";
    private static final String PYTHON_SCRIPT = "/lmnt_tts.py";
    private static final Color CHAT_BG_COLOR = new Color(30, 30, 30, 180); 
    private static final Color CHAT_AREA_BG = new Color(255, 255, 255, 10);
    private static final Color INPUT_BG = new Color(255, 255, 255, 20);
    private static final Color USER_TEXT_COLOR = new Color(255, 255, 255);    // White for user text
    private static final Color BOT_TEXT_COLOR = new Color(255, 255, 255);     // White for bot text
    private static final Font CHAT_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final int BUBBLE_RADIUS = 15;
    private static final int BUBBLE_PADDING = 10;
    private int audioCounter = 0;
    private ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private boolean isMuted = false;

    private GroqClient groqClient;
    private JTextPane chatArea;
    private JTextField playerInput;
    private String gameContext;
    private ArrayList<String> messages;
    private PDFReader pdfReader;  
    private String learningTopic = null; 
    // Track quest progress // Not working
    private int currentQuest = 0;
    private boolean[] questsCompleted = new boolean[5];
    private StringBuilder currentResponse = new StringBuilder();

    public ChatPanel() {
        setOpaque(false); // Make panel transparent
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(0, 0, 0, 0));

        pdfReader = new PDFReader();  // Initialize PDFReader // No longer used
        initializeComponents();
        setupListeners();
        // startChat(); // Commented out - chat will start through NPC interaction instead
        MinigameManager.getInstance().setChatPanel(this);
    }

    private void initializeComponents() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
        
        // Update game context to be topic-neutral
        gameContext = "You are a friendly guide in a desert town. Your role is to help the player learn any topic they choose through an adventure-based journey."; 
        
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 400));
        messages = new ArrayList<>();
        
        // Chat area setup
        chatArea = new JTextPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CHAT_AREA_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        setupChatArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        
        // Input area setup
        playerInput = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        playerInput.setFont(CHAT_FONT);
        playerInput.setOpaque(false);
        playerInput.setBackground(INPUT_BG);
        playerInput.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        playerInput.setForeground(Color.WHITE);

        JButton submitButton = createStyledButton("Submit", new Color(0, 132, 255, 160));
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        inputPanel.add(playerInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        
        // Update upload button text to be more general
        JButton uploadButton = new JButton("Upload Learning Material");
        uploadButton.setOpaque(false);
        uploadButton.setBackground(new Color(255, 255, 255, 160));
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setOpaque(false);
        topPanel.add(uploadButton);
        
        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Make all panels transparent
        for (Component c : getComponents()) {
            if (c instanceof JPanel) {
                ((JPanel)c).setOpaque(false);
            }
        }
    }

    private void setupListeners() {
        playerInput.addActionListener(e -> processPlayerInput());
        for (Component c : ((JPanel)getComponent(1)).getComponents()) {
            if (c instanceof JButton) {
                ((JButton)c).addActionListener(e -> {
                    processPlayerInput();
                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(this);
                        if (window instanceof Game) {
                            ((Game)window).getGameWorld().requestFocusInWindow();
                        }
                    });
                });
            }
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void setupChatArea() {
        chatArea.setOpaque(false);
        chatArea.setBackground(new Color(0, 0, 0, 0));
        chatArea.setEditable(false);
        chatArea.setFont(CHAT_FONT);
        
        // Simple styles without bubbles
        Style userStyle = chatArea.addStyle("UserStyle", null);
        StyleConstants.setForeground(userStyle, USER_TEXT_COLOR);
        StyleConstants.setBold(userStyle, true);
        StyleConstants.setLeftIndent(userStyle, 10f);
        StyleConstants.setSpaceAbove(userStyle, 10f);
        
        Style botStyle = chatArea.addStyle("BotStyle", null);
        StyleConstants.setForeground(botStyle, BOT_TEXT_COLOR);
        StyleConstants.setLeftIndent(botStyle, 10f);
        StyleConstants.setSpaceAbove(botStyle, 10f);
    }

    private String loadApiKey() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
                return ""; 
            }
            props.load(input);
            return props.getProperty("groq.api.key");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /*
    private void startChat() {
        try {
            String startPrompt = gameContext + "\n Ask the user what topic they'd like to learn";
            String response = groqClient.generateResponse(startPrompt);
            appendToChat("Chat Agent: " + response + "\n\nWhat is the right answer?");
        } catch (Exception e) {
            appendToChat("Error starting chat: " + e.getMessage());
        }
    }
    */

    private String buildPrompt(String userInput) {
        if (learningTopic == null) {
            return "You are a friendly guide. The player wants to learn something new. " +
                   "Ask them what topic they'd like to learn about. Be engaging and welcoming.";
        }

        return String.format(
            "You are a mystical desert guide helping travelers uncover the secrets of %s. Current context:\n" +
            "Quest Progress: %d/5 completed\n" +
            "Last location: %s\n" +
            "User input: %s\n\n" +
            "Instructions:\n" +
            "1. Describe magical desert landmarks that represent concepts from %s\n" +
            "2. Hint at mysterious games and challenges that await at each landmark\n" +
            "3. Give directions using desert landmarks (dunes, oases, ancient ruins)\n" +
            "4. Weave interesting facts about %s into the desert adventure narrative\n" +
            "5. Keep responses under 30 words and maintain a sense of mystery and discovery\n" +
            "6. Make each landmark feel unique and connected to what they're learning\n" +
            "DO NOT HALLUCINATE, DO NOT MAKE THINGS UP",
            
            learningTopic,
            getCompletedQuestCount(),
            getCurrentLocation(),
            userInput,
            learningTopic,
            learningTopic
        );
    }

    private String extractLearningTopic(String input) {
        // In the future we can add something to check if user wants to change the learning topic
        try {
            String extractPrompt = "Extract the main learning topic from this text. " +
                "If the text contains phrases like 'I want to learn', 'help me with', 'study for', etc., " +
                "identify the subject matter. Return ONLY the topic without any extra words.\n\n" +
                "Text: \"" + input + "\"";
            
            String extractedTopic = groqClient.generateResponse(extractPrompt).trim();
            return extractedTopic;
        } catch (Exception e) {
            appendToChat("\nError extracting topic: " + e.getMessage());
            return input; // fallback to original input
        }
    }

    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty()) {
            appendToChat("\nYou: " + input);
            playerInput.setText("");
            
            // Check if this is the first input to set the learning topic
            if (learningTopic == null && !input.startsWith("/")) {
                String extractedTopic = extractLearningTopic(input);
                learningTopic = extractedTopic;
                TopicManager.getInstance().setTopic(extractedTopic);
                
                // Generate landmark image for the topic
                try {
                    String imagePath = ImageGenerator.generateLandmarkForTopic();
                    appendToChat("\nGuide: Generated landmark for " + extractedTopic);
                } catch (IOException e) {
                    appendToChat("\nError generating landmark: " + e.getMessage());
                }

                try {
                    String summaryPrompt = "You are helping someone learn about: " + extractedTopic + 
                                         "\nProvide an encouraging response that:" +
                                         "\n1. Confirms the topic" +
                                         "\n2. Shows enthusiasm for teaching it" +
                                         "\n3. Tells them to look around the map for the first landmark" +
                                         "\n4. Reminds them they can talk to you anytime using the toggle chat button in the top left corner or by interacting with you" +
                                         "\n5. Asks if they're ready to begin";
                    
                    String response = groqClient.generateResponse(summaryPrompt);
                    appendToChat("\nGuide: " + response + "\n");
                    generateAndPlaySpeech(response); // Add this line to enable text-to-speech for the summary
                } catch (Exception e) {
                    appendToChat("\nError generating topic summary: " + e.getMessage());
                }
                return;
            }

            try {
                String prompt = buildPrompt(input);
                currentResponse = new StringBuilder();
                appendToChat("\nGuide: "); 
                
                groqClient.generateResponseStreaming(prompt, new StreamingCallback() {
                    @Override
                    public void onToken(String token) {
                        SwingUtilities.invokeLater(() -> {
                            currentResponse.append(token);
                            appendToCurrentResponse(token);
                            // Change generateSpeech to generateAndPlaySpeech
                            if (token.matches(".*[.!?]\\s*")) {
                                String sentence = currentResponse.toString().trim();
                                generateAndPlaySpeech(sentence);
                                currentResponse.setLength(0);
                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        SwingUtilities.invokeLater(() -> {
                            String finalResponse = currentResponse.toString().trim();
                            generateAndPlaySpeech(finalResponse);
                            appendToChat("\n");
                            
                            if (finalResponse.toLowerCase().contains("completed") || 
                                finalResponse.toLowerCase().contains("well done") ||
                                finalResponse.toLowerCase().contains("congratulations")) {
                                questsCompleted[currentQuest] = true;
                                currentQuest++;
                                updateQuestProgress();
                            }
                            currentResponse.setLength(0);
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        SwingUtilities.invokeLater(() -> {
                            appendToChat("\nError: " + t.getMessage());
                        });
                    }
                });
            } catch (Exception e) {
                appendToChat("\nError: " + e.getMessage());
            }
        }
    }

    private void appendToCurrentResponse(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), text, null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void updateQuestProgress() {
        if (MinigameManager.getInstance().areAllMinigamesCompleted()) {
            try {
                String completionPrompt = String.format(
                    "Generate a congratulatory message for someone who just completed learning about %s. The message should:\n" +
                    "1. Congratulate them on completing all games/challenges\n" +
                    "2. Acknowledge their mastery of the concepts\n" +
                    "3. Encourage them to ask questions about what they learned\n" +
                    "4. Mention they can reload the game to learn a new topic\n" +
                    "Keep the tone friendly and enthusiastic!", 
                    learningTopic
                );
                
                String completionMessage = groqClient.generateResponse(completionPrompt);
                appendToChat("\nGuide: " + completionMessage);
                generateAndPlaySpeech(completionMessage);
            } catch (Exception e) {
                // Fallback to default message if Groq fails
                String defaultMessage = String.format(
                    "🎉 Congratulations on completing all the games about %s! " +
                    "You've done an amazing job mastering these concepts through the interactive challenges. " +
                    "Feel free to ask me any questions about what you've learned, " +
                    "or you can reload the game to start learning a new topic!", 
                    learningTopic);
                appendToChat("\nGuide: " + defaultMessage);
                generateAndPlaySpeech(defaultMessage);
            }
        }
    }

    private int getCompletedQuestCount() {
        int count = 0;
        for (boolean quest : questsCompleted) {
            if (quest) count++;
        }
        return count;
    }

    // Replace these with landmarks. Landmarks are still WIP.
    private String getCurrentLocation() {
        switch (currentQuest) {
            case 0: return "Landmark One"; // Says landmark two? 
            // case 0: return "starting point";
            // case 1: return "near the hut";
            // case 2: return "by the water";
            // case 3: return "among cactuses";
            // case 4: return "rock formation";
            default: return "on the path";
        }
    }

    private void appendToChat(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            boolean isUser = text.startsWith("You:");
            Style style = chatArea.getStyle(isUser ? "UserStyle" : "BotStyle");
            
            // Add newlines for spacing
            text = text + "\n\n";
            
            doc.insertString(doc.getLength(), text, style);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String sender, String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String formattedMessage = sender + ": " + message + "\n";
            doc.insertString(doc.getLength(), formattedMessage, null);
            messages.add(formattedMessage);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // If time allows, let user upaload a PDF file to learn from
    // public void uploadPDF() {
    //     JFileChooser fileChooser = new JFileChooser();
    //     fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
    //         public boolean accept(File f) {
    //             return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
    //         }
    //         public String getDescription() {
    //             return "PDF Files (*.pdf)";
    //         }
    //     });

    //     if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
    //         try {
    //             String content = pdfReader.readPDF(fileChooser.getSelectedFile());
    //             gameContext = String.format(
    //                 "You are a knowledgeable guide. Using this learning material: %s\n" +
    //                 "Help the user understand the content through an interactive adventure.\n" +
    //                 "Ask relevant questions and create engaging learning activities.\n" +
    //                 "Keep responses under 30 words.", content);
    //             appendToChat("Guide: I've reviewed your document. Let me help you learn from it!");
    //         } catch (IOException ex) {
    //             appendToChat("Error reading PDF: " + ex.getMessage());
    //         }
    //     }
    // }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill entire background with transparent color
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Create modern blur effect
        g2.setColor(CHAT_BG_COLOR);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        
        // Add subtle border
        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
        
        g2.dispose();
    }

    // Add getter method for learning topic
    // Call this in Minigames
    public String getLearningTopic() {
        return learningTopic;
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            // Stop any currently playing audio
            audioExecutor.shutdownNow();
            audioExecutor = Executors.newSingleThreadExecutor();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    private void generateAndPlaySpeech(String text) {
        if (isMuted) return;
        try {
            // Create audio directory if it doesn't exist
            new File(AUDIO_DIR).mkdirs();
            
            String outputFile = AUDIO_DIR + "speech_" + audioCounter++ + ".mp3";
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            
            // Get the script from resources
            InputStream resourceStream = getClass().getResourceAsStream(PYTHON_SCRIPT);
            if (resourceStream == null) {
                System.err.println("Could not find Python script in resources");
                return;
            }
            
            // Copy script to temp file
            File tempScript = File.createTempFile("lmnt_tts", ".py");
            Files.copy(
                resourceStream,
                tempScript.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
            tempScript.deleteOnExit();
            
            // Use venv python interpreter
            String pythonPath = System.getProperty("os.name").toLowerCase().contains("windows") 
                ? VENV_PATH + "/Scripts/python"
                : VENV_PATH + "/bin/python3";

            ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                tempScript.getAbsolutePath(),
                encodedText,
                new File(outputFile).getAbsolutePath()
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python output: " + line);
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                playMP3(outputFile);
            } else {
                System.err.println("Python script failed with exit code: " + exitCode);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMP3(String filepath) {
        audioExecutor.submit(() -> {
            try (FileInputStream fis = new FileInputStream(filepath)) {
                Player player = new Player(fis);
                player.play();
                // Only delete after playing is complete
                new File(filepath).deleteOnExit();
            } catch (Exception e) {
                System.err.println("Error playing audio: " + filepath);
                e.printStackTrace();
            }
        });
    }

    // Add cleanup on window close - prevent audio from continuing to play even after window is closed
    public void cleanup() {
        audioExecutor.shutdown();
        try {
            if (!audioExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                audioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            audioExecutor.shutdownNow();
        }
    }
}