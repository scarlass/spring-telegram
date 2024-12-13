package dev.scaraz.lib.spring.telegram.config;

import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.config.process.UpdateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramContextInitializer {

    private final TelegramUpdateProcessorRegistry updateProcessorRegistry;

    protected UpdateType getUpdateType(Update update) {
        if (update.hasMessage())
            return UpdateType.MESSAGE;
        else if (update.hasCallbackQuery())
            return UpdateType.CALLBACK_QUERY;
        return null;
    }

    public TelegramContext prepare(Update update) {
        TelegramContext context = new TelegramContext(update);

        UpdateType type = getUpdateType(update);
        context.setAttribute(TelegramContext.update_type, type);

        UpdateProcessor processor = updateProcessorRegistry.getProcessor(type);
        if (processor != null) {
//            context.setAttribute(TelegramContext.initialized, true);
            context.setAttribute(TelegramContext.processor, processor);
            context.setAttribute(TelegramContext.handler_type, HandlerType.from(type));
            context.setAttribute(TelegramContext.chat_id, processor.getChatId(update));
            context.setAttribute(TelegramContext.chat_source, processor.getChatSource(update));
            context.setAttribute(TelegramContext.user_from, processor.getUserFrom(update));
        }

        TelegramContextHolder.setContext(context);
        return context;
    }

}
