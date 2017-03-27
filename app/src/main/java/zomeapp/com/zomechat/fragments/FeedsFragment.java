package zomeapp.com.zomechat.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.FeedsAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.models.Feed;
import zomeapp.com.zomechat.views.ListItemDivider;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private FeedsAdapter feedsAdapter;
    private RecyclerView rvFeed;
    private ArrayList<Feed> myFeeds;
    private Context context;
    private JSONArray messagesArray;

    private ZomeApplication application;

    long startTime, endTime;

    private SharedPreferences preferences;
    private JSONObject loginJson;

    public FeedsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myFeeds = new ArrayList<>();
        startTime = System.currentTimeMillis();
        context = getActivity();
        application = (ZomeApplication) context.getApplicationContext();
        application.mZomeUtils.feedsInstanceFragment = this;
        preferences = getActivity().getSharedPreferences("loginItems", Context.MODE_PRIVATE);
        try {
            loginJson = new JSONObject(preferences.getString("loginJson", "{}"));
            Log.e("loginItems", loginJson.toString());

            application.mSocket.emit("requestMessageboard", loginJson);
            application.mSocket.on("messageboardMessages", messagesListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            System.gc();
        }
    }

    private Emitter.Listener messagesListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("messages", args[0].toString());

            JSONObject object = (JSONObject) args[0];
            try {
                messagesArray = object.getJSONArray("messages");

                getData();

                feedsAdapter = new FeedsAdapter(context, myFeeds);

                endTime = System.currentTimeMillis();

                if (!application.isBackPressed) {
                    application.mZomeUtils.delayForDataRetrieval(endTime - startTime);
                } else {
                    application.isBackPressed = false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private void getData() {
        myFeeds = application.getFeeds(messagesArray);
        Collections.sort(myFeeds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeds_list_view, container, false);
        rvFeed = (RecyclerView) view.findViewById(R.id.rvFeed);
        rvFeed.addItemDecoration(new ListItemDivider(context, R.drawable.custom_list_divider));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        Log.e("OCV ff", "called");

        endTime = System.currentTimeMillis();
        application.mZomeUtils.delayForDataRetrieval(null);
        getData();

        rvFeed.setAdapter(feedsAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rvFeed.setLayoutManager(layoutManager);

    }

    @Override
    public void onResume() {
        onQueryTextChange("");
        super.onResume();
        if (rvFeed != null && myFeeds != null) {
            rvFeed.scrollToPosition(myFeeds.size());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem item = menu.findItem(R.id.action_search);
        if (item != null) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

            if (searchView != null) {
                searchView.setOnQueryTextListener(this);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        Log.e("q", query);
        final List<Feed> filteredFeedsList = filter(myFeeds, query);
        feedsAdapter.animateTo(filteredFeedsList);
        int listSize;
        if (query.isEmpty()) {
            listSize = myFeeds.size();
        } else {
            listSize = filteredFeedsList.size();
        }
        rvFeed.scrollToPosition(listSize);
        return true;
    }

    private List<Feed> filter(List<Feed> feeds, String query) {

        if (!query.isEmpty()) {
            query = "#" + query.toLowerCase();
        }

        Log.e("original feeds size", String.valueOf(myFeeds.size()));

        final List<Feed> filteredFeedsList = new ArrayList<>();
        for (Feed feed : feeds) {
            final String text = feed.getContent().toLowerCase();
            if (text.contains(query)) {
                filteredFeedsList.add(feed);
            }
        }

        return filteredFeedsList;
    }
}
