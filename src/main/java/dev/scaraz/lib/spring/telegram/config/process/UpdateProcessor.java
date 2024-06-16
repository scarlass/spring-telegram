package dev.scaraz.lib.spring.telegram.config.process;

import dev.scaraz.mars.v2.lib.telegram.bind.TelegramHandler;
import dev.scaraz.mars.v2.lib.telegram.bind.enums.ChatSource;
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

    public abstract Optional<TelegramHandler> getHandler(Update update);

    public abstract Long getChatId(Update update);

    public abstract ChatSource getChatSource(Update update);

    public abstract User getUserFrom(Update update);

}
