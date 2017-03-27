package zomeapp.com.zomechat.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.FeedResponsesAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.ReportDialog;
import zomeapp.com.zomechat.models.FeedResponse;
import zomeapp.com.zomechat.views.ListItemDivider;

public class FeedDetailActivity extends AppCompatActivity {

    private Context context;
    private JSONObject feedDetailObject, returnedObject;
    private SharedPreferences loginPrefs;
    private View view;

    private SimpleDraweeView ivPosterImage;
    private SimpleDraweeView ivAttachedPhoto;
    private RelativeLayout rlFeedPostContent;
    private TextView tvUser, tvTime, tvContent;
    private RecyclerView rvFeedResponses;

    private TextInputLayout tilAddResponse;
    private EditText etAddResponse;
    private Button btnReply;

    private ArrayList<FeedResponse> myResponses;
    private FeedResponsesAdapter feedResponsesAdapter;

    private ZomeApplication application;

    private DisplayMetrics metrics;
    private Snackbar repositionedBar;

    private FrameLayout flResponseContent;

    private boolean isDataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);
        view = findViewById(R.id.feedView);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        context = this;

        application = (ZomeApplication) getApplication();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        ivPosterImage = (SimpleDraweeView) findViewById(R.id.ivPosterImage);
        ivAttachedPhoto = (SimpleDraweeView) findViewById(R.id.ivAttachedPhoto);
        rlFeedPostContent = (RelativeLayout) findViewById(R.id.rlFeedPostContent);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tilAddResponse = (TextInputLayout) findViewById(R.id.tilAddResponse);
        etAddResponse = (EditText) findViewById(R.id.etAddResponse);
        btnReply = (Button) findViewById(R.id.btnReply);

        flResponseContent = (FrameLayout) findViewById(R.id.flResponseContent);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "rezland.ttf");

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("zomechat");

        collapsingToolbar.setCollapsedTitleTypeface(typeface);
        collapsingToolbar.setExpandedTitleTypeface(typeface);

        if (Build.VERSION.SDK_INT <= 13) {
            collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
            collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        }

        ivAttachedPhoto.getLayoutParams().height = application.mZomeUtils.metrics.heightPixels / 3;

        Log.e("btnWidth", btnReply.getLayoutParams().width + "");
        Log.e("metrics", metrics.widthPixels + "x" + metrics.heightPixels + " " + toolbar.getLayoutParams().height);

        final AppBarLayout insAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        insAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.e("appBarLayout height FDA", String.valueOf(verticalOffset + insAppBarLayout.getHeight()));
                application.mZomeUtils.barHeight = verticalOffset + insAppBarLayout.getHeight();
            }
        });

        btnReply.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(metrics.widthPixels - btnReply.getMeasuredWidth() - 25, toolbar.getLayoutParams().height);
        params.setMargins(5, 5, 5, 5);
        etAddResponse.setLayoutParams(params);

        flResponseContent.setPadding(0, 25, 0, toolbar.getLayoutParams().height);

        Log.e("etWxH", etAddResponse.getLayoutParams().width + "x" + etAddResponse.getLayoutParams().height);

        CoordinatorLayout.LayoutParams cParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT - etAddResponse.getLayoutParams().width - 5, toolbar.getLayoutParams().height - 10);
        cParams.setMargins(10, 10, 10, 10);
        cParams.gravity = Gravity.BOTTOM | Gravity.END;
        btnReply.setLayoutParams(cParams);

        if (Build.VERSION.SDK_INT <= 13)
            toolbar.setTitleTextColor(Color.WHITE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v != null) {
                    checkIfSnackbarIsOpened();
                }
                return false;
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (application.mZomeUtils.isUserAnonymous) {
                    application.mZomeUtils.showToastAnonymousUserMessage("respond to this feed.");
                } else {
                    if (etAddResponse.getText().toString().equals("")) {
                        Snackbar.make(v, "Comment cannot be empty.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        try {
                            isDataChanged = true;
                            JSONObject object = feedDetailObject;
                            object.put("content", etAddResponse.getText().toString());
                            application.mSocket.emit("requestPostComment", object);
                            application.mZomeUtils.delayForDataRetrieval(null);
                            application.mSocket.emit("requestFeedDetail", feedDetailObject);
                            application.mSocket.on("feedDetail", feedListener);

                            etAddResponse.setText("");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            getDataFromIntent();
        }
    }

    private void getDataFromIntent() {
        loginPrefs = context.getSharedPreferences("saveForLoginSession", MODE_PRIVATE);

        final Snackbar snackbar = Snackbar.make(view, "Report post '" + getIntent().getStringExtra("content") + "'?", Snackbar.LENGTH_INDEFINITE).setActionTextColor(Color.CYAN);
        snackbar.setAction("Click here to Report", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (application.mZomeUtils.isUserAnonymous) {
                    application.mZomeUtils.showToastAnonymousUserMessage("make a report.");
                } else {
                    ReportDialog dialog = new ReportDialog(
                            context,
                            0,
                            "POST",
                            getIntent().getStringExtra("content"),
                            getIntent().getStringExtra("feedId")
                    );
                    dialog.show();
                }
            }
        });

        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(R.drawable.anonymous_large))
                .build();

        if (getIntent().getStringExtra("ownerImage") != null) {
            uri = Uri.parse(getIntent().getStringExtra("ownerImage"));
        }

        ivPosterImage.setImageURI(uri);

        ivPosterImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.e("ivPI dim", ivPosterImage.getMeasuredWidth() + "x" + ivPosterImage.getMeasuredHeight());

        int halfImageHeight = application.mZomeUtils.dpToPx(40);

        Log.e("halfHeight", String.valueOf(halfImageHeight));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, halfImageHeight + 10, 0, 0);

        FrameLayout.LayoutParams txtParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtParams.setMargins(application.mZomeUtils.dpToPx(110), application.mZomeUtils.dpToPx(20), 0, 0);
//                txtParams.height = dpToPx(halfImageHeight - 40);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            params.gravity = Gravity.TOP;
            rlFeedPostContent.setPadding(20, 20, 20, halfImageHeight + 20);
            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(200, 200);
            imgParams.setMargins(application.mZomeUtils.dpToPx(20), 0, 0, 0);
            imgParams.gravity = Gravity.TOP;
            imgParams.height = application.mZomeUtils.dpToPx(80);
            imgParams.width = application.mZomeUtils.dpToPx(80);
            ivPosterImage.setLayoutParams(imgParams);

            ivPosterImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            Log.e("ivPI 2", ivPosterImage.getMeasuredWidth() + "x" + ivPosterImage.getMeasuredHeight());

            txtParams.gravity = Gravity.TOP;
        }

        rlFeedPostContent.setLayoutParams(params);
        tvUser.setLayoutParams(txtParams);

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        try {
            Date parseDate = sdf.parse(getIntent().getStringExtra("time"));
            application.mZomeUtils.returnApproxTime(parseDate, tvTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvUser.setText(getIntent().getStringExtra("profileName"));

        application.mZomeUtils.setTags(tvContent, getIntent().getStringExtra("content"));

        if (!getIntent().getStringExtra("extraImage").equals("")) {
            Log.e("extraImg", getIntent().getStringExtra("extraImage"));
            ivAttachedPhoto.setImageURI(Uri.parse(getIntent().getStringExtra("extraImage")));
        } else {
            Uri attachedPhotoUri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.zome_logo))
                    .build();
            ivAttachedPhoto.setImageURI(attachedPhotoUri);
        }

        rlFeedPostContent.setVisibility(View.VISIBLE);

        rvFeedResponses = (RecyclerView) findViewById(R.id.rvFeedResponses);

        rlFeedPostContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("barHeight", application.mZomeUtils.barHeight + "");
                repositionedBar = application.mZomeUtils.repositionCustomSnackbarBelowToolbar(snackbar);
                repositionedBar.show();
                return true;
            }
        });

        rvFeedResponses.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.e("eIntercept", String.valueOf(e.getY()));
                if (repositionedBar != null) {
                    if (repositionedBar.isShown()) {
                        repositionedBar.dismiss();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                Log.e("e", String.valueOf(e.getY()));
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        rlFeedPostContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) {
                    checkIfSnackbarIsOpened();
                }
            }
        });

        try {
            feedDetailObject = new JSONObject();
            feedDetailObject.put("uid", loginPrefs.getString("uid", ""));
            feedDetailObject.put("feedId", getIntent().getStringExtra("feedId"));

            application.mSocket.emit("requestFeedDetail", feedDetailObject);
            application.mSocket.on("feedDetail", feedListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loginPrefs.getString("uid", "");
        Log.e("intent", getIntent().getStringExtra("feedId"));
    }

    private void checkIfSnackbarIsOpened() throws NullPointerException {
        if (feedResponsesAdapter != null) {
            if (feedResponsesAdapter.snackbar.isShown()) {
                feedResponsesAdapter.snackbar.dismiss();
            }
        }
        if (repositionedBar != null) {
            if (repositionedBar.isShown()) {
                repositionedBar.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isDataChanged) {
            application.isBackPressed = true;
        }
        finish();
        super.onBackPressed();
    }

    private Emitter.Listener feedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("args", args[0].toString());
            returnedObject = (JSONObject) args[0];

            try {
                myResponses = application.getResponses(returnedObject.getJSONArray("comments"));

                feedResponsesAdapter = new FeedResponsesAdapter(context, myResponses);

                // Let's initialize the adapter's snackbar here so there are no NPEs
                feedResponsesAdapter.snackbar = Snackbar.make(view, "It will never get here anyway. ;)", Snackbar.LENGTH_SHORT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvFeedResponses.setAdapter(feedResponsesAdapter);

                        rvFeedResponses.addItemDecoration(new ListItemDivider(context, R.drawable.custom_transparent_divider));

                        rvFeedResponses.setLayoutManager(new LinearLayoutManager(context));
                    }
                });

                invalidateOptionsMenu();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_heart_post, menu);
        MenuItem item = menu.findItem(R.id.action_heart);
        try {
            if (returnedObject != null) {
                for (int i = 0; i < returnedObject.getJSONArray("likes").length(); i++) {
                    if (returnedObject.getJSONArray("likes").get(i).equals(loginPrefs.getString("uid", ""))) {
                        item.setIcon(R.drawable.ic_hearted_post);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (application.mZomeUtils.isUserAnonymous) {
                    application.mZomeUtils.showToastAnonymousUserMessage("heart this post.");
                    return false;
                } else {
                    boolean isLiked = false;
                    Log.e("clicked", "true");
                    try {
                        for (int i = 0; i < returnedObject.getJSONArray("likes").length(); i++) {
                            if (returnedObject.getJSONArray("likes").get(i).equals(loginPrefs.getString("uid", ""))) {
                                Snackbar.make(view, "You have already hearted this post", Snackbar.LENGTH_SHORT).show();
                                return isLiked;
                            }
                        }
                        application.mSocket.emit("requestLikeFeed", feedDetailObject);
                        application.mSocket.on("feedDetail", feedListener);
                        item.setIcon(R.drawable.ic_hearted_post);
                        isDataChanged = true;
                        isLiked = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return isLiked;
                }
            }
        });
        return true;
    }
}
