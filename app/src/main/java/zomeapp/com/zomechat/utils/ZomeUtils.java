package zomeapp.com.zomechat.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.fragments.FeedsFragment;

/**
 * Created by tkiet082187 on 29.09.15.
 */
public class ZomeUtils implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<LocationSettingsResult> {
    private Context mContext;
    private ZomeApplication application;
    //public static final String serverUrl = "http://ec2-54-205-59-87.compute-1.amazonaws.com:1442";
    //public static final String serverUrl = "http://10.0.2.2:1442"; // default emulator localhost
//    public static final String serverUrl = "http://10.0.3.2:1442"; // GenyMotion localhost
    //public static final String serverUrl = "http://192.168.0.142:1442"; // device's localhost (must be connected to same network)
    public boolean mRequestingLocationUpdates;

    public int barHeight;
    public Snackbar mSnackbar;
    public AppBarLayout mainAppBarLayout;

    private final String TAG = ZomeUtils.class.getSimpleName();

    private Location mLastLocation, mCurrentLocation;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private SecretKeyFactory factory;

    LocationSettingsRequest mLocationSettingsRequest;

    public boolean isUserAnonymous;

    private long SECOND_INTERVAL = 1000;

    public DisplayMetrics metrics;

    public Fragment feedsInstanceFragment;

    public ImagePipeline imagePipeline;

    public ZomeUtils(Context mContext, View view) {
        this.mContext = mContext;
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this.mContext).setDownsampleEnabled(true).build();
        Fresco.initialize(this.mContext, config);
        imagePipeline = Fresco.getImagePipeline();
        application = (ZomeApplication) this.mContext.getApplicationContext();
        mSnackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
        mRequestingLocationUpdates = false;
        metrics = Resources.getSystem().getDisplayMetrics();
        buildGoogleApiClient(this.mContext);
        Log.e("context", this.mContext.toString());
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @SuppressLint("SetTextI18n")
    public void returnApproxTime(Date date, TextView textView) {
        Date diffDate = GregorianCalendar.getInstance().getTime();
        long diffTime = diffDate.getTime() - date.getTime();

        long diff = TimeUnit.MILLISECONDS.toSeconds(diffTime);

        String timeText;

        Log.e("diff", String.valueOf(diffTime));
        Log.e("unit", String.valueOf(diff));
        if (diff < 1) {
            textView.setText("now");
        } else if (diff < 60) {
            textView.setText("less than a minute ago");
        } else if (diff < 3600) {
            int min = (int) (diff / 60);
            if (min <= 1) {
                timeText = " minute ago";
            } else {
                timeText = " minutes ago";
            }
            textView.setText(min + timeText);
        } else if (diff < 86400) {
            int hour = (int) (diff / 60 / 60);
            if (hour <= 1) {
                timeText = " hour ago";
            } else {
                timeText = " hours ago";
            }
            textView.setText(hour + timeText);
        } else if (diff < 2628000) {
            int days = (int) (diff / 60 / 60 / 24);
            if (days <= 1) {
                timeText = " day ago";
            } else {
                timeText = " days ago";
            }
            textView.setText(days + timeText);
        } else if (diff < 31536000) {
            int month = (int) (diff / 60 / 60 / 24 / 30);
            if (month <= 1) {
                timeText = " month ago";
            } else {
                timeText = " months ago";
            }
            textView.setText(month + timeText);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/y", Locale.getDefault());
            textView.setText(sdf.format(date));
        }
    }

    public void firstStep() throws NoSuchAlgorithmException {
        factory = SecretKeyFactory.getInstance("DES");
    }

    public final boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public String whoAmI(byte[] firstBytes, byte[] nextBytes) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException {
        SecretKey key = factory.generateSecret(new DESKeySpec(firstBytes));
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] stringBytes = cipher.doFinal(nextBytes);
        return new String(stringBytes, "UTF-8");
    }

    public void showToastAnonymousUserMessage(String additionalMsg) {
        Toast.makeText(mContext, "You are in \"Browse Mode\". Please login to " + additionalMsg, Toast.LENGTH_LONG).show();
    }

    public void setAppBarDragging(final boolean newValue) {
        AppBarLayout appBarLayout = mainAppBarLayout;
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return newValue;
            }
        });
        params.setBehavior(behavior);
    }

    public void setTags(TextView pTextView, String pTagString) {
        SpannableString string = new SpannableString(pTagString);

        int start = -1;
        for (int i = 0; i < pTagString.length(); i++) {
            if (pTagString.charAt(i) == '#') {
                start = i;
            } else if (pTagString.charAt(i) == ' ' || (i == pTagString.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == pTagString.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    final String tag = pTagString.substring(start, i);
                    string.setSpan(new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            Log.d("Hash", String.format("Clicked %s!", tag));
                            String hashed = tag.substring(1, tag.length());
                            if (feedsInstanceFragment instanceof FeedsFragment) {
                                ((FeedsFragment) feedsInstanceFragment).onQueryTextChange(hashed);
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            // link color
                            ds.setColor(Color.parseColor("#33b5e5"));
                            ds.setUnderlineText(false);
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                }
            }
        }

        pTextView.setMovementMethod(LinkMovementMethod.getInstance());
        pTextView.setText(string);
    }

    public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public Snackbar getSnackbar(View v, CharSequence commentOrPost, CharSequence snackText, View.OnClickListener clickListener) {
        mSnackbar = Snackbar.make(v, commentOrPost, Snackbar.LENGTH_INDEFINITE).setActionTextColor(Color.CYAN);
        if (clickListener != null && !snackText.toString().equals("")) {
            mSnackbar.setAction(snackText, clickListener);
        }
        View view = mSnackbar.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.setMargins(0, barHeight, 0, 0);
        view.setLayoutParams(params);
        return mSnackbar;
    }

    public Snackbar repositionCustomSnackbarBelowToolbar(Snackbar mSnackbar) {
        View view = mSnackbar.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.setMargins(0, barHeight, 0, 0);
        view.setLayoutParams(params);
        return mSnackbar;
    }

    public void delayForDataRetrieval(@Nullable final Long value) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (value == null) {
                        Thread.sleep(500);
                    } else {
                        Thread.sleep(value);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        thread.run();
    }

    public void changeToolbarTypeface(Toolbar toolbar, Typeface typeface) {
        TextView titleTextView;
        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
            titleTextView.setTypeface(typeface);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public void changeCollapsingToolbarTypeface(CollapsingToolbarLayout collapsingToolbarLayout, Typeface typeface) {
        try {
            // Retrieve the CollapsingTextHelper Field
            final Field cthf = collapsingToolbarLayout.getClass().getDeclaredField("mCollapsingTextHelper");
            cthf.setAccessible(true);

            // Retrieve an instance of CollapsingTextHelper and its TextPaint
            final Object cth = cthf.get(collapsingToolbarLayout);
            final Field tpf = cth.getClass().getDeclaredField("mTextPaint");
            tpf.setAccessible(true);

            // Apply your Typeface to the CollapsingTextHelper TextPaint
            ((TextPaint) tpf.get(cth)).setTypeface(typeface);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            // Nothing to do
        }
    }

    public Location getLegacyLocation() {
        Location location = null;
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
                if (location != null) {
                    Log.e("loc from network", location.toString());
                    //mLocation = location;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates

        if (Build.VERSION.SDK_INT >= 23)
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                //return;
            }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location == null) {
                boolean enabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (!enabled) {
                    new Utils().displayPromptForEnablingGPS((Activity) mContext);
                    //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //startActivity(intent);
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Log.e("called", "success");
            }

            locationManager.removeUpdates(locationListener);

            if (location != null) {
                Log.e("mLoc", location.toString());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return location;
    }

    public Location getLocation() {
        if (mCurrentLocation != null) {
            return mCurrentLocation;
        } else {
            return mLastLocation;
        }
    }

    public synchronized void buildGoogleApiClient(Context context) {
        application.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(SECOND_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(SECOND_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {

        Log.e("startUpd", "called");

        LocationServices.FusedLocationApi.requestLocationUpdates(
                application.mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(application.mGoogleApiClient.isConnected()){
            Log.e("client", "Google_Api_Client: It was connected on (onConnected) function, working as it should.");
        }
        else{
            Log.e("client", "Google_Api_Client: It was NOT connected on (onConnected) function, It is definetly bugged.");
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                application.mGoogleApiClient);

        Log.e("client", application.mGoogleApiClient.toString());
        if (mLastLocation != null) {
            Log.e("gLoc", mLastLocation.toString());
            application.lat = mLastLocation.getLatitude();
            application.lng = mLastLocation.getLongitude();
        }
        //buildLocationSettingsRequest();
        checkLocationSettings();
        startLocationUpdates();
        //stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.e("gCurLoc", mCurrentLocation.toString());
        application.lat = mCurrentLocation.getLatitude();
        application.lng = mCurrentLocation.getLongitude();
        /*if (mCurrentLocation != null) {
            stopLocationUpdates();
        }*/
        //stopLocationUpdates();
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                application.mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        application.mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        Log.e("status", status.getStatusCode() + "");
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                //stopLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult((Activity) this.mContext, application.REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }
}
