package com.pine.pineplayer.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;

public class SimpleDefaultPinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "SimpleDefaultPinePlayerActivity";

    private PineMediaPlayerView mVideoView;
    private PineMediaController mController;
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
        mController = new PineMediaController(this);

        mVideoView.setMediaController(mController);

        PineMediaPlayerBean pineMediaBean = new PineMediaPlayerBean(String.valueOf(0), "VideoDefinitionSelect",
                MockDataUtil.getMediaUriSourceList(mBasePath), PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null, null);
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
