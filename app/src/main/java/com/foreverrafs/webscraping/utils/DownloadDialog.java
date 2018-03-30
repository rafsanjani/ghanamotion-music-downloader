package com.foreverrafs.webscraping.utils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.foreverrafs.webscraping.R;

import org.w3c.dom.Text;

/**
 * Created by forev on 3/30/2018.
 */

public class DownloadDialog extends DialogFragment implements View.OnClickListener {
    private final String TAG = "musicscrapper";
    private String title;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                downloadDialogListener.onDialogPositiveClick(DownloadDialog.this);
                break;

            case R.id.no:
                downloadDialogListener.onDialogNegativeClick(DownloadDialog.this);
                break;
        }
        dismiss();
    }

    public interface DownloadDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

    DownloadDialogListener downloadDialogListener;

    public void setSongTitle(String title) {
        this.title = title;
    }

    public String getSongTitle(){
        return title;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            downloadDialogListener = (DownloadDialogListener) context;
        } catch (ClassCastException ex) {
            Log.e(TAG, "Interface not implemented yet");
            ex.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_download, container, false);
        TextView songTitle = view.findViewById(R.id.songTitle);
        Button btnYes = view.findViewById(R.id.yes);
        Button btnNo = view.findViewById(R.id.no);

        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);

        songTitle.setText(getSongTitle());
        return view;
    }
}
