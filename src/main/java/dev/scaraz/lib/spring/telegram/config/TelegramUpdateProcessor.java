package dev.scaraz.lib.spring.telegram.config;


import dev.scaraz.lib.spring.telegram.TelegramUtil;
import dev.scaraz.lib.spring.telegram.bind.*;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.lib.spring.telegram.config.process.CallbackQueryUpdateProcessor;
import dev.scaraz.lib.spring.telegram.config.process.MessageUpdateProcessor;
import dev.scaraz.lib.spring.telegram.config.process.UpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateProcessor {

    private final TelegramHandlerRegistry handlerRegistry;
    private final TelegramArgResolverRegistry argResolverRegistry;
    private final TelegramExceptionHandler exceptionHandler;

    private final MessageUpdateProcessor messageUpdateProcess;
    private final CallbackQueryUpdateProcessor callbackQueryUpdateProcess;

    protected UpdateType getUpdateType(Update update) {
        if (update.hasMessage())
            return UpdateType.MESSAGE;
        else if (update.hasCallbackQuery())
            return UpdateType.CALLBACK_QUERY;
        return null;
    }

    public Optional<PartialBotApiMethod<?>> process(Update update) {
        TelegramContext context = new TelegramContext(update);
        TelegramContextHolder.setContext(context);

        UpdateType type = getUpdateType(update);
        log.debug("Process Update type - {}", type);

        context.setAttribute(TelegramContext.update_type, type);

        UpdateProcessor processor;
        TelegramHandlerExecutor executor;
        Optional<TelegramHandler> handler;
        switch (type) {
            case MESSAGE -> {
                log.debug("Processing Update Message");
                processor = messageUpdateProcess;
                handler = messageUpdateProcess.getHandler(update);
                if (handler.isEmpty()) return noHandlerCanProcess();

                executor = new TelegramHandlerExecutor(type, handler.get());
                executor.setCommand(new TelegramCmdMessage(update));
            }
            case CALLBACK_QUERY -> {
                log.debug("Processing Update CallbackQuery");
                processor = callbackQueryUpdateProcess;
                handler = callbackQueryUpdateProcess.getHandler(update);
                if (handler.isEmpty()) return noHandlerCanProcess();

                executor = new TelegramHandlerExecutor(type, handler.get());
            }
            default -> {
                return Optional.empty();
            }
        }

        assignContextAttribute(context, type, update, processor);

        try {
            log.debug("Interceptor check");
            Collection<TelegramInterceptor> allowExecution = interceptExecution(context, executor);
            if (allowExecution.isEmpty()) {
                return Optional.empty();
            }

            Optional<PartialBotApiMethod<?>> result = Optional.empty();
            try {
                Object[] arguments = mapArgument(context, executor);
                result = invokeMethod(context, executor, arguments);
                return result;
            }
            finally {
                for (TelegramInterceptor ti : allowExecution)
                    ti.postProcess(context, executor, result);
            }
        }
        catch (Throwable ex) {
            return handleInvocationException(ex, context, executor);
        }
    }

    protected void assignContextAttribute(TelegramContext context,
                                          UpdateType type,
                                          Update update,
                                          UpdateProcessor processor) {
        context.setAttribute(TelegramContext.update_type, type);
        context.setAttribute(TelegramContext.handler_type, HandlerType.from(type));
        context.setAttribute(TelegramContext.chat_id, processor.getChatId(update));
        context.setAttribute(TelegramContext.chat_source, processor.getChatSource(update));
        context.setAttribute(TelegramContext.user_from, processor.getUserFrom(update));
//        switch (type) {
//            case MESSAGE, CALLBACK_QUERY -> {
//            }
//            default -> {
//            }
//        }
    }

    protected Optional<PartialBotApiMethod<?>> invokeMethod(TelegramContext context,
                                                            TelegramHandlerExecutor executor,
                                                            Object[] args) throws Throwable {
        Method method = executor.getHandler().getMethod();
        Class<?> returnType = method.getReturnType();
        try {
            Object invoke = method.invoke(executor.getHandler().getBean(), args);
            if (ClassUtils.isAssignable(Void.class, returnType))
                return Optional.empty();
            else if (ClassUtils.isAssignable(PartialBotApiMethod.class, returnType)) {

                return Optional.ofNullable((PartialBotApiMethod<?>) invoke);
            }
            else {
                // TODO: Return Type Resolver ?
                log.warn("Unsupported Handler return type");
            }
        }
        catch (InvocationTargetException e) {
            if (e.getCause() != null) throw e.getCause();
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    protected Optional<PartialBotApiMethod<?>> handleInvocationException(Throwable ex,
                                                                         TelegramContext context,
                                                                         TelegramHandlerExecutor executor) {
        return Optional.of(exceptionHandler.catchException(context, executor, ex));
    }

    protected Collection<TelegramInterceptor> interceptExecution(TelegramContext context, TelegramHandlerExecutor executor) {
        List<TelegramInterceptor> interceptors = handlerRegistry.getInterceptors();

        Set<TelegramInterceptor> postExecution = new HashSet<>();
        for (TelegramInterceptor interceptor : interceptors) {
            boolean shouldContinue = interceptor.intercept(context, executor);
            if (!shouldContinue) {
                log.warn("Update method execution is denied by interceptor ({})", interceptor.getClass());
                return Collections.emptySet();
            }
            else postExecution.add(interceptor);
        }
        return postExecution;
    }

    protected Object[] mapArgument(TelegramContext context, TelegramHandlerExecutor executor) {
        return IntStream.range(0, executor.getParameters().length).boxed()
                .map(index -> resolveMethodArgument(index, context, executor))
                .toArray();
    }

    protected Object resolveMethodArgument(int index,
                                           TelegramContext context,
                                           TelegramHandlerExecutor executor
    ) {
        MethodParameter parameter = executor.getParameter(index);
        Method method = parameter.getMethod();
        Class<?> parameterType = parameter.getParameterType();

        log.debug("GET PARAMETER ARG ({}.{}) AT INDEX {}", method.getDeclaringClass().getSimpleName(), method.getName(), index);

        boolean hasParameterAnnotations = parameter.hasParameterAnnotations();
        List<HandlerType> iterateHandler = List.of(HandlerType.ALL, context.getHandlerType());

        if (hasParameterAnnotations) {
            log.debug(" - resolving argument by applied annotation");
            for (HandlerType handlerType : iterateHandler) {
                log.debug(" - handler argument rotation {}", handlerType);
                Optional<TelegramAnnotationArgResolver> resolverOptional = argResolverRegistry.getAnnotationArgResolvers().stream()
                        .filter(r -> r.isHandlerSupported(handlerType))
                        .filter(r -> hasSupportedAnnotations(parameter, r))
                        .findFirst();

                if (resolverOptional.isPresent()) {
                    Object value = resolverOptional.get().resolve(context, index, executor);
                    if (value != null) {
                        if (ClassUtils.isAssignable(value.getClass(), parameterType))
                            return value;
                        throw new IllegalArgumentException(String.format("Invalid parameter type (%s), resolver return type (%s) are different from declared type.",
                                parameterType,
                                value.getClass()
                        ));
                    }

                    return null;
                }
            }
        }
        else {
            log.debug(" - resolving argument by return type");
            for (HandlerType handlerType : iterateHandler) {
                log.debug(" - handler argument rotation {}", handlerType);
                Optional<TelegramTypeArgResolver<?>> resolverOptional = argResolverRegistry.getTypeArgResolvers().stream()
                        .filter(r -> r.isHandlerSupported(handlerType))
                        .filter(r -> {
                            boolean assignable = ClassUtils.isAssignable(parameterType, r.getType()) || ClassUtils.isAssignable(r.getType(), parameterType);
                            log.trace("is assignable from {} ? {}", r.getType(), assignable);
                            return assignable;
                        })
                        .findFirst();

                if (resolverOptional.isPresent())
                    return resolverOptional.get().resolve(context, index, executor);
            }
        }

        throw new IllegalArgumentException(String.format(
                "Unresolved argument type (%s) for method %s.%s at index %s",
                parameterType,
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                index
        ));
    }

    private boolean hasSupportedAnnotations(MethodParameter mp, TelegramAnnotationArgResolver resolver) {
        return resolver.supportedAnnotations().stream()
                .anyMatch(mp::hasParameterAnnotation);
    }

    private Optional<PartialBotApiMethod<?>> noHandlerCanProcess() {
        log.warn("No handler can process current Update");
        return Optional.empty();
    }

}
