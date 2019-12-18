package com.pine.pineplayer.ui.activity.autocephaly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pine.pineplayer.R;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.service.PineMediaPlayerService;
import com.pine.player.util.LogUtils;

/**
 * Created by tanghongfeng on 2018/4/8.
 */

public class AutocephalyMainActivity extends AppCompatActivity {
    private static final String TAG = LogUtils.makeLogTag(AutocephalyMainActivity.class);
    private String mBasePath;
    private PineMediaWidget.IPineMediaPlayer mAutocephalyMediaPlayer;
    private TextView mAutocephalyPausePlayBtn, mAutocephalyReleaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocephaly_main);
        mBasePath = getIntent().getStringExtra("path");

        mAutocephalyPausePlayBtn = (TextView) findViewById(R.id.pause_play_autocephaly_player_tv);
        mAutocephalyReleaseBtn = (TextView) findViewById(R.id.release_autocephaly_player_tv);

        findViewById(R.id.autocephaly_player_tv).setVisibility(View.VISIBLE);
        findViewById(R.id.autocephaly_player_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AutocephalyMainActivity.this, AutocephalyPlayerActivity.class);
                intent.putExtra("path", mBasePath);
                startActivity(intent);
            }
        });
        mAutocephalyReleaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PineMediaWidget.IPineMediaPlayer autocephalyMediaPlayer = PineMediaPlayerService
                        .getMediaPlayerByTag(LogUtils.makeLogTag(AutocephalyPlayerActivity.class));
                if (autocephalyMediaPlayer != null) {
                    PineMediaPlayerService.destroyMediaPlayerByTag(
                            LogUtils.makeLogTag(AutocephalyPlayerActivity.class));
                    mAutocephalyReleaseBtn.setVisibility(View.GONE);
                    mAutocephalyPausePlayBtn.setVisibility(View.GONE);
                }
            }
        });
        mAutocephalyPausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutocephalyMediaPlayer != null) {
                    if ("暂停".equals(mAutocephalyPausePlayBtn.getText())) {
                        mAutocephalyMediaPlayer.pause();
                        mAutocephalyPausePlayBtn.setText("播放");
                    } else if ("播放".equals(mAutocephalyPausePlayBtn.getText())) {
                        mAutocephalyMediaPlayer.start();
                        mAutocephalyPausePlayBtn.setText("暂停");
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mAutocephalyMediaPlayer = PineMediaPlayerService
                .getMediaPlayerByTag(LogUtils.makeLogTag(AutocephalyPlayerActivity.class));
        if (mAutocephalyMediaPlayer != null) {
            mAutocephalyReleaseBtn.setVisibility(View.VISIBLE);
            mAutocephalyPausePlayBtn.setText(mAutocephalyMediaPlayer.isPlaying() ? "暂停" : "播放");
            mAutocephalyPausePlayBtn.setVisibility(View.VISIBLE);
        } else {
            mAutocephalyReleaseBtn.setVisibility(View.GONE);
            mAutocephalyPausePlayBtn.setVisibility(View.GONE);
        }
    }
}
