package com.foreverrafs.webscraping.model;


/**
 * Created by forev on 3/8/2018.
 */

public class Music {
    String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String title;
    private String image;

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public double getFileSize() {
        return fileSize;
    }

    private double fileSize;

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    private String hash;
    private boolean incompleteSong;


    public boolean isIncompleteSong(){
        return incompleteSong;
    }

    public void setIncompleteSong(boolean incompleteSong){
         this.incompleteSong = incompleteSong;
    }
    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public Music(String url, String title, String image, String hash) {
        this.url = url;
        this.title = title;
        this.image = image;
        this.hash = hash;
    }

    public Music() {
    }
}