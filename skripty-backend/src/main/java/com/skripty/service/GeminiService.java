package com.skripty.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripty.dto.gemini.GeneratedStudyMaterial;
import com.skripty.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Talks to the Google Gemini API to turn raw study material into a summary, a
 * set of test questions and a set of flashcards, all in one call, by asking
 * Gemini to return structured JSON (responseMimeType: application/json).
 */
@Slf4j
@Service
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String fallbackModel;

    private static final int MAX_ATTEMPTS_PER_MODEL = 3;
    private static final long[] BACKOFF_MS = {2_000, 5_000};
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 500, 503, 504);

    public GeminiService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${skripty.gemini.base-url}") String baseUrl,
            @Value("${skripty.gemini.api-key}") String apiKey,
            @Value("${skripty.gemini.model}") String model,
            @Value("${skripty.gemini.fallback-model:}") String fallbackModel
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
    }

    /**
     * Calls Gemini and retries automatically when Google is temporarily
     * overloaded (429 / 500 / 503 / 504). If the main model keeps failing, it
     * tries the fallback model before giving up.
     */
    public GeneratedStudyMaterial generateStudyMaterial(String sourceText) {
        Map<String, Object> requestBody = buildRequestBody(buildPrompt(sourceText));

        for (String currentModel : modelsToTry()) {
            for (int attempt = 1; attempt <= MAX_ATTEMPTS_PER_MODEL; attempt++) {
                try {
                    return callModel(currentModel, requestBody);
                } catch (ContentBlockedException e) {
                    log.error("Gemini {} refused this material: {}", currentModel, e.getMessage());
                    throw e; // no retries, no fallback: the content itself is the problem

                } catch (WebClientResponseException e) {
                    int status = e.getStatusCode().value();
                    boolean temporary = RETRYABLE_STATUSES.contains(status);
                    log.warn("Gemini {} attempt {}/{} failed: {} - {}", currentModel, attempt,
                            MAX_ATTEMPTS_PER_MODEL, status, e.getResponseBodyAsString());

                    if (!temporary) {
                        // 400 bad request, 403 bad key, 404 unknown model: retrying won't help
                        break;
                    }
                    if (attempt < MAX_ATTEMPTS_PER_MODEL) {
                        sleep(BACKOFF_MS[attempt - 1]);
                    }

                } catch (Exception e) {
                    // Timeout, network problem, or JSON we couldn't parse
                    log.warn("Gemini {} attempt {}/{} failed: {}", currentModel, attempt,
                            MAX_ATTEMPTS_PER_MODEL, e.toString());
                    if (attempt < MAX_ATTEMPTS_PER_MODEL) {
                        sleep(BACKOFF_MS[attempt - 1]);
                    }
                }
            }
        }

        log.error("All Gemini attempts failed (models tried: {})", modelsToTry());
        throw new ApiException(HttpStatus.BAD_GATEWAY,
                "AI generation failed. Please try again in a moment.");
    }

    private GeneratedStudyMaterial callModel(String modelName, Map<String, Object> requestBody) throws Exception {
        String path = "/models/%s:generateContent?key=%s".formatted(modelName, apiKey);

        JsonNode response = webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        String generatedJson = extractGeneratedText(response);
        return objectMapper.readValue(generatedJson, GeneratedStudyMaterial.class);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{Map.of("text", prompt)})
                },
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "temperature", 0.4
                )
        );
    }

    private List<String> modelsToTry() {
        if (fallbackModel == null || fallbackModel.isBlank() || fallbackModel.equals(model)) {
            return List.of(model);
        }
        return List.of(model, fallbackModel);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractGeneratedText(JsonNode response) {
        // Response shape: { candidates: [ { content: { parts: [ { text: "...json..." } ] }, finishReason } ],
        //                   promptFeedback: { blockReason } }
        String blockReason = response.path("promptFeedback").path("blockReason").asText("");
        if (!blockReason.isBlank()) {
            throw new ContentBlockedException("Prompt blocked by Gemini: " + blockReason);
        }

        JsonNode candidate = response.path("candidates").path(0);
        String finishReason = candidate.path("finishReason").asText("");
        JsonNode textNode = candidate.path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            if (Set.of("SAFETY", "RECITATION", "PROHIBITED_CONTENT", "BLOCKLIST", "SPII").contains(finishReason)) {
                throw new ContentBlockedException("Response blocked by Gemini: " + finishReason);
            }
            log.warn("Gemini returned no text. Full response: {}", response);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI returned an empty response.");
        }
        return textNode.asText();
    }

    /**
     * Gemini refused this content; retrying the same text won't help.
     */
    static class ContentBlockedException extends RuntimeException {

        ContentBlockedException(String message) {
            super(message);
        }
    }

    private String buildPrompt(String sourceText) {
        return """
            You are an assistant that helps students study. Given the study material below,
            produce a JSON object with exactly these fields:

            - "summary": a concise, well-organized study summary of the material (plain text,
              use short paragraphs or simple "- " bullet lines, no markdown headers).
            - "testQuestions": an array of 5 to 8 multiple-choice questions. Each item has:
              "question" (string), "options" (array of exactly 4 strings), "correctAnswer"
              (string, must exactly match one of the options), "explanation" (one sentence
              on why that answer is correct).
            - "flashcards": an array of 8 to 15 flashcards covering the key terms/concepts.
              Each item has "term" (string) and "definition" (string, 1-2 sentences).

            Return ONLY valid JSON matching this shape, no markdown code fences, no extra text.

            STUDY MATERIAL:
            %s
            """.formatted(sourceText);
    }
}
