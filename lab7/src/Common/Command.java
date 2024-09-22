package Common;

import java.io.Serializable;

public class Command implements Serializable {
    private CommandType type;
    private Object payload;

    public Command(CommandType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public CommandType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public Object getArgument() {
        return payload;
    }
}
