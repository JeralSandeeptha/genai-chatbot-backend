package com.thyaga.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thyaga.backend.dto.GenAiChatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;

@Service
public class OpenAiService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public OpenAiService(
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void streamChat(GenAiChatRequest request, Consumer<String> onDelta) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is missing (set OPENAI_API_KEY)");
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", resolveModel(request.model()));
        payload.put("stream", true);

        // Responses API: https://api.openai.com/v1/responses
        // We map chat-like messages into `input`.
        ArrayNode input = payload.putArray("input");
        for (GenAiChatRequest.GenAiMessage msg : request.messages()) {
            ObjectNode item = input.addObject();
            item.put("role", normalizeRole(msg.role()));

            ArrayNode content = item.putArray("content");
            String text = msg.content() == null ? "" : msg.content();
            if (!text.isBlank()) {
                ObjectNode textPart = content.addObject();
                textPart.put("type", "input_text");
                textPart.put("text", text);
            }

            if (msg.imageData() != null && !msg.imageData().isBlank()) {
                ObjectNode imgPart = content.addObject();
                imgPart.put("type", "input_image");
                // imageData is typically a data URL from the browser
                imgPart.put("image_url", msg.imageData());
            }
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/responses"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("OpenAI request failed: " + response.statusCode() + " " + body);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            boolean emittedText = false;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (!line.startsWith("data:")) continue;

                String data = line.substring("data:".length()).trim();
                if (data.equals("[DONE]")) {
                    break;
                }

                try {
                    JsonNode event = objectMapper.readTree(data);
                    String delta = extractDelta(event);
                    if (delta != null && !delta.isEmpty()) {
                        onDelta.accept(delta);
                        emittedText = true;
                    } else if (!emittedText && isCompletedEvent(event)) {
                        String completedText = extractTextFromResponse(event.get("response"));
                        if (completedText != null && !completedText.isBlank()) {
                            onDelta.accept(completedText);
                            emittedText = true;
                        }
                    }
                } catch (Exception ignored) {
                    // ignore malformed / non-delta events
                }
            }
        }
    }

    private String normalizeRole(String role) {
        if (role == null) return "user";
        String r = role.trim().toLowerCase(Locale.ROOT);
        if (r.equals("assistant") || r.equals("system") || r.equals("user")) return r;
        // backend stores USER/ASSISTANT sometimes
        if (r.equals("user")) return "user";
        if (r.equals("assistant")) return "assistant";
        if (r.equals("user".toUpperCase(Locale.ROOT))) return "user";
        return r.contains("assist") ? "assistant" : "user";
    }

    private String resolveModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return model;
        }

        String trimmedModel = requestedModel.trim();
        String normalizedModel = trimmedModel.toLowerCase(Locale.ROOT);
        if (normalizedModel.equals("casual_chat") || normalizedModel.equals("component_generator")) {
            return model;
        }

        return trimmedModel;
    }

    /**
     * Best-effort delta extraction for Responses streaming events.
     * Common event types: response.output_text.delta (field: delta)
     */
    private String extractDelta(JsonNode event) {
        JsonNode typeNode = event.get("type");
        if (typeNode == null) return null;

        String type = typeNode.asText("");
        if (type.endsWith(".delta")) {
            JsonNode delta = event.get("delta");
            if (delta != null && delta.isTextual()) return delta.asText();
        }

        // fallback for other possible formats
        JsonNode text = event.get("text");
        if (text != null && text.isTextual()) return text.asText();
        return null;
    }

    private boolean isCompletedEvent(JsonNode event) {
        JsonNode typeNode = event.get("type");
        return typeNode != null && "response.completed".equals(typeNode.asText(""));
    }

    private String extractTextFromResponse(JsonNode response) {
        if (response == null || response.isMissingNode()) return null;

        JsonNode output = response.get("output");
        if (output == null || !output.isArray()) return null;

        StringBuilder text = new StringBuilder();
        for (JsonNode outputItem : output) {
            JsonNode content = outputItem.get("content");
            if (content == null || !content.isArray()) continue;

            for (JsonNode contentItem : content) {
                JsonNode textNode = contentItem.get("text");
                if (textNode != null && textNode.isTextual()) {
                    text.append(textNode.asText());
                }
            }
        }

        return text.isEmpty() ? null : text.toString();
    }
}
