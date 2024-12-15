package dev.scaraz.lib.spring.telegram.config.longpolling;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import lombok.Setter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.InitializingBean;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.BackOff;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.ExponentialBackOff;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

@Setter
public class LongPollingApplication implements InitializingBean {

    private BackOff backOff;
    private ObjectMapper objectMapper;
    private OkHttpClient okHttpClient;
    private ScheduledExecutorService scheduler;
    private ObservationRegistry observationRegistry;


    private final ConcurrentHashMap<String, BotSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        if (observationRegistry == null) {
            observationRegistry = ObservationRegistry.NOOP;
        }

        if (okHttpClient == null) {
            TelegramOkHttpClientFactory.DefaultOkHttpClientCreator factory = new TelegramOkHttpClientFactory.DefaultOkHttpClientCreator();
            okHttpClient = factory.get();
        }

        if (scheduler == null) {
            scheduler = new LongPollingExecutorService(1, observationRegistry);
        }

        if (backOff == null) {
            backOff = new ExponentialBackOff();
        }
    }

    public BotSession register(String botToken,
                               TelegramUrl telegramUrl,
                               Function<Integer, GetUpdates> getUpdatesGenerator,
                               LongPollingUpdateConsumer updateConsumer
    ) throws TelegramApiException {
        if (sessions.containsKey(botToken)) {
            throw new TelegramApiException("Bot is already registered");
        }


        BotSession session = new BotSession(
                objectMapper,
                okHttpClient,
                scheduler,
                botToken,
                () -> telegramUrl,
                getUpdatesGenerator,
                () -> backOff,
                updateConsumer
        );

        sessions.put(botToken, session);
        return session;
    }

}
