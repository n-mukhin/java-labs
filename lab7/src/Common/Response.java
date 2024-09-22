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

    public int getMessageAsInt() {
        try {
            return Integer.parseInt(message);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The message cannot be converted to an integer: " + message, e);
        }
    }

}
