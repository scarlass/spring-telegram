package dev.scaraz.lib.spring.telegram.config;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

import static dev.scaraz.lib.spring.telegram.config.TelegramUpdateProcessor.applyContextAttribute;

public final class TelegramContextHolder {

    static final ThreadLocal<TelegramContext> contextAttribute = new InheritableThreadLocal<>();

    public static void setContext(TelegramContext context) {
        TelegramContextHolder.contextAttribute.set(context);
    }

    public static TelegramContext getContext() {
        return TelegramContextHolder.contextAttribute.get();
    }

    public static void clearContext() {
        contextAttribute.remove();
    }

    public static boolean isAvailable() {
        return contextAttribute.get() != null;
    }


    public static Runnable wrap(Update update, Consumer<TelegramContext> runnable) {
        return () -> {
            setContext(new TelegramContext(update));
            applyContextAttribute(getContext());
            try {
                runnable.accept(getContext());
            }
            finally {
                clearContext();
            }
        };
    }

}
