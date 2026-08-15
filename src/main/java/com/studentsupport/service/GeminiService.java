package com.studentsupport.service;

import com.studentsupport.entity.ChatMessage;
import com.studentsupport.entity.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String FALLBACK_MESSAGE =
            "Sorry, I couldn't reach the AI assistant right now. Please try again shortly.";
    private static final String NOT_CONFIGURED_MESSAGE =
            "The AI assistant isn't configured yet. Please contact an administrator.";
    private static final String BLOCKED_MESSAGE =
            "I can't help with that request. If you're struggling with something serious, please contact "
                    + "your university's student support services or a relevant helpline.";

    private static final String CHAT_SYSTEM_INSTRUCTION =
            "You are a helpful assistant inside a Student Support & Career Advisor app. "
                    + "Answer student questions about careers, job searching, interview preparation, CVs, "
                    + "study skills, university/wellbeing support, and academic subjects (e.g. coding help, "
                    + "assignment concepts, or other coursework-related questions) concisely and "
                    + "encouragingly. If a student asks about something unrelated to their studies, career, "
                    + "or wellbeing (e.g. general trivia, entertainment, or other off-topic requests), "
                    + "politely decline and steer the conversation back to how you can help with their "
                    + "studies or career. Do not answer the off-topic question itself.";

    private static final String CV_SYSTEM_INSTRUCTION =
            "You are a career advisor reviewing a student's CV or CV section. Give specific, constructive, "
                    + "encouraging feedback: highlight strengths, point out weaknesses, and suggest concrete "
                    + "improvements such as wording, structure, missing sections, or quantifying achievements. "
                    + "Keep it concise and format it as a short bullet list.";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(RestClient.Builder builder,
                          @Value("${gemini.api.key:}") String apiKey,
                          @Value("${gemini.model:gemini-flash-latest}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public record AiResult(String text, boolean flagged) {
    }

    public AiResult generateReply(List<ChatMessage> history) {
        List<Map<String, Object>> contents = history.stream()
                .filter(m -> m.getMessageText() != null)
                .map(m -> Map.<String, Object>of(
                        "role", m.getSender() == MessageSender.USER ? "user" : "model",
                        "parts", List.of(Map.of("text", m.getMessageText()))))
                .toList();
        return generate(CHAT_SYSTEM_INSTRUCTION, contents);
    }

    public AiResult generateCvFeedback(String cvText) {
        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", cvText))));
        return generate(CV_SYSTEM_INSTRUCTION, contents);
    }

    private AiResult generate(String systemInstruction, List<Map<String, Object>> contents) {
        if (apiKey == null || apiKey.isBlank()) {
            return new AiResult(NOT_CONFIGURED_MESSAGE, false);
        }

        Map<String, Object> body = Map.of(
                "contents", contents,
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return extractResult(response);
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return new AiResult(FALLBACK_MESSAGE, false);
        }
    }

    @SuppressWarnings("unchecked")
    private AiResult extractResult(Map<?, ?> response) {
        try {
            Map<String, Object> promptFeedback = (Map<String, Object>) response.get("promptFeedback");
            if (promptFeedback != null && promptFeedback.get("blockReason") != null) {
                return new AiResult(BLOCKED_MESSAGE, true);
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return new AiResult(BLOCKED_MESSAGE, true);
            }

            Map<String, Object> candidate = candidates.get(0);
            String finishReason = (String) candidate.get("finishReason");
            boolean flagged = "SAFETY".equals(finishReason) || "RECITATION".equals(finishReason);

            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = content != null ? (List<Map<String, Object>>) content.get("parts") : null;
            if (parts == null || parts.isEmpty()) {
                return new AiResult(BLOCKED_MESSAGE, true);
            }

            String text = (String) parts.get(0).get("text");
            return new AiResult(text, flagged);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response, e);
            return new AiResult(FALLBACK_MESSAGE, false);
        }
    }
}
