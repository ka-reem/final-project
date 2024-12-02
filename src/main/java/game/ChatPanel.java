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
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ChatPanel extends JPanel {
    // Update style constants
    private static final Color CHAT_BG_COLOR = Color.WHITE;
    private static final Color CHAT_AREA_BG = Color.WHITE;  // Solid white background
    private static final Color INPUT_BG = Color.WHITE;      // Solid white input
    private static final Color USER_TEXT_COLOR = new Color(0, 102, 204); // Blue for user text
    private static final Color BOT_TEXT_COLOR = Color.BLACK;             // Black for AI text
    private static final Font CHAT_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final int BUBBLE_RADIUS = 15;
    private static final int BUBBLE_PADDING = 10;

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
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pdfReader = new PDFReader();  // Initialize PDFReader
        initializeComponents();
        setupListeners();
        // startChat(); // Commented out - chat will start through NPC interaction instead
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
        chatArea = new JTextPane();
        setupChatArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        
        // Input area setup
        playerInput = new JTextField();
        playerInput.setFont(CHAT_FONT);
        playerInput.setOpaque(true);
        playerInput.setBackground(INPUT_BG);
        playerInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200, 100)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

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
        chatArea.setOpaque(true);
        chatArea.setBackground(CHAT_AREA_BG);
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
            "You are a knowledgeable guide helping someone learn about %s. Current context:\n" +
            "Quest Progress: %d/5 completed\n" +
            "Last location: %s\n" +
            "User input: %s\n\n" +
            "Instructions:\n" +
            "1. Connect learning tasks to desert landmarks\n" +
            "2. Make learning fun and adventurous\n" +
            "3. Give clear directions to next location\n" +
            "4. Include interesting facts about %s with each quest\n" +
            "5. Keep responses under 30 words\n" +
            "6. Use emojis and engaging language" +
            "DO NOT HALLUCINATE, DO NOT MAKE THINGS UP",

            
            learningTopic, // Learning topic takes entire sentence and not a word. Specify this to user. 
            getCompletedQuestCount(),
            getCurrentLocation(),
            userInput,
            learningTopic
        );
    }

    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty()) {
            appendToChat("\nYou: " + input);
            playerInput.setText("");
            
            // Check if this is the first input to set the learning topic
            if (learningTopic == null && !input.startsWith("/")) {
                learningTopic = input;
                try {
                    // Change prompt or only summarize if its long (i.e. if topic = banana, it will summarize banana in 5 words)
                    String summaryPrompt = "Provide a brief 1-5 word summary of " + input + 
                                         " that highlights what we'll be learning about.";
                    String topicSummary = groqClient.generateResponse(summaryPrompt);
                    
                    appendToChat("\nGuide: Great choice! Let's learn about, " + 
                               topicSummary + "\n\nAre you ready to begin this adventure?\n");
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
                        });
                    }

                    @Override
                    public void onComplete() {
                        SwingUtilities.invokeLater(() -> {
                            appendToChat("\n");
                            String response = currentResponse.toString();
                            if (response.toLowerCase().contains("completed") || 
                                response.toLowerCase().contains("well done") ||
                                response.toLowerCase().contains("congratulations")) {
                                questsCompleted[currentQuest] = true;
                                currentQuest++;
                                updateQuestProgress();
                            }
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

    private void updateQuestProgress() {
        int completed = getCompletedQuestCount();
        if (completed == questsCompleted.length) {
            appendToChat("🎉 Congratulations! You've completed all learning quests about " + 
                learningTopic + "! Would you like to review what you've learned?");
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
        g.setColor(CHAT_BG_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}