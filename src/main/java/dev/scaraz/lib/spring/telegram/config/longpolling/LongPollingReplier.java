package dev.scaraz.lib.spring.telegram.config.longpolling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
class LongPollingReplier {

    private final TelegramClient telegramClient;

    public void reply(PartialBotApiMethod<?> method) {
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
