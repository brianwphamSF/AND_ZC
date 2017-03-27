package zomeapp.com.zomechat.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.activities.ChatActivity;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.models.ChatsList;
import zomeapp.com.zomechat.utils.RetrieveCityImage;

/**
 * Created by tkiet082187 on 15.10.15.
 */
public class ChatsGridListAdapter extends RecyclerView.Adapter<ChatsGridListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ChatsList> chatsListArrayList;

    private ZomeApplication application;
    private JSONObject object;

    SharedPreferences preferences;

    public ChatsGridListAdapter(Context context, ArrayList<ChatsList> chatsListArrayList) {
        this.context = context;
        this.chatsListArrayList = chatsListArrayList;
        preferences = this.context.getSharedPreferences("loginItems", Context.MODE_PRIVATE);
        application = (ZomeApplication) this.context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_grid_square_item, parent, false);
        return new ChatsGridListAdapter.ViewHolder(v);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        application.mZomeUtils.imagePipeline.clearCaches();
        Log.e("view", "recycling");
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ChatsList chatsList = chatsListArrayList.get(position);

        RetrieveCityImage retrieveCityImage = new RetrieveCityImage(context, chatsList.getLat(), chatsList.getLng());
        retrieveCityImage.loadDistinctImageUrlsFromLocation(holder.ivLocationThumb, position);

        holder.tvRoomName.setText(chatsList.getRoomName());

        holder.rlTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    object = new JSONObject(preferences.getString("loginJson", "{}").split(",")[0] + "}");
//                    object.put("uid", preferences.getString("uid", ""));
                    Toast.makeText(context, "Loading chatroom: " + holder.tvRoomName.getText().toString(), Toast.LENGTH_SHORT).show();
                    object.put("roomKey", chatsList.getRoomKey());
                    application.mSocket.emit("requestEnterChatroom", object);
                    application.mSocket.on("assignChatroom", onAssignChatListener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Emitter.Listener onAssignChatListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("args", args[0].toString());
            Intent intent = new Intent(application.getApplicationContext(), ChatActivity.class);
            intent.putExtra("chats", args[0].toString());
            intent.putExtra("originalJsonData", object.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.getApplicationContext().startActivity(intent);
        }
    };

    @Override
    public int getItemCount() {
        return chatsListArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView ivLocationThumb;
        private TextView tvRoomName;
        private RelativeLayout rlTop;
        public ViewHolder(View itemView) {
            super(itemView);
            ivLocationThumb = (SimpleDraweeView) itemView.findViewById(R.id.ivLocationThumb);
            tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);
            rlTop = (RelativeLayout) itemView.findViewById(R.id.rlTop);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        chatsListArrayList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(ArrayList<ChatsList> list) {
        chatsListArrayList.addAll(list);
        notifyDataSetChanged();
    }
}
