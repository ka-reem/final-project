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
    // Add new style constants
    private static final Color CHAT_BG_COLOR = new Color(245, 245, 245);
    private static final Color USER_BUBBLE_COLOR = new Color(0, 132, 255, 230);
    private static final Color BOT_BUBBLE_COLOR = new Color(240, 240, 240);
    private static final Color USER_TEXT_COLOR = Color.WHITE;
    private static final Color BOT_TEXT_COLOR = Color.BLACK;
    private static final Font CHAT_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final int BUBBLE_RADIUS = 15;
    private static final int BUBBLE_PADDING = 10;

    private GroqClient groqClient;
    private JTextPane chatArea; // Change to JTextPane for better styling
    private JTextField playerInput;
    private String gameContext;
    private ArrayList<String> messages;
    private TaxData taxData;  // Add this field
    private PDFReader pdfReader;
    // Add fields to track quest progress
    private int currentQuest = 0;
    private boolean[] questsCompleted = new boolean[5];
    private StringBuilder currentResponse = new StringBuilder();

    public ChatPanel() {
        setOpaque(true);
        setBackground(CHAT_BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeComponents();
        setupListeners();
        startChat();
    }

    private void initializeComponents() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
        taxData = new TaxData();
        pdfReader = new PDFReader();
        
        // Initialize game context
        gameContext = "You are a friendly tax guide in a desert town..."; // Your existing context string
        
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 400));
        messages = new ArrayList<>();
        
        // Chat area setup
        chatArea = new JTextPane();
        setupChatArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Input area setup
        playerInput = new JTextField();
        playerInput.setFont(CHAT_FONT);
        JButton submitButton = createStyledButton("Submit", new Color(0, 132, 255));
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(playerInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        
        // Upload button setup
        JButton uploadButton = new JButton("Upload Tax Document");
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(uploadButton);
        
        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
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
        chatArea.setBackground(Color.WHITE);
        chatArea.setEditable(false);
        // Replace JTextArea methods with JTextPane equivalents
        ((StyledDocument)chatArea.getDocument()).putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        chatArea.setFont(CHAT_FONT);
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

    private void startChat() {
        try {
            String startPrompt = gameContext + "\n Ask the user what topic they'd like to learn";
            String response = groqClient.generateResponse(startPrompt);
            appendToChat("Chat Agent: " + response + "\n\nWhat is the right answer?");
        } catch (Exception e) {
            appendToChat("Error starting chat: " + e.getMessage());
        }
    }

    private String buildPrompt(String userInput) {
        return String.format(
            "You are a tax guide in the desert. Current context:\n" +
            "Quest Progress: %d/5 completed\n" +
            "Last location: %s\n" +
            "User input: %s\n\n" +
            "Instructions:\n" +
            "1. Always connect tasks to desert landmarks\n" +
            "2. Make tax learning fun and adventurous\n" +
            "3. Give clear directions to next location\n" +
            "4. Include tax facts with each quest\n" +
            "5. Keep responses under 50 words\n" +
            "6. Use emojis and engaging language",
            getCompletedQuestCount(),
            getCurrentLocation(),
            userInput
        );
    }

    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty()) {
            appendToChat("\nYou: " + input);
            playerInput.setText("");
            
            try {
                String prompt = buildPrompt(input);
                currentResponse = new StringBuilder();
                appendToChat("\nTax Advisor: ");
                
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
            appendToChat("🎉 Congratulations! You've completed all tax learning quests! " +
                "You're now ready to handle your taxes with confidence! Would you like to review what you've learned?");
        }
    }

    private int getCompletedQuestCount() {
        int count = 0;
        for (boolean quest : questsCompleted) {
            if (quest) count++;
        }
        return count;
    }

    private String getCurrentLocation() {
        switch (currentQuest) {
            case 0: return "starting point";
            case 1: return "near the hut";
            case 2: return "by the water";
            case 3: return "among cactuses";
            case 4: return "rock formation";
            default: return "on the path";
        }
    }

    public void uploadPDF() {  // Change from private to public
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
            }
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = pdfReader.readPDF(fileChooser.getSelectedFile());
                gameContext = "You are a tax advisor. Using this tax document: " + content + 
                            "\nAsk relevant questions to help fill out the tax form. Keep responses under 50 words.";
                appendToChat("Tax Advisor: I've reviewed your tax document. Let me help you fill out the form.");
            } catch (IOException ex) {
                appendToChat("Error reading PDF: " + ex.getMessage());
            }
        }
    }

    private void appendToChat(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), text + "\n", null);
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
}