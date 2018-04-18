package com.pine.pineplayer.ui.activity.autocephaly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.adapter.DefaultAudioControllerAdapter;
import com.pine.player.widget.adapter.DefaultVideoControllerAdapter;

import java.util.List;

/**
 * Created by tanghongfeng on 2018/4/2.
 */

public class AutocephalyPlayerActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.makeLogTag(AutocephalyPlayerActivity.class);

    private PineMediaPlayerView mCurMediaView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private int mCurrentVideoPosition = -1;
    private List<PineMediaPlayerBean> mMediaList;
    private String mBasePath;
    private PineMediaController.AbstractMediaControllerAdapter mMediaControllerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocephaly_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBasePath = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(mBasePath)) {
            finish();
            return;
        }
        mMediaList = MockDataUtil.getMediaList(this, mBasePath);
        mController = new PineMediaController(this);
        if (PinePlayerApp.mMockCount++ % 2 == 0) {
            mCurMediaView = (PineMediaPlayerView) findViewById(R.id.video_view);
            mMediaControllerAdapter = new DefaultVideoControllerAdapter(this, mMediaList);
            mController.setMediaControllerAdapter(mMediaControllerAdapter);
            mCurMediaView.init(TAG, mController);

        } else {
            mCurMediaView = (PineMediaPlayerView) findViewById(R.id.audio_view);
            mMediaControllerAdapter = new DefaultAudioControllerAdapter(this, mMediaList);
            mController.setMediaControllerAdapter(mMediaControllerAdapter);
            mCurMediaView.init(TAG, mController, false);
        }
        mCurMediaView.setVisibility(View.VISIBLE);
        mPlayer = mCurMediaView.getMediaPlayer();
        if (mPlayer.isInPlaybackState()) {
            PineMediaPlayerBean playerBean = mPlayer.getMediaPlayerBean();
            for (int i = 0; i < mMediaList.size(); i++) {
                if (playerBean.getMediaCode().equals(mMediaList.get(i).getMediaCode())) {
                    mCurrentVideoPosition = i;
                    mMediaControllerAdapter.setCurrentMediaPosition(mCurrentVideoPosition);
                    break;
                }
            }
        } else {
            mCurrentVideoPosition = 15;
            mMediaControllerAdapter.setCurrentMediaPosition(mCurrentVideoPosition);
            mPlayer.setPlayingMedia(mMediaList.get(mCurrentVideoPosition), true);
            mPlayer.start();
        }
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
