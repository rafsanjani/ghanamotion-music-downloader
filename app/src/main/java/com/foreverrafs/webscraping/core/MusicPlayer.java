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

    //TODO: Refactor up this portion of code for readability by performing appropriate method calls
    public void play(Music music) throws IOException {

        switch (getPlayerState()) {

            case playing:
                Log.i(TAG, "Currently playing song :::" + getCurrentMusic().getTitle());

                //if it's the same song, just pause, else stop the current song and play the new song
                if (getCurrentMusic().getHash() == music.getHash()) {
                    Log.i(TAG, "Attempting to Pause");
                    player.pause();
                    playerStatesListener.onPaused(music);
                    setPlayerState(PlayerState.paused);
                } else {
                    Log.i(TAG, "New file detected::::attempting to play::" + music.getTitle());
                    player.reset();
                    player.setDataSource(music.getSongUrl());
                    //if (!isPreparing) {
                        player.prepareAsync();
                     //   isPreparing = true;
                   // }

                }
                break;
            case paused:
                //if it's the same song, just resume it, else stop it altogether and play the new song
                if (getCurrentMusic().getHash() == music.getHash()) {
                    Log.i(TAG, "Music is Paused::Attempting to resume");
                    Log.i(TAG, "Successfully Resumed::::Playing " + music.getTitle());
                    player.start();
                    setPlayerState(PlayerState.playing);
                    playerStatesListener.onPlaying(music);
                } else {
                    Log.i(TAG, "New file detected::::attempting to play::" + music.getTitle());
                    player.reset();
                    player.setDataSource(music.getSongUrl());
                    setCurrentMusic(music);
                   // if (!isPreparing) {
                        player.prepareAsync();
                    //    isPreparing = true;
                    //}
                }

                break;

            case stopped:
                Log.i(TAG, "Music is stopped :: Attempting to start from beginning");
                player.setDataSource(music.getSongUrl());
                setCurrentMusic(music);
               // if (!isPreparing) {
                    player.prepareAsync();
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
        Log.i(TAG, "media player error");
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        if (player.isPlaying()) {
            setPlayerState(PlayerState.playing);
            playerStatesListener.onPlaying(getCurrentMusic());
            setCurrentMusic(getCurrentMusic());
            playedList.add(getCurrentMusic());
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

        void onPaused(Music music);
    }
}
