package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.lib.spring.telegram.bind.TelegramCmdMessage;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.arg.TextSource;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.MessageSource;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;

import java.lang.annotation.Annotation;
import java.util.List;

public class TextSourceArgResolver implements TelegramAnnotationArgResolver {
    @Override
    public HandlerType[] handledFor() {
        return new HandlerType[]{HandlerType.MESSAGE};
    }

    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(TextSource.class);
    }

    @Override
    public MessageSource resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        TelegramCmdMessage command = execution.getCommand();
        return command.getSource();
    }
}
