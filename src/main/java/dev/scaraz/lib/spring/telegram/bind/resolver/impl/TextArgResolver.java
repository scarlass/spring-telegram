package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.lib.spring.telegram.bind.TelegramCmdMessage;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.arg.Text;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;

import java.lang.annotation.Annotation;
import java.util.List;

public class TextArgResolver implements TelegramAnnotationArgResolver {

    @Override
    public HandlerType[] handledFor() {
        return new HandlerType[]{HandlerType.MESSAGE};
    }

    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(Text.class);
    }

    @Override
    public String resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        TelegramCmdMessage command = execution.getCommand();
        return command.getArgument();
    }

}
