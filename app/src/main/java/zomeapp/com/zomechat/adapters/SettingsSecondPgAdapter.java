package zomeapp.com.zomechat.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;

import zomeapp.com.zomechat.activities.LoginActivity;
import zomeapp.com.zomechat.application.ZomeApplication;

/**
 * Created by tkiet082187 on 13.11.15.
 */
public class SettingsSecondPgAdapter extends RecyclerView.Adapter<SettingsSecondPgAdapter.ViewHolder> {
    private String[] strings;
    private Context context;
    private ZomeApplication application;

    public SettingsSecondPgAdapter(Context context, String[] strings) {
        this.context = context;
        this.strings = strings;
        application = (ZomeApplication) this.context.getApplicationContext();
    }

    @Override
    public SettingsSecondPgAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = application.mZomeUtils.dpToPx(35);
        v.setLayoutParams(params);
        return new SettingsSecondPgAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SettingsSecondPgAdapter.ViewHolder holder, final int position) {
        String string = strings[position];
        holder.tvItems.setText(string);

        holder.tvItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (position <= 1) {
                    try {
                        intent = Intent.parseUri("http://www.zomeapp.com/", Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else if (position == 2) {
                    Toast.makeText(context, "ZomeChat App version: 1.15", Toast.LENGTH_SHORT).show();
                } else if (position == 3) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Join me on ZomeChat! https://itunes.apple.com/us/app/id828153007?mt=8");
                    intent.setType("text/plain");
                    context.startActivity(intent);
                } else if (position == 4) {
                    SharedPreferences autoLoginPrefs = context.getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
                    autoLoginPrefs.edit().clear().apply();
                    intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    ((AppCompatActivity) context).finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return strings.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItems;
        public ViewHolder(View itemView) {
            super(itemView);
            tvItems = (TextView) itemView.findViewById(android.R.id.text1);
            tvItems.setTextColor(Color.WHITE);
            tvItems.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tvItems.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}
