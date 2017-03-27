package zomeapp.com.zomechat.models;

/**
 * Created by tkiet082187 on 30.10.15.
 */
public class ChatroomMessages {
    private String message;
    private boolean isImage;
    private String time;
    private String senderId;
    private String senderImageURL;
    private String senderName;
    private int messageId;

    public ChatroomMessages(String message, boolean isImage, String time, String senderId, String senderImageURL, String senderName, int messageId) {
        this.message = message;
        this.isImage = isImage;
        this.time = time;
        this.senderId = senderId;
        this.senderImageURL = senderImageURL;
        this.senderName = senderName;
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isImage() {
        return isImage;
    }

    public String getTime() {
        return time;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderImageURL() {
        return senderImageURL;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getMessageId() {
        return messageId;
    }
}
