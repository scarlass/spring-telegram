package dev.scaraz.lib.spring.telegram.bind.resolver;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramCmdMessage;
import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.HandlerType;
import dev.scaraz.mars.v2.lib.telegram.bind.resolver.impl.*;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;

import java.util.List;

public final class GeneralArgResolvers {

    public static final List<TelegramTypeArgResolver<?>> GENERIC_RESOLVER_BY_TYPE = List.of(
            new ContextArgResolver(),
            new UpdateArgResolver(),
            new UserArgResolver(),
            new MessageArgResolver(),
            new MessageMetadataArgResolver(),
            new CallbackQueryArgResolver(),
            new MessageSourceArgResolver()
    );

    public static final List<TelegramAnnotationArgResolver> GENERIC_RESOLVER_BY_ANNOTATION = List.of(
            new ChatIdArgResolver(),
            new UserIdArgResolver(),
            new CallbackDataArgResolver(),
            new MessageIdArgResolver(),
            new TextArgResolver(),
            new TextSourceArgResolver()
    );

    public static class ContextArgResolver implements TelegramTypeArgResolver<TelegramContext> {
        @Override
        public TelegramContext resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
            return context;
        }
    }

    public static class UpdateArgResolver implements TelegramTypeArgResolver<Update> {
        @Override
        public Update resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
            return context.getUpdate();
        }
    }

    public static class CallbackQueryArgResolver implements TelegramTypeArgResolver<CallbackQuery> {

        @Override
        public HandlerType[] handledFor() {
            return new HandlerType[]{HandlerType.CALLBACK_QUERY};
        }

        @Override
        public CallbackQuery resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
            return context.getUpdate().getCallbackQuery();
        }
    }

    public static class MessageArgResolver implements TelegramTypeArgResolver<MaybeInaccessibleMessage> {
        @Override
        public MaybeInaccessibleMessage resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
            return switch (context.getUpdateType()) {
                case MESSAGE -> context.getUpdate().getMessage();
                case CALLBACK_QUERY -> context.getUpdate().getCallbackQuery().getMessage();
            };
        }
    }

    public static class MessageMetadataArgResolver implements TelegramTypeArgResolver<TelegramCmdMessage> {

        @Override
        public HandlerType[] handledFor() {
            return new HandlerType[]{HandlerType.MESSAGE};
        }

        @Override
        public TelegramCmdMessage resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
            return execution.getCommand();
        }
    }

}
