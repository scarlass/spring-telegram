package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.lib.spring.telegram.bind.TelegramCmdMessage;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.MessageSource;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;

public class MessageSourceArgResolver implements TelegramTypeArgResolver<MessageSource> {

    @Override
    public HandlerType[] handledFor() {
        return new HandlerType[]{HandlerType.MESSAGE};
    }

    @Override
    public MessageSource resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        TelegramCmdMessage command = execution.getCommand();
        return command.getSource();
    }
}
