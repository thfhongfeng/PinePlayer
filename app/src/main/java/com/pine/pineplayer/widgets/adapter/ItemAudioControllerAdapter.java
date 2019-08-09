package com.pine.pineplayer.widgets.adapter;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pine.pineplayer.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by tanghongfeng on 2018/3/7.
 */

/**
 * 参考此Adapter，继承AbstractMediaControllerAdapter进行自定义controller的定制
 **/

public class ItemAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Activity mDContext;
    private ViewGroup mRoot;
    private PineMediaPlayerView mMediaPlayerView;
    private PineBackgroundViewHolder mDBackgroundViewHolder;
    private PineControllerViewHolder mDControllerViewHolder;
    private RelativeLayout mDBackgroundView;
    private ViewGroup mDControllerView;
    private PineMediaWidget.IPineMediaPlayer mDPlayer;
    private PineMediaPlayerBean mMediaBean;
    private ControllersActionListener mControllersActionListener;

    public ItemAudioControllerAdapter(Activity context, ViewGroup root,
                                      PineMediaPlayerView playerView, PineMediaPlayerBean mediaBean,
                                      ControllersActionListener listener) {
        mDContext = context;
        mRoot = root;
        mMediaPlayerView = playerView;
        mMediaBean = mediaBean;
        mControllersActionListener = listener;
    }

    @Override
    protected final boolean init(PineMediaWidget.IPineMediaPlayer player) {
        mDPlayer = player;
        return true;
    }

    @Override
    protected final PineBackgroundViewHolder onCreateBackgroundViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mDBackgroundViewHolder == null) {
            mDBackgroundViewHolder = new PineBackgroundViewHolder();
            if (mDBackgroundView == null) {
                ImageView backgroundView = new ImageView(mDContext);
                backgroundView.setBackgroundResource(android.R.color.darker_gray);
                mDBackgroundView = new RelativeLayout(mDContext);
                mDBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                mDBackgroundView.setLayoutTransition(new LayoutTransition());
                RelativeLayout.LayoutParams backgroundParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mDBackgroundView.addView(backgroundView, backgroundParams);
                mDBackgroundViewHolder.setBackgroundImageView(backgroundView);
            }
        }
        mDBackgroundViewHolder.setContainer(mDBackgroundView);
        return mDBackgroundViewHolder;
    }

    @Override
    protected final PineControllerViewHolder onCreateInRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    private final void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.vb_play_pause_iv));
        SeekBar seekBar = (SeekBar) root.findViewById(R.id.vb_audio_progress);
        viewHolder.setPlayProgressBar(seekBar);
        viewHolder.setEndTimeText(root.findViewById(R.id.vb_audio_end_time));
        viewHolder.setCurrentTimeText(root.findViewById(R.id.vb_audio_cur_time));
    }

    @Override
    public PineControllerViewHolder onCreateOutRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        if (mDControllerViewHolder == null) {
            mDControllerViewHolder = new PineControllerViewHolder();
            if (mDControllerView == null) {
                mDControllerView = mRoot;
            }
            initControllerViewHolder(mDControllerViewHolder, mDControllerView);
        }
        mDControllerViewHolder.setContainer(mDControllerView);
        return mDControllerViewHolder;
    }

    @Override
    protected final PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    @Override
    protected List<PineRightViewHolder> onCreateRightViewHolderList(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    protected PineMediaController.ControllerMonitor onCreateControllerMonitor() {
        return new PineMediaController.ControllerMonitor() {
            public boolean onCurrentTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                               View currentTimeText, int currentTime) {
                if (currentTimeText instanceof TextView) {
                    ((TextView) currentTimeText).setText(stringForTime(currentTime));
                }
                View remindTv = mRoot.findViewById(R.id.vb_audio_remind_time);
                if (remindTv instanceof TextView) {
                    ((TextView) remindTv).setText(stringForTime(player.getDuration() - currentTime));
                }
                return true;
            }

            public boolean onEndTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                           View endTimeText, int endTime) {
                if (endTimeText instanceof TextView) {
                    ((TextView) endTimeText).setText(stringForTime(player.getDuration()));
                }
                return true;
            }
        };
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onPlayPauseBtnClick(View playPauseBtn,
                                               PineMediaWidget.IPineMediaPlayer player) {
                if (mControllersActionListener != null) {
                    return mControllersActionListener.onPlayPauseBtnClick(playPauseBtn, player);
                }
                return false;
            }
        };
    }

    public interface ControllersActionListener {
        boolean onPlayPauseBtnClick(View playPauseBtn,
                                    PineMediaWidget.IPineMediaPlayer player);
    }
}
