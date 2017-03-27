package zomeapp.com.zomechat.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;

public class LoginActivity extends Activity {

    private final String TAG = LoginActivity.class.getSimpleName();

    private ZomeApplication application;

    private LinearLayout linearLayout;

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private Button btnFacebookLogin, btnBrowseMode, btnSignUp;
    private Context context;

    private final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.5/me?fields=id,name,email,picture,gender,bio,birthday";

    private OAuthService service;
    private Token EMPTY_TOKEN = null;
    private String authorizationUrl, code;
    private WebView wvFacebookLogin;
    private JSONObject facebookLoginObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        application = (ZomeApplication) getApplication();

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        RelativeLayout.LayoutParams llParams = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        if (application.mZomeUtils.metrics.heightPixels <= 320) {
            llParams.setMargins(application.mZomeUtils.dpToPx(32), 0, application.mZomeUtils.dpToPx(32), application.mZomeUtils.dpToPx(15));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                linearLayout.setGravity(Gravity.TOP);
            }
            linearLayout.setLayoutParams(llParams);
        }

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnFacebookLogin = (Button) findViewById(R.id.btnFacebookLogin);
        btnBrowseMode = (Button) findViewById(R.id.btnBrowseMode);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        wvFacebookLogin = (WebView) findViewById(R.id.wvFacebookLogin);

        SharedPreferences loginPrefs = context.getSharedPreferences("emailLoginSession", MODE_PRIVATE);

        etEmail.setText(loginPrefs.getString("uid", ""));
        etPassword.setText(loginPrefs.getString("password", ""));

        btnLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnLogin.setTextColor(Color.BLACK);
                } else {
                    btnLogin.setTextColor(Color.parseColor("#767a85"));
                }
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnected("REGISTER");
            }
        });

        btnFacebookLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnFacebookLogin.setTextColor(Color.LTGRAY);
                } else {
                    btnFacebookLogin.setTextColor(Color.BLACK);
                }
                return false;
            }
        });

        btnFacebookLogin.setOnClickListener(facebookListener);

        btnBrowseMode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnBrowseMode.setTextColor(Color.BLACK);
                } else {
                    btnBrowseMode.setTextColor(Color.WHITE);
                }
                return false;
            }
        });

        btnBrowseMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnected("ANONYMOUS");
            }
        });

        btnSignUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnSignUp.setTextColor(Color.BLACK);
                } else {
                    btnSignUp.setTextColor(Color.WHITE);
                }
                return false;
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, 200);
            }
        });
    }

    private View.OnClickListener facebookListener = new View.OnClickListener() {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onClick(View v) {
            try {
                wvFacebookLogin.setVisibility(View.VISIBLE);
                application.mZomeUtils.firstStep();
                service = new ServiceBuilder()
                        .provider(FacebookApi.class)
                        .apiKey(application.mZomeUtils.whoAmI(
                                new byte[] {56, 54, 50, 57, 50, 50, 56, 56, 51, 55, 55, 49, 49, 49, 55},
                                new byte[] {125, -8, -50, 1, 79, -96, 82, -76, 116, -87, -79, -74, -123, -91, -111, -35}
                        ))
                        .apiSecret(application.mZomeUtils.whoAmI(
                                new byte[] {100, 48, 52, 98, 48, 54, 99, 51, 55, 51, 100, 100, 54, 51, 99, 53, 56, 100, 98, 100, 57, 49, 101, 49, 49, 102, 50, 101, 50, 98, 51, 57},
                                new byte[] {42, 36, 6, -71, -16, 79, -111, 54, -113, -83, -22, -24, -126, -6, 53, 84, -121, 92, 92, 112, -52, -69, 57, 19, -80, 18, -48, 117, -102, 83, -119, 64, -59, -67, 57, -24, 22, -81, 74, 11}
                        ))
                        .callback("http://www.zomeapp.com/")
                        .build();

                // Obtain the Authorization URL
                Log.e(TAG, "Fetching the Authorization URL...");
                authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);

                wvFacebookLogin.getSettings().setJavaScriptEnabled(true);

                wvFacebookLogin.requestFocus(View.FOCUS_DOWN);

                wvFacebookLogin.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        // Activities and WebViews measure progress with different scales.
                        // The progress meter will automatically disappear when we reach 100%
                        ((Activity) context).setProgress(progress * 1000);
                    }
                });

                wvFacebookLogin.setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Log.e("url", url);
                        if (url.contains("www.zomeapp.com/?code=")) {
                            code = url.substring(url.indexOf("=") + 1, url.length());
                            Log.e("code", code);
                            new FacebookTask().execute();
                        }
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                });

                wvFacebookLogin.loadUrl(authorizationUrl);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | InvalidKeySpecException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (wvFacebookLogin.getVisibility() == View.VISIBLE) {
            wvFacebookLogin.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void onConnected(String signinType) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("signinType", signinType);
            jsonObject.put("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            switch (signinType) {
                case "REGISTER":
                    jsonObject.put("uid", etEmail.getText().toString());
                    jsonObject.put("password", etPassword.getText().toString());
                    Toast.makeText(context, "Logging in as: " + etEmail.getText().toString(), Toast.LENGTH_LONG).show();
                    break;
                case "FACEBOOK":
                    jsonObject.put("uid", facebookLoginObject.getString("id"));
                    jsonObject.put("fbLink", "https://www.facebook.com/app_scoped_user_id/" + facebookLoginObject.getString("id") + "/");
                    jsonObject.put("thumbnailURL", "https://graph.facebook.com/" + facebookLoginObject.getString("id") + "/picture?type=large&return_ssl_resources=1");
                    jsonObject.put("email", facebookLoginObject.getString("email"));
                    String firstName = facebookLoginObject.getString("name").substring(0, facebookLoginObject.getString("name").indexOf(' '));
                    jsonObject.put("username", firstName);
                    Toast.makeText(context, "Logging in with Facebook", Toast.LENGTH_SHORT).show();
                    break;
                case "ANONYMOUS":
                    jsonObject.put("uid", JSONObject.NULL);
                    jsonObject.put("password", JSONObject.NULL);
                    Toast.makeText(context, "Logging in via view-only mode", Toast.LENGTH_LONG).show();
                    break;
            }
            if (application.lat != null && application.lng != null) {
                jsonObject.put("lat", application.lat);
                jsonObject.put("lng", application.lng);
            }

            if (!signinType.equals("ANONYMOUS")) {
                application.mZomeUtils.isUserAnonymous = false;
                SharedPreferences autoLoginPrefs = getSharedPreferences("autoLogin", MODE_PRIVATE);
                SharedPreferences.Editor autoLoginEditor = autoLoginPrefs.edit();
                autoLoginEditor.putString("jsonLoginObject", jsonObject.toString());
                autoLoginEditor.apply();
            } else {
                application.mZomeUtils.isUserAnonymous = true;
            }

            IO.Options options = new IO.Options();
            options.reconnection = true;

            application.mSocket.emit("login", jsonObject);
            application.mSocket.on("login_response", loginEListener);

        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener loginEListener = new Emitter.Listener() {
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
                    Log.e("logged in", "success");

                    SharedPreferences preferences = context.getSharedPreferences("loginItems", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("loginJson", requestItems.toString());
                    editor.apply();

                    SharedPreferences loginPrefs = context.getSharedPreferences("saveForLoginSession", MODE_PRIVATE);
                    SharedPreferences.Editor loginEditor = loginPrefs.edit();
                    if (loginPass.getString("userType").equals("REGISTER")) {
                        loginEditor.clear();
                        loginEditor.putString("uid", etEmail.getText().toString());
                        loginEditor.putString("password", etPassword.getText().toString());
                        loginEditor.putString("userType", "REGISTER");
                        loginEditor.apply();

                        SharedPreferences loginByEmail = context.getSharedPreferences("emailLoginSession", MODE_PRIVATE);
                        SharedPreferences.Editor emailEditor = loginByEmail.edit();
                        emailEditor.putString("uid", etEmail.getText().toString());
                        emailEditor.putString("password", etPassword.getText().toString());
                        emailEditor.apply();
                    } else if (loginPass.getString("userType").equals("FACEBOOK")) {
                        loginEditor.clear();
                        loginEditor.putString("uid", loginPass.getString("uid"));
                        loginEditor.putString("userType", "FACEBOOK");
                        loginEditor.apply();
                    }

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(context, "Error with login. Reason: " + loginPass.getString("displayMessage"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
    };

    private class FacebookTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            final Verifier verifier = new Verifier(code);

            // Trade the Request Token and Verfier for the Access Token
            Log.e(TAG, "Trading the Request Token for an Access Token...");
            final Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
            Log.e(TAG, "Got the Access Token!");
            Log.e(TAG, "(if your curious it looks like this: " + accessToken + " )");
            Log.e(TAG, "");

            // Now let's go and ask for a protected resource!
            Log.e(TAG, "Now we're going to access a protected resource...");
            final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            service.signRequest(accessToken, request);
            final Response response = request.send();
            Log.e(TAG, "Got it! Lets see what we found...");
            Log.e(TAG, "");
            Log.e(TAG, String.valueOf(response.getCode()));
            Log.e(TAG, response.getBody());

            try {
                facebookLoginObject = new JSONObject(response.getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e(TAG, "");
            Log.e(TAG, "Thats it man! Go and build something awesome with ScribeJava! :)");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("Login FB", "success");
            wvFacebookLogin.setVisibility(View.GONE);
            onConnected("FACEBOOK");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200 && data != null) {
            Toast.makeText(context, "Signed up as " + data.getStringExtra("uid") + " successfully! You may now login as " + data.getStringExtra("uid") + ".", Toast.LENGTH_SHORT).show();
            etEmail.setText(data.getStringExtra("uid"));
            etPassword.setText(data.getStringExtra("password"));
        }
    }
}
