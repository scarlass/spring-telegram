package dev.scaraz.lib.spring.telegram;

import dev.scaraz.lib.spring.telegram.bind.TelegramExceptionHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

class DefaultExceptionHandler implements TelegramExceptionHandler {
    @Override
    public PartialBotApiMethod<?> catchException(TelegramContext context, TelegramHandlerExecutor executor, Throwable throwable) {
        return SendMessage.builder()
                .chatId(context.getChatId())
                .parseMode(ParseMode.MARKDOWNV2)
                .text(TelegramUtil.exception(throwable))
                .build();
    }
}
