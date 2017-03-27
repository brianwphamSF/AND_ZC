package zomeapp.com.zomechat.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by tkiet082187 on 08.10.15.
 */
public class Feed implements Comparable<Feed> {
    private String ownerImageUrl;
    private String imageUrl;
    private String ownerName;
    private String content;
    private String time;
    private String postId;
    private int heartsCount;
    private int commentsCount;

    public Feed(String ownerImageUrl, String imageUrl, String ownerName, String content, String time, String postId, int heartsCount, int commentsCount) {
        this.ownerImageUrl = ownerImageUrl;
        this.imageUrl = imageUrl;
        this.ownerName = ownerName;
        this.content = content;
        this.time = time;
        this.postId = postId;
        this.heartsCount = heartsCount;
        this.commentsCount = commentsCount;
    }

    public String getPostId() {
        return postId;
    }

    public int getHeartsCount() {
        return heartsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public String getOwnerImageUrl() {
        return ownerImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    @Override
    public int compareTo(Feed f) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        if (getTime() == null || f.getTime() == null) {
            return 0;
        }
        try {
            return sdf.parse(getTime()).compareTo(sdf.parse(f.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "".compareTo("It will never get here anyway ;)");
    }
}
