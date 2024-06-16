package dev.scaraz.lib.spring.telegram.bind;

import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;

import java.util.Optional;

public interface TelegramInterceptor {

    default boolean intercept(TelegramContext context, TelegramHandlerExecutor executor) {
        return true;
    }

    default void postProcess(TelegramContext context, TelegramHandlerExecutor executor, Optional<PartialBotApiMethod<?>> result) {};

}
