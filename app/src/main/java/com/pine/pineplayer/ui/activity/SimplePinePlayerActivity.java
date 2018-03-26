package com.pine.pineplayer.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.pine.pineplayer.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineMediaWidget;

public class SimplePinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "SimpleDefaultPinePlayerActivity";

    private PineMediaPlayerView mVideoView;
    private String mBasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_default_pine_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBasePath = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(mBasePath)) {
            finish();
            return;
        }
        mVideoView = (PineMediaPlayerView) findViewById(R.id.video_view);

        PineMediaPlayerBean pineMediaBean = new PineMediaPlayerBean(String.valueOf(0), "Horizontal",
                Uri.parse(mBasePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mVideoView.setPlayingMedia(pineMediaBean);
        mVideoView.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    public void onPause() {
        mVideoView.savePlayerState();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
