package dev.scaraz.lib.spring.telegram.config.longpolling;

import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import dev.scaraz.lib.spring.telegram.config.TelegramContextHolder;
import dev.scaraz.lib.spring.telegram.config.TelegramContextInitializer;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
//@Component
@RequiredArgsConstructor
//@ConditionalOnMissingBean(LongPollingUpdateConsumer.class)
public class DefaultLongPollingConsumer implements LongPollingUpdateConsumer, InitializingBean {

    protected final TelegramContextInitializer telegramContextInitializer;
    protected final TelegramUpdateHandler telegramUpdateHandler;
    protected final LongPollingReplier longPollingReplier;

    @Setter
    @Getter
    private ExecutorService updateExecutor;

    @Setter
    @Getter
    private ObjectProvider<ObservationRegistry> observationRegistries;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (updateExecutor == null) {
            updateExecutor = Executors.newVirtualThreadPerTaskExecutor();
        }
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            updateExecutor.execute(() -> {
                try {
                    TelegramContext context = telegramContextInitializer.prepare(update);
                    startObservation(context, update);
                    telegramUpdateHandler
                            .process(context, update)
                            .ifPresent(longPollingReplier::reply);
                }
                finally {
                    TelegramContextHolder.clearContext();
                }
            });
        }
    }

    protected void startObservation(TelegramContext context, Update update) {
        observationRegistries.ifAvailable(registry -> {
            Observation observation = registry.getCurrentObservation();
            if (observation != null) {
                observation.lowCardinalityKeyValue("tg_update_id", String.valueOf(update.getUpdateId()));
                MDC.put("tg_update_id", String.valueOf(update.getUpdateId()));

                observation.lowCardinalityKeyValue("tg_chat_id", String.valueOf(context.getChatId()));
                MDC.put("tg_chat_id", String.valueOf(context.getChatId()));

                observation.lowCardinalityKeyValue("tg_user_id", String.valueOf(context.getUserFrom().getId()));
                MDC.put("tg_user_id", String.valueOf(context.getUserFrom().getId()));
            }
        });
    }

}
