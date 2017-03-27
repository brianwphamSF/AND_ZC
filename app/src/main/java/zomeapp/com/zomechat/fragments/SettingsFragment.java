package zomeapp.com.zomechat.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.SettingsSecondPgAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.EditUsernameDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private ZomeApplication application;

    private FrameLayout flSettingsContent;
    private Context context;

    private TextView tvPrevPage, tvNextPage;

    private ImageButton btnEdit;
    private TextView tvName, tvUid;
    private SimpleDraweeView ivChangeImg;

    private SharedPreferences preferences;

    private File destination;

    private JSONObject uidObject, returnedObject;

    private final int REQUEST_CAMERA = 2, SELECT_FILE = 3;

    private FrameLayout flFirstPg;
    private RecyclerView rvSecondPg;
    private SettingsSecondPgAdapter adapter;
    private String [] s;

    private View view;

    private boolean isProfileLoaded;

    public SettingsFragment() {
        // Required empty public constructor
        Log.e("constructor called", "success");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        application = (ZomeApplication) context.getApplicationContext();

        Log.e("onCreate called", "success");

        isProfileLoaded = false;

        s = getResources().getStringArray(R.array.settings_items);

        adapter = new SettingsSecondPgAdapter(context, s);

        preferences = context.getSharedPreferences("loginItems", Context.MODE_PRIVATE);

        try {
            uidObject = new JSONObject(preferences.getString("loginJson", "{}").split(",")[0] + "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Emitter.Listener getProfile = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // View should be loaded from here on out
            // due to asynchronous call
            Log.e("argsProfile", args[0].toString());

            try {
                returnedObject = new JSONObject(args[0].toString());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tvName.setText(returnedObject.getString("username"));
                            tvUid.setText(returnedObject.getString("uid"));

                            if (!returnedObject.getString("imageURL").equals("")) {
                                ivChangeImg.setImageURI(Uri.parse(returnedObject.getString("imageURL")));
                            } else {
                                Uri uri = new Uri.Builder()
                                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                        .path(String.valueOf(R.drawable.anonymous_large))
                                        .build();
                                ivChangeImg.setImageURI(uri);
                            }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        flSettingsContent = (FrameLayout) view.findViewById(R.id.flSettingsContent);

        flFirstPg = (FrameLayout) inflater.inflate(R.layout.settings_pg_1, null);
        rvSecondPg = (RecyclerView) inflater.inflate(R.layout.settings_pg_2, null);

        rvSecondPg.setAdapter(adapter);
        rvSecondPg.setLayoutManager(new LinearLayoutManager(context));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            RelativeLayout rlSettings = (RelativeLayout) flFirstPg.findViewById(R.id.rlSetting);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rlSettings.getLayoutParams();
            params.setMargins(0, application.mZomeUtils.dpToPx(100), 0, 0);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            rlSettings.setLayoutParams(params);
            rlSettings.setPadding(0, application.mZomeUtils.dpToPx(25), 0, 0);
        }

        flSettingsContent.addView(flFirstPg);
        flSettingsContent.addView(rvSecondPg);

        setupPages(view);

        return view;
    }

    private void setupPages(View v) {

        btnEdit = (ImageButton) v.findViewById(R.id.btnEdit);
        tvName = (TextView) v.findViewById(R.id.tvName);
        tvUid = (TextView) v.findViewById(R.id.tvUid);
        ivChangeImg = (SimpleDraweeView) v.findViewById(R.id.ivChangeImg);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditUsernameDialog(context, getProfile, uidObject).show();
            }
        });

        ivChangeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        tvPrevPage = (TextView) v.findViewById(R.id.tvPreviousPage);
        tvNextPage = (TextView) v.findViewById(R.id.tvNextPage);

        tvPrevPage.setVisibility(View.GONE);
        rvSecondPg.setVisibility(View.GONE);

        application.mZomeUtils.mainAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.e("offSet", String.valueOf(verticalOffset));
/*
                flFirstPg.setPadding(
                        0,
                        (application.mZomeUtils.metrics.heightPixels / 2) + (verticalOffset / 2) - application.mZomeUtils.barHeight,
                        0,
                        0
                );
*/
                rvSecondPg.setPadding(
                        0,
                        (application.mZomeUtils.metrics.heightPixels / 2) + (verticalOffset / 2) - application.mZomeUtils.barHeight,
                        0,
                        0
                );
            }
        });

        tvNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvNextPage.setVisibility(View.GONE);
                tvPrevPage.setVisibility(View.VISIBLE);

                flFirstPg.setVisibility(View.GONE);
                rvSecondPg.setVisibility(View.VISIBLE);
            }
        });

        tvPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPrevPage.setVisibility(View.GONE);
                tvNextPage.setVisibility(View.VISIBLE);

                rvSecondPg.setVisibility(View.GONE);
                flFirstPg.setVisibility(View.VISIBLE);

                if (application.mZomeUtils.metrics.heightPixels <= 320) {
                    application.mZomeUtils.mainAppBarLayout.setExpanded(false);
                }
            }
        });

        if (tvName.getText().toString().isEmpty() && tvUid.getText().toString().isEmpty() && returnedObject != null) {
            try {
                tvName.setText(returnedObject.getString("username"));
                tvUid.setText(returnedObject.getString("uid"));

                if (!returnedObject.getString("imageURL").equals("")) {
                    ivChangeImg.setImageURI(Uri.parse(returnedObject.getString("imageURL")));
                } else {
                    Uri uri = new Uri.Builder()
                            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                            .path(String.valueOf(R.drawable.anonymous_large))
                            .build();
                    ivChangeImg.setImageURI(uri);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (application.mZomeUtils.isUserAnonymous) {
            btnEdit.setVisibility(View.GONE);
            ivChangeImg.setEnabled(false);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.e("user visibility", "called");

            if (application.mZomeUtils.metrics.heightPixels <= 320) {
                application.mZomeUtils.mainAppBarLayout.setExpanded(false);
            }

            if (!isProfileLoaded) {
                application.mSocket.emit("requestProfile", uidObject);
                application.mSocket.on("myProfile", getProfile);
                isProfileLoaded = true;
                Toast.makeText(context, "Loading your profile information. This may take up to 25 seconds.", Toast.LENGTH_LONG).show();
            }
            /*if (tvName == null && tvUid == null && ivChangeImg == null) {
                setupPages(view);
            }*/
            if (tvName != null && tvUid != null && ivChangeImg != null && returnedObject != null) {
                if (tvName.getText().toString().isEmpty() && tvUid.getText().toString().isEmpty()) {
                    try {
                        tvName.setText(returnedObject.getString("username"));
                        tvUid.setText(returnedObject.getString("uid"));

                        if (!returnedObject.getString("imageURL").equals("")) {
                            ivChangeImg.setImageURI(Uri.parse(returnedObject.getString("imageURL")));
                        } else {
                            Uri uri = new Uri.Builder()
                                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                    .path(String.valueOf(R.drawable.anonymous_large))
                                    .build();
                            ivChangeImg.setImageURI(uri);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (tvPrevPage != null && tvNextPage != null) {
                if (tvNextPage.getVisibility() == View.VISIBLE) {
                    flFirstPg.setVisibility(View.VISIBLE);
                }
                if (tvPrevPage.getVisibility() == View.VISIBLE) {
                    rvSecondPg.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (flFirstPg != null && rvSecondPg != null) {
                if (flFirstPg.getVisibility() == View.VISIBLE) {
                    flFirstPg.setVisibility(View.GONE);
                }
                if (rvSecondPg.getVisibility() == View.VISIBLE) {
                    rvSecondPg.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            Log.e("menu visibility", "called");
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
                Bitmap bitmap = application.mZomeUtils.decodeSampledBitmapFromFile(file.getAbsolutePath(), 500, 350);

                ExifInterface exif;

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
                bitmap.recycle();

            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();

                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                destination = new File(selectedImagePath);

                cursor.close();

            }

            Log.e("file", destination.getAbsoluteFile().toString());

            sendPhoto();
        }
    }

    private Emitter.Listener imgUpdateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("imgUpd", args[0].toString());
            try {
                JSONObject object = new JSONObject(args[0].toString());
                final String imgUrl = object.getString("imageURL");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("imgUrl", imgUrl);
                        Toast.makeText(context, "Updating your profile picture. Please wait a while.", Toast.LENGTH_SHORT).show();
                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        Uri uri = Uri.parse(imgUrl);
                        imagePipeline.evictFromCache(uri);
                        ivChangeImg.setImageURI(uri);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void sendPhoto() {
        try {
            ExifInterface exif;
            Bitmap bitmap = application.mZomeUtils.decodeSampledBitmapFromFile(destination.getAbsolutePath(), 500, 350);

            exif = new ExifInterface(destination.getAbsolutePath());

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            bitmap = application.mZomeUtils.rotateBitmap(bitmap, orientation);

            Log.e("bitmap dim", bitmap.getWidth() + "x" + bitmap.getHeight());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", returnedObject.getString("uid"));
            jsonObject.put("image", new String(Base64.encode(bytes.toByteArray(), Base64.DEFAULT)));

            bitmap.recycle();

            application.mSocket.emit("profileUpdate", jsonObject);
            application.mSocket.on("profileUpdateResponse", imgUpdateListener);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
