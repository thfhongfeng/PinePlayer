package com.pine.pineplayer.ui.activity.autocephaly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.adapter.DefaultMediaControllerAdapter;
import com.pine.player.widget.viewholder.PineControllerViewHolder;

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
    private DefaultMediaControllerAdapter mMediaControllerAdapter;

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
            mCurMediaView.init(TAG);
            mMediaControllerAdapter = new DefaultMediaControllerAdapter(this, mMediaList);
        } else {
            mCurMediaView = (PineMediaPlayerView) findViewById(R.id.audio_view);
            mCurMediaView.init(TAG, false);
            mMediaControllerAdapter = new DefaultMediaControllerAdapter(this, mMediaList) {
                @Override
                public PineControllerViewHolder getInRootControllerViewHolder(boolean isFullScreen) {
                    ViewGroup container = (ViewGroup) View.inflate(AutocephalyPlayerActivity.this,
                            R.layout.player_audio_controller, null);
                    PineControllerViewHolder viewHolder = new PineControllerViewHolder();
                    viewHolder.setPrevButton(container.findViewById(R.id.media_pre));
                    viewHolder.setPausePlayButton(container.findViewById(R.id.pause_play_btn));
                    viewHolder.setNextButton(container.findViewById(R.id.media_next));
                    viewHolder.setPlayProgressBar((SeekBar) container.findViewById(R.id.media_progress));
                    viewHolder.setCurrentTimeText(container.findViewById(R.id.cur_time_text));
                    viewHolder.setEndTimeText(container.findViewById(R.id.end_time_text));
                    viewHolder.setContainer(container);
                    return viewHolder;
                }
            };
        }
        mCurMediaView.setVisibility(View.VISIBLE);
        mController.setMediaControllerAdapter(mMediaControllerAdapter);
        mCurMediaView.setMediaController(mController);
        mPlayer = mCurMediaView.getMediaPlayer();
        mPlayer.setMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public boolean onError(int framework_err, int impl_err) {
                return false;
            }
        });
        if (mPlayer.isInPlaybackState()) {
            PineMediaPlayerBean playerBean = mPlayer.getMediaPlayerBean();
            for (int i = 0; i < mMediaList.size(); i++) {
                if (playerBean.getMediaCode().equals(mMediaList.get(i).getMediaCode())) {
                    mCurrentVideoPosition = i;
                    mMediaControllerAdapter.setCurrentVideoPosition(mCurrentVideoPosition);
                    break;
                }
            }
        } else {
            mCurrentVideoPosition = 15;
            mMediaControllerAdapter.setCurrentVideoPosition(mCurrentVideoPosition);
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
