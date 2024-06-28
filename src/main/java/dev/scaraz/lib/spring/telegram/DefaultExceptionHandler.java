package dev.scaraz.lib.spring.telegram;

import dev.scaraz.lib.spring.telegram.bind.TelegramExceptionHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
class DefaultExceptionHandler implements TelegramExceptionHandler {
    @Override
    public PartialBotApiMethod<?> catchException(TelegramContext context, TelegramHandlerExecutor executor, Throwable ex) {
        log.warn("Execution error:", ex);
        return SendMessage.builder()
                .chatId(context.getChatId())
                .parseMode(ParseMode.MARKDOWNV2)
                .text(TelegramUtil.exception(ex))
                .build();
    }
}
