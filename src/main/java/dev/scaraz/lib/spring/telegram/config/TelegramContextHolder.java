package dev.scaraz.lib.spring.telegram.config;

import org.telegram.telegrambots.meta.api.objects.Update;

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


    public static Runnable wrap(Update update, Runnable runnable) {
        return () -> {
            setContext(new TelegramContext(update));
            try {
                runnable.run();
            }
            finally {
                clearContext();
            }
        };
    }

}
