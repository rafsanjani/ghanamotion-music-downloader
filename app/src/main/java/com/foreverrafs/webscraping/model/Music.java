package com.foreverrafs.webscraping.model;


/**
 * Created by forev on 3/8/2018.
 */

public class Music {
    private String title, imageUrl, songUrl;
    private double fileSize;

    public Music(String songUrl, String title, String image, String hash) {
        this.songUrl = songUrl;
        this.title = title;
        this.imageUrl = image;
        this.hash = hash;
    }

    public Music() {
    }


    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    private String hash;
    private boolean incompleteSong;


    public boolean isIncompleteSong() {
        return incompleteSong;
    }

    public void setIncompleteSong(boolean incompleteSong) {
        this.incompleteSong = incompleteSong;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}