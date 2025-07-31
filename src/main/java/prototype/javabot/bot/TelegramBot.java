package prototype.javabot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import prototype.javabot.model.ContentIdea;
import prototype.javabot.model.ContentType;
import prototype.javabot.model.IdeaStatus;
import prototype.javabot.model.aiSettings.CommunicationStyle;
import prototype.javabot.model.aiSettings.ContentLength;
import prototype.javabot.model.aiSettings.ResponseLanguage;
import prototype.javabot.model.aiSettings.UserAiSetting;
import prototype.javabot.service.AiService;
import prototype.javabot.service.BotCommandService;
import prototype.javabot.service.ContentService;
import prototype.javabot.service.UserStateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final AiService aiService;
    private final ContentService contentService;
    private final BotCommandService commandService;
    private final UserStateService userStateService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallBackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке обновления: {}", String.valueOf(e));
        }
    }

    private void handleMessage(Message message) {
        if (!message.hasText()) return;

        String userMessage = message.getText();
        String chatId = message.getChatId().toString();

        log.info("Получено сообщение: {} от пользователя {}", userMessage, chatId);

        try {
            SendMessage response;

            switch (userMessage) {
                case "/start" -> response = commandService.handleStartCommand(chatId);
                case "/help" -> response = commandService.handleHelpCommand(chatId);
                case "/list" -> response = commandService.handleListCommand(chatId);
                case "/new" -> response = commandService.handleNewContentCommand(chatId);
                case "/settings" -> response = commandService.handleSettingCommand(chatId);
                case "/status" -> response = commandService.handleStatusCommand(chatId);

                default -> {
                    ContentType contentType = userStateService.getUserContentType(chatId);
                    UserAiSetting aiSetting = userStateService.getUserAiSettings(chatId);
                    String aiReply;

                    if (contentType != null) {
                        aiReply = aiService.askAiWithSettings(userMessage, contentType, aiSetting);
                        userStateService.setLastUserRequests(chatId, userMessage);
                    } else {
                        aiReply = aiService.askAiWithSettings(userMessage, null, aiSetting);
                        userStateService.setLastUserRequests(chatId, userMessage);
                    }

                    contentService.save(userMessage, aiReply);
                    response = SendMessage.builder()
                            .chatId(chatId)
                            .text(aiReply)
                            .replyMarkup(commandService.createAiResponseActionsKeyboard())
                            .build();
                }
            }

            execute(response);
            log.info("Ответ отправлен пользователю: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
            sendErrorMessage(chatId, "Извините, произошла ошибка. Попробуйте позже.");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке сообщения: ", e);
            sendErrorMessage(chatId, "Произошла неожиданная ошибка.");
        }
    }

    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        log.info("Получен callback: {} от пользователя {}", callbackData, chatId);

        try {
            switch (callbackData) {
                case "new_content" -> {
                    SendMessage response = commandService.handleNewContentCommand(chatId);
                    execute(response);
                }
                case "list_ideas" -> {
                    SendMessage response = commandService.handleListCommand(chatId);
                    execute(response);
                }
                case "settings_menu" -> {
                    SendMessage response = commandService.handleSettingCommand(chatId);
                    execute(response);
                }
                case "settings_language" -> {
                    SendMessage response = commandService.handleLanguageSettingsCommand(chatId);
                    execute(response);
                }
                case "settings_length" -> {
                    SendMessage response = commandService.handleLengthSettingsCommand(chatId);
                    execute(response);
                }
                case "settings_style" -> {
                    SendMessage response = commandService.handleStyleSettingsCommand(chatId);
                    execute(response);
                }
                case "status_management" -> {
                    SendMessage response = commandService.handleStatusCommand(chatId);
                    execute(response);
                }
                case "help" -> {
                    SendMessage response = commandService.handleHelpCommand(chatId);
                    execute(response);
                }
                case "back_to_main" -> {
                    SendMessage response = commandService.handleStartCommand(chatId);
                    execute(response);
                }
                case "refresh_list" -> {
                    EditMessageText editMessage = EditMessageText.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .text("🔄 Обновляю список...")
                            .build();
                    execute(editMessage);

                    Thread.sleep(500);
                    SendMessage response = commandService.handleListCommand(chatId);
                    execute(response);
                }
                case "retry_generation" -> {
                    handleRetryGeneration(chatId);
                }
                case "edit_request" -> {
                    handleEditRequest(chatId);
                }
                case "content_post", "content_reel", "content_story", "content_hashtags", "content_title" -> {
                    handleContentTypeSelection(chatId, callbackData);
                }
                default -> {
                    if (callbackData.startsWith("manage_idea_")) {
                        String ideaIdStr = callbackData.replace("manage_idea_", "");
                        try {
                            Long ideaId = Long.parseLong(ideaIdStr);
                            SendMessage response = commandService.handleChangeIdeaStatusCommand(chatId, ideaId);
                            execute(response);
                            return;
                        } catch (NumberFormatException e) {
                            log.warn("Неверный ID идеи: {}", ideaIdStr);
                            sendErrorMessage(chatId, "Ошибка: неверный ID идеи");
                            return;
                        }
                    }
                    if (callbackData.startsWith("filter_status_")) {
                        String statusName = callbackData.replace("filter_status_", "");
                        try {
                            IdeaStatus status = IdeaStatus.valueOf(statusName);
                            SendMessage response = commandService.handleFilteredListCommand(chatId, status);
                            execute(response);
                        } catch (TelegramApiException ex) {
                            log.warn("Неизвестный статус: {}", statusName);
                        }
                    }

                    if (callbackData.startsWith("change_status_")) {
                        handleChangeIdeaStatus(chatId, callbackData);
                        return;
                    }
                    if (callbackData.startsWith("delete_idea_")){
                        handleDeleteIdea(chatId, callbackData);
                        return;
                    }
                    if (handleAiSettingsCallback(chatId, callbackData)) {
                        return;
                    }
                    if (handleAiSettingsCallback(chatId, callbackData)) return;

                    log.warn("Неизвестный callback: {}", callbackData);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке callback: ", e);
            sendErrorMessage(chatId, "Ошибка при обработке команды.");
        }
    }

    private void handleRetryGeneration(String chatId) throws TelegramApiException {
        String lastRequest = userStateService.getLastUserRequest(chatId);
        ContentType contentType = userStateService.getUserContentType(chatId);

        if (lastRequest == null) {
            sendErrorMessage(chatId, "Не найден предыдущий запрос для повтора");
            return;
        }

        SendMessage loadingMessage = new SendMessage(chatId, "🔄 Генерирую новый вариант...");
        execute(loadingMessage);

        try {
            String aiReply;
            UserAiSetting aiSetting = userStateService.getUserAiSettings(chatId);

            if (contentType != null) {
                aiReply = aiService.askAiWithSettings(lastRequest, contentType, aiSetting);
            } else {
                aiReply = aiService.askAiWithSettings(lastRequest, null, aiSetting);
            }

            contentService.save(lastRequest, aiReply);

            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("✨ Новый вариант:\n\n" + aiReply)
                    .replyMarkup(commandService.createAiResponseActionsKeyboard())
                    .build();

            execute(response);
        } catch (Exception e) {
            log.error("Ошибка при повторной генерации: ", e);
            sendErrorMessage(chatId, "Произошла ошибка при генерации. Попробуйте еще раз.");
        }
    }

    private void handleEditRequest(String chatId) throws TelegramApiException {
        String lastRequest = userStateService.getLastUserRequest(chatId);
        ContentType contentType = userStateService.getUserContentType(chatId);

        String contentTypeInfo = contentType != null
                ? String.format("Текущий тип: \"%s\"\n\n ", contentType)
                : "";

        String previousRequestInfo = lastRequest != null
                ? String.format("Предыдущий запрос: \"%s\"\n\n", lastRequest)
                : "";

        String instruction = String.format(
                "✏️ Редактирование запроса\n\n" +
                        "%s" +
                        "%s" +
                        "Напишите новый запрос, и я сгенерирую контент:",
                contentTypeInfo, previousRequestInfo
        );

        SendMessage response = new SendMessage(chatId, instruction);
        execute(response);
    }

    private boolean handleAiSettingsCallback(String chatId, String callbackData) throws TelegramApiException {

        ResponseLanguage language = ResponseLanguage.fromCallbackData(callbackData);
        if (language != null) {
            userStateService.updateUserLanguage(chatId, language);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Язык изменен на: " + language.getDisplayName())
                    .replyMarkup(commandService.createSettingMenuKeyboard())
                    .build();
            execute(response);
            return true;
        }

        ContentLength length = ContentLength.fromCallbackData(callbackData);
        if (length != null) {
            userStateService.updateUserLength(chatId, length);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Длина контента изменена на: " + length.getDisplayName())
                    .replyMarkup(commandService.createSettingMenuKeyboard())
                    .build();
            execute(response);
            return true;
        }

        // Проверяем стиль
        CommunicationStyle style = CommunicationStyle.fromCallbackData(callbackData);
        if (style != null) {
            userStateService.updateUserStyle(chatId, style);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text("✅ Стиль общения изменен на: " + style.getDisplayName())
                    .replyMarkup(commandService.createSettingMenuKeyboard())
                    .build();
            execute(response);
            return true;
        }

        return false; // Callback не был обработан
    }

    private void handleChangeIdeaStatus(String chatId, String callbackData)throws TelegramApiException {
        String[] parts = callbackData.split("_");
        if (parts.length != 4) {
            sendErrorMessage(chatId, "Ошибка обработки команды");
            return;
        }
        try {
            Long ideaId = Long.parseLong(parts[2]);
            IdeaStatus newStatus = IdeaStatus.valueOf(parts[3]);

            ContentIdea updatedIdea = contentService.updateStatus(ideaId, newStatus);

            if (updatedIdea != null) {
                SendMessage response = SendMessage.builder()
                        .chatId(chatId)
                        .text(String.format("✅ Статус идеи #%d изменен на: %s",
                                ideaId, getStatusName(newStatus)))
                        .replyMarkup(commandService.createStatusManagementKeyboard())
                        .build();
                execute(response);
            }else {
                sendErrorMessage(chatId, "Идея не найдена");
            }
        } catch (Exception e) {
            log.error("Ошибка при изменении статуса: ", e);
            sendErrorMessage(chatId, "Произошла ошибка при изменении статуса");
        }
    }

    private void handleDeleteIdea(String chatId, String callbackData) throws TelegramApiException {
        String[] parts = callbackData.split("_");
        if (parts.length != 3) {
            sendErrorMessage(chatId, "Ошибка обработки команды");
            return;
        }

        try {
            Long ideaId = Long.parseLong(parts[2]);
            contentService.delete(ideaId);

            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text(String.format("🗑️ Идея #%d удалена", ideaId))
                    .replyMarkup(commandService.createStatusManagementKeyboard())
                    .build();
            execute(response);
        }catch (Exception e) {
            log.error("Ошибка при удалении идеи: ", e);
            sendErrorMessage(chatId, "Произошла ошибка при удалении");
        }
    }

    private void handleContentTypeSelection(String chatId, String contentType) throws TelegramApiException {
        ContentType type = ContentType.fromCallbackData(contentType);

        if (type != null) {
            userStateService.setUserContentType(chatId, type);

            String instruction = String.format(
                    "✅ Выбран тип: %s\n\n" +
                            "📝 Теперь опишите тему или отправьте свой запрос, и я сгенерирую специализированный контент!\n\n" +
                            "💡 Например: \"мотивация к спорту\" или \"здоровое питание\"",
                    type.getDisplayName()
            );

            SendMessage response = new SendMessage(chatId, instruction);
            execute(response);
        } else {
            log.warn("Неизвестный тип контента: {}", contentType);
            sendErrorMessage(chatId, "Неизвестный тип контента");
        }
    }

    private void sendErrorMessage(String chatId, String errorText) {
        try {
            SendMessage errorMessage = new SendMessage(chatId, errorText);
            execute(errorMessage);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение об ошибке: ", e);
        }
    }
    private String getStatusName(IdeaStatus status) {
        return switch (status) {
            case DRAFT -> "Черновик";
            case IN_PROGRESS -> "В работе";
            case PUBLISHED -> "Опубликовано";
        };
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}