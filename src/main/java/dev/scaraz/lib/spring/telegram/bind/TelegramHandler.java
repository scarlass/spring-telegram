package dev.scaraz.lib.spring.telegram.bind;

import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramArgResolver;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class TelegramHandler {
    private final Object bean;
    private final Method method;

    @Getter(AccessLevel.NONE)
    private final Map<Integer, TelegramArgResolver> argumentResolvers;

    public TelegramHandler(Object bean, Method method) {
        this.bean = bean;
        this.method = method;

        this.argumentResolvers = new LinkedHashMap<>();
    }

    public void setParameterResolver(int index, TelegramArgResolver resolver) {
        argumentResolvers.put(index, resolver);
    }

    public TelegramArgResolver getParameterResolver(int index) {
        if (!argumentResolvers.containsKey(index)) return null;
        return argumentResolvers.get(index);
    }

}
