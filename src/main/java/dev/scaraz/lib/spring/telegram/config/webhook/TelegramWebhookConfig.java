package dev.scaraz.lib.spring.telegram.config.webhook;

import dev.scaraz.lib.spring.telegram.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.webhook.TelegramBotsWebhookApplication;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "spring.telegram",
        name = "type",
        havingValue = "webhook")
public class TelegramWebhookConfig {

    private final TelegramProperties properties;

    @Bean
    public TelegramBotsWebhookApplication telegramBotsWebhookApplication() throws TelegramApiException {
        return new TelegramBotsWebhookApplication();
    }

}
