package dev.scaraz.lib.spring.telegram.bind;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@Data
@Builder
@RequiredArgsConstructor
public class TelegramHandler {
    private final Object bean;
    private final Method method;
}
