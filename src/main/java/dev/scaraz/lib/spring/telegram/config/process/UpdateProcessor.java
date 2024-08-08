package dev.scaraz.lib.spring.telegram.config.process;

import dev.scaraz.lib.spring.telegram.bind.TelegramHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.enums.ChatSource;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

public abstract class UpdateProcessor implements ApplicationContextAware {

    protected ApplicationContext applicationContext;



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void additionalHandlerProcess(TelegramContext context, TelegramHandlerExecutor executor) {
    }

    public abstract UpdateType type();

    public abstract Optional<TelegramHandler> getHandler(Update update);

    public abstract Long getChatId(Update update);

    public abstract ChatSource getChatSource(Update update);

    public abstract User getUserFrom(Update update);

}
