package zomeapp.com.zomechat.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.ChatMessagesAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.models.ChatroomMessages;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private ChatMessagesAdapter adapter;
    private Context context;
    private ZomeApplication application;
    private DisplayMetrics metrics;
    private TextInputLayout tilAddResponseToChat;
    private EditText etAddResponseToChat;
    private Button btnChat;
    private ImageButton imgBtnReply;
    private LinearLayoutManager manager;

    ArrayList<ChatroomMessages> messages = new ArrayList<>();

    private final int REQUEST_CAMERA = 2, SELECT_FILE = 3;

    private File destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = this;

        Typeface typeface = Typeface.createFromAsset(getAssets(), "rezland.ttf");

        manager = new LinearLayoutManager(context);

        manager.onSaveInstanceState();
        application = (ZomeApplication) getApplication();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(getResources().getString(R.string.app_name));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT <= 13)
            toolbar.setTitleTextColor(Color.WHITE);

        application.mZomeUtils.changeToolbarTypeface(toolbar, typeface);

        rvChatMessages = (RecyclerView) findViewById(R.id.rvChatMessages);

        imgBtnReply = (ImageButton) findViewById(R.id.btnImgReply);

        tilAddResponseToChat = (TextInputLayout) findViewById(R.id.tilAddResponseToChat);

        etAddResponseToChat = (EditText) findViewById(R.id.etAddResponseToChat);

        btnChat = (Button) findViewById(R.id.btnChat);

        btnChat.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        Log.e("measured btn", btnChat.getMeasuredWidth() + "x" + btnChat.getMeasuredHeight());

        loadData(null);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(metrics.widthPixels - imgBtnReply.getDrawable().getMinimumWidth() - btnChat.getMeasuredWidth() - 25, toolbar.getLayoutParams().height);
        params.setMargins(imgBtnReply.getDrawable().getMinimumWidth() + 5, 5, 5, 5);
        etAddResponseToChat.setLayoutParams(params);

        etAddResponseToChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    tilAddResponseToChat.setHint(getResources().getString(R.string.et_chat_response_hint));
                } else {
                    tilAddResponseToChat.setHint("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    tilAddResponseToChat.setHint(getResources().getString(R.string.et_chat_response_hint));
                }
            }
        });

        rvChatMessages.setPadding(10, 0, 10, toolbar.getLayoutParams().height + 10);

        Log.e("etWxH", etAddResponseToChat.getLayoutParams().width + "x" + etAddResponseToChat.getLayoutParams().height);

        CoordinatorLayout.LayoutParams cParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT - etAddResponseToChat.getLayoutParams().width - 5, toolbar.getLayoutParams().height - 10);
        cParams.setMargins(10, 10, 10, 10);
        cParams.gravity = Gravity.BOTTOM | Gravity.END;
        btnChat.setLayoutParams(cParams);

        btnChat.setOnClickListener(submitChatListener);
        imgBtnReply.setOnClickListener(submitPhotoListener);

        scrollToBottom();

        application.mSocket.on("chatroomMessage", onMessagesListener);
        application.mSocket.on("error", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("arg", args[0].toString());
            }
        });
    }

    private void loadData(ChatroomMessages message) {

        if (getIntent() != null) {
            try {
                Log.e("roomKey", getIntent().getStringExtra("originalJsonData"));
                JSONObject object = new JSONObject(getIntent().getStringExtra("chats"));
                JSONArray jsonArray = object.getJSONArray("messageHistory");

                if (messages.isEmpty())
                    messages.addAll(application.getChatMessages(jsonArray));

                if (message != null) {
                    messages.add(message);
                }

                adapter = new ChatMessagesAdapter(context, messages);

                rvChatMessages.setAdapter(adapter);

                rvChatMessages.setLayoutManager(manager);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private View.OnClickListener submitChatListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (application.mZomeUtils.isUserAnonymous) {
                    application.mZomeUtils.showToastAnonymousUserMessage("chat with others.");
                } else {
                    if (etAddResponseToChat.getText().length() > 0) {
                        JSONObject object = new JSONObject(getIntent().getStringExtra("originalJsonData"));
                        object.put("message", etAddResponseToChat.getText().toString());

                        application.mSocket.emit("sendChatroomMessage", object);
                        etAddResponseToChat.setText("");
                    } else {
                        Snackbar.make(etAddResponseToChat.getRootView(), "Text cannot be empty.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener submitPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (application.mZomeUtils.isUserAnonymous) {
                application.mZomeUtils.showToastAnonymousUserMessage("send an image.");
            } else {
                selectImage();
            }
        }
    };

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
                Bitmap bitmap = application.mZomeUtils.decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);

                Log.e("bitmap dim", bitmap.getWidth() + "x" + bitmap.getHeight());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

                destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".png");

                FileOutputStream fo;
                try {
                    if (!destination.exists()) {
                        destination.createNewFile();
                    }
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (file.exists()) {
                    file.delete();
                }

                //ivAddImage.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();

                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                destination = new File(selectedImagePath);

                //Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);

                int scale = 1;
                while (options.outWidth / scale / 2 >= 1000
                        && options.outHeight / scale / 2 >= 700)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                //bm = BitmapFactory.decodeFile(selectedImagePath, options);

                //ivAddImage.setImageBitmap(bm);
            }

            Log.e("file", destination.getAbsoluteFile().toString());

            sendPhoto();
        }
    }

    private void sendPhoto() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
        //bitmap.copyPixelsToBuffer(byteBuffer);
        InputStream is = null;
        try {
            is = new FileInputStream(new File(destination.getAbsoluteFile().toString()));

            byte[] buffer = new byte[1024 * 4];
            int n;
            while (-1 != (n = is.read(buffer))) {
                stream.write(buffer, 0, n);
            }
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] imageInByte = Base64.encode(stream.toByteArray(), Base64.DEFAULT);
            JSONObject jsonObject = new JSONObject(getIntent().getStringExtra("originalJsonData"));
            jsonObject.put("image", new String(Base64.encode(stream.toByteArray(), Base64.DEFAULT)));

            application.mSocket.emit("chatImage", jsonObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void scrollToBottom() {
        rvChatMessages.scrollToPosition(adapter.getItemCount() - 1);
    }

    private Emitter.Listener onMessagesListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            try {
                Log.e("args", args[0].toString());
                final JSONObject jsonObject = new JSONObject(args[0].toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
                        try {
                            loadData(new ChatroomMessages(
                                    jsonObject.getString("message"),
                                    jsonObject.getBoolean("isImage"),
                                    sdf.format(jsonObject.getLong("time")),
                                    jsonObject.getString("senderId"),
                                    "",
                                    jsonObject.getString("senderName"),
                                    jsonObject.getInt("messageId")
                            ));
                            adapter.notifyItemInserted(messages.size() - 1);
                            scrollToBottom();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void finish() {
        if (getIntent() != null) {
            try {
                JSONObject object = new JSONObject(getIntent().getStringExtra("originalJsonData"));
                Toast.makeText(context, "Leaving chatroom: " + object.getString("roomKey").split(",")[0], Toast.LENGTH_SHORT).show();
                application.mSocket.emit("requestLeaveChatroom", object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.finish();
    }
}
