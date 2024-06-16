package dev.scaraz.lib.spring.telegram.bind.resolver;

import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;


public interface TelegramTypeArgResolver<T> extends TelegramArgResolver {
    static Logger log = LoggerFactory.getLogger(TelegramTypeArgResolver.class);

    @Override
    T resolve(TelegramContext context, int index, TelegramHandlerExecutor execution);

    default Class<T> getType() {
        Optional<ParameterizedType> type = Arrays.stream(getClass().getGenericInterfaces())
                .map(e -> (ParameterizedType) e)
                .filter(e -> TelegramTypeArgResolver.class.isAssignableFrom((Class<?>) e.getRawType()))
                .findFirst();

        return type.map(e -> (Class<T>) e.getActualTypeArguments()[0])
                .orElse(null);
    }

}
