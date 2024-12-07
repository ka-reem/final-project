package game;

import javax.swing.*;

import game.GroqClient;

import java.awt.*;
import java.awt.event.*;

import game.GroqClient;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Game extends JFrame {
    private GameWorld gameWorld;
    private ChatPanel chatPanel;
    private boolean isChatVisible = false;
    private JLayeredPane layeredPane;

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
    
    public Game() {
        // Change the name of the game
        this.setTitle("Taxplorer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Enable resizing
        this.setResizable(true);
        
        // Added keyboard input
        this.setFocusable(true);
        this.requestFocus();
        
        // Create and setup GameWorld
        gameWorld = new GameWorld(this);
        gameWorld.setPreferredSize(new Dimension(GameConstants.GAME_SCREEN_WIDTH, 
                                               GameConstants.GAME_SCREEN_HEIGHT));
        gameWorld.InitializeGame();
        
        // Create and add chat panel
        chatPanel = new ChatPanel();
        chatPanel.setVisible(false);
        chatPanel.setPreferredSize(new Dimension(200, GameConstants.GAME_SCREEN_HEIGHT));
        
        // Set initial window size before adding components
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)(screenSize.width * 0.8);  // 80% of screen width
        int height = (int)(screenSize.height * 0.8); // 80% of screen height
        setPreferredSize(new Dimension(width, height));
        
        // Layout setup
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
        
        // Create toolbar panel
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        JButton backButton = new JButton("Back to Menu");
        JButton toggleChatButton = new JButton("Toggle Chat");
        
        backButton.addActionListener(e -> returnToMenu());
        toggleChatButton.addActionListener(e -> toggleChat());
        
        toolbar.add(backButton);
        toolbar.add(toggleChatButton);
        
        // Initialize component bounds with proper sizes
        gameWorld.setBounds(0, 0, width, height);
        toolbar.setBounds(0, 0, width, 40);
        chatPanel.setBounds(width - 320, 50, 300, height - 100);
        
        // Add components to layered pane with proper z-order
        layeredPane.add(gameWorld, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(toolbar, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(chatPanel, JLayeredPane.PALETTE_LAYER);
        
        // Pack and center the window
        this.pack();
        this.setLocationRelativeTo(null); // Center the window
        
        // Add resize listener
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getContentPane().getWidth();
                int height = getContentPane().getHeight();
                
                gameWorld.setBounds(0, 0, width, height);
                toolbar.setBounds(0, 0, width, 40);
                chatPanel.setBounds(width - 320, 50, 300, height - 100);
                
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
        
        // Start the game thread
        Thread thread = new Thread(gameWorld);
        thread.start();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        if (layeredPane != null) {
            layeredPane.setBounds(0, 0, width, height);
        }
    }

    private void toggleChat() {
        isChatVisible = !isChatVisible;
        chatPanel.setVisible(isChatVisible);
        
        // Reset player movement when toggling chat
        gameWorld.resetPlayerMovement();
        
        // Request focus back to game when chat is closed
        if (!isChatVisible) {
            this.requestFocus();
            gameWorld.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    // Quest like interaction

    public void showNPCChat(String message) {
        if (!isChatVisible) {
            toggleChat();
        }
        chatPanel.addMessage("NPC", message);
        // Reset movement and keep game controls active
        gameWorld.resetPlayerMovement();
        gameWorld.requestFocusInWindow();
    }

    private void returnToMenu() {
        this.dispose();  
        GameMenu menu = new GameMenu();
        menu.display();
    }

    // Add getter for gameWorld
    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setVisible(true);
    } 
}