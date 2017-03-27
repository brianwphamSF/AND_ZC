package zomeapp.com.zomechat.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import zomeapp.com.zomechat.R;

/**
 * Created by tkiet082187 on 12.10.15.
 */
public class RetrieveCityImage extends AsyncHttpClient {
    // http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&minx=-3.59&miny=37.17&maxx=-3.79&maxy=37.37&size=medium&mapfilter=true
    private Context context;
    private double minLat, minLng, maxLat, maxLng;
    private JSONObject jsonObject;
    private AsyncHttpClient asyncHttpClient = this;
    private RequestParams latLngParams = new RequestParams();

    public RetrieveCityImage(Context context, double lat, double lng) {
        this.context = context;
        this.minLat = lat - 0.01;
        this.minLng = lng - 0.01;
        this.maxLat = lat + 0.01;
        this.maxLng = lng + 0.01;
        latLngParams.put("miny", minLat);
        latLngParams.put("minx", minLng);
        latLngParams.put("maxy", maxLat);
        latLngParams.put("maxx", maxLng);
    }

    public void loadFirstImageUrlFromLocation(final SimpleDraweeView draweeView) {
        asyncHttpClient.get("http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&size=medium&mapfilter=true",
                latLngParams,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e("response success", new String(responseBody));
                        try {
                            jsonObject = new JSONObject(new String(responseBody));
                            final JSONArray array = jsonObject.getJSONArray("photos");
                            String firstPhotoUrl = array.getJSONObject(0).getString("photo_file_url");
                            Uri uri = Uri.parse(firstPhotoUrl);
                            if (array.getJSONObject(0).getInt("height") >= array.getJSONObject(0).getInt("width")) {
                                GenericDraweeHierarchyBuilder builder =
                                        new GenericDraweeHierarchyBuilder(context.getResources());
                                GenericDraweeHierarchy hierarchy = builder
                                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
                                draweeView.setHierarchy(hierarchy);
                            }
                            draweeView.setImageURI(uri);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        error.printStackTrace();
                    }
                });

    }

    public void loadDistinctImageUrlsFromLocation(final SimpleDraweeView draweeView, final int position) {
        asyncHttpClient.get("http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&size=medium&mapfilter=true",
                latLngParams,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e("response success", new String(responseBody));
                        try {
                            jsonObject = new JSONObject(new String(responseBody));
                            JSONArray array = jsonObject.getJSONArray("photos");

                            Uri uri;
                            if (position < array.length()) {
                                String photoUrl = array.getJSONObject(position).getString("photo_file_url");
                                uri = Uri.parse(photoUrl);
                            } else {
                                uri = new Uri.Builder()
                                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                        .path(String.valueOf(R.drawable.zome_logo))
                                        .build();
                            }

                            draweeView.setImageURI(uri);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        error.printStackTrace();
                    }
                });

    }

}
