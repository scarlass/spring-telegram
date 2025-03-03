package dev.scaraz.lib.spring.telegram.bind.resolver.impl;

import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.telegram.telegrambots.meta.api.objects.User;

public class UserArgResolver implements TelegramTypeArgResolver<User> {
    @Override
    public User resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        return context.getUserFrom();
    }
}
