package zomeapp.com.zomechat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.ReportDialog;
import zomeapp.com.zomechat.models.FeedResponse;

/**
 * Created by tkiet082187 on 27.10.15.
 */
public class FeedResponsesAdapter extends RecyclerView.Adapter<FeedResponsesAdapter.ViewHolder> {
    private ArrayList<FeedResponse> feedResponses;
    private ZomeApplication application;
    private Context context;
    public Snackbar snackbar;

    public FeedResponsesAdapter(Context context, ArrayList<FeedResponse> feedResponses) {
        this.feedResponses = feedResponses;
        this.context = context;
        application = (ZomeApplication) this.context.getApplicationContext();
    }

    @Override
    public FeedResponsesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_reply_row, parent, false);
        return new FeedResponsesAdapter.ViewHolder(v);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        application.mZomeUtils.imagePipeline.clearCaches();
        Log.e("view", "recycling");
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final FeedResponsesAdapter.ViewHolder holder, int position) {
        final FeedResponse feedResponse = feedResponses.get(position);

        holder.ivProfilePicture.setImageURI(Uri.parse(feedResponse.getOwnerImageURL()));
        holder.tvProfileName.setText(feedResponse.getOwnerName());
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        try {
            Date date = sdf.parse(feedResponse.getTime());
            application.mZomeUtils.returnApproxTime(date, holder.tvTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvContent.setText(feedResponse.getContent());

        holder.rlContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                snackbar = application.mZomeUtils.getSnackbar(v, "Report post '" + holder.tvContent.getText().toString() + "'?", "Click here to report", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("action", "clicked");
                        if (application.mZomeUtils.isUserAnonymous) {
                            application.mZomeUtils.showToastAnonymousUserMessage("make a report.");
                        } else {
                            ReportDialog dialog = new ReportDialog(context, 0, "COMMENT", holder.tvContent.getText().toString(), feedResponse.getCommentId());
                            dialog.show();
                        }
                    }
                });
                snackbar.show();
                return true;
            }
        });

        holder.rlContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (snackbar.isShown()) {
                    snackbar.dismiss();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return feedResponses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvProfileName, tvContent, tvTime;
        private SimpleDraweeView ivProfilePicture;
        private RelativeLayout rlContent;

        public ViewHolder(View itemView) {
            super(itemView);
            tvContent = (TextView) itemView.findViewById(R.id.tvContent);
            tvProfileName = (TextView) itemView.findViewById(R.id.tvProfileName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            ivProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.ivProfilePicture);
            rlContent = (RelativeLayout) itemView.findViewById(R.id.rlContent);
        }
    }
}
