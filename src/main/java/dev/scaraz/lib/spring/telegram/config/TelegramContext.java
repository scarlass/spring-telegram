package dev.scaraz.lib.spring.telegram.config;

import dev.scaraz.lib.spring.telegram.bind.enums.ChatSource;
import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.config.process.UpdateProcessor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TelegramContext {
    public static final String update_type = "int:UPDATE_TYPE";
    public static final String handler_type = "int:HANDLER_TYPE";
    public static final String user_from = "int:USER";
    public static final String chat_id = "int:CHAT_ID";
    public static final String chat_source = "int:CHAT_SOURCE";
    public static final String message = "int:MESSAGE";
    public static final String end_with_error = "bool:ERROR_EXECUTION";
    public static final String processor = "tg:UPDATE_PROCESSOR";
    public static final String initialized = "bool:INITIALIZED";

    TelegramContext(Update update) {
        this.update = update;
    }

    @Getter
    private final Update update;
    private final Map<String, Object> attribute = new LinkedHashMap<>();

    public void setAttribute(String key, Object value) {
        this.attribute.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attribute.get(key);
    }

    public UpdateType getUpdateType() {
        return (UpdateType) attribute.get(update_type);
    }

    public HandlerType getHandlerType() {
        return (HandlerType) attribute.get(handler_type);
    }

    public User getUserFrom() {
        return (User) attribute.get(user_from);
    }

    public Long getChatId() {
        return (Long) attribute.get(chat_id);
    }

    public ChatSource getChatSource() {
        return (ChatSource) attribute.get(chat_source);
    }

    public boolean hasErrorExecution() {
        return (boolean) attribute.getOrDefault(end_with_error, false);
    }


//    boolean isInitialized() {
//        return Boolean.TRUE.equals(getAttribute(initialized));
//    }

    UpdateProcessor getProcessor() {
        return (UpdateProcessor) getAttribute(processor);
    }

}
