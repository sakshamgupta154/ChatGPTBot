package chatgptapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPTApi {

    public static void main(String[] args) {
        System.out.println(chatGPT("hello, how are you?"));
    }

    public static String chatGPT(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "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";
        String model = "gpt-3.5-turbo";

        int maxRetries = 1;
        int retryDelayMillis = 5000; // 5 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Create the HTTP POST request
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                con.setRequestProperty("Content-Type", "application/json");

                // Build the request body
                String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(body);
                writer.flush();
                writer.close();

                // Get the response
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // returns the extracted contents of the response.
                return extractContentFromResponse(response.toString());
            } catch (IOException e) {
            if (attempt < maxRetries) {
                if (isRateLimitExceeded(e)) {
                    // Handle 429 (rate limit exceeded) separately
                    System.out.println("Rate limit exceeded. Waiting before retrying...");
                    try {
                        Thread.sleep(retryDelayMillis * 2); // Wait for a longer duration
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Retry with a regular delay
                    try {
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            } else {
                throw new RuntimeException(e);
            }
        }
        }

        // Return a meaningful value in case of failure
        return "Failed to get a response from ChatGPT";
    }
    private static boolean isRateLimitExceeded(IOException e) {
    if (e instanceof java.io.IOException) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains("HTTP response code: 429");
    }
    return false;
}
    

    // This method extracts the response expected from chatgpt and returns it.
    public static String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content") + 11; // Marker for where the content starts.
        int endMarker = response.indexOf("\"", startMarker); // Marker for where the content ends.
        return response.substring(startMarker, endMarker); // Returns the substring containing only the response.
    }
}
