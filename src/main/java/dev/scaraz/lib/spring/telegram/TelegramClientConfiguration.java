package dev.scaraz.lib.spring.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramClientConfiguration {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramProperties.getToken());
    }

}
