package dev.scaraz.lib.spring.telegram.bind.enums;

public enum ChatSource {
    PRIVATE,
    GROUP,
    SUPERGROUP,
    CHANNEL;

    public static ChatSource fromType(String type) {
        return ChatSource.valueOf(type.toUpperCase());
    }
}
