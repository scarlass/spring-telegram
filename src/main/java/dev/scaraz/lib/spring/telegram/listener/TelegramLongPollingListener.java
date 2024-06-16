package dev.scaraz.lib.spring.telegram.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scaraz.lib.spring.telegram.TelegramProperties;
import dev.scaraz.lib.spring.telegram.config.TelegramContextHolder;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "telegram",
        name = "type",
        havingValue = "long_polling")
public class TelegramLongPollingListener implements LongPollingUpdateConsumer {

    private final ObjectMapper objectMapper;
    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;
    private final TelegramUpdateProcessor telegramUpdateProcessor;
    private final TelegramBotsLongPollingApplication telegramBotsLongPollingApplication;

    private final TaskExecutor executor = new VirtualThreadTaskExecutor("tg-update-");

    @Override
    public void consume(List<Update> list) {
        for (Update update : list) {
            executor.execute(() -> {
                try {
                    log.info("Received update - {}", objectMapper.writeValueAsString(update));
                }
                catch (JsonProcessingException e) {
                }

                try {
                    telegramUpdateProcessor.process(update).ifPresent(this::reply);
                }
                finally {
                    TelegramContextHolder.clearContext();
                }
            });
        }
    }

    private void reply(PartialBotApiMethod<?> method) {
        try {
            switch (method) {
                case BotApiMethod<?> bam -> telegramClient.execute(bam);
                case SendDocument sd -> telegramClient.execute(sd);
                case SendPhoto sp -> telegramClient.execute(sp);
                case SendVideo sv -> telegramClient.execute(sv);
                case SendVideoNote svn -> telegramClient.execute(svn);
                case SendSticker ss -> telegramClient.execute(ss);
                case SendAudio ss -> telegramClient.execute(ss);
                case SendVoice ss -> telegramClient.execute(ss);
                case SendMediaGroup ss -> telegramClient.execute(ss);
                case SendAnimation ss -> telegramClient.execute(ss);
                default -> log.warn("Unknown method or operation not implemented");
            }
        }
        catch (TelegramApiException ex) {
            log.warn("Reply Sendback Error:", ex);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws TelegramApiException {
        telegramBotsLongPollingApplication.registerBot(telegramProperties.getToken(), this);
        log.info("Telegram long-polling started");
    }
}
