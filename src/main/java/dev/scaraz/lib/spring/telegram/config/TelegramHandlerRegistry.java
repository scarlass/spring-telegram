package dev.scaraz.lib.spring.telegram.config;


import dev.scaraz.lib.spring.telegram.bind.TelegramHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramInterceptor;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerCallbackQuery;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerCommand;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerMessage;
import dev.scaraz.lib.spring.telegram.bind.handler.TelegramController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramHandlerRegistry implements BeanPostProcessor {

    private final Map<String, Class<?>> BOT_CONTROLLERS = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> cls = bean.getClass();
        TelegramController controller = AnnotatedElementUtils.findMergedAnnotation(cls, TelegramController.class);
        if (controller != null)
            BOT_CONTROLLERS.put(beanName, cls);

        if (TelegramInterceptor.class.isAssignableFrom(cls))
            interceptors.add((TelegramInterceptor) bean);

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (BOT_CONTROLLERS.containsKey(beanName)) {
            Class<?> type = BOT_CONTROLLERS.get(beanName);
            for (Method method : type.getDeclaredMethods()) {
                boolean skip = Modifier.isStatic(method.getModifiers()) || Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(method.getModifiers());
                if (skip) continue;

                if (hasAnnotatedElement(method, HandlerMessage.class)) {
                    setDefaultMessageHandler(bean, method);
                }
                else if (hasAnnotatedElement(method, HandlerCommand.class)) {
                    addCommandHandler(AnnotatedElementUtils.findMergedAnnotation(method, HandlerCommand.class), bean, method);
                }
                else if (hasAnnotatedElement(method, HandlerCallbackQuery.class)) {
                    HandlerCallbackQuery handlerCallbackQuery = AnnotatedElementUtils.findMergedAnnotation(method, HandlerCallbackQuery.class);
                    if (handlerCallbackQuery.callbacks().length == 0)
                        setDefaultCallbackQueryHandler(bean, method);
                    else
                        addCallbackQueryHandler(handlerCallbackQuery, bean, method);
                }

            }

            BOT_CONTROLLERS.remove(beanName);
        }
        return bean;
    }


    private boolean hasAnnotatedElement(Method method, Class<? extends Annotation> annotationType) {
        return method.getAnnotation(annotationType) != null;
    }

// Registry

    private boolean sortedInterceptors = false;
    private final List<TelegramInterceptor> interceptors = new ArrayList<>();
    private final Map<String, TelegramHandler> commandHandlers = new HashMap<>();
    private final Map<String, TelegramHandler> callbackQueryHandlers = new HashMap<>();

    @Getter
    private TelegramHandler defaultCallbackQueryHandler;
    @Getter
    private TelegramHandler defaultMessageHandler;

    public synchronized List<TelegramInterceptor> getInterceptors() {
        if (!sortedInterceptors) {
            interceptors.sort(AnnotationAwareOrderComparator.INSTANCE);
            sortedInterceptors = true;
        }

        return Collections.unmodifiableList(interceptors);
    }

    public Map<String, TelegramHandler> getCommandHandlers() {
        return Collections.unmodifiableMap(commandHandlers);
    }

    public Map<String, TelegramHandler> getCallbackQueryHandlers() {
        return Collections.unmodifiableMap(callbackQueryHandlers);
    }

    public void setDefaultCallbackQueryHandler(Object bean, Method method) {
        if (defaultMessageHandler != null)
            log.info("Overriding default CallbackQuery handler");
        this.defaultCallbackQueryHandler = new TelegramHandler(bean, method);
    }

    public void setDefaultMessageHandler(Object bean, Method method) {
        if (defaultMessageHandler != null)
            log.info("Overriding default Message handler");
        this.defaultMessageHandler = new TelegramHandler(bean, method);
    }

    public void addCommandHandler(HandlerCommand ant, Object bean, Method method) {
        String[] commands = ant.commands();
        log.trace("Add {} command(s) - {}", commands.length, commands);
        for (String command : commands)
            commandHandlers.put(command, new TelegramHandler(bean, method));
    }

    public void addCallbackQueryHandler(HandlerCallbackQuery ant, Object bean, Method method) {
        String[] callbacks = ant.callbacks();
        log.trace("Add {} callback(s) - {}", callbacks.length, callbacks);
        for (String callback : callbacks)
            callbackQueryHandlers.put(callback, new TelegramHandler(bean, method));
    }

}
