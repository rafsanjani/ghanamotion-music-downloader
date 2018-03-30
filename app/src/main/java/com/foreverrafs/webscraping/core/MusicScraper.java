package com.foreverrafs.webscraping.core;

import android.os.AsyncTask;
import android.util.Log;

import com.foreverrafs.webscraping.model.Music;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by forev on 3/8/2018.
 */

public class MusicScraper extends AsyncTask<String, Long, List<Music>> {
    public final String TAG = "musicscrapper";
    private ScrappingEventsListener scrappingEventsListener = null;


    private final Lock lock = new ReentrantLock();

    private static ScrapperState state;

    public MusicScraper() {

    }

    public void setOnCompleteListener(ScrappingEventsListener scrappingEventsListener) {
        state = ScrapperState.Idle;
        this.scrappingEventsListener = scrappingEventsListener;
    }


    public ScrapperState getState() {
        return state;
    }


    @Override
    protected void onPostExecute(List<Music> musicList) {
        //filter out bad items
        Iterator<Music> iterator = musicList.iterator();

        while (iterator.hasNext()) {
            Music music = iterator.next();
            if (music.getBadSong()) {
                iterator.remove();
            }
        }

        Log.i(TAG, "Items Added: " + musicList.size());
        scrappingEventsListener.onScrappingCompleted(musicList);
    }

    @Override
    protected List<Music> doInBackground(String... urls) {
        state = ScrapperState.Busy;
        scrappingEventsListener.onScrappingStarted();

        List<Music> musicList = new ArrayList<>();

        if (lock.tryLock()) {
            try {
                Log.i(TAG, "starting background job");
                int urlsCount = urls.length;
                Music music = null;
                for (int i = 0; i < urlsCount; i++) {
                    music = new Music();
                    Log.i(TAG, "Begin scraping: " + urls[i]);
                    Connection con = Jsoup.connect(urls[i]);

                    try {
                        org.jsoup.nodes.Document dom = con.get();
                        Element mp3Element = dom.selectFirst(".zbPlayer").selectFirst(".zbPlayerNativeMobile");
                        Element imageElement = dom.select("p img").first();

                        Element titleElement = dom.selectFirst(".post-inner").select("span").first();

                        String mp3Url = mp3Element.attr("src");
                        String imgUrl = imageElement.attr("src");

                        music.setTitle(titleElement.text());
                        music.setUrl(mp3Url);
                        music.setImage(imgUrl);


                        String hash = new String(Hex.encodeHex(DigestUtils.md5(music.getTitle() + music.getUrl())));
                        music.setHash(hash);

                        URLConnection urlConnection = new URL(music.getUrl()).openConnection();
                        urlConnection.connect();
                        music.setFileSize(urlConnection.getContentLength());

                        Log.i(TAG, "Title: " + music.getTitle());
                        Log.i(TAG, "url: " + music.getUrl());
                        Log.i(TAG, "Image Url: " + music.getImage() + "\\n");
                        Log.i(TAG, "Hash : " + music.getHash());
                        Log.i(TAG, "Size : " + music.getFileSize());


                    } catch (Exception e) {
                        Log.i(TAG, "Scrapper Error" + e.getMessage());
                        music.setBadSong(true);
                    }
                    musicList.add(music);
                }

            } finally {
                lock.unlock();
            }
        } else {
            Log.i(TAG, "error acquiring lock on thread");
        }
        return musicList;
    }

    public enum ScrapperState {
        Busy,
        Idle,
        Indeterminate;
    }
}



