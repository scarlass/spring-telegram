package dev.scaraz.lib.spring.telegram.config.process;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandler;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.ChatSource;
import dev.scaraz.mars.v2.lib.telegram.config.TelegramHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryUpdateProcessor extends UpdateProcessor {

    private final TelegramHandlerRegistry registry;
    private final AntPathMatcher pathMatcher = new AntPathMatcher(":");

    private Optional<TelegramHandler> findHandler(Update update) {
        String data = update.getCallbackQuery().getData();
        log.trace("Find Query - {}", data);
        for (String path : registry.getCallbackQueryHandlers().keySet()) {
            boolean match = pathMatcher.match(path, data);
            log.trace("[{}] Pattern matcher {} - match ? {}", data, path, match);
            if (match)
                return Optional.ofNullable(registry.getCallbackQueryHandlers().get(path));
        }

        log.debug("using default callback query handler - if any");
        return Optional.ofNullable(registry.getDefaultCallbackQueryHandler());
    }

    @Override
    public Optional<TelegramHandler> getHandler(Update update) {
        return findHandler(update);
    }

    @Override
    public Long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    public ChatSource getChatSource(Update update) {
        return ChatSource.PRIVATE;
    }

    @Override
    public User getUserFrom(Update update) {
        return update.getCallbackQuery().getFrom();
    }

}
