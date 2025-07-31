package prototype.javabot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContentType {
    POST("content_post", "📱 Пост", "Создай увлекательный пост для социальных сетей на тему: "),
    REEL("content_reel", "🎬 Reels", "Напиши динамичный сценарий для короткого видео (Reels/TikTok) с хуками и CTA на тему: "),
    STORY("content_story", "📖 Story", "Создай идею для Stories с интерактивными элементами на тему: "),
    HASHTAGS("content_hashtags", "#️⃣ Хештеги", "Сгенерируй 15-20 релевантных хештегов для продвижения контента на тему: "),
    TITLE("content_title", "📰 Заголовок", "Придумай 5 цепляющих заголовков, которые привлекут внимание, для темы: ");

    private final String callbackData;
    private final String displayName;
    private final String promptTemplate;

    public static ContentType fromCallbackData(String callbackData) {
        for (ContentType type : values()) {
            if (type.getCallbackData().equals(callbackData)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isContentTypeCallback(String callbackData) {
        return fromCallbackData(callbackData) != null;
    }
}
