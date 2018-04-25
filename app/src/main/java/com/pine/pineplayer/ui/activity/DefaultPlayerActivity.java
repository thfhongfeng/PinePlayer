package com.pine.pineplayer.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;

public class DefaultPlayerActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.makeLogTag(DefaultPlayerActivity.class);

    private PineMediaPlayerView mVideoView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private String mBasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_player);
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
        mVideoView.init(TAG, mController);
        mPlayer = mVideoView.getMediaPlayer();
        mPlayer.setAutocephalyPlayMode(false);
        PineMediaPlayerBean pineMediaBean = new PineMediaPlayerBean(String.valueOf(0), "VideoDefinitionSelect",
                MockDataUtil.getMediaUriSourceList(mBasePath), PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null, null);
        mPlayer.setPlayingMedia(pineMediaBean);
        mPlayer.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
