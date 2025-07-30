package prototype.javabot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunicationStyle {
    FRIENDLY("style_friendly", "😊 Дружелюбный", "Используй дружелюбный, легкий тон с эмодзи и разговорными выражениями."),
    BUSINESS("style_business", "🎯 Деловой", "Используй профессиональный, деловой тон без лишних эмоций."),
    EMOTIONAL("style_emotional", "🔥 Эмоциональный", "Используй эмоциональный, вдохновляющий тон с сильными словами и призывами.");

    private final String callbackData;
    private final String displayName;
    private final String instruction;

    public static CommunicationStyle fromCallbackData(String callbackData) {
        for (CommunicationStyle style : values()) {
            if (style.getCallbackData().equals(callbackData)) {
                return style;
            }
        }
        return null;
    }

}
