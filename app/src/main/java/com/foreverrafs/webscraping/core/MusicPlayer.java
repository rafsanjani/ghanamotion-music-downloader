package com.foreverrafs.webscraping.core;

import android.media.MediaPlayer;
import android.util.Log;

import com.foreverrafs.webscraping.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rafsanjani Abdul Aziz on 3/23/2018.
 */

public class MusicPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private static MusicPlayer musicPlayer;
    private static MediaPlayer player;

    private static PlayerState playerState;
    private PlayerStatesListener playerStatesListener;
    private final String TAG = "musicscrapper";


    //keep a reference to the list of played music
    private List<Music> playedList;

    //initialize all variables
    public void init() {
        playerState = PlayerState.stopped;
        playerStatesListener = null;
        playedList = new ArrayList<>();
        player = new MediaPlayer();
        player.setOnCompletionListener(this);

        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public void setCurrentMusic(Music currentMusic) {
        this.currentMusic = currentMusic;
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    private Music currentMusic;


    //singleton instance
    public static MusicPlayer getInstance() {
        if (musicPlayer == null) {
            musicPlayer = new MusicPlayer();
        }
        return musicPlayer;
    }

    public List<Music> getPlayedList() {
        Log.i(TAG, "Total Played: " + playedList);
        return playedList;
    }

    private MusicPlayer() {
    }

    public void setPlayerStatesListener(PlayerStatesListener playerStatesListener) {
        this.playerStatesListener = playerStatesListener;
    }

    private boolean isPreparing = false;

    //TODO: Refactor up this portion of code for readability by performing appropriate method calls
    public void play(Music selectedMusic) throws IOException {
        if (isPreparing)
            return;

        //get a handle to the currently loaded music in memory
        Music currentMusic = getCurrentMusic();

        switch (getPlayerState()) {
            case playing:
                Log.i(TAG, "Currently playing song :::" + getCurrentMusic().getTitle());

                //if it's the same song, just pause, else stop the current song and play the new song
                if (currentMusic.getHash() == selectedMusic.getHash()) {
                    Log.i(TAG, "Attempting to Pause");
                    player.pause();
                    playerStatesListener.onPaused(selectedMusic);
                    setPlayerState(PlayerState.paused);
                } else {
                    Log.i(TAG, "New file detected::::attempting to play::" + selectedMusic.getTitle());
                    player.reset();

                    setCurrentMusic(selectedMusic);
                    player.setDataSource(selectedMusic.getSongUrl());
                    //if (!isPreparing) {
                    player.prepareAsync();
                    isPreparing = true;
                    //   isPreparing = true;
                    // }

                }
                break;
            case paused:
                //if it's the same song, just resume it, else stop it altogether and play the new song
                if (currentMusic.getHash() == selectedMusic.getHash()) {
                    Log.i(TAG, "Music is Paused::Attempting to resume");
                    Log.i(TAG, "Successfully Resumed::::Playing " + selectedMusic.getTitle());
                    player.start();
                    setPlayerState(PlayerState.playing);
                    playerStatesListener.onPlaying(selectedMusic);
                } else {
                    Log.i(TAG, "New file detected::::attempting to play::" + selectedMusic.getTitle());
                    player.reset();
                    player.setDataSource(selectedMusic.getSongUrl());
                    setCurrentMusic(selectedMusic);
                    // if (!isPreparing) {
                    player.prepareAsync();
                    isPreparing = true;
                    //    isPreparing = true;
                    //}
                }

                break;

            case stopped:
                Log.i(TAG, "Music is stopped :: Attempting to start from beginning");
                player.setDataSource(selectedMusic.getSongUrl());
                setCurrentMusic(selectedMusic);
                // if (!isPreparing) {
                player.prepareAsync();
                isPreparing = true;
                //    isPreparing = true;
                //}
                break;
            default:
                Log.i(TAG, "NOTHING TO DO");
                break;
        }

    }


    public void stop() {
        if (getPlayerState() != PlayerState.playing) {
            Log.i(TAG, "No Song Playing:::Unable to Stop");
            return;
        }
        player.reset();

        setPlayerState(PlayerState.stopped);
        playerStatesListener.onStopped();
        Log.i(TAG, "Music is stopped:::" + getPlayerState());
    }

    public void release() {
        player.release();
    }

    public void pause() {
        if (getPlayerState() != PlayerState.playing) {
            Log.i(TAG, "No Song Playing ::: Unable to Pause");
        }
        player.stop();
        setPlayerState(playerState.paused);
        playerStatesListener.onPaused(currentMusic);
        Log.i(TAG, "Music is Paused:::" + getPlayerState());
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    private void clearPlayList() {
        playedList.clear();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setPlayerState(PlayerState.stopped);
        playerStatesListener.onStopped();
        Log.i(TAG, "Finished playing current file::::" + getPlayerState());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        playerStatesListener.onError(mp);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            player.start();
            if (player.isPlaying()) {
                setPlayerState(PlayerState.playing);
                playerStatesListener.onPrepared();
                playerStatesListener.onPlaying(getCurrentMusic());
                setCurrentMusic(getCurrentMusic());
                playedList.add(getCurrentMusic());
            }
        } finally {
            isPreparing = false;
        }
    }

    public enum PlayerState {
        playing,
        stopped,
        paused
    }

    public interface PlayerStatesListener {
        void onPlaying(Music music);

        void onStopped();

        void onPrepared();

        void onError(MediaPlayer mp);

        void onPaused(Music music);
    }
}
