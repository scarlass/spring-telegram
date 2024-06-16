package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramCmdMessage;
import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.arg.Text;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.HandlerType;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;

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
