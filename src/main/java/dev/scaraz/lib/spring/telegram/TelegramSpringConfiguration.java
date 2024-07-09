package dev.scaraz.lib.spring.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scaraz.lib.spring.telegram.bind.TelegramExceptionHandler;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateProcessor;
import dev.scaraz.lib.spring.telegram.listener.TelegramLongPollingListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.ExponentialBackOff;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
@ComponentScan(basePackages = {
        "dev.scaraz.lib.spring.telegram.config",
        "dev.scaraz.lib.spring.telegram.listener"
})
public class TelegramSpringConfiguration {

    private static ObjectMapper getOrDefault(ObjectProvider<ObjectMapper> objectMapper) {
        return objectMapper.getIfAvailable(ObjectMapper::new);
    }

    @Bean
    @ConditionalOnMissingBean
    public TelegramExceptionHandler telegramExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.telegram",
            name = "type",
            havingValue = "long_polling",
            matchIfMissing = true)
    @ConditionalOnMissingBean(TelegramBotsLongPollingApplication.class)
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication(ObjectProvider<ObjectMapper> objectMapper) {
        return new TelegramBotsLongPollingApplication(
                () -> getOrDefault(objectMapper),
                new TelegramOkHttpClientFactory.DefaultOkHttpClientCreator(),
//                Executors::newSingleThreadScheduledExecutor,
                this::scheduledExecutorService,
                ExponentialBackOff::new
        ) {

            @Override
            public void close() throws Exception {
                log.info("Closing long-polling bot");
                super.close();
            }
        };
    }

    protected ScheduledExecutorService scheduledExecutorService() {
        ThreadFactory factory = Thread.ofVirtual().factory();
        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    @Configuration
    @RequiredArgsConstructor
    @ConditionalOnMissingBean(LongPollingUpdateConsumer.class)
    @ConditionalOnProperty(
            prefix = "spring.telegram",
            name = "type",
            havingValue = "long_polling",
            matchIfMissing = true)
    static class StartBotListener implements ApplicationListener<ApplicationReadyEvent> {

        private final ApplicationContext applicationContext;
        private final TelegramClient telegramClient;
        private final TelegramProperties telegramProperties;
        private final TelegramUpdateProcessor telegramUpdateProcessor;
        private final TelegramBotsLongPollingApplication telegramBotsLongPollingApplication;


        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            ObjectProvider<ObjectMapper> om = applicationContext.getBeanProvider(ObjectMapper.class);

            TelegramLongPollingListener listener = new TelegramLongPollingListener(getOrDefault(om), telegramClient, telegramUpdateProcessor);

            try {
                telegramBotsLongPollingApplication.registerBot(telegramProperties.getToken(), listener);
                log.info("Telegram long-polling started");
            }
            catch (TelegramApiException e) {
                throw new RuntimeException("unable to run default long-polling listener", e);
            }
        }

    }

}
