package zomeapp.com.zomechat.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zomeapp.com.zomechat.R;
import zomeapp.com.zomechat.activities.FeedDetailActivity;
import zomeapp.com.zomechat.application.ZomeApplication;
import zomeapp.com.zomechat.dialogs.ReportDialog;
import zomeapp.com.zomechat.models.Feed;

/**
 * Created by tkiet082187 on 07.10.15.
 */
public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ViewHolder> {

    private final ArrayList<Feed> feedArrayList;
    private Context context;
    private ZomeApplication application;
    private Snackbar snackbar;
    private DisplayMetrics metrics;

    public FeedsAdapter(Context context, ArrayList<Feed> feeds) {
        this.context = context;
        feedArrayList = feeds;
        application = (ZomeApplication) this.context.getApplicationContext();
        metrics = Resources.getSystem().getDisplayMetrics();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feeds_optimized_row, viewGroup, false);
        snackbar = Snackbar.make(viewGroup, "", Snackbar.LENGTH_SHORT);
        return new FeedsAdapter.ViewHolder(v);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        application.mZomeUtils.imagePipeline.clearCaches();
        Log.e("view", "recycling");
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final Feed feed = feedArrayList.get(i);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.mIvProfileImage.getLayoutParams();

        if (!feed.getImageUrl().equals("")) {
            viewHolder.ivAttachedPhoto.setVisibility(View.VISIBLE);

            Log.e("device dim", metrics.widthPixels + "x" + metrics.heightPixels);

            Log.e("wLP, wIV", viewHolder.ivAttachedPhoto.getLayoutParams().width + ", " + viewHolder.ivAttachedPhoto.getWidth());

            Postprocessor postProcessor = new Postprocessor() {
                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    Log.e("pp bmp dim", sourceBitmap.getWidth() + "x" + sourceBitmap.getHeight());
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public CacheKey getPostprocessorCacheKey() {
                    return null;
                }
            };

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(feed.getImageUrl())).setPostprocessor(postProcessor).build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(feed.getImageUrl()))
//                    .setImageRequest(request)
                    .setOldController(viewHolder.ivAttachedPhoto.getController())
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                            super.onFinalImageSet(id, imageInfo, animatable);
//                            Log.e("imageInfo", imageInfo.getWidth() + "x" + imageInfo.getHeight());
//                            Log.e("vh info", viewHolder.ivAttachedPhoto.getWidth() + "x" + viewHolder.ivAttachedPhoto.getHeight());
//                            Log.e("vh drawable info", viewHolder.ivAttachedPhoto.getDrawable().getBounds().width() + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().height());
//                            Log.e("vh drawable dim", viewHolder.ivAttachedPhoto.getDrawable().getBounds().top + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().bottom + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().left + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().right);
                            viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            Log.e("vh drawable info", viewHolder.ivAttachedPhoto.getDrawable().getBounds().width() + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().height());
                            /*if (viewHolder.ivAttachedPhoto.getHeight() > viewHolder.ivAttachedPhoto.getWidth() &&
                                    imageInfo.getWidth() > imageInfo.getHeight()) {
                                viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getHeight() / imageInfo.getWidth());
                            } else {
                                viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            }*/
                        }

                        @Override
                        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                            super.onIntermediateImageSet(id, imageInfo);
//                            Log.e("imageInfo", imageInfo.getWidth() + "x" + imageInfo.getHeight());
//                            Log.e("vh info", viewHolder.ivAttachedPhoto.getWidth() + "x" + viewHolder.ivAttachedPhoto.getHeight());
//                            Log.e("vh drawable info", viewHolder.ivAttachedPhoto.getDrawable().getBounds().width() + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().height());
//                            Log.e("vh drawable dim", viewHolder.ivAttachedPhoto.getDrawable().getBounds().top + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().bottom + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().left + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().right);
                            viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            Log.e("vh drawable info", viewHolder.ivAttachedPhoto.getDrawable().getBounds().width() + "x" + viewHolder.ivAttachedPhoto.getDrawable().getBounds().height());
                            /*if (viewHolder.ivAttachedPhoto.getHeight() > viewHolder.ivAttachedPhoto.getWidth() &&
                                    imageInfo.getWidth() > imageInfo.getHeight()) {
                                viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getHeight() / imageInfo.getWidth());
                            } else {
                                viewHolder.ivAttachedPhoto.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
                            }*/
                        }
                    })
                    .build();

            viewHolder.ivAttachedPhoto.setController(controller);

            params.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            viewHolder.ivAttachedPhoto.setVisibility(View.GONE);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a zzz M/d/y", Locale.getDefault());
        try {
            Date parseDate = sdf.parse(feed.getTime());
            application.mZomeUtils.returnApproxTime(parseDate, viewHolder.mTvTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        application.mZomeUtils.setTags(viewHolder.mTvContent, feed.getContent());
        viewHolder.mTvProfileName.setText(feed.getOwnerName());

        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(R.drawable.anonymous_large))
                .build();

        if (!feed.getOwnerImageUrl().equals("")) {
            uri = Uri.parse(feed.getOwnerImageUrl());
        }

        viewHolder.mIvProfileImage.setImageURI(uri);

        viewHolder.rlContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("feedId", feed.getPostId());

                Intent intent = new Intent(context.getApplicationContext(), FeedDetailActivity.class);
                intent.putExtra("feedId", feed.getPostId());
                intent.putExtra("time", feed.getTime());
                intent.putExtra("profileName", feed.getOwnerName());
                intent.putExtra("content", feed.getContent());
                intent.putExtra("extraImage", feed.getImageUrl());
                if (!feed.getImageUrl().equals("")) {
                    intent.putExtra("image", feed.getImageUrl());
                }
                if (!feed.getOwnerImageUrl().equals("")) {
                    intent.putExtra("ownerImage", feed.getOwnerImageUrl());
                }

                context.startActivity(intent);
            }
        });

        viewHolder.rlContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                snackbar = application.mZomeUtils.getSnackbar(
                        v,
                        "Report post '" + feed.getContent() + "'?",
                        "Click here to Report",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (application.mZomeUtils.isUserAnonymous) {
                                    application.mZomeUtils.showToastAnonymousUserMessage("make a report.");
                                } else {
                                    ReportDialog dialog = new ReportDialog(
                                            context,
                                            0,
                                            "POST",
                                            feed.getContent(),
                                            feed.getPostId()
                                    );
                                    dialog.show();
                                }
                            }
                        }
                );
                snackbar.show();
                return true;
            }
        });

        viewHolder.mTvHearts.setText(String.valueOf(feed.getHeartsCount()));
        viewHolder.mTvReplies.setText(String.valueOf(feed.getCommentsCount()));
    }

    @Override
    public int getItemCount() {
        return feedArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final SimpleDraweeView mIvProfileImage;
        private final TextView mTvProfileName, mTvContent, mTvTime, mTvHearts, mTvReplies;
        private final SimpleDraweeView ivAttachedPhoto;
        private final RelativeLayout rlContent;

        ViewHolder(View v) {
            super(v);
            rlContent = (RelativeLayout) v.findViewById(R.id.rlContent);
            mIvProfileImage = (SimpleDraweeView) v.findViewById(R.id.ivProfilePicture);
            mTvProfileName = (TextView) v.findViewById(R.id.tvProfileName);
            mTvContent = (TextView) v.findViewById(R.id.tvContent);
            mTvTime = (TextView) v.findViewById(R.id.tvTime);
            ivAttachedPhoto = (SimpleDraweeView) v.findViewById(R.id.ivAttachedPhoto);
            mTvHearts = (TextView) v.findViewById(R.id.tvHearts);
            mTvReplies = (TextView) v.findViewById(R.id.tvReplies);
        }
    }

    public void animateTo(List<Feed> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Feed> newFeeds) {
        for (int i = feedArrayList.size() - 1; i >= 0; i--) {
            final Feed feed = feedArrayList.get(i);
            if (!newFeeds.contains(feed)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Feed> newFeeds) {
        for (int i = 0, count = newFeeds.size(); i < count; i++) {
            final Feed feed = newFeeds.get(i);
            if (!feedArrayList.contains(feed)) {
                addItem(i, feed);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Feed> newFeeds) {
        for (int toPosition = newFeeds.size() - 1; toPosition >= 0; toPosition--) {
            final Feed feed = newFeeds.get(toPosition);
            final int fromPosition = feedArrayList.indexOf(feed);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Feed removeItem(int position) {
        final Feed model = feedArrayList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Feed feed) {
        feedArrayList.add(position, feed);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Feed feed = feedArrayList.remove(fromPosition);
        feedArrayList.add(toPosition, feed);
        notifyItemMoved(fromPosition, toPosition);
    }

}
