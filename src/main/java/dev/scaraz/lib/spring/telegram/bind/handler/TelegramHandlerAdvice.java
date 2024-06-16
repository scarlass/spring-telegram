package dev.scaraz.lib.spring.telegram.bind.handler;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component

public @interface TelegramHandlerAdvice {

    @AliasFor(annotation = Component.class, attribute = "value")
    String name() default "";
}
