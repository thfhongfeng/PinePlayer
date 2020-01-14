package com.pine.pineplayer.ui.activity.autocephaly;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtils;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.adapter.DefaultAudioControllerAdapter;
import com.pine.player.widget.adapter.DefaultVideoControllerAdapter;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by tanghongfeng on 2018/4/2.
 */

public class AutocephalyPlayerActivity extends AppCompatActivity {
    private static final String TAG = LogUtils.makeLogTag(AutocephalyPlayerActivity.class);

    private PineMediaPlayerView mCurMediaView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private String mCurrentMediaCode = "";
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
        mMediaList = MockDataUtils.getMediaList(this, mBasePath);
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
        mPlayer = mCurMediaView.getMediaPlayer();
        mPlayer.setAutocephalyPlayMode(true);
        mCurMediaView.setVisibility(View.VISIBLE);
        if (mPlayer.isInPlaybackState()) {
            PineMediaPlayerBean playerBean = mPlayer.getMediaPlayerBean();
            mCurrentMediaCode = playerBean.getMediaCode();
            mMediaControllerAdapter.onMediaSelect(mCurrentMediaCode, false);
        } else {
            mCurrentMediaCode = mMediaList.get(15).getMediaCode();
            mMediaControllerAdapter.onMediaSelect(mCurrentMediaCode, true);
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
