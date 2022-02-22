package com.govind.androidtvlauncher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder>{

    private List<VideoDetail> videoList = new ArrayList<>();
    private Context context;

    public VideoAdapter(Context context,List<VideoDetail> videoList){
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_layout_listview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if(position==2) {

            AdLoader.Builder builder = new AdLoader.Builder(
                    context, "ca-app-pub-8868204021755122/8335248805");

            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    holder.templateView.setNativeAd(unifiedNativeAd);
                }
            });

            final AdLoader adLoader = builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
            holder.adLinearLayout.setVisibility(View.VISIBLE);
        }else {
            int pos = position;
            if(position>2)
                pos = position-1;
            final String url = videoList.get(pos).getVideoUrl();
            holder.videoTitle.setText(videoList.get(pos).getTitle());
            Glide.with(context)
                    .asBitmap()
                    .load(videoList.get(pos).getImageUrl())
                    .into(holder.videoImage);
            holder.videoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //scaleView(holder.videoLayout);
                    watchYoutubeVideo(context, url);
                }
            });
            holder.videoLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        zoomIn(v);
                    } else {
                        zoomOut(v);
                    }
                }
            });
            holder.videoLayout.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                        v.requestFocus();
                        //  Toast.makeText(context,appList.get(position).getPackageName(),Toast.LENGTH_LONG).show();
                    } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                        v.clearFocus();
                    }
                    return false;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView videoImage;
        TextView videoTitle;
        FocusFixFrameLayout videoLayout;
        TemplateView templateView;
        LinearLayout adLinearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            videoImage = itemView.findViewById(R.id.VideoImage);
            videoTitle = itemView.findViewById(R.id.VideoTitle);
            videoLayout = itemView.findViewById(R.id.video_layout);
            templateView = itemView.findViewById(R.id.my_template);
            adLinearLayout = itemView.findViewById(R.id.adLinearLayout);
        }
    }

    private void zoomIn(View view){
        view.setElevation(10f);
        Animation zoom_in = new ScaleAnimation(
                1f, 1.2f,
                1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        zoom_in.setFillAfter(true);
        zoom_in.setDuration(50);
        view.startAnimation(zoom_in);
    }

    private void zoomOut(View view){
        view.setElevation(1f);
        Animation zoom_out = new ScaleAnimation(
                1.2f, 1f,
                1.2f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        zoom_out.setFillAfter(true);
        zoom_out.setDuration(1);
        view.startAnimation(zoom_out);
    }

    public static void watchYoutubeVideo(Context context, String id){
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + id));
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                context.startActivity(webIntent);
            }
        }catch (Exception e){
            Toast.makeText(context,"Proper Application not found!",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
