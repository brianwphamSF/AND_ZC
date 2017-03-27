package zomeapp.com.zomechat.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.activities.ChatActivity;
import zomeapp.com.zomechat.application.ZomeApplication;

/**
 * Created by tkiet082187 on 11.11.15.
 */
public class CreateChatroomDialog extends Dialog {
    private EditText etCreateChatroom;
    private Button btnCancelCreateChatroom, btnCreateChatroom;
    private ZomeApplication application;
    private Context context;
    private SharedPreferences preferences;

    public CreateChatroomDialog(final Context context) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_chatroom_dialog);

        preferences = context.getSharedPreferences("saveForLoginSession", Context.MODE_PRIVATE);

        application = (ZomeApplication) context.getApplicationContext();

        etCreateChatroom = (EditText) findViewById(R.id.etCreateChat);
        btnCancelCreateChatroom = (Button) findViewById(R.id.btnCancelCreateChatroom);
        btnCreateChatroom = (Button) findViewById(R.id.btnCreateChatroom);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            etCreateChatroom.setTextColor(Color.WHITE);
        }

        btnCancelCreateChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnCreateChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject object = new JSONObject();
                try {
                    object.put("uid", preferences.getString("uid", ""));
                    object.put("roomName", etCreateChatroom.getText().toString());
                    application.mSocket.emit("requestCreateNewRoom", object);
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
            JSONObject object = new JSONObject();
            try {
                object.put("uid", preferences.getString("uid", ""));
                object.put("roomKey", ((JSONObject) args[0]).getString("roomKey"));
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chats", args[0].toString());
                intent.putExtra("originalJsonData", object.toString());
                context.startActivity(intent);
                dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
