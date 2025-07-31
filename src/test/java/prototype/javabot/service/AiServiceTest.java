package prototype.javabot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import prototype.javabot.model.ContentType;
import prototype.javabot.model.aiSettings.UserAiSetting;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AiService aiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Создаем реальный AiService
        aiService = new AiService();
        objectMapper = new ObjectMapper();

        // Устанавливаем тестовые значения через рефлексию
        ReflectionTestUtils.setField(aiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "test-model");
        ReflectionTestUtils.setField(aiService, "webClient", webClient);
        ReflectionTestUtils.setField(aiService, "objectMapper", objectMapper);

        // Настраиваем цепочку моков WebClient
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void askAi_ShouldReturnGeneratedContent_WhenApiCallSuccessful() {
        // Given
        String userMessage = "Напиши пост про Java";
        String expectedResponse = "Java - отличный язык программирования!";

        String mockApiResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "%s"
                    }
                }]
            }
            """.formatted(expectedResponse);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockApiResponse));

        // When
        String result = aiService.askAi(userMessage);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        // Проверяем, что API был вызван правильно
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/chat/completions");
        verify(requestBodySpec, times(2)).header(anyString(), anyString());
    }

    @Test
    void askAiWithSettings_ShouldApplyUserSettings() {
        // Given
        String userMessage = "Test message";
        ContentType contentType = ContentType.POST;
        UserAiSetting settings = UserAiSetting.getDefault();
        String expectedResponse = "Generated content";

        String mockApiResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "%s"
                    }
                }]
            }
            """.formatted(expectedResponse);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockApiResponse));

        // When
        String result = aiService.askAiWithSettings(userMessage, contentType, settings);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void askAi_ShouldReturnErrorMessage_WhenApiReturnsError() {
        // Given
        String userMessage = "Test message";
        String errorMessage = "API limit exceeded";

        String mockErrorResponse = """
            {
                "error": {
                    "message": "%s"
                }
            }
            """.formatted(errorMessage);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockErrorResponse));

        // When
        String result = aiService.askAi(userMessage);

        // Then
        assertTrue(result.contains("Извините"));
        assertTrue(result.contains(errorMessage));
    }

    @Test
    void askAi_ShouldHandleEmptyChoices() {
        // Given
        String userMessage = "Test";
        String emptyResponse = """
            {
                "choices": []
            }
            """;

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(emptyResponse));

        // When
        String result = aiService.askAi(userMessage);

        // Then
        assertTrue(result.contains("пустой ответ"));
    }

    @Test
    void askAi_ShouldHandleWebClientException() {
        // Given
        String userMessage = "Test";

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // When
        String result = aiService.askAi(userMessage);

        // Then
        assertTrue(result.contains("Извините") || result.contains("ошибка"));
    }
}