package game;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class HeyGen {
    private final String API_KEY;
    private final String BASE_URL = "https://api.heygen.com/v2";
    private final HttpClient client;
    private final Gson gson = new Gson();

    public HeyGen(String apiKey) {
        this.API_KEY = apiKey;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    public String generateVideo(String text) throws Exception {
        System.out.println("Starting video generation...");
        
        JsonObject requestBody = new JsonObject();
        JsonObject videoInput = new JsonObject();
        
        // Set up character with Nik avatar
        JsonObject character = new JsonObject();
        character.addProperty("type", "avatar");
        character.addProperty("avatar_id", "nik_expressive_20240910");
        character.addProperty("avatar_style", "normal");
        
        // Set up voice with Tarquin voice
        JsonObject voice = new JsonObject();
        voice.addProperty("type", "text");
        voice.addProperty("input_text", text);
        voice.addProperty("voice_id", "1985984feded457b9d013b4f6551ac94");
        
        // Set up background
        JsonObject background = new JsonObject();
        background.addProperty("type", "color");
        background.addProperty("value", "#008000");
        
        // Put it all together
        videoInput.add("character", character);
        videoInput.add("voice", voice);
        videoInput.add("background", background);
        
        // Add dimension
        JsonObject dimension = new JsonObject();
        dimension.addProperty("width", 1280);
        dimension.addProperty("height", 720);
        
        JsonObject[] videoInputs = new JsonObject[]{videoInput};
        requestBody.add("video_inputs", gson.toJsonTree(videoInputs));
        requestBody.add("dimension", dimension);

        System.out.println("Request body: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/video/generate"))
            .header("X-Api-Key", API_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        System.out.println("Sending request to: " + BASE_URL + "/video/generate");

        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());

        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        
        if (responseJson.get("video_id") == null) {
            if (responseJson.has("data") && responseJson.getAsJsonObject("data").has("video_id")) {
                return responseJson.getAsJsonObject("data").get("video_id").getAsString();
            }
            throw new Exception("No video ID in response: " + response.body());
        }

        return responseJson.get("video_id").getAsString();
    }

    public String getVideoStatus(String videoId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.heygen.com/v1/video_status.get?video_id=" + videoId))
            .header("X-Api-Key", API_KEY)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());

        System.out.println("Video status response: " + response.body());

        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        if (responseJson.has("data")) {
            JsonObject data = responseJson.getAsJsonObject("data");
            if (data.has("error") && !data.get("error").isJsonNull()) {
                return "failed";
            }
            return data.get("status").getAsString();
        }
        throw new Exception("Invalid response format: " + response.body());
    }

    public String getVideoUrl(String videoId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.heygen.com/v1/video_status.get?video_id=" + videoId))
            .header("X-Api-Key", API_KEY)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, 
            HttpResponse.BodyHandlers.ofString());

        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        if (responseJson.has("data")) {
            JsonObject data = responseJson.getAsJsonObject("data");
            if (data.has("video_url") && !data.get("video_url").isJsonNull()) {
                return data.get("video_url").getAsString();
            }
            throw new Exception("Video URL not available yet");
        }
        throw new Exception("Video URL not found in response: " + response.body());
    }
}