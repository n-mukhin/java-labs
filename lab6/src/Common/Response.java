package Common;

import java.io.Serializable;


public class Response implements Serializable {
    private String message;
    private ResponseType type;

    public Response(String message, ResponseType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public ResponseType getType() {
        return type;
    }
}
