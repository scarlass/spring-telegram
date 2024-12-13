package dev.scaraz.lib.spring.telegram.config;


import dev.scaraz.lib.spring.telegram.bind.TelegramExceptionHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.TelegramInterceptor;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramArgResolver;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.lib.spring.telegram.config.process.UpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler implements InitializingBean {

    static TelegramUpdateHandler instance;

    private final TelegramHandlerRegistry handlerRegistry;
    private final TelegramArgResolverRegistry argResolverRegistry;
    private final TelegramExceptionHandler exceptionHandler;

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    @Deprecated
    public Optional<PartialBotApiMethod<?>> process(Update update) {
        return process(TelegramContextHolder.getContext(), update);
    }

    public Optional<PartialBotApiMethod<?>> process(TelegramContext context, Update update) {
        UpdateType type = context.getUpdateType();
        log.debug("Process Update type - {}", type);

        UpdateProcessor processor = context.getProcessor();
        if (processor == null) return noHandlerCanProcess();

        Optional<TelegramHandler> handler = processor.getHandler(update);
        if (handler.isEmpty()) return noHandlerCanProcess();

        TelegramHandlerExecutor executor = new TelegramHandlerExecutor(type, handler.get());
        processor.additionalHandlerProcess(context, executor);

        try {
            log.debug("Interceptor check");
            Collection<TelegramInterceptor> allowExecution = interceptExecution(context, executor);
            if (allowExecution.isEmpty()) {
                return Optional.empty();
            }

            Optional<PartialBotApiMethod<?>> result = Optional.empty();
            try {
                Object[] arguments = mapArgument(context, executor);
                result = invokeMethod(executor, arguments);
                context.setAttribute(TelegramContext.end_with_error, false);
                return result;
            }
            catch (Throwable ex) {
                context.setAttribute(TelegramContext.end_with_error, true);
                throw ex;
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

    protected Optional<PartialBotApiMethod<?>> invokeMethod(TelegramHandlerExecutor executor,
                                                            Object[] args) throws Throwable {
        Method method = executor.getHandler().getMethod();
        Class<?> returnType = method.getReturnType();
        try {
            Object invoke = method.invoke(executor.getHandler().getBean(), args);
            if (ClassUtils.isAssignable(Void.class, returnType))
                return Optional.empty();
            else if (ClassUtils.isAssignable(PartialBotApiMethod.class, returnType))
                return Optional.ofNullable((PartialBotApiMethod<?>) invoke);
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
        if (executor.getHandler().getParameterResolver(index) != null) {
            TelegramArgResolver resolver = executor.getHandler().getParameterResolver(index);
            if (resolver instanceof TelegramAnnotationArgResolver ant)
                return resolveArgument(context, executor, executor.getParameter(index), ant);
            else if (resolver instanceof TelegramTypeArgResolver<?> arg)
                return resolveArgument(context, executor, executor.getParameter(index), arg);
        }

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

                if (resolverOptional.isPresent())
                    return resolveArgument(context, executor, parameter, resolverOptional.get());
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
                    return resolveArgument(context, executor, parameter, resolverOptional.get());
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

    protected Object resolveArgument(TelegramContext context,
                                     TelegramHandlerExecutor executor,
                                     MethodParameter parameter,
                                     TelegramAnnotationArgResolver resolver) {
        executor.getHandler().setParameterResolver(parameter.getParameterIndex(), resolver);

        Object value = resolver.resolve(context, parameter.getParameterIndex(), executor);
        if (value != null) {
            if (ClassUtils.isAssignable(value.getClass(), parameter.getParameterType()))
                return value;
            throw new IllegalArgumentException(String.format("Invalid parameter type (%s), resolver return class (%s) are different from parameter class.",
                    parameter.getParameterType(),
                    value.getClass()
            ));
        }

        return null;
    }

    protected Object resolveArgument(TelegramContext context,
                                     TelegramHandlerExecutor executor,
                                     MethodParameter parameter,
                                     TelegramTypeArgResolver<?> resolver) {
        executor.getHandler().setParameterResolver(parameter.getParameterIndex(), resolver);
        return resolver.resolve(context, parameter.getParameterIndex(), executor);
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
