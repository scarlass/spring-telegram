package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.arg.ChatId;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;

import java.lang.annotation.Annotation;
import java.util.List;

public class ChatIdArgResolver implements TelegramAnnotationArgResolver {

    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(ChatId.class);
    }

    @Override
    public Long resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        return context.getChatId();
    }
}
