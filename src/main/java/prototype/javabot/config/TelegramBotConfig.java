package prototype.javabot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import prototype.javabot.bot.TelegramBot;

@Slf4j
@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            log.info("Telegram bot успешно зарегистрирован");
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            log.error("Ошибка при регистрации Telegram bot: ", e);
            throw new RuntimeException("Не удалось зарегистрировать Telegram bot", e);
        }
    }
}