package com.govind.androidtvlauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accessibilityservice.AccessibilityService;
import android.app.Instrumentation;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.inputmethodservice.Keyboard;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<AppObject> installedAppList = new ArrayList<>();
    List<AppObject> recentAppList = new ArrayList<>();
    List<VideoDetail> videoList = new ArrayList<>();
    ArrayList<String> recentAppPackage = new ArrayList<>();
    RecentAppAdapter recentAppAdapter;
    EditText searchView;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    AppAdapter adapter;
 //   ImageView MouseInfo;
    String AppRemove[] = {"com.govind.androidtvlauncher","com.android.settings"};
    Instrumentation inst = new Instrumentation();
    private static MainActivity mainActivityRunningInstance;

    @Override
    protected void onPause() {
        super.onPause();
        unregisterInstallStateUpdListener();
        unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PackageManager pm = getApplicationContext().getPackageManager();
            final String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                try {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        if (MainActivity.getInstace() != null)
                            MainActivity.getInstace().updateUI(true);
                    } else {
                        if (MainActivity.getInstace() != null)
                            MainActivity.getInstace().updateUI(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
                try {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Drawable icon = null;
                    icon = getApplicationContext().getPackageManager().getApplicationIcon(packageName);
                    String name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
                    AppObject app = new AppObject(packageName, name, icon);
                    installedAppList.add(app);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
                try {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Drawable icon = null;
                    icon = getApplicationContext().getPackageManager().getApplicationIcon(packageName);
                    String name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
                    AppObject app = new AppObject(packageName, name, icon);
                    installedAppList.remove(app);
                    adapter.notifyDataSetChanged();
                    TinyDB tinyDB = new TinyDB(getApplicationContext());
                    recentAppPackage=tinyDB.getListString("recentApp");
                    if(recentAppPackage.contains(packageName)){
                        recentAppPackage.remove(packageName);
                        tinyDB.putListString("recentApp",recentAppPackage);
                        recentAppList.remove(app);
                        recentAppAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };
    public static MainActivity  getInstace(){
        return mainActivityRunningInstance;
    }
    //AIzaSyAYia28kkAyP46epPRPRhqKCixvCflefkg
    //https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&chart=mostPopular&maxResults=5&regionCode=IN
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TinyDB tinyDB = new TinyDB(this);
        Boolean first = tinyDB.getBoolean("FirstOpen");
        if(!first){
            setWallpaper();
            tinyDB.putBoolean("FirstOpen",true);
        }
        checkForAppUpdate();
        searchView = (EditText)findViewById(R.id.search_bar);
        final LinearLayout searchTile = (LinearLayout)findViewById(R.id.searchTile);
        searchView.setFocusable(true);
        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    searchView.setFocusable(false);
                    searchView.setFocusableInTouchMode(true);
                }
                return false;
            }
        });
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().matches("")) {
                    recentAppAdapter.filterList(recentAppList);
                    searchTile.setVisibility(View.GONE);
                }else{
                    filter(s.toString());
                    searchTile.setVisibility(View.VISIBLE);
                }
            }
        });

        Button googleSearch = (Button)findViewById(R.id.googleSearch);
        googleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/#q=" + searchView.getText())));
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Proper Application not found!",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        googleSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    zoomIn(v);
                }else{
                    zoomOut(v);
                }
            }
        });

        Button youtubeSearch = (Button)findViewById(R.id.youtubeSearch);
        youtubeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + searchView.getText()));
                    webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(webIntent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Proper Application not found!",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                }
        });

        youtubeSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    zoomIn(v);
                }else{
                    zoomOut(v);
                }
            }
        });

        final ImageView setting = (ImageView)findViewById(R.id.btnSetting);
        setting.setFocusable(true);
        setting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    zoomIn(v);
                }else{
                    zoomOut(v);
                }
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        ImageView wifi = (ImageView)findViewById(R.id.btnWifi);
        wifi.setFocusable(true);
        wifi.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    zoomIn(v);
                }else{
                    zoomOut(v);
                }
            }
        });
        if(checkWifi()){
           wifi.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.wifi));
        }else{
            wifi.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.wifi_off));
        }
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        mainActivityRunningInstance =this;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(broadcastReceiver, intentFilter);
        initialAppDrawer();
        initialRecentAppDrawer();
        if(!tinyDB.getString("countryCode").equals("")){
            fetchVideoData(tinyDB.getString("countryCode"));
        }else{
            getCountryCode();
        }
     //   MouseInfo = (ImageView)findViewById(R.id.mouse);
        SendIPAddress();
        Thread thread = new Thread(new Server());
        thread.start();

    }

    private void filter(String query) {
        List<AppObject> filterList = new ArrayList<>();
        for(AppObject item:installedAppList){
            if(item.getname().toLowerCase().contains(query.toLowerCase())){
                filterList.add(item);
            }
        }
        recentAppAdapter.filterList(filterList);
    }

    private void initialRecentAppDrawer() {
        TinyDB tinydb = new TinyDB(this);
        RecyclerView recyclerView = findViewById(R.id.RecentAppListView);
        recyclerView.requestFocus();
        recentAppPackage=tinydb.getListString("recentApp");
        for(int i=recentAppPackage.size();i<7;i++){
            if(installedAppList.size()>=(i+1) && !recentAppPackage.contains(installedAppList.get(i).getPackageName()))
                recentAppPackage.add(installedAppList.get(i).getPackageName());
        }
        PackageManager pm = getApplicationContext().getPackageManager();
        for(int i=0;i<recentAppPackage.size();i++){
            try {
                    Drawable icon = getApplicationContext().getPackageManager().getApplicationIcon(recentAppPackage.get(i));
                    String name = pm.getApplicationLabel(pm.getApplicationInfo(recentAppPackage.get(i), 0)).toString();
                    AppObject app = new AppObject(recentAppPackage.get(i), name, icon);
                    recentAppList.add(app);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recentAppAdapter = new RecentAppAdapter(this,recentAppList);
        recyclerView.setAdapter(recentAppAdapter);
        recyclerView.requestChildFocus(recyclerView.getChildAt(0),recyclerView.getChildAt(0));
    }


    private void initialVideoDrawer() {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
                    RecyclerView recyclerView = findViewById(R.id.VideoListView);
                    recyclerView.setLayoutManager(layoutManager);
                    if(videoList.size()==0){
                        recyclerView.setVisibility(View.GONE);
                    }
                    VideoAdapter adapter = new VideoAdapter(getApplicationContext(),videoList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
            }
    private void fetchVideoData(String countryCode){
        final TinyDB tinyDB = new TinyDB(this);
        String today = tinyDB.getString("Date");
        Date d = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String date = df.format(d);
        final String[] VideoJSON = {""};
        if(!date.equals(today)) {
            String url="https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&chart=mostPopular&maxResults=6&regionCode="+countryCode+"&key=AIzaSyAYia28kkAyP46epPRPRhqKCixvCflefkg";
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getApplicationContext(),"Videos Updated",Toast.LENGTH_SHORT).show();
                    tinyDB.putString("VideoDetail",response);
                    Date d = new Date();
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    String date = df.format(d);
                    tinyDB.putString("Date", date);
                    FetchJSON(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Youtube Video Error",error.getMessage());
                }
            });
            requestQueue.add(stringRequest);
        }else{
            VideoJSON[0] = tinyDB.getString("VideoDetail");
            FetchJSON(VideoJSON[0]);
        //    Toast.makeText(getApplicationContext(),"Saved Video",Toast.LENGTH_SHORT).show();
        }

    }
    private void FetchJSON(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObjectItem = jsonArray.getJSONObject(i);
                JSONObject jsonSnippet = jsonObjectItem.getJSONObject("snippet");
                JSONObject jsonThumbnail = jsonSnippet.getJSONObject("thumbnails").getJSONObject("high");
                String VideoID = jsonObjectItem.getString("id");
                String VideoTitle = jsonSnippet.getString("title");
                String VideoImage = jsonThumbnail.getString("url");
                VideoDetail video = new VideoDetail(VideoTitle,VideoImage,VideoID);
                if(!videoList.contains(video))
                    videoList.add(video);
            }
            initialVideoDrawer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initialAppDrawer() {
        installedAppList = getInstallAppList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView recyclerView = findViewById(R.id.AppListView);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AppAdapter(this,installedAppList);
        recyclerView.setAdapter(adapter);
        recyclerView.requestFocus();
    }

    private List<AppObject> getInstallAppList() {
        List<AppObject> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> untreatedAppList = getApplicationContext().getPackageManager().queryIntentActivities(intent,0);
        List AppRemoveList = Arrays.asList(AppRemove);
        for(ResolveInfo untreatedapp : untreatedAppList){
            String appName = untreatedapp.activityInfo.loadLabel(getPackageManager()).toString();
            String appPackageName = untreatedapp.activityInfo.packageName;
            Drawable appImage = untreatedapp.activityInfo.loadIcon(getPackageManager());
            AppObject app = new AppObject(appPackageName,appName,appImage);
            if(AppRemoveList.contains(appPackageName))
                continue;
            if(!list.contains(app))
                list.add(app);
        }
        return list;
    }

    private boolean checkWifi() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Network Issue",Toast.LENGTH_SHORT).show();
            return false;
        }
//        WifiManager wifiMgr = (WifiManager) getSystemService(this.WIFI_SERVICE);
//
//        if (wifiMgr.isWifiEnabled()) {
////            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
////
////            if( wifiInfo.getNetworkId() == -1 ){
////                return false;
////            }
//            return true;
//        }
//        else {
//            return false;
//        }
    }

    public void updateUI(boolean wifistatus) {
        ImageView wifi = (ImageView)findViewById(R.id.btnWifi);
        if(wifistatus){
            wifi.setBackgroundResource(R.drawable.wifi);
        }else{
            wifi.setBackgroundResource(R.drawable.wifi_off);
        }
    }

    private void setWallpaper() {
        try{
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
            WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
            try{
                manager.setBitmap(bitmap);
            } catch (IOException e) {

            }
        }catch (Exception e){

        }
    }

    private void getCountryCode() {
        String url="https://ipapi.co/json/?key=UzGjZLqIKvR2r3UChbNwOAlqLf0eDCj6IcKYm6kQ1sKuHc1Pjj";
        final String[] country = {"US"};
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    country[0] = jsonObject.getString("country_code");
                    if(!country[0].equals("")){
                        TinyDB tinyDB =new TinyDB(getApplicationContext());
                        tinyDB.putString("countryCode",country[0]);
                        fetchVideoData(country[0]);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
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

    private void keyPress(final int key){
        new Thread(new Runnable() {
            @Override
            public void run() {
                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, key));
            }
        }).start();
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       // Toast.makeText(getApplicationContext(),keyCode+"",Toast.LENGTH_LONG).show();
        return super.onKeyDown(keyCode, event);
    }*/

   // Remote Control
   private String SERVICE_NAME = "AndroidTV";
    private String SERVICE_TYPE = "_androidtv._tcp.";
    private NsdManager mNsdManager;
    private int hostPort=8989;
    class Server implements Runnable{

        ServerSocket ss;
        Socket socket;
        DataInputStream dis;
        String message;
        Handler handler = new Handler();
        @Override
        public void run() {
            try {

                ss=new ServerSocket(hostPort);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                //        Toast.makeText(getApplicationContext(),"Waiting for client",Toast.LENGTH_LONG).show();
                    }
                });
                while (true){
                    socket = ss.accept();
                    dis = new DataInputStream(socket.getInputStream());
                    message = dis.readUTF();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        //    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                            if(message.equals("up")){
                                keyPress(KeyEvent.KEYCODE_DPAD_UP);
                            }else if(message.equals("down")){
                                keyPress(KeyEvent.KEYCODE_DPAD_DOWN);
                            }else if(message.equals("right")){
                                keyPress(KeyEvent.KEYCODE_DPAD_RIGHT);
                            }else if(message.equals("left")){
                                keyPress(KeyEvent.KEYCODE_DPAD_LEFT);
                            }else if(message.equals("select")){
                                getCurrentFocus().callOnClick();
                            }else if(message.equals("poweroff")){
                                try {
                                    Process proc = Runtime.getRuntime()
                                            .exec(new String[]{ "su", "-c", "reboot -p" });
                                    proc.waitFor();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }else if(message.equals("volumeup")){
                                keyPress(KeyEvent.KEYCODE_VOLUME_UP);
                            }else if(message.equals("volumedown")){
                                keyPress(KeyEvent.KEYCODE_VOLUME_DOWN);
                            }else if(message.equals("mute")){
                                keyPress(KeyEvent.KEYCODE_VOLUME_MUTE);
                            }else if(searchView.isFocusable()){
                                searchView.setText(message);
                                Button googleSearch = (Button)findViewById(R.id.googleSearch);
                                googleSearch.requestFocus();
                            }
                           /* else if(message.contains(",")){
                                MouseInfo.setVisibility(View.VISIBLE);
                                float movex=Float.parseFloat(message.split(",")[0]);
                                float movey=Float.parseFloat(message.split(",")[1]);
                                int array[] = new int[2];
                                MouseInfo.getLocationOnScreen(array);
                                Point point=new Point();
                                point.y = array[1];
                                point.x = array[0];
                                float nowx=point.x;
                                float nowy=point.y;
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) MouseInfo.getLayoutParams();
                                MouseInfo.setX(nowx+movex);
                                MouseInfo.setY(nowy+movey);
                            }
                            else if(message.contains("leftClick")){
                                final int array[] = new int[2];
                                MouseInfo.getLocationOnScreen(array);
                                View rootView = getWindow().getDecorView().getRootView();
                                long initTime = android.os.SystemClock.uptimeMillis();
                                MotionEvent event = MotionEvent.obtain(initTime, initTime+100, MotionEvent.ACTION_UP, array[0], array[1], 0);
                                event.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
                                rootView.dispatchTouchEvent(event);
                            }*/
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


   private void SendIPAddress(){
        try {
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            registerService(hostPort);
        }catch (Exception e){
        }
   }


    public void registerService(int port) {
        try {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setServiceName(SERVICE_NAME);
            serviceInfo.setServiceType(SERVICE_TYPE);
            serviceInfo.setPort(port);

            mNsdManager.registerService(serviceInfo,
                    NsdManager.PROTOCOL_DNS_SD,
                    mRegistrationListener);
        }catch (Exception e){

        }
    }

    NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            String mServiceName = NsdServiceInfo.getServiceName();
            SERVICE_NAME = mServiceName;
            Log.d("IP Address", "Registered name : " + mServiceName);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {
            // Registration failed! Put debugging code here to determine
            // why.
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            // Service has been unregistered. This only happens when you
            // call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d("IP Address",
                    "Service Unregistered : " + serviceInfo.getServiceName());
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                           int errorCode) {
            // Unregistration failed. Put debugging code here to determine
            // why.
        }
    };


    //check for update

    @Override
    protected void onResume() {
        super.onResume();
        checkNewAppVersionState();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            case REQ_CODE_VERSION_UPDATE:
                if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
                    Log.d("Error","Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                    unregisterInstallStateUpdListener();
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        //Ip Address
        if (mNsdManager != null) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
        unregisterInstallStateUpdListener();
        try {
            unregisterReceiver(broadcastReceiver);
        }catch (Exception e){

        }
        super.onDestroy();
    }


    private void checkForAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState installState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister();
            }
        };

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Before starting an update, register a listener for updates.
                        appUpdateManager.registerListener(installStateUpdatedListener);
                        // Start an update.
                        MainActivity.this.startAppUpdateFlexible(appUpdateInfo);
                    }
                }
            }
        });
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MainActivity.REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {
        Snackbar snackbar =
                Snackbar.make(getCurrentFocus(), "Loading", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Restart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        snackbar.show();

        unregisterInstallStateUpdListener();
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        new OnSuccessListener<AppUpdateInfo>() {
                            @Override
                            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                                //FLEXIBLE:
                                // If the update is downloaded but not installed,
                                // notify the user to complete the update.
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    MainActivity.this.popupSnackbarForCompleteUpdateAndUnregister();
                                }

                            }
                        });

    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

}
