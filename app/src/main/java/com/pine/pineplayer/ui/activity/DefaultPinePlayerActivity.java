package com.pine.pineplayer.ui.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.util.FileUtil;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.adapter.DefaultMediaControllerAdapter;

import java.util.List;

public class DefaultPinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "DefaultPinePlayerActivity";

    private static final int GET_MEDIA_LIST_DONE = 1;

    private PineMediaPlayerView mVideoView;
    private PineMediaController mController;
    private int mCurrentVideoPosition = -1;
    private List<PineMediaPlayerBean> mMediaList;
    private Handler mHandler;
    private String mBasePath;
    private DefaultMediaControllerAdapter mMediaControllerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_pine_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mBasePath = getExternalCacheDir().getPath().toString();
        } else {
            mBasePath = getCacheDir().getPath().toString();
        }

        mMediaList = MockDataUtil.getMediaList(this, mBasePath);
        initHandler();
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
        copyAssets();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.savePlayerState();
    }

    @Override
    public void onDestroy() {
        mHandler.removeMessages(GET_MEDIA_LIST_DONE);
        super.onDestroy();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GET_MEDIA_LIST_DONE:
                        mMediaControllerAdapter.setCurrentVideoPosition(mCurrentVideoPosition);
                        mVideoView.setPlayingMedia(mMediaList.get(mCurrentVideoPosition));
                        mVideoView.start();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void copyAssets() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "resource.zip",
                        mBasePath, true, "GBK");
                mHandler.sendEmptyMessage(GET_MEDIA_LIST_DONE);
            }
        }).start();
    }
}
