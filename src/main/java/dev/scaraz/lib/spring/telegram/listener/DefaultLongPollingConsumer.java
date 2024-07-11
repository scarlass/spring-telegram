package dev.scaraz.lib.spring.telegram.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.scaraz.lib.spring.telegram.config.TelegramContextHolder;
import dev.scaraz.lib.spring.telegram.config.TelegramUpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DefaultLongPollingConsumer implements LongPollingUpdateConsumer {

    private final ObjectMapper objectMapper;
    private final TelegramClient telegramClient;
    private final TelegramUpdateProcessor telegramUpdateProcessor;

    private final TaskExecutor executor = new VirtualThreadTaskExecutor("tg-update-");

    @Override
    public void consume(List<Update> list) {
        for (Update update : list) {
            executor.execute(TelegramContextHolder.wrap(update, () -> {
                try {
                    if (log.isDebugEnabled())
                        log.debug("Received update - {}", objectMapper.writeValueAsString(update));
                    else if (log.isInfoEnabled())
                        log.debug("Received update id - {}", update.getUpdateId());
                }
                catch (JsonProcessingException e) {
                }

                telegramUpdateProcessor.process(update);
            }));
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

}
