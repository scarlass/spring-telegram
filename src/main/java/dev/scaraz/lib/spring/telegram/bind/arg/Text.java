package dev.scaraz.lib.spring.telegram.bind.arg;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Text {
}
