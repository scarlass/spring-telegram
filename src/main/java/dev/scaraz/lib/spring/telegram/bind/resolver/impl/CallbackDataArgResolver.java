package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.arg.CallbackData;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.HandlerType;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;
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
