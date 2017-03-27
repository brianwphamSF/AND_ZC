package zomeapp.com.zomechat.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;

/**
 * Created by tkiet082187 on 12.11.15.
 */
public class EditUsernameDialog extends Dialog {
    private EditText etEditUsername;
    private Button btnCancelEdit, btnEditUsername;
    private ZomeApplication application;
    private SharedPreferences preferences;

    public EditUsernameDialog(Context context, final Emitter.Listener listener, final JSONObject jsonObject) {
        super(context);
        setContentView(R.layout.edit_username_dialog);

        preferences = context.getSharedPreferences("saveForLoginSession", Context.MODE_PRIVATE);

        application = (ZomeApplication) context.getApplicationContext();

        etEditUsername = (EditText) findViewById(R.id.etEditUsername);
        btnCancelEdit = (Button) findViewById(R.id.btnCancelEdit);
        btnEditUsername = (Button) findViewById(R.id.btnEditUsername);

        setTitle("Edit Username");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            etEditUsername.setTextColor(Color.WHITE);
        }

        btnCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONObject object = new JSONObject();
                try {
                    object.put("uid", preferences.getString("uid", ""));
                    object.put("username", etEditUsername.getText().toString());
                    application.mSocket.emit("requestUsernameChange", object);
                    application.mSocket.emit("requestProfile", jsonObject);
                    application.mSocket.on("myProfile", listener);
                    dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
