package prototype.javabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import prototype.javabot.model.ContentType;
import prototype.javabot.model.aiSettings.UserAiSetting;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askAi(String userMessage) {
        return askAiWithContentType(userMessage, null);
    }

    public String askAiWithContentType(String userMessage, ContentType contentType) {
        return askAiWithSettings(userMessage, contentType, null);
    }

    public String askAiWithSettings(String userMessage, ContentType contentType, UserAiSetting aiSetting) {
        try {

            String finalPrompt = buildPrompt(userMessage, contentType);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", getSystemPrompt(contentType, aiSetting)),
                            Map.of("role", "user", "content", finalPrompt)
                    )
            );

            String jsonResponse = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root.has("error")) {
                String errorMessage = root.get("error").get("message").asText();
                log.error("Ошибка API OpenRouter: {}", errorMessage);
                return "Извините, произошла ошибка при обращении к AI: " + errorMessage;
            }

            if ((!root.has("choices")) || root.get("choices").isEmpty()) {
                log.error("Пустой ответ от API OpenRouter");
                return "Извините получен пустой ответ от AI";
            }

            String result = root.at("/choices/0/message/content").asText();

            if (contentType != null) {
                String settingsInfo = aiSetting != null ? " с настройками: " + aiSetting.getSettingsSummary() : "";
                log.info("Сгенерирован контент типа: {} для запроса: {}{}", contentType.getDisplayName(), userMessage, settingsInfo);
            }

            return result;
        } catch (WebClientResponseException e) {
            log.error("HTTP ошибка при обращении к OpenRouter API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Извините, произошла ошибка сети при обращении к AI";
        } catch (JsonProcessingException e) {
            log.error("Ошибка при обработке JSON: {}", e.getMessage());
            return "Извините, произошла ошибка при обработке ответа AI";
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обращении к AI: {}", e.getMessage());
            return "Извините, произошла неожиданная ошибка";
        }
    }

    private String buildPrompt(String userMessage, ContentType contentType) {
        if (contentType != null) {
            return contentType.getPromptTemplate() + userMessage;
        }
        return userMessage;
    }

    private String getSystemPrompt(ContentType contentType, UserAiSetting aiSetting) {
       StringBuilder systemPrompt = new StringBuilder();

        if (contentType == null) {
            return "Ты полезный ассистент.";
        }else {
            systemPrompt.append(switch (contentType) {
                case POST -> "Ты эксперт по созданию вирусного контента для социальных сетей. " +
                        "Создавай посты, которые вызывают эмоции, содержат призыв к действию и подходят для Instagram, VK, Telegram.";

                case REEL -> "Ты сценарист коротких видео. Создавай динамичные сценарии с хуками в первые 3 секунды, " +
                        "понятной структурой и сильным CTA. Формат: Reels/TikTok до 60 секунд.";

                case STORY -> "Ты специалист по Stories. Создавай интерактивный контент с опросами, вопросами, " +
                        "стикерами. Думай о вовлечении аудитории и создании диалога.";

                case HASHTAGS -> "Ты эксперт по хештегам и продвижению в социальных сетях. " +
                        "Создавай mix популярных, средних и нишевых хештегов для максимального охвата.";

                case TITLE -> "Ты копирайтер-эксперт по заголовкам. Создавай цепляющие заголовки, " +
                        "которые останавливают скролл и заставляют кликнуть. Используй психологические триггеры.";
            });
        }

        if (aiSetting != null) {
            systemPrompt.append(" ").append(aiSetting.getAllInstructions());
        }
        return systemPrompt.toString();
    }
}