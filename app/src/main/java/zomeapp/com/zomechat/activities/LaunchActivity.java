package zomeapp.com.zomechat.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.utils.ZomeUtils;

public class LaunchActivity extends Activity {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 103;
    private final String TAG = LaunchActivity.class.getSimpleName();

    private TextView tvLaunch;
    private ZomeApplication application;
    private Context context;
    private Location mLocation;
    private SharedPreferences autoLoginPrefs;
    private JSONObject autoLoginObject;

    //private final int SPLASH_TIME_OUT = application.SPLASH_TIME_OUT;

    private void initSocket() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            //HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

            // mSocket options
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.secure = true;
            opts.sslContext = sc;
            application.mSocket = IO.socket(application.serverUrl, opts);
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        IO.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    } };

    public Emitter.Listener loginEListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("login", args[0].toString());
            final JSONObject loginPass = (JSONObject) args[0];
            try {
                JSONObject requestItems = new JSONObject();
                requestItems.put("uid", loginPass.getString("uid"));
                requestItems.put("lat", application.lat);
                requestItems.put("lng", application.lng);
                if (loginPass.getString("respond").equals("LOGIN_SUCCESS")) {
                    SharedPreferences preferences = context.getSharedPreferences("loginItems", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("loginJson", requestItems.toString());
                    editor.apply();
                    Log.e("logged in", "success");
                    Intent mainIntent = new Intent(LaunchActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(context, "Error with autologin. Reason: " + loginPass.getString("displayMessage"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Intent mainIntent = new Intent(LaunchActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        context = this;
        View view = findViewById(R.id.view);

        autoLoginPrefs = getSharedPreferences("autoLogin", MODE_PRIVATE);

        application = (ZomeApplication) getApplication();

        application.mZomeUtils = new ZomeUtils(context, view);

        if (Build.VERSION.SDK_INT <= 13) {
            mLocation = application.mZomeUtils.getLegacyLocation();
        } else {
            mLocation = application.mZomeUtils.getLocation();
        }

        if (mLocation != null) {
            Log.e("mLocation", mLocation.toString());
            application.lat = mLocation.getLatitude();
            application.lng = mLocation.getLongitude();
        }

        try {
            autoLoginObject = new JSONObject(autoLoginPrefs.getString("jsonLoginObject", "{}"));
            Log.e("autoLoginObj", autoLoginObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initSocket();

        application.mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("connected", "Socket Connected");
            }
        }).on("message", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("msg", "'message' event: " + args[0].toString());
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("disconnected", "Socket Disconnected");
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("error", "error: " + args[0].toString());

            }

        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("reconnect", "reconnect: " + args[0].toString());
            }
        });

        if (!application.mSocket.connected()) {
            Log.e("mSocket", "connecting");
            application.mSocket.connect();
        } else {
            Log.e("mSocket", "connected");
        }

        Typeface tf = Typeface.createFromAsset(getAssets(), "rezland.ttf");

        tvLaunch = (TextView) findViewById(R.id.tvLuanch);
        tvLaunch.setTypeface(tf);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    application.mZomeUtils.stopLocationUpdates();
                }

                if (application.lat == null && application.lng == null) {
                    Toast.makeText(context, "Location not found. Please enable it in order to connect! Exiting ZomeChat.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (autoLoginObject.toString().equals("{}") || !application.mSocket.connected()) {
                        // Start your app main activity
                        Intent i = new Intent(LaunchActivity.this, LoginActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    } else {

                        try {
                            autoLoginPrefs.edit().clear().apply();
                            autoLoginObject.put("lat", application.lat);
                            autoLoginObject.put("lng", application.lng);
                            autoLoginPrefs.edit().putString("jsonLoginObject", autoLoginObject.toString()).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("autoLoginObj2ndCall", autoLoginObject.toString());
                        Toast.makeText(context, "Login session saved. Trying auto-login.", Toast.LENGTH_LONG).show();
                        application.mSocket.emit("login", autoLoginObject);
                        application.mSocket.on("login_response", loginEListener);
                    }
                }
            }
        }, application.getSplashTimeOut());
    }

    private void getLocationThroughGoogle() {
        //mSocketIOUtil.createLocationRequest();
        if (application.mGoogleApiClient.isConnected() && !application.mZomeUtils.mRequestingLocationUpdates) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                application.mZomeUtils.startLocationUpdates();
            } else {
                int permissionCheck = ContextCompat.checkSelfPermission(application,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION);

                    // REQUEST_ACCESS_FINE_LOCATION is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    application.mZomeUtils.startLocationUpdates();

                } else {

                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 14) {
            getLocationThroughGoogle();
            application.mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (Build.VERSION.SDK_INT >= 14) {
            application.mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT <= 13) {
            application.mZomeUtils.getLegacyLocation();
        } else {
            Log.e("testing if this runs", "success");
            getLocationThroughGoogle();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == application.REQUEST_CHECK_SETTINGS) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    getLocationThroughGoogle();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    break;
            }
        }
    }
}
