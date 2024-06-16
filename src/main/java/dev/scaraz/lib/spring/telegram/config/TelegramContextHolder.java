package dev.scaraz.lib.spring.telegram.config;

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

}
