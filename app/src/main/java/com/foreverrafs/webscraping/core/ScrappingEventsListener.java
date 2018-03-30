package com.foreverrafs.webscraping.core;

import com.foreverrafs.webscraping.model.Music;

import java.util.List;

/**
 * Created by forev on 3/12/2018.
 */

public interface ScrappingEventsListener {
    void onScrappingCompleted(List<Music> musicList);
    void onScrappingStarted();
}
