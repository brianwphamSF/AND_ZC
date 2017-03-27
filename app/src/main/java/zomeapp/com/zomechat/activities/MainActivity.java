package zomeapp.com.zomechat.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.FragmentViewPagerAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.CreateChatroomDialog;
import zomeapp.com.zomechat.utils.RetrieveCityImage;

public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private ViewPager viewPager;

    private int pageReference;

    private ZomeApplication application;

    private boolean pageFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            System.gc();
        }

        application = (ZomeApplication) getApplication();

        Typeface typeface = Typeface.createFromAsset(getAssets(), "rezland.ttf");

        frameLayout = (FrameLayout) findViewById(R.id.fGeneric);
        Log.e("density", String.valueOf(Resources.getSystem().getDisplayMetrics().density));
        frameLayout.getLayoutParams().height = application.mZomeUtils.metrics.heightPixels / 3;

        final SimpleDraweeView iv = (SimpleDraweeView) getLayoutInflater().inflate(R.layout.image_collapse, null);
        iv.setBackgroundColor(Color.parseColor("#000000"));
        application.mapView = (MapView) getLayoutInflater().inflate(R.layout.maps_collapse, null);
        application.mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);

        application.mapView.setBuiltInZoomControls(true);
        application.mapView.setMultiTouchControls(true);

        RetrieveCityImage retrieveCityImage = new RetrieveCityImage(this, application.lat, application.lng);
        retrieveCityImage.loadFirstImageUrlFromLocation(iv);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("zomechat");

        collapsingToolbar.setCollapsedTitleTypeface(typeface);
        collapsingToolbar.setExpandedTitleTypeface(typeface);

        final AppBarLayout insAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        application.mZomeUtils.mainAppBarLayout = insAppBarLayout;

        insAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.e("appBarLayout height", String.valueOf(verticalOffset + insAppBarLayout.getHeight()));
                application.mZomeUtils.barHeight = verticalOffset + insAppBarLayout.getHeight();
            }
        });

        if (Build.VERSION.SDK_INT <= 13) {
            collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
            collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        FragmentViewPagerAdapter pagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager(),
                MainActivity.this);

        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getTabView(i));
            }
        }

        application.mapView.setUseDataConnection(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFloatingActionButtonParams(0);
            }
        });

        application.mapView.setVisibility(View.GONE);

        frameLayout.addView(application.mapView);
        frameLayout.addView(iv);

        pageFlag = true;

        application.mapView.setBuiltInZoomControls(false);
        application.mapView.setMultiTouchControls(false);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                Log.e("OPSelected pos", position + "");

                application.mZomeUtils.imagePipeline.clearCaches();

                invalidateOptionsMenu();

                pageReference = position;

                // maps rendering on layout change fixed
                if (position == 0 || position == 2) {

                    application.mapView.setBuiltInZoomControls(false);
                    application.mapView.setMultiTouchControls(false);
                    application.mapView.setVisibility(View.GONE);

                    if (!pageFlag)
                        frameLayout.addView(iv);

                    pageFlag = true;
                } else {
                    frameLayout.removeView(iv);
                    pageFlag = false;

                    application.mapView.setVisibility(View.VISIBLE);

                    application.mapView.setBuiltInZoomControls(true);
                    application.mapView.setMultiTouchControls(true);

                }
                if (position == 1 || position == 2) {
                    application.mZomeUtils.setAppBarDragging(false);
                } else {
                    application.mZomeUtils.setAppBarDragging(true);
                }
                if (position == 2) {
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setFloatingActionButtonParams(position);
                        }
                    });
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.mZomeUtils.imagePipeline.clearCaches();
        viewPager.getAdapter().notifyDataSetChanged();

        //viewPager.scrollTo(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        application.mZomeUtils.imagePipeline.clearCaches();
    }

    private void setFloatingActionButtonParams(int position) {

        if (position == 0) {
            if (application.mZomeUtils.isUserAnonymous) {
                application.mZomeUtils.showToastAnonymousUserMessage("make a new post.");
            } else {
                Intent intent = new Intent(MainActivity.this, AddFeedActivity.class);
                startActivity(intent);
            }
        } else {
            if (application.mZomeUtils.isUserAnonymous) {
                application.mZomeUtils.showToastAnonymousUserMessage("create a new chatroom.");
            } else {
                new CreateChatroomDialog(this).show();
            }
        }

    }

    @Override
    public void finish() {
        application.mSocket.off();
        application.mSocket.disconnect();
        application.mSocket.connect();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            System.gc();
        }
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflateInPosition(pageReference, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void inflateInPosition(int page, Menu menu) {
        int id = 0;
        if (page == 0) {
            id = R.menu.menu_search;
        } else if (page == 1) {
            id = R.menu.menu_refresh;
        }
        if (id != 0) {
            getMenuInflater().inflate(id, menu);
        }
    }
}
