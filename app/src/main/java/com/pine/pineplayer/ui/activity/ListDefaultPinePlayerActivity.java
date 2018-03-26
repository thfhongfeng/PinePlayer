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
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.adapter.DefaultMediaControllerAdapter;

import java.util.List;

public class ListDefaultPinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "ListDefaultPinePlayerActivity";

    private PineMediaPlayerView mVideoView;
    private PineMediaController mController;
    private int mCurrentVideoPosition = -1;
    private List<PineMediaPlayerBean> mMediaList;
    private String mBasePath;
    private DefaultMediaControllerAdapter mMediaControllerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_default_pine_player);
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

        mMediaControllerAdapter = new DefaultMediaControllerAdapter(this, mMediaList);

        mController.setMediaControllerAdapter(mMediaControllerAdapter);
        mVideoView.setMediaController(mController);
        mVideoView.setLocalStreamMode(true);
        mVideoView.setMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public boolean onError(int framework_err, int impl_err) {
                return false;
            }
        });
        mCurrentVideoPosition = 0;
        mMediaControllerAdapter.setCurrentVideoPosition(mCurrentVideoPosition);
        mVideoView.setPlayingMedia(mMediaList.get(mCurrentVideoPosition));
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
