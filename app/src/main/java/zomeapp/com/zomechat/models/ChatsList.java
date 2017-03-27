package zomeapp.com.zomechat.models;

/**
 * Created by tkiet082187 on 15.10.15.
 */
public class ChatsList {
    private double lat, lng;
    private String roomName;
    private String roomType;
    private String roomKey;

    public ChatsList(double lat, double lng, String roomName, String roomType, String roomKey) {
        this.lat = lat;
        this.lng = lng;
        this.roomName = roomName;
        this.roomType = roomType;
        this.roomKey = roomKey;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getRoomKey() {
        return roomKey;
    }
}
