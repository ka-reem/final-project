import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Properties;
import java.io.FileInputStream;

public class ImageGenerator {
    private static String ACCOUNT_ID;
    private static String API_TOKEN;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
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

    public static void main(String[] args) {
        try {
            // Get prompt from user
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your map description:");
            String prompt = scanner.nextLine();
            
            // Generate and save image
            generateImage(prompt);
            
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void generateImage(String prompt) throws IOException {
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

        // Read and save image
        try (InputStream in = conn.getInputStream()) {
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = "generated-map-" + timestamp + ".png";
            
            // Save the image
            Files.copy(in, new File(filename).toPath());
            System.out.println("Image saved as: " + filename);
        }
    }
}