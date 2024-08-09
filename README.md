# Spring-Telegram

## Build
### Prerequisite
- Java 21
- maven

clone this repository to your local and then run
```
mvn clean install
```


## Usage
add spring properties:
```yml
spring:
  telegram:
    token: <your_bot_token>
    username: <your_bot_username>
```

in your root application:
```java
import dev.scaraz.lib.spring.telegram.EnableTelegramBot;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableTelegramBot
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // ...
    }

}
```

then create new class and mark it as `TelegramController`

```java

import dev.scaraz.lib.spring.telegram.bind.arg.CallbackData;
import dev.scaraz.lib.spring.telegram.bind.arg.ChatId;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerCallbackQuery;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerCommand;
import dev.scaraz.lib.spring.telegram.bind.handler.HandlerMessage;
import dev.scaraz.lib.spring.telegram.bind.handler.TelegramController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@TelegramController
public class GeneralListener {

    // Add command
    @HandlerCommand("/start")
    public void start(User user) {
        // ...
    }

    // send back reply
    @HandlerCommand("/help")
    public SendMessage help(@ChatId long chatId) {
        // ...
        return SendMessage.builder()
                .chatId(chatId)
                .text("hello")
                .build();
    }

    // General message handler
    @HandlerMessage
    public void generalMessage(Message message) {
        // ...
    }

    // General callback query handler
    @HandlerCallbackQuery
    public void generalCallbackQuery(CallbackQuery callbackQuery) {
        // ...
    }


    @HandlerCallbackQuery({
            "ANSWER:TRUE",
            "ANSWER:FALSE",
            // support * for path matching
            "USER:SEX:*"
    })
    public void answerACallbackQuery(@CallbackData String data) {
        if (data.startsWith(("ANSWER:"))) {
            // ...
        }
        else if (data.startsWith("USER:SEX:")) {
            boolean isMale = data.substring("USER:SEX:".length()).equals("MALE");
            // ...
        }
    }

}
```

### Handler parameter resolver
From the example above, each handler parameters are resolved base on its type or parameter annotation.
You may add additional parameter resolver as `spring bean`.

example parameter annotation resolver:

```java
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramAnnotationArgResolver;
import dev.scaraz.lib.spring.telegram.bind.resolver.TelegramTypeArgResolver;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UserFullNameResolver implements TelegramAnnotationArgResolver {
    public @interface FullName {
    }

    // specify supported annotation 
    @Override
    public List<Class<? extends Annotation>> supportedAnnotations() {
        return List.of(FullName.class);
    }

    @Override
    public String resolve(TelegramContext context, int index, TelegramHandlerExecutor execution) {
        User user = switch (context.getHandlerType()) {
            case ALL, MESSAGE -> context.getUpdate().getMessage()
                    .getFrom();
            case CALLBACK_QUERY -> context.getUpdate().getCallbackQuery()
                    .getFrom();
        };

        return Stream.of(user.getFirstName(), user.getLastName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

    }
}
```

```java
// in telegram controller

import dev.scaraz.lib.spring.telegram.bind.handler.HandlerMessage;

public class TelegramListener {

    // ...
    @HandlerMessage
    public void message(@UserFullNameResolver.FullName String userFullName) {
        // print user full name
        System.out.println(userFullName);
    }
}

```
