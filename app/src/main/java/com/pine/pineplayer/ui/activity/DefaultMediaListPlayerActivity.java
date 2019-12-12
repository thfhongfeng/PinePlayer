package com.pine.pineplayer.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.adapter.DefaultVideoControllerAdapter;

import java.util.List;

public class DefaultMediaListPlayerActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.makeLogTag(DefaultMediaListPlayerActivity.class);

    private PineMediaPlayerView mVideoView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private int mCurrentVideoPosition = -1;
    private List<PineMediaPlayerBean> mMediaList;
    private String mBasePath;
    private DefaultVideoControllerAdapter mMediaControllerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_media_list_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBasePath = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(mBasePath)) {
            finish();
            return;
        }
        mMediaList = MockDataUtil.getMediaList(this, mBasePath);
        mVideoView = (PineMediaPlayerView) findViewById(R.id.video_view);
        mController = new PineMediaController(this);
        mMediaControllerAdapter = new DefaultVideoControllerAdapter(this, mMediaList);
        mController.setMediaControllerAdapter(mMediaControllerAdapter);
        mVideoView.init(TAG, mController);
        mPlayer = mVideoView.getMediaPlayer();
        mPlayer.setAutocephalyPlayMode(false);
        mCurrentVideoPosition = 0;
        mMediaControllerAdapter.setCurrentMediaPosition(mCurrentVideoPosition);
        mPlayer.setPlayingMedia(mMediaList.get(mCurrentVideoPosition));
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
