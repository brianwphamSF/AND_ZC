package zomeapp.com.zomechat.application;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.Socket;
import zomeapp.com.zomechat.models.ChatroomMessages;
import zomeapp.com.zomechat.models.ChatsList;
import zomeapp.com.zomechat.models.Feed;
import zomeapp.com.zomechat.models.FeedResponse;
import zomeapp.com.zomechat.utils.ZomeUtils;

/**
 * Created by tkiet082187 on 28.10.15.
 */
public class ZomeApplication extends Application {
    public IMapController mIMapController;
    public MapView mapView;
//    public final String serverUrl = "http://zomeressurection-brianwpham.rhcloud.com:1442";
    public final String serverUrl = "http://10.0.2.2:1442";
//    public final String serverUrl = "http://ec2-54-205-59-87.compute-1.amazonaws.com:1442";
//    public final String serverUrl = "http://ec2-54-204-21-144.compute-1.amazonaws.com:443";
    public Socket mSocket;
    public Double lat, lng;
    public boolean isBackPressed = false;
    public GoogleApiClient mGoogleApiClient;
    public final int REQUEST_CHECK_SETTINGS = 0x1;

    public ZomeUtils mZomeUtils;

    public int getSplashTimeOut() {
        return 3000;
    }

    public ArrayList<ChatsList> getChatsList(JSONArray chatsArray) {
        ArrayList<ChatsList> chatsList = new ArrayList<>();

        for (int i = 0; i < chatsArray.length(); i++) {
            try {
                ChatsList chats = new ChatsList(
                        //chatsArray.getJSONObject(i).getDouble("lat"),
                        //chatsArray.getJSONObject(i).getDouble("lng"),
                        lat,
                        lng,
                        chatsArray.getJSONObject(i).getString("roomName"),
                        chatsArray.getJSONObject(i).getString("roomType"),
                        chatsArray.getJSONObject(i).getString("roomKey")
                );

                chatsList.add(chats);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return chatsList;
    }

    public ArrayList<Feed> getFeeds(JSONArray array) {
        ArrayList<Feed> feeds = new ArrayList<>();

        //TODO format time so that it becomes more human readable
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        for (int i = 0; i < array.length(); i++) {

            try {
                String parseTime = sdf.format(array.getJSONObject(i).getLong("time"));
                Feed feed = new Feed(array.getJSONObject(i).getString("ownerImageURL"),
                        array.getJSONObject(i).getString("imageURL"),
                        array.getJSONObject(i).getString("ownerName"),
                        array.getJSONObject(i).getString("content"),
                        parseTime,
                        array.getJSONObject(i).getString("postId"),
                        array.getJSONObject(i).getInt("likesCount"),
                        array.getJSONObject(i).getInt("commentCount"));
                feeds.add(feed);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return feeds;
    }

    public ArrayList<FeedResponse> getResponses(JSONArray jsonArray) {
        ArrayList<FeedResponse> responses = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                responses.add(new FeedResponse(
                        jsonArray.getJSONObject(i).getString("content"),
                        sdf.format(jsonArray.getJSONObject(i).getLong("time")),
                        jsonArray.getJSONObject(i).getString("ownerImageURL"),
                        jsonArray.getJSONObject(i).getString("ownerName"),
                        jsonArray.getJSONObject(i).getString("commentId")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return responses;
    }

    public ArrayList<ChatroomMessages> getChatMessages(JSONArray jsonArray) {
        ArrayList<ChatroomMessages> messages = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String parseTime = sdf.format(jsonArray.getJSONObject(i).getLong("time"));
                messages.add(new ChatroomMessages(
                        jsonArray.getJSONObject(i).getString("message"),
                        jsonArray.getJSONObject(i).getBoolean("isImage"),
                        parseTime,
                        jsonArray.getJSONObject(i).getString("senderId"),
                        jsonArray.getJSONObject(i).getString("senderImageURL"),
                        jsonArray.getJSONObject(i).getString("senderName"),
                        jsonArray.getJSONObject(i).getInt("messageId")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return messages;
    }
}
