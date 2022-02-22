package com.govind.androidtvlauncher;

public class VideoDetail {
    private String Title,ImageUrl,VideoUrl;
    public VideoDetail(String Title,String ImageUrl,String VideoUrl){
        this.Title=Title;
        this.ImageUrl=ImageUrl;
        this.VideoUrl=VideoUrl;
    }

    public String getTitle(){return Title;}
    public String getImageUrl(){return ImageUrl;}
    public String getVideoUrl(){return VideoUrl;}

}
