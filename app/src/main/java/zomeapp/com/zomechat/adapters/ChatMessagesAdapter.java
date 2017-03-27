package zomeapp.com.zomechat.adapters;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.models.ChatroomMessages;

/**
 * Created by tkiet082187 on 30.10.15.
 */
public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ChatroomMessages> chatroomMessages;
    private SharedPreferences preferences;
    private ActionMode mMode;
    private ZomeApplication application;

    public ChatMessagesAdapter(Context context, ArrayList<ChatroomMessages> chatroomMessages) {
        this.context = context;
        this.chatroomMessages = chatroomMessages;
        preferences = this.context.getSharedPreferences("saveForLoginSession", Context.MODE_PRIVATE);
        application = (ZomeApplication) this.context.getApplicationContext();
        mMode = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_msg_row_optimized, parent, false);
        return new ChatMessagesAdapter.ViewHolder(v);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        application.mZomeUtils.imagePipeline.clearCaches();
        Log.e("view", "recycling");
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ChatroomMessages message = chatroomMessages.get(position);

        Log.e("senderId", message.getSenderId());

        if (message.isImage()) {
            Log.e("img", message.getMessage());
            holder.tvMessage.setVisibility(View.GONE);
            holder.ivMessage.setVisibility(View.VISIBLE);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(message.getMessage()))
                    .setOldController(holder.ivMessage.getController())
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                            super.onFinalImageSet(id, imageInfo, animatable);
                            Log.e("imageInfo", imageInfo.getWidth() + "x" + imageInfo.getHeight());
                            float factor = (application.mZomeUtils.metrics.widthPixels * 0.85f) / imageInfo.getWidth();

                            holder.ivMessage.getLayoutParams().height = (int) (imageInfo.getHeight() * factor);
                        }
                    })
                    .build();

            holder.ivMessage.setController(controller);

        } else {
            holder.ivMessage.setVisibility(View.GONE);
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(message.getMessage());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        try {
            Date date = sdf.parse(message.getTime());
            application.mZomeUtils.returnApproxTime(date, holder.tvTimeAndUser);
            holder.tvTimeAndUser.append(", by " + message.getSenderName());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (preferences.getString("uid", "").equals(message.getSenderId())) {
            holder.rlChatContent.setGravity(Gravity.END);
            holder.ivMessage.setBackgroundResource(R.drawable.in_message_bg);
            Log.e("holder layout", holder.ivMessage.getLayoutParams().toString());

            Log.e("width and height", holder.ivMessage.getLayoutParams().width + "x" + holder.ivMessage.getLayoutParams().height);

            RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams((int) (application.mZomeUtils.metrics.widthPixels * 0.85f), (int) (application.mZomeUtils.metrics.heightPixels * 0.4f));
            RelativeLayout.LayoutParams dateAndUserParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rlParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            }
            rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ivParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            holder.tvMessage.setBackgroundResource(R.drawable.in_message_bg);
            holder.ivProfileAvatar.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                dateAndUserParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            }
            dateAndUserParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            holder.tvTimeAndUser.setLayoutParams(dateAndUserParams);
            rlParams.addRule(RelativeLayout.BELOW, R.id.tvTimeAndUser);
            ivParams.addRule(RelativeLayout.BELOW, R.id.tvTimeAndUser);
            holder.tvMessage.setLayoutParams(rlParams);
            holder.ivMessage.setLayoutParams(ivParams);

        } else {
            holder.rlChatContent.setGravity(Gravity.START);
            holder.ivMessage.setBackgroundResource(R.drawable.out_message_bg);
            Log.e("holder layout", holder.ivMessage.getLayoutParams().toString());

            Log.e("width and height", holder.ivMessage.getLayoutParams().width + "x" + holder.ivMessage.getLayoutParams().height);

            RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams((int) (application.mZomeUtils.metrics.widthPixels * 0.85f), (int) (application.mZomeUtils.metrics.heightPixels * 0.4f));
            RelativeLayout.LayoutParams dateAndUserParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            holder.tvMessage.setBackgroundResource(R.drawable.out_message_bg);
            holder.ivProfileAvatar.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                dateAndUserParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            }
            dateAndUserParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            holder.tvTimeAndUser.setLayoutParams(dateAndUserParams);
            rlParams.addRule(RelativeLayout.BELOW, R.id.tvTimeAndUser);
            rlParams.addRule(RelativeLayout.RIGHT_OF, R.id.ivProfileAvatar);

            ivParams.addRule(RelativeLayout.BELOW, R.id.tvTimeAndUser);
            ivParams.addRule(RelativeLayout.RIGHT_OF, R.id.ivProfileAvatar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rlParams.addRule(RelativeLayout.END_OF, R.id.ivProfileAvatar);
                ivParams.addRule(RelativeLayout.END_OF, R.id.ivProfileAvatar);
            }

            holder.tvMessage.setLayoutParams(rlParams);
            holder.ivMessage.setLayoutParams(ivParams);

            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.anonymous_large))
                    .build();

            if (!message.getSenderImageURL().equals("")) {
                uri = Uri.parse(message.getSenderImageURL());
            }
            holder.ivProfileAvatar.setImageURI(uri);
        }

        holder.tvMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean hasCheckedElement = false;

                if (v != null) {
                    if (mMode != null) {
                        mMode.finish();
                    }

                    if (mMode == null) {
                        //We will use the ModeCallback class for API < 11
                        mMode = ((AppCompatActivity) context).startSupportActionMode(new ModeCallback(holder.tvMessage.getText().toString()));
                        hasCheckedElement = true;
                    }
                } else {
                    if (mMode != null) {
                        mMode.finish();
                        hasCheckedElement = false;
                    }
                }

                return hasCheckedElement;
            }
        });
    }

    private final class ModeCallback implements ActionMode.Callback {

        private String string;

        public ModeCallback(String string) {
            this.string = string;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_selected, menu);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                ((AppCompatActivity) context).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                ((AppCompatActivity) context).getSupportActionBar().setTitle("");
            }

            // For ImageView
            //menu.getItem(0).setVisible(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here, you can checked selected items to adapt available actions

            return false;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                ((AppCompatActivity) context).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) context).getSupportActionBar().setTitle(context.getApplicationContext().getString(R.string.app_name));
            }
            if (mode == mMode) {
                mMode = null;
            }
        }

        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            Toast.makeText(context, "Action - " + item.getTitle() + " ; Selected items: " + string, Toast.LENGTH_LONG).show();
            switch (item.getItemId()) {
                case R.id.cab_copy:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("copied text", string);
                        clipboardManager.setPrimaryClip(data);
                    } else {
                        android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setText(string);
                    }
                    break;

                default:
                    break;
            }

            mode.finish();
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return chatroomMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlChatContent;
        private TextView tvTimeAndUser, tvMessage;
        private SimpleDraweeView ivMessage;
        private SimpleDraweeView ivProfileAvatar;
        public ViewHolder(View v) {
            super(v);
            ivProfileAvatar = (SimpleDraweeView) v.findViewById(R.id.ivProfileAvatar);
            rlChatContent = (RelativeLayout) v.findViewById(R.id.rlChatContent);
            tvTimeAndUser = (TextView) v.findViewById(R.id.tvTimeAndUser);
            tvMessage = (TextView) v.findViewById(R.id.tvMessage);
            ivMessage = (SimpleDraweeView) v.findViewById(R.id.ivMessage);
        }
    }
}
