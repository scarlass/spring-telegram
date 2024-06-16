package dev.scaraz.lib.spring.telegram.config;

import dev.scaraz.lib.spring.telegram.bind.resolver.GeneralArgResolvers;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class TelegramArgResolverRegistry implements BeanPostProcessor {

    private final Set<TelegramTypeArgResolver<?>> typeArgResolvers = new HashSet<>(GeneralArgResolvers.GENERIC_RESOLVER_BY_TYPE);

    private final Set<TelegramAnnotationArgResolver> annotationArgResolvers = new HashSet<>(GeneralArgResolvers.GENERIC_RESOLVER_BY_ANNOTATION);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        if (TelegramTypeArgResolver.class.isAssignableFrom(aClass))
            typeArgResolvers.add((TelegramTypeArgResolver<?>) bean);
        else if (TelegramAnnotationArgResolver.class.isAssignableFrom(aClass))
            annotationArgResolvers.add((TelegramAnnotationArgResolver) bean);

        return bean;
    }

    public Set<TelegramAnnotationArgResolver> getAnnotationArgResolvers() {
        return Collections.unmodifiableSet(annotationArgResolvers);
    }

    public Set<TelegramTypeArgResolver<?>> getTypeArgResolvers() {
        return Collections.unmodifiableSet(typeArgResolvers);
    }
}
