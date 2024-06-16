package dev.scaraz.lib.spring.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.ExponentialBackOff;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
@ComponentScan(basePackages = {
        "dev.scaraz.lib.spring.telegram.config",
        "dev.scaraz.lib.spring.telegram.listener"
})
public class TelegramSpringConfiguration {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramProperties.getToken());
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "telegram",
            name = "type",
            havingValue = "long_polling")
    @ConditionalOnMissingBean(TelegramBotsLongPollingApplication.class)
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication(
                this::objectMapper,
                new TelegramOkHttpClientFactory.DefaultOkHttpClientCreator(),
                Executors::newSingleThreadScheduledExecutor,
                ExponentialBackOff::new
        ) {

            @Override
            public void close() throws Exception {
                log.info("Closing long-polling bot");
                super.close();
            }
        };
    }


}
