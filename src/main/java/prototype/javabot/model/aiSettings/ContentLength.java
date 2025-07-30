package prototype.javabot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContentLength {
    SHORT("length_short", "📝 Короткий", "Создавай краткий контент (1-2 абзаца, до 200 слов)."),
    MEDIUM("length_medium", "📄 Средний", "Создавай средний контент (3-4 абзаца, 200-400 слов)."),
    LONG("length_long", "📜 Длинный", "Создавай подробный контент (5+ абзацев, 400+ слов).");

    private final String callbackData;
    private final String displayName;
    private final String instruction;

    private static ContentLength fromCallbackData(String callbackData) {
        for (ContentLength length : values()) {
            if (length.getCallbackData().equals(callbackData)) {
                return length;
            }
        }
        return null;
    }
}
