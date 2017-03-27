package zomeapp.com.zomechat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;

public class SignUpActivity extends Activity {

    private LinearLayout linearLayout;
    private EditText etEmailSignUp, etPasswordSignUp, etConfirmPassword;
    private Button btnSignUp;
    private Button btnBack;

    private Context context;

    private ZomeApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        context = this;

        application = (ZomeApplication) getApplication();

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        etEmailSignUp = (EditText) findViewById(R.id.etEmailSignUp);
        etPasswordSignUp = (EditText) findViewById(R.id.etPasswordSignUp);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnBack = (Button) findViewById(R.id.btnBack);

        RelativeLayout.LayoutParams llParams = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        if (application.mZomeUtils.metrics.heightPixels <= 320) {
            llParams.setMargins(application.mZomeUtils.dpToPx(32), 0, application.mZomeUtils.dpToPx(32), application.mZomeUtils.dpToPx(15));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                linearLayout.setGravity(Gravity.TOP);
            }
            linearLayout.setLayoutParams(llParams);
        }

        btnSignUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnSignUp.setTextColor(Color.BLACK);
                } else {
                    btnSignUp.setTextColor(Color.parseColor("#767a85"));
                }
                return false;
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etPasswordSignUp.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    Toast.makeText(context, "Passwords do not match. Please double-check your password.", Toast.LENGTH_SHORT).show();
                } else if (etPasswordSignUp.getText().length() < 5) {
                    Toast.makeText(context, "Passwords must contain at least 5 characters.", Toast.LENGTH_SHORT).show();
                } else {
                    if (application.mZomeUtils.isValidEmail(etEmailSignUp.getText())) {
                        postSignUpData();
                    } else {
                        Toast.makeText(context, "Not a valid email address.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    btnBack.setTextColor(Color.BLACK);
                } else {
                    btnBack.setTextColor(Color.WHITE);
                }
                return false;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void postSignUpData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            jsonObject.put("password", etPasswordSignUp.getText().toString());
            jsonObject.put("uid", etEmailSignUp.getText().toString());
            jsonObject.put("lat", application.lat);
            jsonObject.put("lng", application.lng);

            application.mSocket.emit("signup", jsonObject);

            Intent returnData = new Intent();
            returnData.putExtra("uid", jsonObject.getString("uid"));
            returnData.putExtra("password", jsonObject.getString("password"));
            setResult(RESULT_OK, returnData);
            finish();
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
