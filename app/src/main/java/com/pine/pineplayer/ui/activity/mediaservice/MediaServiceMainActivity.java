package com.pine.pineplayer.ui.activity.mediaservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pine.pineplayer.R;
import com.pine.pineplayer.util.MockDataUtils;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.service.IPineMediaPlayerService;
import com.pine.player.service.PineMediaPlayerService;
import com.pine.player.util.LogUtils;

/**
 * Created by tanghongfeng on 2018/4/8.
 */

public class MediaServiceMainActivity extends AppCompatActivity {
    private static final String TAG = LogUtils.makeLogTag(MediaServiceMainActivity.class);
    private String mBasePath;
    private PineMediaWidget.IPineMediaPlayer mMediaServicePlayer;
    private IPineMediaPlayerService mPlayerService;
    private TextView mGoMediaServicePlayerBtn, mMediaServicePausePlayerBtn, mMediaServiceReleaseBtn;
    private ServiceConnection mMediaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = (IPineMediaPlayerService) service;
            mMediaServicePlayer = mPlayerService.getMediaPlayer();
            findViewById(R.id.release_service_player_tv).setVisibility(View.VISIBLE);
            findViewById(R.id.service_player_tv).setVisibility(View.VISIBLE);

            mMediaServicePlayer.setPlayingMedia(MockDataUtils
                    .getMediaList(MediaServiceMainActivity.this, mBasePath).get(15), true);
            mMediaServicePlayer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediaServicePausePlayerBtn.setVisibility(View.VISIBLE);
            mMediaServicePausePlayerBtn.setText("播放");
            mMediaServicePlayer = null;
            mPlayerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_service_main);
        mBasePath = getIntent().getStringExtra("path");

        mGoMediaServicePlayerBtn = (TextView) findViewById(R.id.service_player_tv);
        mMediaServicePausePlayerBtn = (TextView) findViewById(R.id.pause_play_service_player_tv);
        mMediaServicePausePlayerBtn.setVisibility(View.VISIBLE);
        mMediaServiceReleaseBtn = (TextView) findViewById(R.id.release_service_player_tv);

        mMediaServicePlayer = PineMediaPlayerService
                .getMediaPlayerByTag(PineMediaPlayerService.SERVICE_MEDIA_PLAYER_TAG);

        mGoMediaServicePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaServiceMainActivity.this, MediaServicePlayerActivity.class);
                intent.putExtra("path", mBasePath);
                startActivity(intent);
            }
        });
        mMediaServiceReleaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerService != null) {
                    unbindService(mMediaServiceConnection);
                    mMediaServicePausePlayerBtn.setVisibility(View.VISIBLE);
                    mMediaServiceReleaseBtn.setVisibility(View.GONE);
                    mGoMediaServicePlayerBtn.setVisibility(View.GONE);
                    mMediaServicePausePlayerBtn.setText("播放");
                    mMediaServicePlayer = null;
                    mPlayerService = null;
                }
            }
        });

        mMediaServicePausePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaServicePlayer != null) {
                    if ("暂停".equals(mMediaServicePausePlayerBtn.getText())) {
                        mMediaServicePlayer.pause();
                        mMediaServicePausePlayerBtn.setText("播放");
                    } else if ("播放".equals(mMediaServicePausePlayerBtn.getText())) {
                        mMediaServicePlayer.start();
                        mMediaServicePausePlayerBtn.setText("暂停");
                    }
                } else {
                    Intent intent = new Intent("media.player.server");
                    intent.setPackage(getPackageName());
                    bindService(intent, mMediaServiceConnection, BIND_AUTO_CREATE);
                    mMediaServicePausePlayerBtn.setText("暂停");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMediaServicePlayer != null) {
            mMediaServiceReleaseBtn.setVisibility(View.VISIBLE);
            mGoMediaServicePlayerBtn.setVisibility(View.VISIBLE);
            mMediaServicePausePlayerBtn.setText(mMediaServicePlayer.isPlaying() ? "暂停" : "播放");
        } else {
            mMediaServiceReleaseBtn.setVisibility(View.GONE);
            mGoMediaServicePlayerBtn.setVisibility(View.GONE);
            mMediaServicePausePlayerBtn.setText("播放");
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayerService != null) {
            unbindService(mMediaServiceConnection);
            mMediaServicePausePlayerBtn.setVisibility(View.VISIBLE);
            mMediaServicePausePlayerBtn.setText("播放");
            mMediaServicePlayer = null;
            mPlayerService = null;
        }
        super.onDestroy();
    }
}
