package zomeapp.com.zomechat.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;

/**
 * Created by tkiet082187 on 29.10.15.
 */
public class ReportDialog extends Dialog {
    private String reportType;
    private String commentOrPostString;
    private String selectedId;

    private TextView tvComment;
    private EditText etReportReason;
    private Button btnCancel, btnReport;

    private ReportDialog mReportDialog;

    private ZomeApplication application;

    public ReportDialog(final Context context, int themeResId, final String reportType, String commentOrPostString, final String selectedId) {
        super(context, themeResId);
        mReportDialog = this;
        this.reportType = reportType;
        this.commentOrPostString = commentOrPostString;
        this.selectedId = selectedId;

        application = (ZomeApplication) context.getApplicationContext();

        setContentView(R.layout.report_dialog);

        mReportDialog.setTitle(context.getApplicationContext().getString(R.string.report_comment));

        tvComment = (TextView) findViewById(R.id.tvComment);
        etReportReason = (EditText) findViewById(R.id.etReportReason);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            etReportReason.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnReport = (Button) findViewById(R.id.btnReport);

        tvComment.setText(commentOrPostString);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences preferences = context.getSharedPreferences("saveForLoginSession", Context.MODE_PRIVATE);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("objectId", selectedId);
                    jsonObject.put("uid", preferences.getString("uid", ""));
                    jsonObject.put("reportReason", etReportReason.getText().toString());
                    jsonObject.put("reportObject", reportType);

                    String correlateReportType = "";

                    if (reportType.equals("COMMENT")) {
                        correlateReportType = "This comment is going under our inspection list. It will be removed shortly if it violates our terms";
                    } else if (reportType.equals("POST")) {
                        correlateReportType = "This post is going under our inspection list. It will be removed shortly if it violates our terms";
                    } else if (reportType.equals("MESSAGE")) {
                        correlateReportType = "This message is going under our inspection list. It will be removed shortly if it violates our terms";
                    }

                    application.mSocket.emit("requestReport", jsonObject);
                    Toast.makeText(context,
                            correlateReportType,
                            Toast.LENGTH_LONG)
                            .show();
                    dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
