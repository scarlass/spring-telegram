package dev.scaraz.lib.spring.telegram;

import dev.scaraz.lib.spring.telegram.bind.TelegramExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
@ComponentScan(basePackages = "dev.scaraz.lib.spring.telegram.config")
public class TelegramSpringConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TelegramExceptionHandler telegramExceptionHandler() {
        return new DefaultExceptionHandler();
    }

}
