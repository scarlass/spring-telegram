package dev.scaraz.lib.spring.telegram.bind.enums;

public enum HandlerType {
    ALL,
    MESSAGE,
    CALLBACK_QUERY;

    public static HandlerType from(UpdateType type) {
        return switch (type) {
            case MESSAGE -> MESSAGE;
            case CALLBACK_QUERY -> CALLBACK_QUERY;
            default -> ALL;
        };
    }
}
