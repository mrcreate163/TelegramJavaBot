package prototype.javabot.model.aiSettings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseLanguage {
    RUSSIAN("lang_ru", "🇷🇺 Русский", "Отвечай ТОЛЬКО на русском языке."),
    ENGLISH("lang_en", "🇺🇸 English", "Answer ONLY in English."),
    UKRAINIAN("lang_ua", "🇺🇦 Українська", "Відповідай ТІЛЬКИ українською мовою.");


    private final String callbackData;
    private final String displayName;
    private final String instruction;

    public static ResponseLanguage fromCallbackData(String callbackData) {
        for (ResponseLanguage lang : values()) {
            if (lang.getCallbackData().equals(callbackData)) {
                return lang;
            }
        }
        return null;
    }


}
