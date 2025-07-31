package prototype.javabot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// @ExtendWith говорит JUnit использовать Mockito для создания моков
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    // @Mock создаёт фиктивный объект WebClient
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

    // @InjectMocks создаёт реальный AiService и внедряет в него моки
    @InjectMocks
    private AiService aiService;

    @BeforeEach
    void setUp() {
        // Устанавливаем значения приватных полей для теста
        ReflectionTestUtils.setField(aiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "test-model");

        // Настраиваем цепочку вызовов WebClient
        // Это называется stubbing - мы говорим моку, что возвращать
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void askAi_ShouldReturnGeneratedContent_WhenApiCallSuccessful() {
        // Given (Подготовка данных)
        String userMessage = "Напиши пост про Java";
        String expectedResponse = "Java - отличный язык программирования!";

        // Подготавливаем JSON ответ от API
        String mockApiResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "%s"
                    }
                }]
            }
            """.formatted(expectedResponse);

        // Настраиваем мок для возврата нашего ответа
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(mockApiResponse));

        // When (Выполнение тестируемого метода)
        String result = aiService.askAi(userMessage);

        // Then (Проверка результатов)
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(expectedResponse, result, "Ответ должен совпадать с ожидаемым");

        // Verify проверяет, что методы были вызваны
        verify(webClient, times(1)).post();
        verify(requestBodySpec, times(2)).header(anyString(), anyString());
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
        assertTrue(result.contains("Извините"), "Должно содержать извинение");
        assertTrue(result.contains(errorMessage), "Должно содержать сообщение об ошибке");
    }

    @Test
    void askAi_ShouldHandleEmptyResponse() {
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
        assertTrue(result.contains("пустой ответ"), "Должно быть сообщение о пустом ответе");
    }
}

// Основные концепции тестирования:
// 1. @Mock - создаёт фиктивные объекты (заглушки)
// 2. when().thenReturn() - определяет поведение моков
// 3. Given-When-Then - структура теста (Дано-Когда-Тогда)
// 4. verify() - проверяет, что методы были вызваны
// 5. assert*() - проверяет результаты
