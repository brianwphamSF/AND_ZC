package zomeapp.com.zomechat.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;

public class AddFeedActivity extends AppCompatActivity {

    private ZomeApplication application;

    private EditText etMessage;
    private ImageView ivAddImage;
    private ImageButton btnAddPhoto;
    private Button btnCancel, btnPost;

    private File destination;

    private final int REQUEST_CAMERA = 2, SELECT_FILE = 3;

    private Context context;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);
        context = this;
        application = (ZomeApplication) getApplication();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(getResources().getString(R.string.app_name));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(backClickListener);
        }

        if (Build.VERSION.SDK_INT <= 13)
            toolbar.setTitleTextColor(Color.WHITE);

        application.mZomeUtils.changeToolbarTypeface(toolbar, Typeface.createFromAsset(getAssets(), "rezland.ttf"));

        etMessage = (EditText) findViewById(R.id.etMessage);

        if (application.mZomeUtils.metrics.heightPixels <= 320) {
            etMessage.getLayoutParams().height = application.mZomeUtils.metrics.heightPixels / 4;
        }

        etMessage.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e("actionId", String.valueOf(actionId));
                if (actionId == 0) {
                    View view = etMessage.getRootView();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
        etMessage.addTextChangedListener(tagsWatcher);
        ivAddImage = (ImageView) findViewById(R.id.ivAddImage);
        btnAddPhoto = (ImageButton) findViewById(R.id.btnAddPhoto);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnPost = (Button) findViewById(R.id.btnPost);

        btnAddPhoto.setOnClickListener(addPhotoListener);

        btnPost.setOnClickListener(addPostListener);

        btnCancel.setOnClickListener(backClickListener);
    }

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            application.isBackPressed = true;
            onBackPressed();
        }
    };

    private TextWatcher tagsWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String[] strings = s.toString().split(" ");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#33b5e5"));
            for (String string : strings) {
                if (string.startsWith("#")) {
                    etMessage.getText().setSpan(colorSpan,
                            s.toString().trim().lastIndexOf(string.trim()),
                            s.toString().trim().lastIndexOf(string.trim()) + string.length(),
                            0
                    );
                }
            }

            for (int i = s.length(); i > 0; i--) {
                if (s.subSequence(i - 1, i).toString().equals("\n"))
                    s.replace(i - 1, i, "");
            }

        }
    };

    private View.OnClickListener addPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ivAddImage.getDrawable() == null) {
                selectImage();
            } else {
                btnAddPhoto.setImageResource(R.drawable.ic_select_photo);
                ivAddImage.setImageDrawable(null);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
    };

    private View.OnClickListener addPostListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences preferences = context.getSharedPreferences("loginItems", MODE_PRIVATE);
            try {
                JSONObject jsonObject = new JSONObject(preferences.getString("loginJson", "{}"));
                jsonObject.put("content", etMessage.getText().toString());
                boolean isImgUp = false;
                if (ivAddImage.getDrawable() == null) {
                    jsonObject.put("image", "");
                } else {

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    jsonObject.put("image", new String(Base64.encode(stream.toByteArray(), Base64.DEFAULT)));

                    isImgUp = true;
                }

                if (!etMessage.getText().toString().isEmpty()) {
                    application.mSocket.emit("requestCreateMessage", jsonObject);

                    if (isImgUp) {
                        application.mZomeUtils.delayForDataRetrieval(1200l);
                        Toast.makeText(context, "Uploading to feed with image. Please wait...", Toast.LENGTH_SHORT).show();
                    }

                    finish();
                } else {
                    Snackbar.make(v, "Message field cannot be empty.", Snackbar.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (bitmap != null) {
            bitmap.recycle();
        }
        Log.e("activity", "destroyed");
        super.onDestroy();
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddFeedActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
            btnAddPhoto.setImageResource(R.drawable.ic_btn_cancel);
            if (bitmap != null) {
                bitmap.recycle();
            }

            ExifInterface exif;

            if (requestCode == REQUEST_CAMERA) {
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
                bitmap = application.mZomeUtils.decodeSampledBitmapFromFile(file.getAbsolutePath(), application.mZomeUtils.metrics.widthPixels, 700);

                try {
                    exif = new ExifInterface(file.getAbsolutePath());

                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    bitmap = application.mZomeUtils.rotateBitmap(bitmap, orientation);

                    Log.e("bitmap dim", bitmap.getWidth() + "x" + bitmap.getHeight());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

                    destination = new File(Environment.getExternalStorageDirectory(),
                            System.currentTimeMillis() + ".png");

                    FileOutputStream fo;
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

                ivAddImage.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();

                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                try {
                    exif = new ExifInterface(selectedImagePath);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= application.mZomeUtils.metrics.widthPixels
                            && options.outHeight / scale / 2 >= 700)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    bitmap = application.mZomeUtils.rotateBitmap(bitmap, orientation);

                    destination = new File(selectedImagePath);

                    ivAddImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cursor.close();
            }

            Log.e("file", destination.getAbsoluteFile().toString());
        }


    }

}