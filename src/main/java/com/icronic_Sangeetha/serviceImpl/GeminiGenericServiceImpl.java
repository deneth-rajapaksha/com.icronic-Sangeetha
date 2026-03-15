package com.icronic_Sangeetha.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icronic_Sangeetha.service.GenericGeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiGenericServiceImpl implements GenericGeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${google.gemini.model}")
    private String modelNames;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T generateContent(String prompt, Class<T> responseType) {
        String[] models = modelNames.split(",");

        for (String model : models) {
            model = model.trim();
            if (model.isEmpty())
                continue;

            try {
                String url = String.format(
                        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                        model, apiKey);

                // Build request body
                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", prompt)))));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String jsonResponse = extractTextFromResponse(response.getBody());
                    jsonResponse = cleanJsonResponse(jsonResponse);
                    return objectMapper.readValue(jsonResponse, responseType);
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    log.warn("Rate limit exceeded for model: {}. Trying next model...", model);
                    continue; // Try next model
                }
                log.error("HTTP error with model {}: {}", model, e.getMessage());
                throw new RuntimeException("AI service error: " + e.getMessage());
            } catch (Exception e) {
                log.error("Error with model {}: {}", model, e.getMessage());
                throw new RuntimeException("Failed to process AI response: " + e.getMessage());
            }
        }

        throw new RuntimeException("All AI models are currently unavailable. Please try again later.");
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            var root = objectMapper.readTree(responseBody);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from AI response", e);
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null)
            return response;

        // Remove Markdown code block formatting (```json ... ```)
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
}
