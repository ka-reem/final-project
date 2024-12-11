package game;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import org.json.JSONObject;


public class HeyGen {
    private final String API_KEY;
    // private final String BASE_URL = "https://api.heygen.com/api/v1";   // Updated to v2
    private final String BASE_URL = "https://api.heygen.com/v2/video/generate";
    private final HttpClient client;

    public HeyGen(String apiKey) {
        this.API_KEY = apiKey;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    public String generateVideo(String text) throws Exception {
        // Verify API key is set
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("HeyGen API key not configured. Please check your config.properties file.");
        }

        // Create video generation request
        JSONObject requestBody = new JSONObject();
        
        // Updated request structure based on v2 API
        JSONObject video = new JSONObject();
        video.put("text", text);
        video.put("avatar_id", "nik_expressive_20240910"); // nik
        video.put("voice_id", "1985984feded457b9d013b4f6551ac94"); // Tarquin
        
        requestBody.put("video", video);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/videos/generate"))
            .header("X-Api-Key", API_KEY)  // Updated header name
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        try {
            // Send request and get video ID
            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new RuntimeException("API endpoint not found. Please check if the API URL is correct.");
            } else if (response.statusCode() == 401) {
                throw new RuntimeException("Invalid API key. Please check your API key in config.properties.");
            } else if (response.statusCode() != 200) {
                throw new RuntimeException("API error (Status " + response.statusCode() + "): " + response.body());
            }

            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getString("video_id");
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to HeyGen API: " + e.getMessage());
        }
    }

    public String getVideoStatus(String videoId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/videos/" + videoId))
            .header("X-Api-Key", API_KEY)  // Updated header name
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get video status: " + response.body());
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getString("status");
    }
}