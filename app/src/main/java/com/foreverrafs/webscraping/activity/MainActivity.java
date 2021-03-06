package com.foreverrafs.webscraping.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.foreverrafs.webscraping.R;
import com.foreverrafs.webscraping.adapter.MusicAdapter;
import com.foreverrafs.webscraping.core.MusicPlayer;
import com.foreverrafs.webscraping.core.MusicScraper;
import com.foreverrafs.webscraping.core.ScrappingEventsListener;
import com.foreverrafs.webscraping.model.Music;
import com.foreverrafs.webscraping.utils.AppStatus;
import com.foreverrafs.webscraping.utils.DownloadDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicAdapter.ClickListener, ScrappingEventsListener, MusicPlayer.PlayerStatesListener, AdapterView.OnItemSelectedListener {
    private final String TAG = "musicscrapper";
    private final int writePermission = 1000;
    private MusicAdapter musicAdapter;
    private MusicPlayer musicPlayer;
    private RecyclerView recyclerView;
    private MusicAdapter.MusicViewHolder holder;
    private MusicAdapter.MusicViewHolder prevHolder;
    private boolean intendedTouch = false;

    private boolean initialFetchCompleted = false;
    private Music musicToDownload;
    private View fetchedSongsView;
    private View progressBarLoadingOverlay;

    private SearchView searchView;
    private ImageButton playBtnLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!AppStatus.getInstance(this).isOnline()) {
            Log.i(TAG, "No Network Interface found. Assuming there is no internet. Exiting...");
            startActivity(new Intent(this, activity_no_network.class));
            finish();
        }

        fetchedSongsView = getLayoutInflater().inflate(R.layout.fragment_fragment_home, null);
    }

    private void init() {
        progressBarLoadingOverlay = findViewById(R.id.loading);
        recyclerView = findViewById(R.id.musicList);
        //searchView = findViewById(R.id.search_view);
        final LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                for (int a = llm.findFirstVisibleItemPosition(); a < llm.findLastVisibleItemPosition(); a++) {
                    prevHolder = (MusicAdapter.MusicViewHolder) recyclerView.findViewHolderForAdapterPosition(a);
                    if (prevHolder != null && prevHolder != holder) {
                        prevHolder.setPlayButtonImage(getResources().getDrawable(R.drawable.play_small));
                        prevHolder.setBackground(getResources().getColor(R.color.cardview_light_background));
                    }
                }
            }
        });
        recyclerView.setLayoutManager(llm);
        recyclerView.setClickable(true);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0x01, 0);
        // playButton = findViewById(R.id.playBtnMain);
        musicPlayer = MusicPlayer.getInstance();
        musicPlayer.init();

        musicPlayer.setPlayerStatesListener(this);
        playBtnLarge = findViewById(R.id.mediaPlayBtn);

        List<String> pageList = new ArrayList<>();
        for (int a = 1; a <= 10; a++) {
            pageList.add("Page " + a);
        }


        ArrayAdapter pageListAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, pageList);
        Spinner spinner = findViewById(R.id.pageNum);
        spinner.setAdapter(pageListAdapter);

        spinner.setOnItemSelectedListener(this);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                intendedTouch = true;
                return false;
            }
        });

        final EditText txtSearch = findViewById(R.id.searchSong);
        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        String query = txtSearch.getText().toString();

                        Log.i(TAG, "Searching for " + query);
                        fetchedSongsView.findViewById(R.id.loading).setVisibility(View.VISIBLE);

                        scrapWebsite(String.format("http://www.ghanamotion.com/?s=%s", query));

                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(getLayoutInflater().inflate(R.layout.progress_loading_overlay,null   ))
                                .create();

                        builder.show();
                        */

                        break;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (initialFetchCompleted) {
            Log.i(TAG, "Song list has already been fetched::::Suspending");
            return;
        }
        scrapWebsite("http://www.ghanamotion.com/music/page/1");
    }

    private void scrapWebsite(final String url) {
        final List<String> urlList = new ArrayList<>();
        //get the music lists in this thread
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Document mainPage = Jsoup.connect(url).get();
                    Elements elements = mainPage.select(".post-box-title");
                    for (Element element : elements) {
                        urlList.add(element.selectFirst("a[href]").attr("href"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        try {
            Thread thread = new Thread(runnable);
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("TAG", "Number of Urls to pass to Scrapper:  " + urlList.size());


        String[] urls = urlList.toArray(new String[urlList.size()]);

        MusicScraper musicScraper = new MusicScraper();
        musicScraper.setOnCompleteListener(this);

        Log.i(TAG, musicScraper.getState().toString());
        try {
            if (!initialFetchCompleted) setContentView(R.layout.fragment_fragment_loading);
            musicScraper.execute(urls);
        } catch (Exception ex) {
            Log.i(TAG, ex.getMessage());
        }
    }

    @Override
    public void itemClicked(View view, int position, int prevPosition) {
        holder = (MusicAdapter.MusicViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        prevHolder = (MusicAdapter.MusicViewHolder) recyclerView.findViewHolderForAdapterPosition(prevPosition);

        final Music music = musicAdapter.getSongAt(position);

        switch (view.getId()) {
            case R.id.playBtnMain:
                Log.i(TAG, "Play/Pause Button Invoked:::::position = " + position + ":::Previous Position = " + prevPosition);
                try {
                    // if (musicPlayer.getPlayerState() == MusicPlayer.PlayerState.stopped ||
                    //        musicPlayer.getPlayerState() == MusicPlayer.PlayerState.playing)
                    holder.setLoadingProgressVisibility(true);
                    musicPlayer.play(music);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.downloadBtnMain:
                DownloadDialog downloadDialog = new DownloadDialog();
                downloadDialog.setSongTitle(music.getTitle());
                downloadDialog.setDownloadDialogListener(new DownloadDialog.DownloadDialogListener() {
                    @Override
                    public void onDialogPositiveClick(DialogFragment dialog) {
                        Log.i(TAG, "Download cancelled");
                        String[] permissions = new String[1];
                        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;

                        musicToDownload = music;

                        int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        //request runtime permission on android 6+
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(permissions, writePermission);
                            }
                        } else {
                            downloadMusic(musicToDownload);
                        }
                    }

                    @Override
                    public void onDialogNegativeClick(DialogFragment dialog) {

                    }
                });

                downloadDialog.show(getFragmentManager(), "Download Dialog");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case writePermission:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Write Permission Granted::::Download can proceed");
                    downloadMusic(musicToDownload);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission Denied")
                            .setMessage("Write Permission is required to download music")
                            .create();
                    builder.show();
                }
                break;
        }

    }

    private void downloadMusic(Music music) {
        Toast.makeText(this, "Music is being downloaded...", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Downloading " + music.getTitle());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(music.getSongUrl()));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, music.getTitle() + ".mp3");


        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

    //TODO: Cache the search results for faster future searches and delete caches older than 5 hours or make it adjustable by user
    @Override
    public void onScrappingCompleted(List<Music> musicList) {
        if (!initialFetchCompleted) {
            setContentView(fetchedSongsView);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            init();
            initialFetchCompleted = true;
        }
        //hide the loading overlay
        fetchedSongsView.findViewById(R.id.loading).setVisibility(View.GONE);

        if (musicList.size() == 0) {
            Log.i(TAG, "No Music Present::::suspending");
            //fetchedSongsView.findViewById(R.id.search_empty_layout).setVisibility(View.VISIBLE);
            return;
        }


        Log.i(TAG, "scrapping completed with " + musicList.size() + " items returned");

        //uncomment these lines if you want to persist the existing adapter and add the fetched songs to it
        //if (musicAdapter != null) {
        //    musicAdapter.getAllSongs().addAll(musicList);
        //    musicAdapter.notifyDataSetChanged();
        //} else {
        musicAdapter = new MusicAdapter(getApplicationContext(), musicList);
        //}

        musicAdapter.setClickListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(musicAdapter);
    }

    @Override
    public void onScrappingStarted() {
        Log.i(TAG, "Begin Fetching songs....");
    }

    @Override
    public void onMusicSizesFetchCompleted(List<String> musicSizes) {

        Log.i(TAG, "music sizes has been fetched successfully. Returned " + musicSizes.size() + " items");
        for (int a = 0; a < musicSizes.size(); a++) {
            // viewHolder = (MusicAdapter.MusicViewHolder) recyclerView.findViewHolderForAdapterPosition(a);
            musicAdapter.getSongAt(a).setFileSize(Long.valueOf(musicSizes.get(a)));
            musicAdapter.notifyItemChanged(a);
        }
    }

    @Override
    public void onPlay(Music music) {
        setStateChanges(musicPlayer.getPlayerState(), prevHolder, holder);
    }

    //set appropriate images and colors based on player state
    private void setStateChanges(MusicPlayer.PlayerState playerState, MusicAdapter.MusicViewHolder previous, MusicAdapter.MusicViewHolder current) {
        if (previous != null) {
            previous.setPlayButtonImage(getResources().getDrawable(R.drawable.play_small));
            previous.setBackground(getResources().getColor(R.color.cardview_light_background));
        }

        switch (playerState) {
            case playing:
                Log.i(TAG, "Changing player UI  to reflect state of Playing");
                if (current != null) {
                    current.setPlayButtonImage(getResources().getDrawable(R.drawable.pause_small));
                    current.setBackground(getResources().getColor(R.color.item_playing));
                }
                break;

            case paused:
                Log.i(TAG, "Changing player UI to reflect state of Paused");
                if (holder != null) {
                    holder.setPlayButtonImage(getResources().getDrawable(R.drawable.play_small));
                    holder.setLoadingProgressVisibility(false);
                }
                break;
            case stopped:
                Log.i(TAG, "Changing player UI state to reflect state of Stopped");
                if (holder != null) {
                    holder.setPlayButtonImage(getResources().getDrawable(R.drawable.play_small));
                    holder.setBackground(getResources().getColor(R.color.cardview_light_background));
                    holder.setLoadingProgressVisibility(false);
                }
                break;

            default:
                Log.i(TAG, "Indeterminate state");
                break;

        }
    }

    @Override
    public void onStop() {
        Log.i(TAG, "Player stopped:::" + musicPlayer.getPlayerState());
        setStateChanges(musicPlayer.getPlayerState(), prevHolder, holder);
    }

    @Override
    public void onPrepared() {
        if (holder != null) {
            holder.setLoadingProgressVisibility(false);
        }
    }


    @Override
    public void onError(MediaPlayer mp) {
        if (holder != null) {
            holder.setPlayButtonImage(getResources().getDrawable(R.drawable.play_small));
            holder.setBackground(getResources().getColor(R.color.cardview_light_background));
            holder.setLoadingProgressVisibility(false);
        }
        Log.e(TAG, "Error playing media");
        Toast.makeText(MainActivity.this, "Error during playback!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) musicPlayer.release();
    }


    @Override
    public void onPause(Music music) {
        setStateChanges(musicPlayer.getPlayerState(), prevHolder, holder);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!intendedTouch) {
            Log.i(TAG, "Event fired unnecessarily::::Suspending");
            return;
        }

        Log.i(TAG, "Fetching for page: " + (position + 1));
        if (!initialFetchCompleted) {
            Log.i(TAG, "Initial fetch isn't completed yet::::Suspending");
            return;
        }
        View loadingScreen = fetchedSongsView.findViewById(R.id.loading);
        loadingScreen.setVisibility(View.VISIBLE);

        if (loadingScreen.getVisibility() == View.VISIBLE) {
            scrapWebsite(String.format("http://www.ghanamotion.com/music/page%s", (position + 1)));
        }
        intendedTouch = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
