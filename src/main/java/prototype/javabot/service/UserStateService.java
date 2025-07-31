package prototype.javabot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import prototype.javabot.model.ContentType;
import prototype.javabot.model.aiSettings.CommunicationStyle;
import prototype.javabot.model.aiSettings.ContentLength;
import prototype.javabot.model.aiSettings.ResponseLanguage;
import prototype.javabot.model.aiSettings.UserAiSetting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UserStateService {

    private final Map<String, ContentType> userStates = new ConcurrentHashMap<>();

    private final Map<String, String> lastUserRequests = new ConcurrentHashMap<>();

    private final Map<String, UserAiSetting> userAiSettings = new ConcurrentHashMap<>();

    public void setUserContentType(String chatId, ContentType contentType) {
        userStates.put(chatId, contentType);
        log.debug("Установлен тип контента {} для пользователя {}", contentType.getDisplayName(), chatId);
    }

    public ContentType getUserContentType(String chatId) {
        return userStates.get(chatId);
    }

    public boolean hasUserContentType(String chatId) {
        return userStates.containsKey(chatId);
    }

    public void setLastUserRequests(String chatId, String request) {
        lastUserRequests.put(chatId, request);
        log.debug("Сохранён последний запрос для пользователя {}: {}", chatId, request);
    }

    public String getLastUserRequest(String chatId) {
        return lastUserRequests.get(chatId);
    }

    public boolean hasLastUserRequest(String chatId) {
        return lastUserRequests.containsKey(chatId);
    }

    public void updateUserLanguage(String chatId, ResponseLanguage language) {
        UserAiSetting setting = getUserAiSettings(chatId);
        setting.setLanguage(language);
        setUserAiSettings(chatId, setting);
    }

    public void updateUserLength(String chatId, ContentLength length) {
        UserAiSetting setting = getUserAiSettings(chatId);
        setting.setLength(length);
        setUserAiSettings(chatId, setting);
    }

    public void updateUserStyle(String chatId, CommunicationStyle style) {
        UserAiSetting setting = getUserAiSettings(chatId);
        setting.setStyle(style);
        setUserAiSettings(chatId, setting);
    }

    public UserAiSetting getUserAiSettings(String chatId) {
        return userAiSettings.getOrDefault(chatId, UserAiSetting.getDefault());
    }

    public void setUserAiSettings(String chatId, UserAiSetting setting) {
        userAiSettings.put(chatId, setting);
        log.debug("Обновлены AI настройки для пользователя {}: {}", chatId, setting);
    }

    public void clearUserState(String chatId) {
        ContentType removed = userStates.remove(chatId);
        if (removed != null) {
            log.debug("Очищено состояние пользователя {}, был тип: {}", chatId, removed.getDisplayName());
        }
    }

    public void clearAllUserData(String chatId) {
        userStates.remove(chatId);
        lastUserRequests.remove(chatId);
        log.debug("Очищены временные данные пользователя {}", chatId);
    }

    public void clearAllUserDataIncludeSettings(String chatId) {
        userStates.remove(chatId);
        lastUserRequests.remove(chatId);
        userAiSettings.remove(chatId);
        log.debug("Очищены ВСЕ данные пользователя {}", chatId);
    }

    public int getActiveUsersCount() {
        return userStates.size();
    }

    public void clearAllStates() {
        int stateCount = userStates.size();
        int requestCount = lastUserRequests.size();
        int settingCount = userAiSettings.size();
        userStates.clear();
        lastUserRequests.clear();
        userAiSettings.clear();
        log.info("Очищены состояния {} пользователей и {} запросов и {} настроек", stateCount, requestCount, settingCount);
    }
}
