package dev.scaraz.lib.spring.telegram.bind.handler;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerCommand {

    @AliasFor("commands")
    String[] value() default {};
    @AliasFor("value")
    String[] commands() default {};
}
