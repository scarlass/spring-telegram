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