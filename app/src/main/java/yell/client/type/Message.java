package yell.client.type;

import java.util.Date;

/**
 * Created by abdulkerim on 14.05.2016.
 */
public class Message {
    public boolean isSentByThis;
    public String message;
    public String messageType;
    public String date;

    public Message(boolean isSentByThis, String message, String messageType, String date) {
        this.isSentByThis = isSentByThis;
        this.message = message;
        this.messageType = messageType;
        this.date = date;
    }
}
