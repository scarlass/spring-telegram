package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.arg.MessageId;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;

import java.lang.annotation.Annotation;
import java.util.List;

public class MessageIdArgResolver implements TelegramAnnotationArgResolver {

    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(MessageId.class);
    }

    @Override
    public Object resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        return switch (context.getUpdateType()) {
            case CALLBACK_QUERY -> context.getUpdate().getCallbackQuery().getMessage().getMessageId();
            case MESSAGE -> context.getUpdate().getMessage().getMessageId();
            case null, default -> null;
        };
    }
}
