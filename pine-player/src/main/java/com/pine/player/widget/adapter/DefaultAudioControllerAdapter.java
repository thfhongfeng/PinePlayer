package com.pine.player.widget.adapter;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.pine.player.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.util.List;

/**
 * Created by tanghongfeng on 2018/3/7.
 */

/**
 * 参考此Adapter，继承AbstractMediaControllerAdapter进行自定义controller的定制
 **/

public class DefaultAudioControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Activity mDContext;
    private List<PineMediaPlayerBean> mDMediaList;
    private PineBackgroundViewHolder mDBackgroundViewHolder;
    private PineControllerViewHolder mDFullControllerViewHolder, mDControllerViewHolder;
    private RelativeLayout mDBackgroundView;
    private ViewGroup mDFullControllerView, mDControllerView;
    private PineMediaWidget.IPineMediaPlayer mDPlayer;
    private int mDCurrentVideoPosition = -1;
    private boolean mDEnableSpeed, mDEnablePreNext;
    private boolean mDEnableCurTime, mDEnableProgressBar, mDEnableTotalTime;
    private boolean mDEnableVolumeText, mDEnableFullScreen;

    public DefaultAudioControllerAdapter(Activity context) {
        this(context, null, true, true, true, true, true, true, true);
    }

    public DefaultAudioControllerAdapter(Activity context, List<PineMediaPlayerBean> mediaList) {
        this(context, mediaList, true, true, true, true, true, true, true);
    }

    public DefaultAudioControllerAdapter(Activity context,
                                         List<PineMediaPlayerBean> mediaList, boolean enablePreNext,
                                         boolean enableSpeed, boolean enableCurTime,
                                         boolean enableProgressBar, boolean enableTotalTime,
                                         boolean enableVolumeText, boolean enableFullScreen) {
        mDContext = context;
        mDMediaList = mediaList;
        mDEnablePreNext = enablePreNext;
        mDEnableSpeed = enableSpeed;
        mDEnableCurTime = enableCurTime;
        mDEnableProgressBar = enableProgressBar;
        mDEnableTotalTime = enableTotalTime;
        mDEnableVolumeText = enableVolumeText;
        mDEnableFullScreen = enableFullScreen;
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
        if (isFullScreenMode) {
            if (mDFullControllerViewHolder == null) {
                mDFullControllerViewHolder = new PineControllerViewHolder();
                if (mDFullControllerView == null) {
                    mDFullControllerView = (ViewGroup) View.inflate(mDContext,
                            R.layout.pine_player_audio_controller, null);
                }
                initControllerViewHolder(mDFullControllerViewHolder, mDFullControllerView);
            }
            mDFullControllerViewHolder.setContainer(mDFullControllerView);
            return mDFullControllerViewHolder;
        } else {
            if (mDControllerViewHolder == null) {
                if (mDControllerView == null) {
                    mDControllerView = (ViewGroup) View.inflate(mDContext,
                            R.layout.pine_player_audio_controller, null);
                }
                mDControllerViewHolder = new PineControllerViewHolder();
                initControllerViewHolder(mDControllerViewHolder, mDControllerView);
            }
            mDControllerViewHolder.setContainer(mDControllerView);
            return mDControllerViewHolder;
        }
    }

    @Override
    protected PineControllerViewHolder onCreateOutRootControllerViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    @Override
    protected PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    @Override
    protected List<PineRightViewHolder> onCreateRightViewHolderList(
            PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode) {
        return null;
    }

    private final void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.pause_play_btn));
        View preTv = root.findViewById(R.id.media_pre);
        View nextTv = root.findViewById(R.id.media_next);
        if (mDEnablePreNext) {
            viewHolder.setPrevButton(preTv);
            viewHolder.setNextButton(nextTv);
            preTv.setEnabled(mDMediaList != null && mDCurrentVideoPosition > 0);
            nextTv.setEnabled(mDMediaList != null && mDCurrentVideoPosition < mDMediaList.size());
        } else {
            preTv.setVisibility(View.GONE);
            nextTv.setVisibility(View.GONE);
        }
        SeekBar seekBar = (SeekBar) root.findViewById(R.id.media_progress);
        if (mDEnableProgressBar) {
            viewHolder.setPlayProgressBar(seekBar);
        } else {
            seekBar.setVisibility(View.GONE);
        }
        View curTimeTv = root.findViewById(R.id.cur_time_text);
        if (mDEnableCurTime) {
            viewHolder.setCurrentTimeText(curTimeTv);
        } else {
            curTimeTv.setVisibility(View.GONE);
        }
        View endTimeTv = root.findViewById(R.id.end_time_text);
        if (mDEnableTotalTime) {
            viewHolder.setEndTimeText(endTimeTv);
        } else {
            endTimeTv.setVisibility(View.GONE);
        }
        View VolumesTv = root.findViewById(R.id.volumes_text);
        if (mDEnableVolumeText) {
            viewHolder.setVolumesText(VolumesTv);
        } else {
            VolumesTv.setVisibility(View.GONE);
        }
        View fullScreenTv = root.findViewById(R.id.full_screen_btn);
        if (mDEnableFullScreen) {
            viewHolder.setFullScreenButton(fullScreenTv);
        } else {
            fullScreenTv.setVisibility(View.GONE);
        }
        View speedTv = root.findViewById(R.id.media_speed_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mDEnableSpeed) {
            viewHolder.setSpeedButton(speedTv);
        } else {
            speedTv.setVisibility(View.GONE);
        }
        viewHolder.setMediaNameText(root.findViewById(R.id.media_name_text));
    }

    @Override
    public PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onPreBtnClick(View preBtn, PineMediaWidget.IPineMediaPlayer player) {
                audioSelected(mDCurrentVideoPosition - 1);
                preBtn.setEnabled(mDMediaList != null && mDCurrentVideoPosition > 0);
                return true;
            }

            @Override
            public boolean onNextBtnClick(View nextBtn, PineMediaWidget.IPineMediaPlayer player) {
                audioSelected(mDCurrentVideoPosition + 1);
                nextBtn.setEnabled(mDMediaList != null && mDCurrentVideoPosition < mDMediaList.size());
                return true;
            }

            @Override
            public boolean onGoBackBtnClick(View fullScreenBtn, PineMediaWidget.IPineMediaPlayer player,
                                            boolean isFullScreenMode) {
                if (isFullScreenMode && mDEnableFullScreen) {
                    mDControllerViewHolder.getFullScreenButton().performClick();
                } else {
                    mDContext.finish();
                }
                return false;
            }
        };
    }

    @Override
    public final void setCurrentMediaPosition(int position) {
        mDCurrentVideoPosition = position;
    }

    private void audioSelected(int position) {
        PineMediaPlayerBean pineMediaPlayerBean = null;
        if (mDMediaList != null && mDMediaList.size() > 0) {
            if (position >= 0 && position < mDMediaList.size()) {
                pineMediaPlayerBean = mDMediaList.get(position);
            } else {
                return;
            }
        } else {
            pineMediaPlayerBean = mDPlayer.getMediaPlayerBean();
        }
        mDCurrentVideoPosition = position;
        mDPlayer.setPlayingMedia(pineMediaPlayerBean);
        mDPlayer.start();
    }
}
