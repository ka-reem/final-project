package game;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class ImageGenerator {
    private static String ACCOUNT_ID;
    private static String API_TOKEN;
    private static final String RESOURCES_PATH = "src/main/resources/";

    static {
        try {
            Properties props = new Properties();
            String configPath = RESOURCES_PATH + "config.properties";
            if (!Files.exists(Paths.get(configPath))) {
                throw new RuntimeException("config.properties not found in resources directory");
            }
            props.load(new FileInputStream(configPath));
            ACCOUNT_ID = props.getProperty("ACCOUNT_ID");
            API_TOKEN = props.getProperty("API_TOKEN");
            
            if (ACCOUNT_ID == null || API_TOKEN == null) {
                throw new RuntimeException("Missing required properties in config.properties");
            }
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
            System.exit(1);
        }
    }

    public static String generateLandmarkForTopic() throws IOException {
        TopicManager topicManager = TopicManager.getInstance();
        if (!topicManager.hasValidTopic()) {
            throw new IllegalStateException("No topic set in TopicManager");
        }
        
        String topic = topicManager.getTopic();
        String prompt = "Picture of " + topic;
        
        String filename = "landmark.png";
        System.out.println("Generating landmark image for topic: " + topic);
        String fullPath = generateImage(prompt, filename);
        Landmark.updateLandmark(fullPath);
        System.out.println("Landmark image generation and update completed");
        return filename;
    }

    private static String generateImage(String prompt, String filename) throws IOException {
        // Delete existing file if it exists
        File outputFile = new File(RESOURCES_PATH + filename);
        if (outputFile.exists()) {
            outputFile.delete();
        }

        String url = "https://api.cloudflare.com/client/v4/accounts/" + ACCOUNT_ID + 
                    "/ai/run/@cf/bytedance/stable-diffusion-xl-lightning";

        // Setup connection
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        
        // Match exact format from working Node.js code
        conn.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        System.out.println("\nUsing same headers as Node.js:");
        System.out.println("Authorization: Bearer " + API_TOKEN);
        System.out.println("Content-Type: application/json");

        // Send request with proper JSON formatting
        String jsonInput = "{\"prompt\":\"" + prompt.replace("\"", "\\\"") + "\"}";

        // Add more detailed error handling
        System.out.println("Sending request to: " + url);
        System.out.println("With data: " + jsonInput);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Print more detailed error information
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        
        if (responseCode != 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                String errorResponse = br.lines().reduce("", (a, b) -> a + b);
                System.out.println("Error Response: " + errorResponse);
                throw new IOException("API Error: " + responseCode + " - " + errorResponse);
            }
        }

        // Modified image saving code
        try (InputStream in = conn.getInputStream()) {
            String resourcePath = "src/main/resources/" + filename;
            File resourceFile = new File(resourcePath);
            
            // Ensure parent directories exist
            resourceFile.getParentFile().mkdirs();
            
            // Save directly to resources
            try (FileOutputStream fos = new FileOutputStream(resourceFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            System.out.println("Successfully saved landmark image to: " + resourcePath);
            return resourcePath;
        }
    }
}