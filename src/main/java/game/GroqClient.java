package game;

import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.BufferedReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GroqClient {
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final OkHttpClient client;
    private final String apiKey; 
    private final Gson gson;

    public GroqClient(String apiKey) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.gson = new Gson();
    }

    public String generateResponse(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        // requestBody.addProperty("model", "llama3-70b-8192"); 
        requestBody.addProperty("model", "llama-3.3-70b-versatile");  

        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);
    
        // Build the request
        RequestBody body = RequestBody.create(
            gson.toJson(requestBody),
            MediaType.parse("application/json; charset=utf-8")
        );
    
        Request request = new Request.Builder()
            .url(GROQ_API_URL)
            .post(body)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .build();
    
        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                System.err.println("Error response: " + errorBody);
                throw new IOException("Request failed with code " + response.code() + ": " + errorBody);
            }
    
            // Parse JSON
            String jsonResponse = response.body().string();
            System.out.println("Raw response: " + jsonResponse); 
            return extractContentFromResponse(jsonResponse);
        }
    }
    
    private String extractContentFromResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray choices = jsonObject.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message");
                return message.get("content").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error processing response";
    }

    public void generateResponseStreaming(String prompt, StreamingCallback callback) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "llama3-70b-8192");
        requestBody.addProperty("stream", true);
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);

        RequestBody body = RequestBody.create(
            gson.toJson(requestBody),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(GROQ_API_URL)
            .post(body)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("Request failed: " + response.code()));
                        return;
                    }

                    BufferedReader reader = new BufferedReader(responseBody.charStream());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String json = line.substring(6);
                            if (json.equals("[DONE]")) {
                                callback.onComplete();
                                continue;
                            }
                            JsonObject chunk = gson.fromJson(json, JsonObject.class);
                            JsonArray choices = chunk.getAsJsonArray("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonObject choice = choices.get(0).getAsJsonObject();
                                JsonObject delta = choice.getAsJsonObject("delta");
                                if (delta.has("content")) {
                                    String content = delta.get("content").getAsString();
                                    callback.onToken(content);
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}