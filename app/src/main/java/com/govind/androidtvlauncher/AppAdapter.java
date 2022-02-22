package com.govind.androidtvlauncher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private List<AppObject> appList = new ArrayList<>();
    private Context context;
    private ArrayList<String> recentAppList = new ArrayList<>(5);

    public AppAdapter(Context context,List<AppObject> appList){
        this.appList = appList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_layout_listview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.appCardView.setCardBackgroundColor(getDominantColor(appList.get(position).getImage()));
        holder.appImage.setImageDrawable(appList.get(position).getImage());
        holder.appName.setText(appList.get(position).getname());
        holder.appLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyDB tinydb = new TinyDB(context);
                recentAppList=tinydb.getListString("recentApp");
                Intent launchAppIntent = context.getPackageManager().getLaunchIntentForPackage(appList.get(position).getPackageName());
                if(launchAppIntent!=null) {
                    if(!recentAppList.contains(appList.get(position).getPackageName()))
                        recentAppList.add(0,appList.get(position).getPackageName());
                    if(recentAppList.size()==6)
                        recentAppList.remove(5);

                    tinydb.putListString("recentApp", recentAppList);
                  //  scaleView(holder.appLayout);
                    context.startActivity(launchAppIntent);

                }
            }
        });
        holder.appLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    zoomIn(v);
                    if(position==0){
                        v.requestFocus();
                      //  v.getParent().clearChildFocus(v);
                    }else if(position==(appList.size()-1)){
                        v.requestFocus();
                    //    v.clearFocus();
                    }
                  //  Toast.makeText(context,appList.get(position).getPackageName(),Toast.LENGTH_LONG).show();
                }else{
                    zoomOut(v);
                }
            }
        });

        holder.appLayout.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_HOVER_ENTER){
                    v.requestFocus();
                    //  Toast.makeText(context,appList.get(position).getPackageName(),Toast.LENGTH_LONG).show();
                }else if(event.getAction() == MotionEvent.ACTION_HOVER_EXIT){
                    v.clearFocus();
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView appImage;
        TextView appName;
        CardView appCardView;
        FocusFixFrameLayout appLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            appImage = itemView.findViewById(R.id.app_image);
            appName = itemView.findViewById(R.id.app_name);
            appCardView = itemView.findViewById(R.id.appCardView);
            appLayout = itemView.findViewById(R.id.app_layout);
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

    public int getDominantColor(Drawable image) {
        Bitmap bitmap = null;

        if (image instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) image;
            if(bitmapDrawable.getBitmap() != null) {
                bitmap =  bitmapDrawable.getBitmap();
            }
        }else {

            if (image.getIntrinsicWidth() <= 0 || image.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            image.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            image.draw(canvas);
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }


}
