package prototype.javabot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prototype.javabot.model.ContentType;
import prototype.javabot.model.aiSettings.UserAiSetting;


import static org.junit.jupiter.api.Assertions.*;

class UserStateServiceTest {

    private UserStateService userStateService;
    private final String testChatId = "12345";

    @BeforeEach
    void setUp() {
        userStateService = new UserStateService();
    }

    @Test
    void setUserContentType_ShouldStoreContentType() {
        // Given
        ContentType contentType = ContentType.POST;

        // When
        userStateService.setUserContentType(testChatId, contentType);

        // Then
        assertEquals(contentType, userStateService.getUserContentType(testChatId));
        assertTrue(userStateService.hasUserContentType(testChatId));
    }

    @Test
    void getUserContentType_ShouldReturnNull_WhenNotSet() {
        // When
        ContentType result = userStateService.getUserContentType(testChatId);

        // Then
        assertNull(result);
        assertFalse(userStateService.hasUserContentType(testChatId));
    }

    @Test
    void setLastUserRequest_ShouldStoreRequest() {
        // Given
        String request = "Test request";

        // When
        userStateService.setLastUserRequests(testChatId, request);

        // Then
        assertEquals(request, userStateService.getLastUserRequest(testChatId));
        assertTrue(userStateService.hasLastUserRequest(testChatId));
    }

    @Test
    void getLastUserRequest_ShouldReturnNull_WhenNotSet() {
        // When
        String result = userStateService.getLastUserRequest(testChatId);

        // Then
        assertNull(result);
        assertFalse(userStateService.hasLastUserRequest(testChatId));
    }

    @Test
    void getUserAiSettings_ShouldReturnDefault_WhenNotSet() {
        // When
        UserAiSetting result = userStateService.getUserAiSettings(testChatId);

        // Then
        assertNotNull(result);
        assertEquals(UserAiSetting.getDefault().getLanguage(), result.getLanguage());
        assertEquals(UserAiSetting.getDefault().getLength(), result.getLength());
        assertEquals(UserAiSetting.getDefault().getStyle(), result.getStyle());
    }

    @Test
    void setUserAiSettings_ShouldStoreSettings() {
        // Given
        UserAiSetting customSettings = UserAiSetting.builder()
                .build();

        // When
        userStateService.setUserAiSettings(testChatId, customSettings);

        // Then
        UserAiSetting result = userStateService.getUserAiSettings(testChatId);
        assertNotNull(result);
        assertEquals(customSettings.getLanguage(), result.getLanguage());
    }

    @Test
    void clearUserState_ShouldRemoveContentType() {
        // Given
        userStateService.setUserContentType(testChatId, ContentType.POST);

        // When
        userStateService.clearUserState(testChatId);

        // Then
        assertNull(userStateService.getUserContentType(testChatId));
        assertFalse(userStateService.hasUserContentType(testChatId));
    }

    @Test
    void clearAllUserData_ShouldRemoveStateAndRequest() {
        // Given
        userStateService.setUserContentType(testChatId, ContentType.POST);
        userStateService.setLastUserRequests(testChatId, "test request");

        // When
        userStateService.clearAllUserData(testChatId);

        // Then
        assertNull(userStateService.getUserContentType(testChatId));
        assertNull(userStateService.getLastUserRequest(testChatId));
        assertFalse(userStateService.hasUserContentType(testChatId));
        assertFalse(userStateService.hasLastUserRequest(testChatId));

        // AI настройки должны остаться
        assertNotNull(userStateService.getUserAiSettings(testChatId));
    }

    @Test
    void clearAllUserDataIncludingSettings_ShouldRemoveEverything() {
        // Given
        userStateService.setUserContentType(testChatId, ContentType.POST);
        userStateService.setLastUserRequests(testChatId, "test request");
        userStateService.setUserAiSettings(testChatId, UserAiSetting.getDefault());

        // When
        userStateService.clearAllUserDataIncludeSettings(testChatId);

        // Then
        assertNull(userStateService.getUserContentType(testChatId));
        assertNull(userStateService.getLastUserRequest(testChatId));

        // AI настройки должны вернуться к дефолтным
        UserAiSetting result = userStateService.getUserAiSettings(testChatId);
        assertEquals(UserAiSetting.getDefault().getLanguage(), result.getLanguage());
    }

    @Test
    void getActiveUsersCount_ShouldReturnCorrectCount() {
        // Given
        userStateService.setUserContentType("user1", ContentType.POST);
        userStateService.setUserContentType("user2", ContentType.REEL);

        // When
        int count = userStateService.getActiveUsersCount();

        // Then
        assertEquals(2, count);
    }

    @Test
    void clearAllStates_ShouldClearEverything() {
        // Given
        userStateService.setUserContentType("user1", ContentType.POST);
        userStateService.setLastUserRequests("user1", "request");
        userStateService.setUserAiSettings("user1", UserAiSetting.getDefault());

        // When
        userStateService.clearAllStates();

        // Then
        assertEquals(0, userStateService.getActiveUsersCount());
        assertNull(userStateService.getUserContentType("user1"));
        assertNull(userStateService.getLastUserRequest("user1"));

        // AI настройки должны вернуться к дефолтным
        UserAiSetting result = userStateService.getUserAiSettings("user1");
        assertEquals(UserAiSetting.getDefault().getLanguage(), result.getLanguage());
    }
}