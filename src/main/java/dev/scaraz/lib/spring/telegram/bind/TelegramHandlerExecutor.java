package dev.scaraz.lib.spring.telegram.bind;

import dev.scaraz.lib.spring.telegram.bind.enums.HandlerType;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.MethodParameter;

@Getter
@Setter
public class TelegramHandlerExecutor {

    private final HandlerType[] type;

    private final TelegramHandler handler;

    private final MethodParameter[] parameters;

    private TelegramCmdMessage command;

    public TelegramHandlerExecutor(
            UpdateType type,
            TelegramHandler handler
    ) {
        this.type = new HandlerType[]{HandlerType.ALL, HandlerType.from(type)};
        this.handler = handler;

        int parameterCount = handler.getMethod().getParameterCount();
        this.parameters = new MethodParameter[parameterCount];

        for (int i = 0; i < parameterCount; i++)
            this.parameters[i] = new MethodParameter(handler.getMethod(), i);
    }

    public int parameterCount() {
        return parameters.length;
    }

    public MethodParameter getParameter(int index) {
        return parameters[index];
    }

}
