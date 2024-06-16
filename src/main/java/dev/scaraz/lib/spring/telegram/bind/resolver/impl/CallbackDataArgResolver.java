package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.arg.CallbackData;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.lang.annotation.Annotation;
import java.util.List;

public class CallbackDataArgResolver implements TelegramAnnotationArgResolver {

    @Override
    public HandlerType[] handledFor() {
        return new HandlerType[]{HandlerType.CALLBACK_QUERY};
    }

    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(CallbackData.class);
    }

    @Override
    public String resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        CallbackQuery callbackQuery = context.getUpdate().getCallbackQuery();
        return callbackQuery.getData();
    }
}
