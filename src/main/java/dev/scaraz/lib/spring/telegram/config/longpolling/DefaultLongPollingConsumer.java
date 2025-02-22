package dev.scaraz.lib.spring.telegram.config.longpolling;

import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import dev.scaraz.lib.spring.telegram.config.TelegramContextHolder;
import dev.scaraz.lib.spring.telegram.config.TelegramContextInitializer;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AccessLevel;
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
import java.util.function.Consumer;

@Slf4j
//@Component
@RequiredArgsConstructor
//@ConditionalOnMissingBean(LongPollingUpdateConsumer.class)
public class DefaultLongPollingConsumer implements LongPollingUpdateConsumer, InitializingBean {

    protected final TelegramContextInitializer telegramContextInitializer;
    protected final TelegramUpdateHandler telegramUpdateHandler;
    protected final LongPollingReplier longPollingReplier;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private ExecutorService updateExecutor;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private ObjectProvider<ObservationRegistry> observationRegistries;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (updateExecutor == null) {
            updateExecutor = Executors.newVirtualThreadPerTaskExecutor();
        }

        observationRegistries.ifAvailable(obs -> {
            log.trace("include observation registry for tracing");
        });
    }

    @Override
    public void consume(List<Update> updates) {
        startObservationIfAvailable(observation -> {
            updates.forEach(update -> updateExecutor.execute(() -> {
                try {
                    TelegramContext context = telegramContextInitializer.prepare(update);
                    attachObservationContext(context, update, observation);

                    if (log.isDebugEnabled()) {
                        log.debug("received update - {}", update);
                    } else {
                        log.info("received update - {}", update.getUpdateId());
                    }

                    telegramUpdateHandler
                            .process(context, update)
                            .ifPresent(longPollingReplier::reply);
                }
                finally {
                    TelegramContextHolder.clearContext();
                }
            }));
        });
    }

    protected void attachObservationContext(TelegramContext context, Update update, Observation observation) {
        if (observation != null) {
            observation.lowCardinalityKeyValue("tg_update_id", String.valueOf(update.getUpdateId()));
            MDC.put("tg_update_id", String.valueOf(update.getUpdateId()));

            observation.lowCardinalityKeyValue("tg_chat_id", String.valueOf(context.getChatId()));
            MDC.put("tg_chat_id", String.valueOf(context.getChatId()));

            observation.lowCardinalityKeyValue("tg_user_id", String.valueOf(context.getUserFrom().getId()));
            MDC.put("tg_user_id", String.valueOf(context.getUserFrom().getId()));
        }
    }

    protected void startObservationIfAvailable(Consumer<Observation> runnable) {
        ObservationRegistry observationRegistry = observationRegistries.getIfAvailable();
        if (observationRegistry != null) {
            Observation obs = Observation.createNotStarted("telegram-updates", observationRegistry);
            runnable.accept(obs);
        }
        else {
            runnable.accept(null);
        }
    }

}
