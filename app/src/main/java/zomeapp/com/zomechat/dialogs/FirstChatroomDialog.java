package zomeapp.com.zomechat.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import zomeapp.com.zomechat.R;

/**
 * Created by tkiet082187 on 11.11.15.
 */
public class FirstChatroomDialog extends Dialog {
    private Button btnCancelCreateChat, btnCreateChatDialog;
    public FirstChatroomDialog(final Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.first_chatroom_dialog);

        btnCancelCreateChat = (Button) findViewById(R.id.btnCancelCreateChat);
        btnCreateChatDialog = (Button) findViewById(R.id.btnCreateChatDialog);

        btnCancelCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnCreateChatDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateChatroomDialog(context).show();
                dismiss();
            }
        });
    }
}
