package com.pine.pineplayer.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pine.pineplayer.R;
import com.pine.pineplayer.widgets.adapter.ItemAudioControllerAdapter;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;

/**
 * Created by tanghongfeng on 2018/4/26.
 */

public class MultiControllerSwitchActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.makeLogTag(MultiControllerSwitchActivity.class);
    PineMediaController mMediaController1, mMediaController2, mMediaController3;
    PineMediaPlayerView mAudioPlayerView;
    PineMediaWidget.IPineMediaPlayer mMediaPlayer;
    private String mBasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_controller_switch);
        mBasePath = getIntent().getStringExtra("path");
        preparePlayer();
    }

    private void preparePlayer() {
        mAudioPlayerView = (PineMediaPlayerView) findViewById(R.id.audio_view);
        RelativeLayout itemRl1 = (RelativeLayout) findViewById(R.id.item_controller1_rl);
        RelativeLayout itemRl2 = (RelativeLayout) findViewById(R.id.item_controller2_rl);
        RelativeLayout itemRl3 = (RelativeLayout) findViewById(R.id.item_controller3_rl);

        mMediaController1 = new PineMediaController(this);
        final PineMediaPlayerBean playerBean1 = new PineMediaPlayerBean(String.valueOf(0), "Scenery",
                Uri.parse(mBasePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        ((TextView) itemRl1.findViewById(R.id.vb_message_tv)).setText("Scenery");
        itemRl1.findViewById(R.id.vb_play_pause_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayerView.resetMediaController(mMediaController1,
                        true, true);
                mMediaPlayer.setPlayingMedia(playerBean1);
                mMediaPlayer.start();
            }
        });
        mMediaController1.setMediaControllerAdapter(new ItemAudioControllerAdapter(this,
                itemRl1, mAudioPlayerView, playerBean1,
                new ItemAudioControllerAdapter.ControllersActionListener() {
                    @Override
                    public boolean onPlayPauseBtnClick(View playPauseBtn, PineMediaWidget.IPineMediaPlayer player) {
                        if (player.getMediaPlayerBean() != playerBean1) {
                            mAudioPlayerView.resetMediaController(mMediaController1,
                                    true, true);
                            mMediaPlayer.setPlayingMedia(playerBean1);
                        }
                        return false;
                    }
                }));

        mMediaController2 = new PineMediaController(this);
        final PineMediaPlayerBean playerBean2 = new PineMediaPlayerBean(String.valueOf(1), "yesterday once more",
                Uri.parse(mBasePath + "/resource/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        ((TextView) itemRl2.findViewById(R.id.vb_message_tv)).setText("yesterday once more");
        itemRl2.findViewById(R.id.vb_play_pause_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayerView.resetMediaController(mMediaController2,
                        true, true);
                mMediaPlayer.setPlayingMedia(playerBean2);
                mMediaPlayer.start();
            }
        });
        mMediaController2.setMediaControllerAdapter(new ItemAudioControllerAdapter(this,
                itemRl2, mAudioPlayerView, playerBean2,
                new ItemAudioControllerAdapter.ControllersActionListener() {
                    @Override
                    public boolean onPlayPauseBtnClick(View playPauseBtn, PineMediaWidget.IPineMediaPlayer player) {
                        if (player.getMediaPlayerBean() != playerBean2) {
                            mAudioPlayerView.resetMediaController(mMediaController2,
                                    true, true);
                            mMediaPlayer.setPlayingMedia(playerBean2);
                        }
                        return false;
                    }
                }));

        mMediaController3 = new PineMediaController(this);
        final PineMediaPlayerBean playerBean3 = new PineMediaPlayerBean(String.valueOf(2), "HometownScenery",
                Uri.parse(mBasePath + "/resource/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        ((TextView) itemRl3.findViewById(R.id.vb_message_tv)).setText("HometownScenery");
        itemRl3.findViewById(R.id.vb_play_pause_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayerView.resetMediaController(mMediaController3,
                        true, true);
                mMediaPlayer.setPlayingMedia(playerBean3);
                mMediaPlayer.start();
            }
        });
        mMediaController3.setMediaControllerAdapter(new ItemAudioControllerAdapter(this,
                itemRl3, mAudioPlayerView, playerBean2,
                new ItemAudioControllerAdapter.ControllersActionListener() {
                    @Override
                    public boolean onPlayPauseBtnClick(View playPauseBtn, PineMediaWidget.IPineMediaPlayer player) {
                        if (player.getMediaPlayerBean() != playerBean3) {
                            mAudioPlayerView.resetMediaController(mMediaController3,
                                    true, true);
                            mMediaPlayer.setPlayingMedia(playerBean3);
                        }
                        return false;
                    }
                }));
        mAudioPlayerView.init(TAG, null, false);
        mMediaPlayer = mAudioPlayerView.getMediaPlayer();
        mMediaPlayer.setAutocephalyPlayMode(false);
    }
}
