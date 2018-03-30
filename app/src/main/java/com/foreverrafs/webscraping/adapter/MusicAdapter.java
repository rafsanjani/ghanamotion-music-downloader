package com.foreverrafs.webscraping.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.foreverrafs.webscraping.R;
import com.foreverrafs.webscraping.model.Music;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by forev on 3/8/2018.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {


    private final String TAG = "musicscrapper";
    private Context mCtx;
    private List<Music> musicList;
    private ClickListener clickListener;
    private ViewsBoundedListener viewsBoundedListener;
    private int adapterPosition = -1;

    public MusicAdapter() {
    }

    public MusicAdapter(Context mCtx, List<Music> musicList) {
        this.mCtx = mCtx;
        this.musicList = musicList;
    }

    public MusicAdapter get() {
        return this;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setViewsBoundedListener(ViewsBoundedListener viewsBoundedListener){
        this.viewsBoundedListener = viewsBoundedListener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.music_list, parent, false);
        MusicViewHolder musicViewHolder = new MusicViewHolder(view);

        return musicViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return 0x01;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Log.i(TAG, "binding view items");
        Music music = musicList.get(position);
        holder.title.setText(music.getTitle());
        double fileSizeInMB = music.getFileSize() / 1024 / 1024;
        String fileSizeInMBStr = String.format("%.2f", fileSizeInMB) + "MB";


        holder.fileSize.setText(fileSizeInMBStr);

        try {
            Picasso.get()
                    .load(music.getImageUrl())
                    .resize(250, 250)
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music)
                    .into(holder.albumArt);
            viewsBoundedListener.onBindView(position);

            // holder.albumArt.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.music));
        } catch (Exception ex) {
            Log.i(TAG, "Error binding view item");
            Log.e(TAG,ex.getMessage());
        }

        Log.i(TAG, "View Item Bounded");
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    public void removeSongAt(int position) {
        musicList.remove(position);
        notifyItemRemoved(position);
    }

    public void addSong(Music music) {
        musicList.add(music);
        notifyDataSetChanged();
    }

    public Music getSongAt(int position) {
        return musicList.get(position);
    }

    public List<Music> getAllSongs() {
        return musicList;
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView albumArt;
        private ImageView playButton;
        private TextView title, fileSize;
        private ImageView download;

        private RelativeLayout musicBackground;
        public String getTitle(){
            return title.getText().toString();
        }

        public void setPlayButtonImage(Drawable drawable) {
            playButton.setImageDrawable(drawable);
        }

        public void setBackground(int color) {
            musicBackground.setBackgroundColor(color);
        }

        public MusicViewHolder(View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.albumArt);
            title = itemView.findViewById(R.id.title);
            playButton = itemView.findViewById(R.id.playBtnMain);
            musicBackground = itemView.findViewById(R.id.music_background);
            fileSize = itemView.findViewById(R.id.textviewFileSize);
            download = itemView.findViewById(R.id.downloadBtnMain);


            TextView title = itemView.findViewById(R.id.title);
            Typeface typeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/Quotus-Bold.ttf");
            title.setTypeface(typeface);

            title.setOnClickListener(this);
            albumArt.setOnClickListener(this);
            itemView.setOnClickListener(this);
            playButton.setOnClickListener(this);
            download.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //adapterPosition will be -1 if it's the first item being clicked in the holder so a null check
            //is required on the viewholder that will be generated at the index
            if (clickListener != null) {
                clickListener.itemClicked(v, getAdapterPosition(), adapterPosition);
                adapterPosition = getAdapterPosition();
            }
        }
    }

    public interface ClickListener {
        void itemClicked(View view, int position, int prevPosition);
    }

    public interface ViewsBoundedListener{
        void onBindView(int position);
    }
}
