package dev.scaraz.lib.spring.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.telegram")
public class TelegramProperties {
    private BotType type = BotType.LONG_POLLING;
    private String token;
    private String name;

    private final LongPolling longPolling = new LongPolling();

    @Getter
    @Setter
    public static class LongPolling {
        private boolean startOnReady = true;
    }
}
