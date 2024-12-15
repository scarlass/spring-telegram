package dev.scaraz.lib.spring.telegram.config.longpolling;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scaraz.lib.spring.telegram.TelegramProperties;
import dev.scaraz.lib.spring.telegram.config.TelegramContextInitializer;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateHandler;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.ExponentialBackOff;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(
        prefix = "spring.telegram",
        name = "type",
        havingValue = "long_polling",
        matchIfMissing = true)
public class SpringLongPollingBotConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication(ObjectProvider<ObjectMapper> objectMapper,
                                                                                 ObjectProvider<ObservationRegistry> observationRegistry
    ) {
        TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication(
                () -> objectMapper.getIfAvailable(ObjectMapper::new),
                new TelegramOkHttpClientFactory.DefaultOkHttpClientCreator(),
                () -> new LongPollingExecutorService(1, observationRegistry.getIfAvailable()),
                ExponentialBackOff::new
        ) {

            @Override
            public void close() throws Exception {
                log.info("Closing long-polling bot");
                super.close();
            }
        };

        return application;
    }

    @Bean
    @ConditionalOnMissingBean
    LongPollingUpdateConsumer longPollingUpdateConsumer(ObjectProvider<ObservationRegistry> observationRegistryProvider,
                                                        TelegramContextInitializer telegramContextInitializer,
                                                        TelegramUpdateHandler telegramUpdateHandler,
                                                        LongPollingReplier longPollingReplier
    ) {
        DefaultLongPollingConsumer consumer = new DefaultLongPollingConsumer(telegramContextInitializer, telegramUpdateHandler, longPollingReplier);
        consumer.setUpdateExecutor(Executors.newVirtualThreadPerTaskExecutor());
        consumer.setObservationRegistries(observationRegistryProvider);
        return consumer;
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> startLongPollingApplication(TelegramBotsLongPollingApplication application,
                                                                           TelegramProperties telegramProperties,
                                                                           LongPollingUpdateConsumer updateConsumer) {
        return event -> {
            try {
                if (!telegramProperties.getLongPolling().isStartOnReady()) {
                    application.stop();
                }

                application.registerBot(telegramProperties.getToken(), updateConsumer);
            }
            catch (TelegramApiException e) {
                throw new RuntimeException("unable to register long-polling consumer", e);
            }
        };
    }


}
