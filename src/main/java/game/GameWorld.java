package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.net.URL;  
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

public class GameWorld extends JPanel implements Runnable {
    // Add static instance
    private static GameWorld instance;

    private BufferedImage world;
    private BufferedImage mapBackground; 
    private Player t1;
    private final JFrame frame;  
    private long tick = 0;
    private NPC npc1;  
    private NPC npc2;  
    private Landmark landmark1;
    private Landmark landmark2;
    private Landmark landmark3;
    private Landmark landmark4;
    private Game game;  
    private boolean isNearNPC1 = false;  
    private boolean isNearNPC2 = false;
    private boolean isNearLandmark1 = false;  
    private boolean isNearLandmark2 = false;
    private boolean isNearLandmark3 = false;
    private boolean isNearLandmark4 = false;
    private boolean firstNPCInteraction = true; 

    // Add viewport tracking
    private int viewportX = 0;
    private int viewportY = 0;
    private final int SCROLL_SPEED = 5;
    private Rectangle viewport;
    private static final float ZOOM_LEVEL = 1.0f;  // Changed from 0.75f to 1.0f
    private static final int MAP_SCALE = 2;  


    private double scaleX = 1.0;
    private double scaleY = 1.0;

    public GameWorld(Game game) {
        instance = this; // Set instance in constructor
        this.game = game;
        this.frame = game;
        viewport = new Rectangle(0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);
        
        // Add key bindings for scrolling - not needed for now
        addKeyBindings();

        setFocusable(true);
        requestFocusInWindow();
    }

    private void addKeyBindings() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("LEFT"), "scrollLeft");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("RIGHT"), "scrollRight");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("UP"), "scrollUp");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("DOWN"), "scrollDown");

        this.getActionMap().put("scrollLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(-SCROLL_SPEED, 0);
            }
        });
        this.getActionMap().put("scrollRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(SCROLL_SPEED, 0);
            }
        });
        this.getActionMap().put("scrollUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(0, -SCROLL_SPEED);
            }
        });
        this.getActionMap().put("scrollDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(0, SCROLL_SPEED);
            }
        });
    }

    private void scrollViewport(int dx, int dy) {
        if (mapBackground != null) {
            viewportX = Math.max(0, Math.min(viewportX + dx, mapBackground.getWidth() - viewport.width));
            viewportY = Math.max(0, Math.min(viewportY + dy, mapBackground.getHeight() - viewport.height));
            repaint();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.tick++;
                this.t1.update(); // update tank
                
                // Check NPCs and landmarks interaction
                boolean nearNPC1 = npc1.isPlayerInRange(t1);
                boolean nearNPC2 = npc2.isPlayerInRange(t1);
                boolean nearLandmark1 = landmark1.isPlayerInRange(t1);
                boolean nearLandmark2 = landmark2.isPlayerInRange(t1);
                boolean nearLandmark3 = landmark3.isPlayerInRange(t1);
                boolean nearLandmark4 = landmark4.isPlayerInRange(t1);

                // Handle NPC interactions
                // Prevents objects from infinitely detecting interaction
                // Handle NPC1 interaction
                if (nearNPC1 != isNearNPC1) {
                    isNearNPC1 = nearNPC1;
                    if (isNearNPC1) {
                        if (firstNPCInteraction) {
                            game.showNPCChat("Hello! Write a topic to learn about.");
                            firstNPCInteraction = false;
                        } else {
                            game.showNPCChat("Hello!");
                        }
                    }
                }
                
                // Handle NPC2 interaction
                if (nearNPC2 != isNearNPC2) {
                    isNearNPC2 = nearNPC2;
                    if (isNearNPC2) {
                        if (firstNPCInteraction) {
                            game.showNPCChat("Hello! Write a topic to learn about.");
                            firstNPCInteraction = false;
                        } else {
                            game.showNPCChat("Hello!"); 
                        }
                    }
                }
                
                // Handle all landmark interactions
                if (nearLandmark1 != isNearLandmark1) {
                    isNearLandmark1 = nearLandmark1;
                    if (isNearLandmark1) landmark1.activateMinigame(t1);
                }
                
                if (nearLandmark2 != isNearLandmark2) {
                    isNearLandmark2 = nearLandmark2;
                    if (isNearLandmark2) landmark2.activateMinigame(t1);
                }
                
                if (nearLandmark3 != isNearLandmark3) {
                    isNearLandmark3 = nearLandmark3;
                    if (isNearLandmark3) landmark3.activateMinigame(t1);
                }
                
                if (nearLandmark4 != isNearLandmark4) {
                    isNearLandmark4 = nearLandmark4;
                    if (isNearLandmark4) landmark4.activateMinigame(t1);
                }
                
                // Update viewport to follow player
                updateViewportPosition();
                
                this.repaint();   // redraw game
                /*
                 * Sleep for 1000/144 ms (~6.9ms). This is done to have our 
                 * loop run at a fixed rate per/sec. 
                 * Redo for efficiency?
                */
                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {
            System.out.println(ignored);
        }
    }

    public void resetGame() {
        this.tick = 0;
        this.t1.setX(300);
        this.t1.setY(300);
    }

    public void InitializeGame() {
        this.world = new BufferedImage(GameConstants.GAME_SCREEN_WIDTH,
                GameConstants.GAME_SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        // Debug map loading
        try {
            System.out.println("Looking for map.png in resources...");
            URL mapUrl = getClass().getClassLoader().getResource("map.png");
            System.out.println("Map URL: " + mapUrl);
            
            if (mapUrl != null) {
                BufferedImage originalMap = ImageIO.read(mapUrl);
                
                // Scale map to fit screen exactly
                int scaledWidth = GameConstants.GAME_SCREEN_WIDTH * MAP_SCALE;
                int scaledHeight = GameConstants.GAME_SCREEN_HEIGHT * MAP_SCALE;
                
                mapBackground = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = mapBackground.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(originalMap, 0, 0, scaledWidth, scaledHeight, null);
                g2d.dispose();
                
                System.out.println("Map scaled to: " + scaledWidth + "x" + scaledHeight);
            } else {
                System.err.println("ERROR: map.png not found in resources!");
                // Create a default background with grid - temporary
                mapBackground = createDefaultBackground();
            }
        } catch (IOException e) {
            System.err.println("Error loading map.png: " + e.getMessage());
            e.printStackTrace();
            mapBackground = createDefaultBackground();
        }

        BufferedImage t1img = null;
        try {
            System.out.println("Attempting to load tank1.png...");
            URL resourceUrl = GameWorld.class.getClassLoader().getResource("tank1.png");
            System.out.println("Resource URL: " + resourceUrl);
            BufferedImage originalImg = ImageIO.read(Objects.requireNonNull(resourceUrl));
            
            // Change sprite size to 40x40 (smaller than before)
            t1img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = t1img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImg, 0, 0, 40, 40, null);
            g2d.dispose();
            
        } catch (Exception ex) {
            System.out.println("Error loading tank1.png: " + ex.getMessage());
            ex.printStackTrace();
            // Default smaller image if loading fails
            t1img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        }

        // Center the tank by accounting for its size
        float startX = (GameConstants.GAME_SCREEN_WIDTH - t1img.getWidth()) / 2f;
        float startY = (GameConstants.GAME_SCREEN_HEIGHT - t1img.getHeight()) / 2f;
        
        // Create images for game objects first
        BufferedImage npc1Img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = npc1Img.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillOval(0, 0, 20, 20);
        g2d.dispose();
        
        BufferedImage npc2Img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        g2d = npc2Img.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillOval(0, 0, 20, 20);
        g2d.dispose();
        
        BufferedImage landmark1Img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        g2d = landmark1Img.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(0, 0, 30, 30);
        g2d.dispose();

        // Then create game objects
        t1 = new Player(startX, startY, t1img);
        npc1 = new NPC(GameConstants.GAME_SCREEN_WIDTH / 4f, GameConstants.GAME_SCREEN_HEIGHT / 3f, npc1Img, "Hello!");
        npc2 = new NPC(GameConstants.GAME_SCREEN_WIDTH * 3f / 4f, GameConstants.GAME_SCREEN_HEIGHT * 2f / 3f, npc2Img, "Welcome!");

        // Replace the old landmark creation code with the new method
        createLandmarks();

        // Setup controls
        PlayerControl tc1 = new PlayerControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        this.frame.addKeyListener(tc1);
        this.addKeyListener(tc1);
    }

    private BufferedImage createDefaultBackground() {
        BufferedImage defaultBg = new BufferedImage(
            GameConstants.GAME_SCREEN_WIDTH,
            GameConstants.GAME_SCREEN_HEIGHT,
            BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = defaultBg.createGraphics();
        // Draw a grid pattern
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, defaultBg.getWidth(), defaultBg.getHeight());
        g2d.setColor(Color.GRAY);
        for (int x = 0; x < defaultBg.getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, defaultBg.getHeight());
        }
        for (int y = 0; y < defaultBg.getHeight(); y += 50) {
            g2d.drawLine(0, y, defaultBg.getWidth(), y);
        }
        g2d.dispose();
        return defaultBg;
    }

    private BufferedImage createLandmarkImage(Color color) {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillOval(0, 0, 30, 30);
        g2d.dispose();
        return img;
    }

    private void createLandmarks() {
        // Create different colored landmarks
        BufferedImage landmarkImg1 = createLandmarkImage(Color.YELLOW);
        BufferedImage landmarkImg2 = createLandmarkImage(Color.GREEN);
        BufferedImage landmarkImg3 = createLandmarkImage(Color.BLUE);
        BufferedImage landmarkImg4 = createLandmarkImage(Color.RED);

        // Get map dimensions for random positioning
        int mapWidth = mapBackground.getWidth();
        int mapHeight = mapBackground.getHeight();
        
        // Add padding to keep landmarks away from edges
        int padding = 100;
        
        // Create landmarks with random positions
        landmark1 = new Landmark(
            padding + (float)(Math.random() * (mapWidth - 2*padding)),
            padding + (float)(Math.random() * (mapHeight - 2*padding)),
            landmarkImg1, new minigame1()
        );
        
        landmark2 = new Landmark(
            padding + (float)(Math.random() * (mapWidth - 2*padding)),
            padding + (float)(Math.random() * (mapHeight - 2*padding)),
            landmarkImg2, new minigame2()
        );
        
        landmark3 = new Landmark(
            padding + (float)(Math.random() * (mapWidth - 2*padding)),
            padding + (float)(Math.random() * (mapHeight - 2*padding)),
            landmarkImg3, new minigame3()
        );
        
        landmark4 = new Landmark(
            padding + (float)(Math.random() * (mapWidth - 2*padding)),
            padding + (float)(Math.random() * (mapHeight - 2*padding)),
            landmarkImg4, new minigame4()
        );
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();

        // Calculate scale factors based on current panel size
        scaleX = getWidth() / (double)GameConstants.GAME_SCREEN_WIDTH;
        scaleY = getHeight() / (double)GameConstants.GAME_SCREEN_HEIGHT;

        // Enable better rendering
        buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        buffer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clear the background first
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, world.getWidth(), world.getHeight());
        
        // Scale the graphics context
        g2.scale(scaleX, scaleY);
        
        // Draw visible portion of map
        if (mapBackground != null) {
            buffer.drawImage(mapBackground, 
                           0, 0,                                    // Destination coordinates
                           GameConstants.GAME_SCREEN_WIDTH,         // Destination width
                           GameConstants.GAME_SCREEN_HEIGHT,        // Destination height
                           viewportX, viewportY,                    // Source coordinates
                           viewportX + GameConstants.GAME_SCREEN_WIDTH,  // Source width
                           viewportY + GameConstants.GAME_SCREEN_HEIGHT, // Source height
                           null);
        }

        // Adjust game object positions relative to viewport
        AffineTransform old = buffer.getTransform();
        buffer.translate(-viewportX, -viewportY);
        
        // Draw game objects
        this.t1.draw((Graphics2D)buffer);
        this.npc1.draw((Graphics2D)buffer);
        this.npc2.draw((Graphics2D)buffer);
        this.landmark1.draw((Graphics2D)buffer);
        this.landmark2.draw((Graphics2D)buffer);
        this.landmark3.draw((Graphics2D)buffer);
        this.landmark4.draw((Graphics2D)buffer);
        
        // After drawing all game objects, draw debug hitboxes with viewport adjustment
        if (t1 != null) {
            Graphics2D g2d = (Graphics2D) buffer;
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1.0f));
            Rectangle hitbox = t1.getHitbox();
            g2d.drawRect((int)t1.getX(), (int)t1.getY(), hitbox.width, hitbox.height);
        }

        buffer.setTransform(old);
        buffer.dispose();
        
        // Draw scaled world to screen
        g2.drawImage(world, 0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT, null);
    }

    // Update player movement to account for scrolling
    public void updatePlayerPosition(Player player) {
        // Remove viewport bounds checking to allow free movement
        player.setPosition(player.getX(), player.getY());
        
        // Update viewport to follow player
        if (player.getX() < viewportX + 100) scrollViewport(-SCROLL_SPEED, 0);
        if (player.getX() > viewportX + viewport.width - 100) scrollViewport(SCROLL_SPEED, 0);
        if (player.getY() < viewportY + 100) scrollViewport(0, -SCROLL_SPEED);
        if (player.getY() > viewportY + viewport.height - 100) scrollViewport(0, SCROLL_SPEED);
    }

    private void updateViewportPosition() {
        // Center viewport on player with bounds checking
        int targetX = (int)t1.getX() - (GameConstants.GAME_SCREEN_WIDTH / 2);
        int targetY = (int)t1.getY() - (GameConstants.GAME_SCREEN_HEIGHT / 2);
        
        // Smooth scrolling with bounds checking
        viewportX = (int)Math.max(0, Math.min(targetX, mapBackground.getWidth() - GameConstants.GAME_SCREEN_WIDTH));
        viewportY = (int)Math.max(0, Math.min(targetY, mapBackground.getHeight() - GameConstants.GAME_SCREEN_HEIGHT));
    }

    public void resetPlayerMovement() {
        if (t1 != null) {
            t1.resetMovement();
        }
    }

    // Update mouse/interaction coordinates to account for scaling
    public Point getScaledPoint(Point original) {
        return new Point(
            (int)(original.x / scaleX),
            (int)(original.y / scaleY)
        );
    }

    public void reloadGameObjects() {
        // Force reload of all landmarks
        if (landmark1 != null) landmark1.forceReload();
        if (landmark2 != null) landmark2.forceReload();
        if (landmark3 != null) landmark3.forceReload();
        if (landmark4 != null) landmark4.forceReload();
        
        // Request repaint
        repaint(); // Use this.repaint() since GameWorld extends JPanel
    }
    
    // Make this accessible to Landmark
    public static GameWorld getInstance() {
        return instance;
    }
}
