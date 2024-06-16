package dev.scaraz.lib.spring.telegram.bind;

import dev.scaraz.mars.v2.lib.telegram.bind.enums.MessageSource;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@ToString
public class TelegramCmdMessage {

    @Getter
    private final MessageSource source;

    private final boolean isCommand;

    /**
     * Current command received from user.
     */
    @Getter
    private final String command;

    /**
     * Command arguments.
     */
    @Getter
    private final String argument;

    public TelegramCmdMessage(Update update) {
        Message message = update.getMessage();

        boolean fromCaption = message.getPhoto() != null || message.getDocument() != null;
        this.source = fromCaption ?
                MessageSource.CAPTION :
                MessageSource.TEXT;

        String text = fromCaption ?
                message.getCaption() :
                message.getText();

        MessageEntity commandEntity = getCommandEntity(message);
        if (commandEntity != null && commandEntity.getOffset() == 0) {
            isCommand = true;
            command = commandEntity.getText();
            String temp = text
                    .substring(commandEntity.getLength())
                    .trim();
            if (StringUtils.isBlank(temp)) argument = null;
            else argument = temp;
        }
        else {
            command = null;
            argument = text;
            isCommand = false;
        }

//        this.forwardedFrom = Optional.ofNullable(message.getForwardFrom())
//                .map(User::getId)
//                .orElse(null);
    }


    /**
     * {@code true} if current message is command.
     */
    public boolean isCommand() {
        return isCommand;
    }

    public boolean isFromText() {
        return source == MessageSource.TEXT;
    }

    public boolean isFromCaption() {
        return source == MessageSource.CAPTION;
    }


    private boolean isSlashStart(String message) {
        return message != null && message.trim()
                .startsWith("/");
    }

    private MessageEntity getCommandEntity(Message message) {
        MessageEntity result = null;
        List<MessageEntity> entities = isFromCaption() ?
                message.getCaptionEntities() :
                message.getEntities();

        if (entities != null) {
            for (MessageEntity entity : entities) {
                if (!entity.getType()
                        .equals("bot_command")) continue;
                result = entity;
            }
        }

        return result;
    }
}
