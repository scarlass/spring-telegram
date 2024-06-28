package dev.scaraz.lib.spring.telegram.bind;

import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;

public interface TelegramExceptionHandler {

    PartialBotApiMethod<?> catchException(TelegramContext context,
                                          TelegramHandlerExecutor executor,
                                          Throwable throwable);

}
