package dev.scaraz.lib.spring.telegram.bind.resolver;


import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;

@FunctionalInterface
public interface TelegramArgResolver {

    default HandlerType[] handledFor() {
        return new HandlerType[]{HandlerType.ALL};
    }

    default boolean isHandlerSupported(HandlerType type) {
        for (HandlerType t : handledFor())
            if (t == type) return true;
        return false;
    }

    Object resolve(TelegramContext context, int index, TelegramHandlerExecutor execution);

}
