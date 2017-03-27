package zomeapp.com.zomechat.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.adapters.ChatsGridListAdapter;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.FirstChatroomDialog;
import zomeapp.com.zomechat.models.ChatsList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatroomFragment extends Fragment {

    private Context context;
    private RecyclerView rvChatroomGridList;
    private ChatsGridListAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private JSONArray jsonArray;
    private GeoPoint startPoint, otherPoints;
    private Drawable my_loc, other_loc;
    private String myLocMarkerTitle;
    private ArrayList<ChatsList> myChatsList;

    private ZomeApplication application;

    private boolean isMapRendered;
    private boolean isFirstDialogDisplayed;

    long startTime, endTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        application = (ZomeApplication) context.getApplicationContext();

        my_loc = ContextCompat.getDrawable(context, R.drawable.ic_blue_marker);
        other_loc = ContextCompat.getDrawable(context, R.drawable.ic_grn_marker);

        isMapRendered = false;

        startTime = System.currentTimeMillis();

        isFirstDialogDisplayed = false;
        getData();
    }

    private void getData() {
        SharedPreferences preferences = context.getSharedPreferences("loginItems", Context.MODE_PRIVATE);
        try {
            JSONObject loginJson = new JSONObject(preferences.getString("loginJson", "{}").split(",")[0] + "}");
            Log.e("loginItems", loginJson.toString());

            application.mSocket.emit("requestChatroomList", loginJson);
            application.mSocket.on("chatroomList", chatListListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener chatListListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("chatList", args[0].toString());
            try {
                jsonArray = ((JSONObject) args[0]).getJSONArray("chatrooms");
                myChatsList = application.getChatsList(jsonArray);
                if (adapter == null)
                    adapter = new ChatsGridListAdapter(context, myChatsList);

                Log.e("array", jsonArray.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    double lat;
                    double lng;

                    if (jsonArray.getJSONObject(i).has("lat") || jsonArray.getJSONObject(i).has("lng")) {
                        lat = jsonArray.getJSONObject(i).getDouble("lat");
                        lng = jsonArray.getJSONObject(i).getDouble("lng");
                    } else {
                        String key = jsonArray.getJSONObject(i).getString("roomKey");
                        lat = Double.parseDouble(key.split(",")[1]);
                        lng = Double.parseDouble(key.split(",")[2]);
                    }

                    myLocMarkerTitle = jsonArray.getJSONObject(i).getString("roomName");

                    application.mIMapController = application.mapView.getController();

                    if (i == 0) {
                        startPoint = new GeoPoint(lat, lng);

                        Marker startMarker = new Marker(application.mapView);
                        startMarker.setIcon(my_loc);

                        startMarker.setTitle(myLocMarkerTitle);
                        startMarker.setPosition(startPoint);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        application.mapView.getOverlays().add(startMarker);

                    } else {
                        otherPoints = new GeoPoint(lat, lng);

                        Marker otherMarkers = new Marker(application.mapView);
                        otherMarkers.setIcon(other_loc);

                        otherMarkers.setTitle(myLocMarkerTitle);
                        otherMarkers.setPosition(otherPoints);
                        otherMarkers.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        application.mapView.getOverlays().add(otherMarkers);
                    }
                }

                if (isMapRendered) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //application.mZomeUtils.delayForDataRetrieval(endTime - startTime + 5l);
                            adapter.clear();
                            adapter.addAll(myChatsList);
                            rvChatroomGridList.invalidate();
                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public ChatroomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO: load in chatroom data
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        rvChatroomGridList = (RecyclerView) view.findViewById(R.id.rvChatroomGridList);

        setHasOptionsMenu(true);

        if (!application.isBackPressed) {
            application.mZomeUtils.delayForDataRetrieval(null);
        } else {
            application.isBackPressed = false;
        }

        mLayoutManager = new GridLayoutManager(context, 2);

        rvChatroomGridList.setAdapter(adapter);

        rvChatroomGridList.setLayoutManager(mLayoutManager);

        Log.e("OCV crf", "called");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rvChatroomGridList != null && myChatsList != null) {
            rvChatroomGridList.scrollToPosition(myChatsList.size());
        }
        Log.e("onResume", "called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause", "called");
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            System.gc();
        }
        if (menuVisible) {
            Log.e("mapRendered", String.valueOf(isMapRendered));
            if (!isMapRendered) {
                application.mIMapController.setZoom(20);
                application.mIMapController.setCenter(startPoint);
            }
            isMapRendered = true;
            if (!application.mZomeUtils.isUserAnonymous) {
                if (myChatsList != null) {
                    if (myChatsList.size() <= 1 && !isFirstDialogDisplayed) {
                        new FirstChatroomDialog(context).show();
                    }
                    isFirstDialogDisplayed = true;
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem menuItem = menu.findItem(R.id.action_refresh);
        if (menuItem != null) {
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    isMapRendered = true;
                    startTime = System.currentTimeMillis();
                    getData();
                    return false;
                }
            });
        }
    }

}
