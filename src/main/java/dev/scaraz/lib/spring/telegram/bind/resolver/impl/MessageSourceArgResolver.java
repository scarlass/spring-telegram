package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramCmdMessage;
import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.HandlerType;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.MessageSource;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;

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
