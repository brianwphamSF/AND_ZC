package zomeapp.com.zomechat.models;

/**
 * Created by tkiet082187 on 27.10.15.
 */
public class FeedResponse {
    private String ownerName;
    private String ownerImageURL;
    private String time;
    private String content;
    private String commentId;

    public FeedResponse(String content, String time, String ownerImageURL, String ownerName, String commentId) {
        this.content = content;
        this.time = time;
        this.ownerImageURL = ownerImageURL;
        this.ownerName = ownerName;
        this.commentId = commentId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerImageURL() {
        return ownerImageURL;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public String getCommentId() {
        return commentId;
    }
}
