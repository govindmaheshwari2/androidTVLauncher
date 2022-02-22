package com.govind.androidtvlauncher;

import android.graphics.drawable.Drawable;

public class AppObject {
    private String name,packageName;
    private Drawable image;

    public AppObject(String packageName,String name,Drawable image){
        this.packageName = packageName;
        this.name = name;
        this.image = image;
    }

    public String getPackageName(){return packageName;}
    public String getname(){return name;}
    public Drawable getImage(){return image;}
}
