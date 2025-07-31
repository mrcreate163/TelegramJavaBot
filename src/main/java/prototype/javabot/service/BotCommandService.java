package prototype.javabot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import prototype.javabot.model.ContentIdea;
import prototype.javabot.model.IdeaStatus;
import prototype.javabot.model.aiSettings.CommunicationStyle;
import prototype.javabot.model.aiSettings.ContentLength;
import prototype.javabot.model.aiSettings.ResponseLanguage;
import prototype.javabot.model.aiSettings.UserAiSetting;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotCommandService {

    private final ContentService contentService;
    private final UserStateService userStateService;

    public SendMessage handleStartCommand(String chatId) {
        String welcomeText = """
                🤖 Добро пожаловать в ContentMaker Bot!
                
                                Я помогу вам генерировать идеи для контента с помощью ИИ.
                
                                📝 Доступные команды:
                                /help - список всех команд
                                /list - последние 10 идей
                                /new - создать новую идею
                
                                Или просто напишите мне любой запрос, и я сгенерирую контент!
                """;

        return SendMessage.builder()
                .chatId(chatId)
                .text(welcomeText)
                .replyMarkup(createMainMenuKeyboard())
                .build();
    }

    public SendMessage handleHelpCommand(String chatId) {
        String helpText = """
                📖 Справка по командам:
                
                                /start - главное меню
                                /help - эта справка
                                /list - показать последние идеи
                                /new - создать новую идею
                                /settings - настройка AI
                
                                🎯 Типы контента:
                                • Посты для соцсетей
                                • Сценарии для Reels
                                • Идеи для Stories
                                • Хештеги
                                • Заголовки
                
                                💡 Просто опишите, что вам нужно, и я сгенерирую контент!
                """;

        return SendMessage.builder()
                .chatId(chatId)
                .text(helpText)
                .build();
    }

    public SendMessage handleListCommand(String chatId) {
        List<ContentIdea> ideas = contentService.findAll();

        if (ideas.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("\uD83D\uDCDD У вас пока нет сохраненных идей. Создайте первую!")
                    .replyMarkup(createMainMenuKeyboard())
                    .build();
        }

        StringBuilder messageText = new StringBuilder("\uD83D\uDCCB Ваши последние идеи:\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        List<ContentIdea> recentIdeas = ideas.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();
        recentIdeas.forEach(idea -> {
            String statusEmoji = getStatusEmoji(idea.getStatus());
            String shortPrompt = idea.getPrompt().length() > 50
                    ? idea.getPrompt().substring(0, 50) + "..."
                    : idea.getPrompt();

            messageText.append(String.format(
                    "%s ID: %d\n" +
                            "📝 %s\n" +
                            "🕒 %s\n\n",
                    statusEmoji,
                    idea.getId(),
                    shortPrompt,
                    idea.getCreatedAt().format(formatter)
            ));
        });

        return SendMessage.builder()
                .chatId(chatId)
                .text(messageText.toString())
                .replyMarkup(createIdeaManagementKeyboard(recentIdeas))
                .build();
    }

    public SendMessage handleNewContentCommand(String chatId) {
        String text = "\uD83C\uDFAF Выберите тип контента для генерации:";

        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createContentTypeKeyboard())
                .build();
    }

    public SendMessage handleSettingCommand(String chatId) {
        UserAiSetting settings = userStateService.getUserAiSettings(chatId);

        String settingText = String.format(
                "⚙️ Настройки AI\n\n" +
                        "🌍 Язык: %s\n" +
                        "📏 Длина: %s\n" +
                        "🎭 Стиль: %s\n\n" +
                        "Выберите, что хотите изменить:",
                settings.getLanguage().getDisplayName(),
                settings.getLength().getDisplayName(),
                settings.getStyle().getDisplayName()
        );

        return SendMessage.builder()
                .chatId(chatId)
                .text(settingText)
                .replyMarkup(createSettingMenuKeyboard())
                .build();
    }

    public SendMessage handleLanguageSettingsCommand(String chatId) {
        String text = "🌍 Выберите язык ответов AI:";

        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createLanguageKeyboard())
                .build();
    }

    public SendMessage handleLengthSettingsCommand(String chatId) {
        String text = "📏 Выберите длину контента:";

        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createLengthKeyboard())
                .build();
    }

    public SendMessage handleStyleSettingsCommand(String chatId) {
        String text = "🎭 Выберите стиль общения:";

        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createStyleKeyboard())
                .build();
    }

    public SendMessage handleStatusCommand(String chatId) {
        String text = "📊 Управление статусами идей\n\nВыберите действие:";

        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createStatusManagementKeyboard())
                .build();
    }

    public SendMessage handleFilteredListCommand(String chatId, IdeaStatus status) {
        List<ContentIdea> ideas = contentService.findAll();

        List<ContentIdea> filteredIdeas = ideas.stream()
                .filter(idea -> idea.getStatus() == status)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();

        if (filteredIdeas.isEmpty()) {
            String statusName = getStatusName(status);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(String.format("📝 У вас нет идей со статусом \"%s\"", statusName))
                    .replyMarkup(createStatusManagementKeyboard())
                    .build();
        }

        StringBuilder messageText = new StringBuilder(
                String.format("📋 Идеи со статусом \"%s\":\n\n", getStatusName(status))
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

        filteredIdeas.forEach(idea -> {
            String statusEmoji = getStatusEmoji(idea.getStatus());
            String shortPrompt = idea.getPrompt().length() > 50
                    ? idea.getPrompt().substring(0, 50) + "..."
                    : idea.getPrompt();

            messageText.append(String.format(
                    "%s ID: %d\n" +
                            "📝 %s\n" +
                            "🕒 %s\n\n",
                    statusEmoji,
                    idea.getId(),
                    shortPrompt,
                    idea.getCreatedAt().format(formatter)
            ));
        });
        return SendMessage.builder()
                .chatId(chatId)
                .text(messageText.toString())
                .replyMarkup(createIdeaManagementKeyboard(filteredIdeas))
                .build();

    }

    public SendMessage handleChangeIdeaStatusCommand(String chatId, Long ideaId) {
        ContentIdea idea = contentService.findById(ideaId);

        if (idea == null) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Идея не найдена")
                    .replyMarkup(createStatusManagementKeyboard())
                    .build();
        }

        String shortPrompt = idea.getPrompt().length() > 50
                ? idea.getPrompt().substring(0, 50) + "..."
                : idea.getPrompt();

        String text = String.format(
                "📝 Изменение статуса идеи #%d\n\n" +
                        "Текст: %s\n\n" +
                        "Текущий статус: %s %s\n\n" +
                        "Выберите новый статус:",
                idea.getId(),
                shortPrompt,
                getStatusEmoji(idea.getStatus()),
                getStatusName(idea.getStatus())
        );
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createChangeStatusKeyboard(ideaId))
                .build();
    }

    private InlineKeyboardMarkup createIdeaManagementKeyboard(List<ContentIdea> ideas) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        ideas.stream().limit(5).forEach(idea -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            String shortText = idea.getPrompt().length() > 20
                    ? idea.getPrompt().substring(0, 20)
                    : idea.getPrompt();

            row.add(createInLineButton(
                    String.format("✏️ #%d: %s", idea.getId(), shortText),
                    "manage_idea_" + idea.getId()
            ));
            keyboard.add(row);
        });

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        navRow.add(createInLineButton("📊 К управлению статусами", "status_management"));
        navRow.add(createInLineButton("🔙 Главное меню", "back_to_main"));
        keyboard.add(navRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup createAiResponseActionsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("🔄 Сгенерировать еще раз", "retry_generation"));
        row1.add(createInLineButton("✏️ Изменить запрос", "edit_request"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("🔙 Главное меню", "back_to_main"));

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    private InlineKeyboardMarkup createMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("📝 Новая идея", "new_content"));
        row1.add(createInLineButton("📋 Мои идеи", "list_ideas"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("⚙️ Настройки", "settings_menu"));
        row2.add(createInLineButton("❓ Помощь", "help"));

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createContentTypeKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("📱 Пост", "content_post"));
        row1.add(createInLineButton("🎬 Reels", "content_reel"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("📖 Story", "content_story"));
        row2.add(createInLineButton("#️⃣ Хештеги", "content_hashtags"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInLineButton("📰 Заголовок", "content_title"));
        row3.add(createInLineButton("🔙 Назад", "back_to_main"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createListActionsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInLineButton("🔄 Обновить", "refresh_list"));
        row.add(createInLineButton("🔙 Главное меню", "back_to_main"));

        keyboard.add(row);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup createSettingMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("🌍 Язык", "settings_language"));
        row1.add(createInLineButton("📏 Длина", "settings_length"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("🎭 Стиль", "settings_style"));
        row2.add(createInLineButton("🔙 Назад", "back_to_main"));

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createLanguageKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton(ResponseLanguage.RUSSIAN.getDisplayName(), ResponseLanguage.RUSSIAN.getCallbackData()));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton(ResponseLanguage.ENGLISH.getDisplayName(), ResponseLanguage.ENGLISH.getCallbackData()));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInLineButton(ResponseLanguage.UKRAINIAN.getDisplayName(), ResponseLanguage.UKRAINIAN.getCallbackData()));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInLineButton("🔙 К настройкам", "settings_menu"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createLengthKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton(ContentLength.SHORT.getDisplayName(), ContentLength.SHORT.getCallbackData()));
        row1.add(createInLineButton(ContentLength.MEDIUM.getDisplayName(), ContentLength.MEDIUM.getCallbackData()));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton(ContentLength.LONG.getDisplayName(), ContentLength.LONG.getCallbackData()));
        row2.add(createInLineButton("🔙 К настройкам", "settings_menu"));

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createStyleKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton(CommunicationStyle.FRIENDLY.getDisplayName(), CommunicationStyle.FRIENDLY.getCallbackData()));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton(CommunicationStyle.BUSINESS.getDisplayName(), CommunicationStyle.BUSINESS.getCallbackData()));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInLineButton(CommunicationStyle.EMOTIONAL.getDisplayName(), CommunicationStyle.EMOTIONAL.getCallbackData()));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createInLineButton("🔙 К настройкам", "settings_menu"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup createStatusManagementKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("📝 Черновики", "filter_status_DRAFT"));
        row1.add(createInLineButton("⏳ В работе", "filter_status_IN_PROGRESS"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("✅ Опубликованные", "filter_status_PUBLISHED"));
        row2.add(createInLineButton("📋 Все идеи", "list_ideas"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInLineButton("🔙 Главное меню", "back_to_main"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createFilteredListActionsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInLineButton("📊 Управление статусами", "status_management"));
        row.add(createInLineButton("🔙 Главное меню", "back_to_main"));

        keyboard.add(row);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardMarkup createChangeStatusKeyboard(Long ideaId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInLineButton("📝 Черновик", "change_status_" + ideaId + "_DRAFT"));
        row1.add(createInLineButton("⏳ В работе", "change_status_" + ideaId + "_IN_PROGRESS"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInLineButton("✅ Опубликовано", "change_status_" + ideaId + "_PUBLISHED"));
        row2.add(createInLineButton("🗑️ Удалить", "delete_idea_" + ideaId));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInLineButton("🔙 К управлению", "status_management"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardButton createInLineButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private String getStatusName(IdeaStatus status) {
        return switch (status) {
            case DRAFT -> "Черновик";
            case IN_PROGRESS -> "В работе";
            case PUBLISHED -> "Опубликовано";
        };
    }

    private String getStatusEmoji(IdeaStatus status) {
        return switch (status) {
            case DRAFT -> "📝";
            case IN_PROGRESS -> "⏳";
            case PUBLISHED -> "✅";
        };
    }
}
