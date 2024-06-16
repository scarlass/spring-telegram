package dev.scaraz.lib.spring.telegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TelegramUtil {
    private TelegramUtil() {
    }

    public static final String RESERVED_CHAR_REGX = "([\\[\\])(~`>#+=|{}.!\\-])";
//    public static final String RESERVED_CHAR_REGX = "([)(~`>#+=|{}.!\\-])";


    public static String esc(String... texts) {
        return Arrays.stream(texts)
                .filter(Objects::nonNull)
                .map(t -> Arrays.stream(t.split("\n"))
                        .map(s -> s.replaceAll(RESERVED_CHAR_REGX, "\\\\$1"))
                        .collect(Collectors.joining("\n"))
                )
                .collect(Collectors.joining("\n"));
    }

    public static String esc(List<String> texts) {
        return esc(texts.toArray(String[]::new));
    }

    public static String exception(Throwable throwable) {
        List<String> messageChain = new ArrayList<>();

        String message = throwable.getMessage();
        exceptionRootCause(messageChain, throwable);

        List<String> messageForm = new ArrayList<>(List.of(
                "Error:",
                Objects.requireNonNullElse(message, "-")
        ));

        if (!messageChain.isEmpty()) {
            messageForm.add("");
            messageForm.add("");
            messageForm.addAll(messageChain);
        }

        return esc(messageForm.toArray(String[]::new));
    }

    private static void exceptionRootCause(List<String> messages, Throwable ex) {
        Throwable cause = ex.getCause();
        if (cause != null) {
            messages.add("%s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            exceptionRootCause(messages, cause);
        }
    }

}
