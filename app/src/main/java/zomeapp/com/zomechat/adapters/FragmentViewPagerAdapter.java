package zomeapp.com.zomechat.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.fragments.ChatroomFragment;
import zomeapp.com.zomechat.fragments.FeedsFragment;
import zomeapp.com.zomechat.fragments.SettingsFragment;

/**
 * Created by tkiet082187 on 08.10.15.
 */
public class FragmentViewPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Feed", "Chatroom", "Other" };
    private Context context;

    private int[] imageResId = {
            R.drawable.ic_feed,
            R.drawable.ic_chatroom,
            R.drawable.ic_settings
    };

    public FragmentViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == 0) {
            //fragment = FeedsFragment.newInstance();
            fragment = new FeedsFragment();
        } else if (position == 1) {
            //fragment = ChatroomFragment.newInstance();
            fragment = new ChatroomFragment();
        } else if (position == 2) {
            //fragment = SettingsFragment.newInstance();
            fragment = new SettingsFragment();
        }

        return fragment;
    }



    public View getTabView(int position) {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        TextView tv = (TextView) v.findViewById(R.id.tvTabName);
        tv.setText(tabTitles[position]);
        tv.setTypeface(Typeface.createFromAsset(context.getAssets(), "rezland.ttf"));
        ImageView img = (ImageView) v.findViewById(R.id.ivTabIcon);
        img.setImageResource(imageResId[position]);
        return v;
    }

    @Override
    public int getItemPosition(Object object) {
        Log.e("item pos", String.valueOf(super.getItemPosition(object)));
        //return super.getItemPosition(object);
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        Drawable image = ContextCompat.getDrawable(context, imageResId[position]);
//        Drawable image = context.getResources().getDrawable(imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        // Replace blank spaces with image icon
        SpannableString sb = new SpannableString("   " + tabTitles[position]);
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;

    }
}