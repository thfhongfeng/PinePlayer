package com.pine.player.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.component.PineMediaPlayerComponent;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.adapter.DefaultVideoControllerAdapter;
import com.pine.player.widget.view.PineProgressBar;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PinePluginViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.text.NumberFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by tanghongfeng on 2017/8/16.
 */

public class PineMediaController extends RelativeLayout
        implements PineMediaWidget.IPineMediaController, GestureDetector.OnGestureListener {
    private final static String TAG = LogUtil.makeLogTag(PineMediaController.class);

    private static final int MSG_FADE_OUT = 1;
    private static final int MSG_SHOW_PROGRESS = 2;
    private static final int MSG_BACKGROUND_FADE_OUT = 3;
    private static final int MSG_WAITING_FADE_OUT = 4;
    private static final int MSG_PLUGIN_REFRESH = 5;

    private final static String CONTROLLER_TAG = "Controller_Tag";
    private final Activity mContext;
    private final float INSTANCE_PER_VOLUME = 40.0f;
    private final float INSTANCE_PER_BRIGHTNESS = 2.0f;
    private final float INSTANCE_DEVIATION = 20.0f;
    private String mMediaViewTag;
    private AudioManager mAudioManager;
    private Window mWindow;
    private int mMaxVolumes;
    // 播放器
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    // 控制器适配器
    private AbstractMediaControllerAdapter mAdapter;
    // 播放实体
    private PineMediaPlayerBean mMediaBean;
    private IControllersActionListener mControllersActionListener;
    private final View.OnClickListener mNextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onNextBtnClick(v, mPlayer)) {

            }
        }
    };
    private final View.OnClickListener mPrevListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onPreBtnClick(v, mPlayer)) {

            }
        }
    };
    private ControllerMonitor mControllerMonitor;
    private int mPreFadeOutTime;
    // 播放控制器自动隐藏时间
    private int mFadeOutTime = PineConstants.DEFAULT_SHOW_TIMEOUT;
    // 进度条是否正在拖动
    private boolean mDragging;
    // 控制器是否以getControllerView方式被内置在Media View上
    // （如果不是，说明控制器是完全由使用者布局的，
    // 需要使用者通过继承ControllerMonitor自行控制其显示需求）
    private boolean mControllerContainerInRoot;
    private boolean mIsFirstAttach = true;
    private boolean mUseFastForward;
    // 控制器锁是否锁定状态
    private boolean mIsControllerLocked;
    // 在第一次绘制MediaList之前需要调整它的布局属性以适应Controller布局。
    // 设置此变量是为了防止每次绘制之前重复去调整布局
    private boolean mIsNeedResizeRightContainerView;
    private boolean mIsNeedResizeControllerPluginView, mIsNeedResizeSurfacePluginView;
    private PineBackgroundViewHolder mBackgroundViewHolder;
    // 右侧View容器
    private RelativeLayout mRightViewContainer;
    // 插件View容器
    private RelativeLayout mPluginViewContainer;
    // 与播放器控件宽高匹配的插件容器view，由插件的containerType决定
    private RelativeLayout mControllerPluginViewContainer;
    // 仅与播放内容（SurfaceView）宽高匹配的插件容器view，由插件的containerType决定
    private RelativeLayout mSurfacePluginViewContainer;
    private List<PineRightViewHolder> mRightViewHolderList;
    private HashMap<Integer, PinePluginViewHolder> mPluginViewHolderMap;
    private PineControllerViewHolder mControllerViewHolder;
    private final View.OnClickListener mSpeedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onSpeedBtnClick(v, mPlayer)) {
                float speed = mPlayer.getSpeed() + 1.0f;
                if (speed >= 4.0f) {
                    speed = 0.5f;
                } else if (speed == 1.5f) {
                    speed = 1.0f;
                }
                mPlayer.setSpeed(speed);
                updateSpeedButton();
            }
        }
    };
    private final View.OnClickListener mVolumesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onVolumesBtnClick(v, mPlayer)) {
                mControllerViewHolder.getVolumesButton().setSelected(
                        !mControllerViewHolder.getVolumesButton().isSelected());
            }
        }
    };
    private PineWaitingProgressViewHolder mWaitingProgressViewHolder;
    private PineMediaPlayerView.PineMediaViewLayout mAdaptionControllerLayout =
            new PineMediaPlayerView.PineMediaViewLayout();
    // Controller控件的当前父布局PineMediaPlayerView
    private PineMediaPlayerView mMediaPlayerView;
    private final View.OnClickListener mGoBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onGoBackBtnClick(v, mPlayer,
                    mMediaPlayerView.isFullScreenMode())) {
            }
        }
    };
    private final View.OnClickListener mFullScreenListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onFullScreenBtnClick(v, mPlayer)) {
                mMediaPlayerView.toggleFullScreenMode(mIsControllerLocked);
            }
        }
    };
    ViewTreeObserver.OnPreDrawListener mRightViewContainerPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {

        @Override
        public boolean onPreDraw() {
            // 在第一次绘制MediaList之前需要调整它的布局属性以适应Controller布局。
            if (mRightViewContainer != null
                    && mMediaPlayerView.isFullScreenMode() && mIsNeedResizeRightContainerView) {
                int topMargin = 0;
                int bottomMargin = 0;
                if (mControllerViewHolder.getTopControllerView() != null) {
                    topMargin = mControllerViewHolder
                            .getTopControllerView().getHeight();
                }
                if (mControllerViewHolder.getBottomControllerView() != null) {
                    bottomMargin = mControllerViewHolder
                            .getBottomControllerView().getHeight();
                }
                RelativeLayout.LayoutParams oldLayoutParams = (RelativeLayout.LayoutParams)
                        mRightViewContainer.getLayoutParams();
                if (oldLayoutParams.topMargin == topMargin
                        && oldLayoutParams.bottomMargin == bottomMargin) {
                    mIsNeedResizeRightContainerView = false;
                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.topMargin = topMargin;
                    layoutParams.bottomMargin = bottomMargin;
                    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                    mRightViewContainer.setLayoutParams(layoutParams);
                }
            }
            return true;
        }
    };
    ViewTreeObserver.OnPreDrawListener mControllerPluginPreDrawListener =
            new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    // 在绘制SubtitleView之前需要调整它的布局属性以适应Controller布局。
                    if (mControllerPluginViewContainer != null && mIsNeedResizeControllerPluginView) {
                        int topMargin = 0, bottomMargin = 0;
                        if (mControllerViewHolder.getTopControllerView() != null && isShowing()) {
                            topMargin = mControllerViewHolder.getTopControllerView().getHeight();
                        }
                        if (mControllerViewHolder.getBottomControllerView() != null && isShowing()) {
                            bottomMargin = mControllerViewHolder.getBottomControllerView().getHeight();
                        }
                        RelativeLayout.LayoutParams oldLayoutParams = (RelativeLayout.LayoutParams)
                                mControllerPluginViewContainer.getLayoutParams();
                        if (oldLayoutParams != null && oldLayoutParams.topMargin == topMargin &&
                                oldLayoutParams.bottomMargin == bottomMargin) {
                            mIsNeedResizeControllerPluginView = false;
                        } else {
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            layoutParams.topMargin = topMargin;
                            layoutParams.bottomMargin = bottomMargin;
                            layoutParams.addRule(CENTER_IN_PARENT);
                            mControllerPluginViewContainer.setLayoutParams(layoutParams);
                        }
                    }
                    return true;
                }
            };
    ViewTreeObserver.OnPreDrawListener mSurfacePluginPreDrawListener =
            new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    // 在绘制SubtitleView之前需要调整它的布局属性以适应Controller布局。
                    if (mSurfacePluginViewContainer != null && mIsNeedResizeSurfacePluginView) {
                        PineMediaPlayerView.PineMediaViewLayout playerLayoutParams = null;
                        if (mMediaBean.getMediaType() == PineMediaPlayerBean.MEDIA_TYPE_VIDEO) {
                            playerLayoutParams = mPlayer.getMediaAdaptionLayout();
                        } else {
                            playerLayoutParams = mAdaptionControllerLayout;
                        }
                        if (playerLayoutParams == null) {
                            return false;
                        }
                        int topMargin = -1, bottomMargin = -1;
                        if (mControllerViewHolder.getTopControllerView() != null) {
                            int bBottom = mControllerViewHolder.getTopControllerView().getBottom();
                            if (isShowing() && bBottom > playerLayoutParams.top && !mIsControllerLocked) {
                                topMargin = bBottom;
                            } else {
                                topMargin = playerLayoutParams.top;
                            }
                        }
                        if (mControllerViewHolder.getBottomControllerView() != null) {
                            int bTop = mControllerViewHolder.getBottomControllerView().getTop();
                            if (isShowing() && bTop < playerLayoutParams.bottom && !mIsControllerLocked) {
                                bottomMargin = getMeasuredHeight() - bTop;
                            } else {
                                bottomMargin = getMeasuredHeight() - playerLayoutParams.bottom;
                            }
                        }
                        RelativeLayout.LayoutParams oldLayoutParams = (RelativeLayout.LayoutParams)
                                mSurfacePluginViewContainer.getLayoutParams();
                        if (oldLayoutParams != null && oldLayoutParams.topMargin == topMargin &&
                                oldLayoutParams.bottomMargin == bottomMargin &&
                                oldLayoutParams.width == playerLayoutParams.width &&
                                oldLayoutParams.height == playerLayoutParams.height) {
                            mIsNeedResizeSurfacePluginView = false;
                        } else {
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    playerLayoutParams.width, playerLayoutParams.height);
                            if (topMargin != -1) {
                                layoutParams.topMargin = topMargin;
                            }
                            if (bottomMargin != -1) {
                                layoutParams.bottomMargin = bottomMargin;
                            }
                            layoutParams.addRule(CENTER_IN_PARENT);
                            mSurfacePluginViewContainer.setLayoutParams(layoutParams);
                        }
                    }
                    return true;
                }
            };
    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(MSG_SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            if (duration < 0) {
                return;
            }
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
            updateCurrentTimeText((int) newPosition);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlayButton();

            show(PineConstants.DEFAULT_SHOW_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
        }
    };
    private final PineProgressBar.OnProgressBarChangeListener mCustomProgressListener =
            new PineProgressBar.OnProgressBarChangeListener() {
                @Override
                public void onStartTrackingTouch(PineProgressBar bar) {
                    show(3600000);
                    mDragging = true;
                    mHandler.removeMessages(MSG_SHOW_PROGRESS);
                }

                @Override
                public void onProgressChanged(PineProgressBar bar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        return;
                    }

                    long duration = mPlayer.getDuration();
                    if (duration < 0) {
                        return;
                    }
                    long newPosition = (duration * progress) / 1000L;
                    mPlayer.seekTo((int) newPosition);
                    updateCurrentTimeText((int) newPosition);
                }

                @Override
                public void onStopTrackingTouch(PineProgressBar bar) {
                    mDragging = false;
                    setProgress();
                    updatePausePlayButton();

                    show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                    mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
                }
            };
    private final View.OnClickListener mPausePlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onPlayPauseBtnClick(v, mPlayer)) {
                doPauseResume();
                show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                updatePausePlayButton();
            }
        }
    };
    private final View.OnClickListener mRewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onFastBackwardBtnClick(v, mPlayer)) {
                int pos = mPlayer.getCurrentPosition();
                pos -= 5000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();
                show(PineConstants.DEFAULT_SHOW_TIMEOUT);
            }
        }
    };
    private final View.OnClickListener mFfwdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onFastForwardBtnClick(v, mPlayer)) {
                int pos = mPlayer.getCurrentPosition();
                pos += 15000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();
                show(PineConstants.DEFAULT_SHOW_TIMEOUT);
            }
        }
    };
    private final View.OnClickListener mRightControlBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            List<View> rightControllerBtnList = mControllerViewHolder.getRightViewControlBtnList();
            if (rightControllerBtnList == null || mRightViewHolderList == null ||
                    rightControllerBtnList.size() != mRightViewHolderList.size()) {
                return;
            }
            if (mControllersActionListener == null
                    || !mControllersActionListener.onRightViewControlBtnClick(view,
                    rightControllerBtnList, mRightViewHolderList, mPlayer)) {
                int visibilityIndex = -1;
                for (int i = 0; i < rightControllerBtnList.size(); i++) {
                    if (!view.equals(rightControllerBtnList.get(i))) {
                        rightControllerBtnList.get(i).setSelected(false);
                        mRightViewHolderList.get(i).getContainer().setVisibility(GONE);
                    } else {
                        visibilityIndex = i;
                    }
                }
                boolean isCurViewLastSelected = view.isSelected();
                mRightViewContainer.setVisibility(isCurViewLastSelected ? GONE : VISIBLE);
                if (visibilityIndex > -1) {
                    view.setSelected(!isCurViewLastSelected);
                    mRightViewHolderList.get(visibilityIndex).getContainer().setVisibility(isCurViewLastSelected ? GONE : VISIBLE);
                }
                show(!isCurViewLastSelected || !mPlayer.isInPlaybackState() ? 0 :
                        PineConstants.DEFAULT_SHOW_TIMEOUT, true);
            }
        }
    };
    private final View.OnClickListener mLockControllerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersActionListener == null
                    || !mControllersActionListener.onLockControllerBtnClick(v,
                    PineMediaController.this, mPlayer)) {
                mControllerViewHolder.getLockControllerButton().setSelected(
                        !mControllerViewHolder.getLockControllerButton().isSelected());
                mIsControllerLocked = !mIsControllerLocked;
                hide();
                if (!isLocked()) {
                    show();
                }
                judgeAndChangeRequestedOrientation();
            }
        }
    };
    // Controller控件本身
    private View mRoot;
    private GestureDetector mGestureDetector;
    private HashMap<Integer, IPinePlayerPlugin> mPinePluginMap;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                // 控制器自动隐藏消息
                case MSG_FADE_OUT:
                    hide();
                    break;
                // 进度条更新消息
                case MSG_SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && (isProgressNeeded() || isShowing()) &&
                            (mPlayer.isPlaying() || mPlayer.isPause())) {
                        msg = obtainMessage(MSG_SHOW_PROGRESS);
                        int sum = (int) mPlayer.getSpeed();
                        sum = sum < 1 ? 1 : sum;
                        sendMessageDelayed(msg, (1000 - (pos % 1000)) / sum);
                    }
                    break;
                // 背景延迟隐藏消失
                case MSG_BACKGROUND_FADE_OUT:
                    if (mBackgroundViewHolder.getContainer() != null) {
                        mBackgroundViewHolder.getContainer().setVisibility(GONE);
                    }
                    break;
                // 加载等待界面延迟隐藏消失
                case MSG_WAITING_FADE_OUT:
                    if (mWaitingProgressViewHolder.getContainer() != null) {
                        mWaitingProgressViewHolder.getContainer().setVisibility(GONE);
                    }
                    setControllerEnabled(true);
                    break;
                // 每PLUGIN_REFRESH_TIME_DELAY毫秒刷新一次插件View
                case MSG_PLUGIN_REFRESH:
                    Iterator iterator = mPinePluginMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        ((IPinePlayerPlugin) entry.getValue()).onTime(mPlayer.getCurrentPosition());
                    }
                    if (mPlayer.isPlaying() && !mHandler.hasMessages(MSG_PLUGIN_REFRESH)) {
                        msg = obtainMessage(MSG_PLUGIN_REFRESH);
                        sendMessageDelayed(msg, PineConstants.PLUGIN_REFRESH_TIME_DELAY);
                    }
                    break;
            }
        }
    };
    private boolean mPausedByBufferingUpdate;
    private boolean mDraggingX, mDraggingY, mStartDragging;
    private int mStartVolumeByDragging;
    private int mStartBrightnessByDragging;
    private float mPreX, mPreY;

    public PineMediaController(Activity context) {
        this(context, true);
    }

    public PineMediaController(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mWindow = mContext.getWindow();
        mMaxVolumes = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(context, this);
    }

    public PineMediaController(Activity context, boolean useFastForward) {
        super(context);
        mRoot = this;
        mRoot.setTag(CONTROLLER_TAG);
        mContext = context;
        mUseFastForward = useFastForward;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mWindow = mContext.getWindow();
        mMaxVolumes = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(context, this);
    }

    private boolean isProgressNeeded() {
        if (mMediaPlayerView == null) {
            return false;
        }
        return mControllerViewHolder.getPlayProgressBar() != null
                && mControllerViewHolder.getPlayProgressBar().isShown() ||
                mControllerViewHolder.getCustomProgressBar() != null
                        && mControllerViewHolder.getCustomProgressBar().isShown() ||
                mControllerViewHolder.getCurrentTimeText() != null
                        && mControllerViewHolder.getCurrentTimeText().isShown();
    }

    private void addPreDrawListener(View view, ViewTreeObserver.OnPreDrawListener listener) {
        view.getViewTreeObserver().removeOnPreDrawListener(listener);
        view.getViewTreeObserver().addOnPreDrawListener(listener);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setMediaControllerAdapter(AbstractMediaControllerAdapter adapter) {
        mAdapter = adapter;
    }

    public View getMediaPlayerView() {
        return mMediaPlayerView;
    }

    @Override
    public void setMediaPlayerView(PineMediaPlayerView playerView) {
        if (playerView != null && playerView instanceof RelativeLayout) {
            mMediaPlayerView = playerView;
        } else {
            mMediaPlayerView = null;
        }
        removeAllViews();
    }

    public PineMediaWidget.IPineMediaPlayer getPlayer() {
        return mPlayer;
    }

    public void setFadeOutTime(int time) {
        mFadeOutTime = time;
    }

    private int getCurVolumes() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取系统当前亮度
     *
     * @return
     */
    private int getSystemBrightness() {
        int brightValue = 0;
        try {
            brightValue = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }

    /**
     * 获取应用当前亮度
     *
     * @return 0.0（暗）～1.0（亮）
     */
    private float getAppBrightness() {
        WindowManager.LayoutParams layoutParams = mWindow.getAttributes();
        return layoutParams.screenBrightness;
    }

    /**
     * @param brightnessValue 0（暗）～255（亮）(screenBrightness = -1.0f表示恢复为系统亮度)
     */
    private void setAppBrightness(int brightnessValue) {
        WindowManager.LayoutParams layoutParams = mWindow.getAttributes();
        layoutParams.screenBrightness = (brightnessValue < 0 ? -1.0f : brightnessValue / 255f);
        mWindow.setAttributes(layoutParams);
    }

    /**
     * ----------------   IPineMediaController begin   --------------------
     **/

    @Override
    public void setMediaPlayer(PineMediaWidget.IPineMediaPlayer player) {
        mPlayer = player;
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, String mediaViewTag) {
        mMediaBean = pineMediaPlayerBean;
        mMediaViewTag = mediaViewTag;
    }

    /**
     * 将PineMediaController各个部分挂载到PineMediaPlayerView中
     *
     * @param isPlayerReset 本此attach是否重置了MediaPlayer
     * @param isResumeState 本此attach是否是为了恢复状态
     */
    @Override
    public void attachToParentView(boolean isPlayerReset, boolean isResumeState) {
        LogUtil.d(TAG, "Attach to media view. isPlayerReset: " + isPlayerReset
                + ", isResumeState: " + isResumeState + ", mMediaPlayerView:" + mMediaPlayerView);
        if (mMediaPlayerView == null) {
            return;
        }
        removeAllViews();
        if (mAdapter == null) {
            mAdapter = new DefaultVideoControllerAdapter(mContext);
        }
        if (!mAdapter.init(mPlayer)) {
            return;
        }
        mControllerMonitor = mAdapter.onCreateControllerMonitor();
        mControllersActionListener = mAdapter.onCreateControllersActionListener();
        mBackgroundViewHolder
                = mAdapter.onCreateBackgroundViewHolder(mPlayer, mMediaPlayerView.isFullScreenMode());
        releasePlugin();
        mPinePluginMap = mMediaBean.getPlayerPluginMap();
        if (needInitPlugin()) {
            LogUtil.d(TAG, "construct plugin view holder map");
            mPluginViewHolderMap = new HashMap<Integer, PinePluginViewHolder>();
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                PinePluginViewHolder pinePluginViewHolder = pinePlayerPlugin.createViewHolder(mContext,
                        mMediaPlayerView.isFullScreenMode());
                pinePluginViewHolder.setContainerType(pinePlayerPlugin.getContainerType());
                mPluginViewHolderMap.put((Integer) entry.getKey(), pinePluginViewHolder);
            }
        }
        mControllerViewHolder = mAdapter
                .onCreateOutRootControllerViewHolder(mPlayer, mMediaPlayerView.isFullScreenMode());
        if (mControllerViewHolder == null) {
            mControllerContainerInRoot = true;
            mControllerViewHolder = mAdapter
                    .onCreateInRootControllerViewHolder(mPlayer, mMediaPlayerView.isFullScreenMode());
        } else {
            mControllerContainerInRoot = false;
        }
        mWaitingProgressViewHolder = mAdapter
                .onCreateWaitingProgressViewHolder(mPlayer, mMediaPlayerView.isFullScreenMode());
        mRightViewHolderList = mAdapter
                .onCreateRightViewHolderList(mPlayer, mMediaPlayerView.isFullScreenMode());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        // 背景图View
        if (mBackgroundViewHolder != null && mBackgroundViewHolder.getContainer() != null) {
            addView(mBackgroundViewHolder.getContainer(), layoutParams);
            mBackgroundViewHolder.getContainer().setVisibility(VISIBLE);
        } else {
            mBackgroundViewHolder = new PineBackgroundViewHolder();
        }
        // 插件View
        if (mPluginViewHolderMap != null) {
            mPluginViewContainer = new RelativeLayout(getContext());
            mControllerPluginViewContainer = new RelativeLayout(getContext());
            mSurfacePluginViewContainer = new RelativeLayout(getContext());
            Iterator iterator = mPluginViewHolderMap.entrySet().iterator();
            PinePluginViewHolder pinePluginViewHolder = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePluginViewHolder = (PinePluginViewHolder) entry.getValue();
                if (pinePluginViewHolder.getContainer() != null) {
                    if (pinePluginViewHolder.getContainerType()
                            == IPinePlayerPlugin.TYPE_MATCH_SURFACE) {
                        mSurfacePluginViewContainer.addView(pinePluginViewHolder.getContainer(),
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    } else {
                        mControllerPluginViewContainer.addView(pinePluginViewHolder.getContainer(),
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    }
                }
            }
            if (mSurfacePluginViewContainer.getChildCount() > 0) {
                LogUtil.d(TAG, "attach surface plugin container");
                if (mControllerContainerInRoot) {
                    mIsNeedResizeSurfacePluginView = true;
                    addPreDrawListener(mSurfacePluginViewContainer, mSurfacePluginPreDrawListener);
                }
                mPluginViewContainer.addView(mSurfacePluginViewContainer,
                        new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            if (mControllerPluginViewContainer.getChildCount() > 0) {
                LogUtil.d(TAG, "attach controller plugin container");
                if (mControllerContainerInRoot) {
                    mIsNeedResizeControllerPluginView = true;
                    addPreDrawListener(mControllerPluginViewContainer, mControllerPluginPreDrawListener);
                }
                mPluginViewContainer.addView(mControllerPluginViewContainer,
                        new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            LogUtil.d(TAG, "attach all plugin containers root");
            addView(mPluginViewContainer, new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        // 控制器内置View
        if (mControllerViewHolder != null && mControllerViewHolder.getContainer() != null) {
            if (mControllerContainerInRoot) {
                addView(mControllerViewHolder.getContainer(), layoutParams);
            }
        } else {
            mControllerViewHolder = new PineControllerViewHolder();
        }
        // 加载等待View
        if (mWaitingProgressViewHolder != null && mWaitingProgressViewHolder.getContainer() != null) {
            addView(mWaitingProgressViewHolder.getContainer(), layoutParams);
            mWaitingProgressViewHolder.getContainer().setVisibility(VISIBLE);
        } else {
            mWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
        }
        // 内置右侧Views
        if (mRightViewHolderList != null && mRightViewHolderList.size() > 0) {
            mRightViewContainer = new RelativeLayout(getContext());
            for (int i = 0; i < mRightViewHolderList.size(); i++) {
                PineRightViewHolder pineRightViewHolder =
                        mRightViewHolderList.get(i);
                if (pineRightViewHolder.getContainer() != null) {
                    mRightViewContainer.addView(pineRightViewHolder.getContainer(),
                            new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                }
            }
            if (mControllerContainerInRoot) {
                mIsNeedResizeRightContainerView = true;
                addPreDrawListener(mRightViewContainer, mRightViewContainerPreDrawListener);
            }
            addView(mRightViewContainer, new RelativeLayout.LayoutParams(0, 0));
        }
        if (mIsFirstAttach) {
            mMediaPlayerView.addView(mRoot, layoutParams);
        }
        initControllerView(isPlayerReset, isResumeState);
        mIsFirstAttach = false;

        if (needInitPlugin()) {
            LogUtil.d(TAG, "init plugin mPlayer:" + mPlayer);
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onInit(mContext, mPlayer, this, isPlayerReset, isResumeState);
            }
        }
    }

    private void initControllerView(boolean isPlayerReset, boolean isResumeState) {
        LogUtil.d(TAG, "initControllerView");
        setControllerEnabled(false);
        if (mControllerViewHolder.getGoBackButton() != null) {
            mControllerViewHolder.getGoBackButton().setOnClickListener(mGoBackListener);
            mControllerViewHolder.getGoBackButton().setVisibility(View.VISIBLE);
        }
        List<View> rightViewControlBtnList = mControllerViewHolder.getRightViewControlBtnList();
        if (rightViewControlBtnList != null && rightViewControlBtnList.size() > 0) {
            for (int i = 0; i < rightViewControlBtnList.size(); i++) {
                rightViewControlBtnList.get(i).setOnClickListener(mRightControlBtnListener);
                rightViewControlBtnList.get(i).setVisibility(View.VISIBLE);
            }
            mRightViewContainer.setVisibility(VISIBLE);
        }
        if (mControllerViewHolder.getLockControllerButton() != null) {
            mControllerViewHolder.getLockControllerButton().setOnClickListener(mLockControllerListener);
            mControllerViewHolder.getLockControllerButton().setSelected(mIsControllerLocked);
            mControllerViewHolder.getLockControllerButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getFullScreenButton() != null) {
            mControllerViewHolder.getFullScreenButton().setOnClickListener(mFullScreenListener);
            mControllerViewHolder.getFullScreenButton().setVisibility(View.VISIBLE);
        }
        show(0);
        if (!isPlayerReset && isResumeState) {
            LogUtil.d(TAG, "resume media controller");
            onMediaPlayerPrepared();
            onMediaPlayerStart();
        }
    }

    private void installClickListeners() {
        if (mControllerViewHolder.getSpeedButton() != null) {
            mControllerViewHolder.getSpeedButton().requestFocus();
            mControllerViewHolder.getSpeedButton().setOnClickListener(mSpeedListener);
            mControllerViewHolder.getSpeedButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().requestFocus();
            mControllerViewHolder.getPausePlayButton().setOnClickListener(mPausePlayListener);
            mControllerViewHolder.getPausePlayButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getPlayProgressBar() != null) {
            if (mControllerViewHolder.getPlayProgressBar() instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mControllerViewHolder.getPlayProgressBar();
                seeker.setOnSeekBarChangeListener(mSeekListener);
                mControllerViewHolder.getPlayProgressBar().setVisibility(View.VISIBLE);
            }
            mControllerViewHolder.getPlayProgressBar().setMax(1000);
        }
        if (mControllerViewHolder.getCustomProgressBar() != null) {
            if (mControllerViewHolder.getCustomProgressBar() instanceof PineProgressBar) {
                PineProgressBar progressBar = mControllerViewHolder.getCustomProgressBar();
                progressBar.setOnProgressBarChangeListener(mCustomProgressListener);
                mControllerViewHolder.getCustomProgressBar().setVisibility(View.VISIBLE);
            }
            mControllerViewHolder.getCustomProgressBar().setMax(1000);
        }
        if (mControllerViewHolder.getFastForwardButton() != null) {
            mControllerViewHolder.getFastForwardButton().setOnClickListener(mFfwdListener);
            mControllerViewHolder.getFastForwardButton().setVisibility(
                    mUseFastForward ? View.VISIBLE : View.GONE);
        }
        if (mControllerViewHolder.getFastBackwardButton() != null) {
            mControllerViewHolder.getFastBackwardButton().setOnClickListener(mRewListener);
            mControllerViewHolder.getFastBackwardButton().setVisibility(
                    mUseFastForward ? View.VISIBLE : View.GONE);
        }
        if (mControllerViewHolder.getNextButton() != null) {
            mControllerViewHolder.getNextButton().setOnClickListener(mNextListener);
            mControllerViewHolder.getNextButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getPrevButton() != null) {
            mControllerViewHolder.getPrevButton().setOnClickListener(mPrevListener);
            mControllerViewHolder.getPrevButton().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void doPauseResume() {
        if (mMediaPlayerView == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlayButton();
    }

    @Override
    public void toggleMediaControlsVisibility() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    @Override
    public void show() {
        show(mFadeOutTime);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    @Override
    public void show(int timeout) {
        show(timeout, false);
    }

    private void show(int timeout, boolean refreshAnyway) {
        if (mMediaPlayerView == null || mControllerViewHolder == null
                || mControllerViewHolder.getContainer() == null) {
            return;
        }
        if (!(mControllerViewHolder.getContainer().getVisibility() == VISIBLE) ||
                timeout != mPreFadeOutTime || refreshAnyway) {
            LogUtil.d(TAG, "show timeout: " + timeout);
            mPreFadeOutTime = timeout;
            mIsNeedResizeControllerPluginView = true;
            mIsNeedResizeSurfacePluginView = true;
            setProgress();
            if (mControllerViewHolder.getPausePlayButton() != null) {
                mControllerViewHolder.getPausePlayButton().requestFocus();
            }
            disableUnsupportedButtons();
            mControllerViewHolder.getContainer().setVisibility(VISIBLE);
            updateControllerVisibility(true);
            updateVolumesText(getCurVolumes(), mMaxVolumes);
            updatePausePlayButton();
        }

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.removeMessages(MSG_SHOW_PROGRESS);
        mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
        mHandler.removeMessages(MSG_FADE_OUT);
        if (timeout > 0 && mPlayer.getSurfaceView() != null && mControllerContainerInRoot) {
            Message msg = mHandler.obtainMessage(MSG_FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        if (mMediaPlayerView == null || mControllerViewHolder == null
                || mControllerViewHolder.getContainer() == null ||
                mPlayer == null || mPlayer.getSurfaceView() == null ||
                !mControllerContainerInRoot) {
            return;
        }
        if (mControllerViewHolder.getContainer().getVisibility() == VISIBLE) {
            LogUtil.d(TAG, "hide");
            try {
                mIsNeedResizeControllerPluginView = true;
                mIsNeedResizeSurfacePluginView = true;
                if (!isProgressNeeded()) {
                    mHandler.removeMessages(MSG_SHOW_PROGRESS);
                }
                if (mControllerContainerInRoot) {
                    mControllerViewHolder.getContainer().setVisibility(GONE);
                }
                updateControllerVisibility(false);
            } catch (IllegalArgumentException ex) {
                LogUtil.w("MediaController", "already removed");
            }
        }
    }

    @Override
    public void onMediaPlayerStart() {
        if (mMediaPlayerView == null) {
            return;
        }
        updatePausePlayButton();
        if (needActivePlugin()) {
            // 启动插件刷新
            if (!mHandler.hasMessages(MSG_PLUGIN_REFRESH)) {
                mHandler.sendEmptyMessage(MSG_PLUGIN_REFRESH);
            }
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerStart();
            }
        }
        if (isShowing()) {
            mHandler.removeMessages(MSG_SHOW_PROGRESS);
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
        }
    }

    @Override
    public void onMediaPlayerPause() {
        if (mMediaPlayerView == null) {
            return;
        }
        updatePausePlayButton();
        mHandler.removeMessages(MSG_PLUGIN_REFRESH);
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerPause();
            }
        }
    }

    @Override
    public void onMediaPlayerPrepared() {
        if (mMediaPlayerView == null) {
            return;
        }
        mIsNeedResizeControllerPluginView = true;
        mIsNeedResizeSurfacePluginView = true;
        mIsNeedResizeRightContainerView = true;
        // 设置设备方向
        judgeAndChangeRequestedOrientation();
        updateMediaNameText(mPlayer.getMediaPlayerBean());
        updateSpeedButton();
        updatePausePlayButton();
        installClickListeners();
        setControllerEnabled(true);
        if (mPluginViewContainer != null) {
            mPluginViewContainer.setVisibility(VISIBLE);
        }
        show();
        mHandler.sendEmptyMessageDelayed(MSG_WAITING_FADE_OUT, 200);
        if (mMediaBean.getMediaType() == PineMediaPlayerBean.MEDIA_TYPE_VIDEO) {
            mHandler.sendEmptyMessageDelayed(MSG_BACKGROUND_FADE_OUT, 200);
        }
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerPrepared();
            }
        }
    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerInfo(what, extra);
            }
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        float position = (float) mPlayer.getCurrentPosition();
        float duration = (float) mPlayer.getDuration();
        LogUtil.v(TAG, "onBufferingUpdate percent: " + percent + ", duration:" + duration
                + ", position:" + position);
        if (mPlayer.getMediaPlayerState() == PineMediaPlayerComponent.STATE_PLAYBACK_COMPLETED
                || position >= duration) {
            return;
        }
        if (position > 0 && duration > 0) {
            float per = position * 100 / duration;
            if (per > (float) percent) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mPausedByBufferingUpdate = true;
                    if (mWaitingProgressViewHolder.getContainer() != null) {
                        mWaitingProgressViewHolder.getContainer().setVisibility(VISIBLE);
                    }
                    show();
                }
            } else {
                if (mPlayer.isPause() && mPausedByBufferingUpdate) {
                    mPlayer.start();
                    mPausedByBufferingUpdate = false;
//                    hide();
                    mHandler.sendEmptyMessageDelayed(MSG_WAITING_FADE_OUT, 50);
                }

            }
        }
    }

    @Override
    public void onMediaPlayerComplete() {
        if (mMediaPlayerView == null) {
            return;
        }
        show(0);
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerComplete();
            }
        }
    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {
        if (mMediaPlayerView == null) {
            return;
        }
        if (mWaitingProgressViewHolder.getContainer() != null) {
            mWaitingProgressViewHolder.getContainer().setVisibility(GONE);
        }
        if (mPluginViewContainer != null) {
            mPluginViewContainer.setVisibility(GONE);
        }
        if (mControllerViewHolder != null) {
            setControllerEnabled(false, false, false, false, true, false, false, false, false, false);
            show(0);
        }
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onMediaPlayerError(framework_err, impl_err);
            }
        }
    }

    @Override
    public void onAbnormalComplete() {
        show(0);
        if (needActivePlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onAbnormalComplete();
            }
        }
    }

    @Override
    public void onMediaPlayerRelease(boolean clearTargetState) {
        releasePlugin();
        updatePausePlayButton();
    }

    @Override
    public void updateVolumesText() {
        if (mMediaPlayerView == null) {
            return;
        }
        updateVolumesText(getCurVolumes(), mMaxVolumes);
    }

    @Override
    public void pausePlayBtnRequestFocus() {
        if (mMediaPlayerView == null) {
            return;
        }
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().requestFocus();
        }
    }

    @Override
    public boolean isShowing() {
        if (mMediaPlayerView == null) {
            return false;
        }
        return mControllerViewHolder.getContainer() != null
                && mControllerViewHolder.getContainer().getVisibility() == VISIBLE;
    }

    @Override
    public boolean isLocked() {
        return mIsControllerLocked;
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (duration < 0) {
            return position;
        }
        if (mPlayer.getMediaPlayerState() == PineMediaPlayerComponent.STATE_PLAYBACK_COMPLETED
                || position > duration) {
            position = duration;
        }
        if (mControllerViewHolder.getPlayProgressBar() != null) {
            long max = mControllerViewHolder.getPlayProgressBar().getMax();
            if (duration > 0) {
                // use long to avoid overflow
                long pos = max * position / duration;
                mControllerViewHolder.getPlayProgressBar().setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mControllerViewHolder.getPlayProgressBar().setSecondaryProgress(percent * (int) max / 100);
        }
        if (mControllerViewHolder.getCustomProgressBar() != null) {
            long max = mControllerViewHolder.getCustomProgressBar().getMax();
            if (duration > 0) {
                // use long to avoid overflow
                long pos = max * position / duration;
                mControllerViewHolder.getCustomProgressBar().setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mControllerViewHolder.getCustomProgressBar().setSecondaryProgress(percent * (int) max);
        }
        updateEndTimeText(duration);
        updateCurrentTimeText(position);

        return position;
    }

    @Override
    public void setControllerEnabled(boolean enabled) {
        setControllerEnabled(enabled, true, enabled, enabled, enabled, enabled, enabled, enabled,
                enabled, enabled);
    }

    @Override
    public void setControllerEnabled(boolean enabledSpeed, boolean enabledRightView,
                                     boolean enabledPlayerPause, boolean enabledProgressBar,
                                     boolean enabledToggleFullScreen, boolean enabledLock,
                                     boolean enabledFastForward, boolean enabledFastBackward,
                                     boolean enabledNext, boolean enabledPrev) {
        if (mMediaPlayerView == null) {
            return;
        }
        LogUtil.d(TAG, "setControllerEnabled enabledSpeed: " + enabledSpeed
                + ", enabledRightView: " + enabledRightView
                + ", enabledPlayerPause: " + enabledPlayerPause
                + ", enabledProgressBar: " + enabledProgressBar
                + ", enabledToggleFullScreen: " + enabledToggleFullScreen
                + ", enabledLock: " + enabledLock
                + ", enabledFastForward: " + enabledFastForward
                + ", enabledFastBackward: " + enabledFastBackward
                + ", enabledNext: " + enabledNext
                + ", enabledPrev: " + enabledPrev);
//        if (mPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            MediaPlayer.TrackInfo[] trackInfoArr = mPlayer.getTrackInfo();
//        }
        if (mControllerViewHolder.getSpeedButton() != null) {
            mControllerViewHolder.getSpeedButton().setEnabled(enabledSpeed);
        }
        List<View> rightViewControlBtnList = mControllerViewHolder.getRightViewControlBtnList();
        if (rightViewControlBtnList != null && rightViewControlBtnList.size() > 0) {
            for (int i = 0; i < rightViewControlBtnList.size(); i++) {
                rightViewControlBtnList.get(i).setEnabled(enabledRightView);
            }
        }
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().setEnabled(enabledPlayerPause);
        }
        if (mControllerViewHolder.getPlayProgressBar() != null) {
            mControllerViewHolder.getPlayProgressBar().setEnabled(enabledProgressBar);
        }
        if (mControllerViewHolder.getCustomProgressBar() != null) {
            mControllerViewHolder.getCustomProgressBar().setEnabled(enabledProgressBar);
        }
        if (mControllerViewHolder.getFullScreenButton() != null) {
            mControllerViewHolder.getFullScreenButton().setEnabled(enabledToggleFullScreen);
        }
        if (mControllerViewHolder.getLockControllerButton() != null) {
            mControllerViewHolder.getLockControllerButton().setEnabled(enabledLock);
        }
        if (mControllerViewHolder.getFastForwardButton() != null) {
            mControllerViewHolder.getFastForwardButton().setEnabled(enabledFastForward);
        }
        if (mControllerViewHolder.getFastBackwardButton() != null) {
            mControllerViewHolder.getFastBackwardButton().setEnabled(enabledFastBackward);
        }
        if (mControllerViewHolder.getNextButton() != null) {
            mControllerViewHolder.getNextButton().setEnabled(enabledNext);
        }
        if (mControllerViewHolder.getPrevButton() != null) {
            mControllerViewHolder.getPrevButton().setEnabled(enabledPrev);
        }
        disableUnsupportedButtons();
        setEnabled(enabledPlayerPause || enabledProgressBar || enabledToggleFullScreen ||
                enabledLock || enabledFastForward || enabledFastBackward ||
                enabledNext || enabledPrev);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control io to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mControllerViewHolder.getPausePlayButton() != null && !mPlayer.canPause()) {
                mControllerViewHolder.getPausePlayButton().setEnabled(false);
            }
            if (mControllerViewHolder.getFastForwardButton() != null && !mPlayer.canSeekForward()) {
                mControllerViewHolder.getFastForwardButton().setEnabled(false);
            }
            if (mControllerViewHolder.getFastBackwardButton() != null && !mPlayer.canSeekBackward()) {
                mControllerViewHolder.getFastBackwardButton().setEnabled(false);
            }
            // TODO What we really should do is add a canSeek to the MediaPlayerControl io;
            // this scheme can break the case when applications want to allow seek through the
            // progress bar but disable forward/backward buttons.
            //
            // However, currently the flags SEEK_BACKWARD_AVAILABLE, SEEK_FORWARD_AVAILABLE,
            // and SEEK_AVAILABLE are all (un)set together; as such the aforementioned issue
            // shouldn't arise in existing applications.
            if (!mPlayer.canSeekBackward() && !mPlayer.canSeekForward()) {
                if (mControllerViewHolder.getPlayProgressBar() != null) {
                    mControllerViewHolder.getPlayProgressBar().setEnabled(false);
                }
                if (mControllerViewHolder.getCustomProgressBar() != null) {
                    mControllerViewHolder.getCustomProgressBar().setEnabled(false);
                }
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the io, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * ----------------   IPineMediaController end   --------------------
     **/

    public void removeAllViews() {
        if (mRightViewContainer != null) {
            mRightViewContainer.getViewTreeObserver()
                    .removeOnPreDrawListener(mRightViewContainerPreDrawListener);
            mRightViewContainer.removeAllViewsInLayout();
            mRightViewContainer = null;
        }
        if (mControllerPluginViewContainer != null) {
            mControllerPluginViewContainer.getViewTreeObserver()
                    .removeOnPreDrawListener(mSurfacePluginPreDrawListener);
            mControllerPluginViewContainer.removeAllViewsInLayout();
            mControllerPluginViewContainer = null;
        }
        if (mSurfacePluginViewContainer != null) {
            mSurfacePluginViewContainer.getViewTreeObserver()
                    .removeOnPreDrawListener(mSurfacePluginPreDrawListener);
            mSurfacePluginViewContainer.removeAllViewsInLayout();
            mSurfacePluginViewContainer = null;
        }
        if (mPluginViewContainer != null) {
            mPluginViewContainer.removeAllViewsInLayout();
            mPluginViewContainer = null;
        }
        removeAllViewsInLayout();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mAdaptionControllerLayout.width = getMeasuredWidth();
        mAdaptionControllerLayout.height = getMeasuredHeight();
        mAdaptionControllerLayout.left = getLeft();
        mAdaptionControllerLayout.right = getRight();
        mAdaptionControllerLayout.top = getTop();
        mAdaptionControllerLayout.bottom = getBottom();
    }

    /**
     * 更新控制器及其子控件显示状态(默认方式)
     *
     * @param needShow
     */
    private void updateControllerVisibility(boolean needShow) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onControllerVisibilityUpdate(needShow,
                this, mPlayer, mControllerViewHolder)) {
            if (needShow) {
                if (mControllerViewHolder.getTopControllerView() != null) {
                    mControllerViewHolder.getTopControllerView()
                            .setVisibility(mIsControllerLocked ? GONE : VISIBLE);
                }
                if (mControllerViewHolder.getBottomControllerView() != null) {
                    mControllerViewHolder.getBottomControllerView()
                            .setVisibility(mIsControllerLocked ? GONE : VISIBLE);
                }
                List<View> blockControllerViewList = mControllerViewHolder.getBlockControllerViewList();
                if (blockControllerViewList != null) {
                    for (int i = 0; i < blockControllerViewList.size(); i++) {
                        blockControllerViewList.get(i)
                                .setVisibility(mIsControllerLocked ? GONE : VISIBLE);
                    }
                }
                if (mRightViewContainer != null) {
                    boolean isRightViewVisibility = false;
                    if (mRightViewHolderList != null) {
                        for (int i = 0; i < mRightViewHolderList.size(); i++) {
                            if (mRightViewHolderList.get(i).getContainer().getVisibility() == VISIBLE) {
                                isRightViewVisibility = true;
                                break;
                            }
                        }
                    }
                    mRightViewContainer.setVisibility(
                            !mIsControllerLocked && isRightViewVisibility
                                    && mMediaPlayerView.isFullScreenMode() ? View.VISIBLE : View.GONE);
                }
                if (mControllerViewHolder.getRightControllerView() != null) {
                    mControllerViewHolder.getRightControllerView()
                            .setVisibility(mIsControllerLocked ||
                                    mRightViewContainer.getVisibility() == VISIBLE ? GONE : VISIBLE);
                }
                if (mControllerViewHolder.getCenterControllerView() != null) {
                    mControllerViewHolder.getCenterControllerView().setVisibility(View.VISIBLE);
                }
            } else {
                if (mControllerViewHolder.getTopControllerView() != null) {
                    mControllerViewHolder.getTopControllerView().setVisibility(View.GONE);
                }
                if (mControllerViewHolder.getCenterControllerView() != null) {
                    mControllerViewHolder.getCenterControllerView().setVisibility(View.GONE);
                }
                if (mControllerViewHolder.getBottomControllerView() != null) {
                    mControllerViewHolder.getBottomControllerView().setVisibility(View.GONE);
                }
                List<View> blockControllerViewList = mControllerViewHolder.getBlockControllerViewList();
                if (blockControllerViewList != null) {
                    for (int i = 0; i < blockControllerViewList.size(); i++) {
                        blockControllerViewList.get(i)
                                .setVisibility(GONE);
                    }
                }
                if (mRightViewContainer != null) {
                    mRightViewContainer.setVisibility(View.GONE);
                }
                if (mControllerViewHolder.getRightControllerView() != null) {
                    mControllerViewHolder.getRightControllerView().setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 通过综合判断改变设备方向(默认方式)
     */
    private void judgeAndChangeRequestedOrientation() {
        if (mMediaPlayerView == null) {
            return;
        }
        PineMediaPlayerBean pineMediaPlayerBean = mPlayer.getMediaPlayerBean();
        if (pineMediaPlayerBean == null) {
            return;
        }
        int mediaWidth = mPlayer.getMediaViewWidth();
        int mediaHeight = mPlayer.getMediaViewHeight();
        int mediaType = pineMediaPlayerBean.getMediaType();
        if (mControllerMonitor == null
                || !mControllerMonitor.judgeAndChangeRequestedOrientation(mContext,
                this, mPlayer, mediaWidth, mediaHeight, mediaType)) {
            // 根据视频的属性调整其显示的模式
            if (!mMediaPlayerView.isFullScreenMode()) {
                if (((Activity) mContext).getRequestedOrientation()
                        != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    ((Activity) mContext).setRequestedOrientation(
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                return;
            }
            if (mediaWidth > mediaHeight || mediaType == PineMediaPlayerBean.MEDIA_TYPE_AUDIO) {
                if (mIsControllerLocked) {
                    if (((Activity) mContext).getRequestedOrientation()
                            != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        ((Activity) mContext).setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                } else {
                    if (((Activity) mContext).getRequestedOrientation()
                            != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                        ((Activity) mContext).setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }
                }
            } else {
                if (mIsControllerLocked) {
                    if (((Activity) mContext).getRequestedOrientation()
                            != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        ((Activity) mContext).setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } else {
                    if (((Activity) mContext).getRequestedOrientation()
                            != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
                        ((Activity) mContext).setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    }
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mIsNeedResizeControllerPluginView = true;
                mIsNeedResizeSurfacePluginView = true;
                mIsNeedResizeRightContainerView = true;
                requestLayout();
            }
        });
    }

    /**
     * 更新播放/暂停按键显示状态(默认方式)
     */
    private void updateSpeedButton() {
        if (mControllerMonitor == null
                || !mControllerMonitor.onSpeedUpdate(mPlayer, mControllerViewHolder
                .getSpeedButton())) {
            if (mControllerViewHolder.getSpeedButton() == null) {
                return;
            }
            if (mControllerViewHolder.getSpeedButton() instanceof TextView) {
                ((TextView) mControllerViewHolder.getSpeedButton())
                        .setText(String.format("%.1fX", mPlayer.getSpeed()));
            }
        }
    }

    /**
     * 更新播放/暂停按键显示状态(默认方式)
     */
    private void updatePausePlayButton() {
        if (mControllerMonitor == null
                || !mControllerMonitor.onPausePlayUpdate(mPlayer, mControllerViewHolder
                .getPausePlayButton())) {
            if (mControllerViewHolder.getPausePlayButton() == null) {
                return;
            }
            mControllerViewHolder.getPausePlayButton().setSelected(mPlayer.isPlaying());
        }
    }

    /**
     * 更新播放时间显示状态(默认方式)
     */
    public void updateCurrentTimeText(int position) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onCurrentTimeUpdate(mPlayer, mControllerViewHolder
                .getCurrentTimeText(), position)) {
            if (mControllerViewHolder.getCurrentTimeText() == null) {
                return;
            }
            if (mControllerViewHolder.getCurrentTimeText() instanceof TextView) {
                ((TextView) mControllerViewHolder.getCurrentTimeText()).setText(stringForTime(position));
            }
        }
    }

    /**
     * 更新总时长显示状态(默认方式)
     */
    public void updateEndTimeText(int duration) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onEndTimeUpdate(mPlayer, mControllerViewHolder
                .getEndTimeText(), duration)) {
            if (mControllerViewHolder.getEndTimeText() == null) {
                return;
            }
            if (mControllerViewHolder.getEndTimeText() instanceof TextView) {
                ((TextView) mControllerViewHolder.getEndTimeText()).setText(stringForTime(duration));
            }
        }
    }

    /**
     * 更新音量显示状态(默认方式)
     */
    public void updateVolumesText(int curVolumes, int maxVolumes) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onVolumesUpdate(mPlayer, mControllerViewHolder
                .getVolumesText(), curVolumes, maxVolumes)) {
            if (mControllerViewHolder.getVolumesText() == null) {
                return;
            }
            if (mControllerViewHolder.getVolumesText() instanceof TextView) {
                ((TextView) mControllerViewHolder.getVolumesText())
                        .setText(volumesPercentFormat(curVolumes, maxVolumes));
            }
        }
    }

    /**
     * 更新Media name(默认方式)
     */
    public void updateMediaNameText(PineMediaPlayerBean pineMediaPlayerBean) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onMediaNameUpdate(mPlayer, mControllerViewHolder
                .getMediaNameText(), pineMediaPlayerBean)) {
            if (mControllerViewHolder.getMediaNameText() == null) {
                return;
            }
            if (mControllerViewHolder.getMediaNameText() instanceof TextView) {
                ((TextView) mControllerViewHolder.getMediaNameText())
                        .setText(pineMediaPlayerBean.getMediaName());
            }
        }
    }

    public void updateBrightnessText(int brightValue) {
        if (mControllerMonitor == null
                || !mControllerMonitor.onBrightnessUpdate(mPlayer, brightValue)) {
        }
    }

    @Override
    public void updateFullScreenMode() {
        if (mControllerMonitor == null
                || !mControllerMonitor.onFullScreenModeUpdate(mPlayer, mMediaPlayerView.isFullScreenMode())) {
            judgeAndChangeRequestedOrientation();
            attachToParentView(false, true);

            updateMediaNameText(mPlayer.getMediaPlayerBean());
            updateSpeedButton();
            mControllerViewHolder.getFullScreenButton().setSelected(mMediaPlayerView.isFullScreenMode());
            if (mPlayer.isInPlaybackState()) {
                installClickListeners();
                show();
                mHandler.sendEmptyMessageDelayed(MSG_WAITING_FADE_OUT, 50);
                if (mMediaBean.getMediaType() == PineMediaPlayerBean.MEDIA_TYPE_VIDEO
                        && mBackgroundViewHolder.getContainer() != null) {
                    mBackgroundViewHolder.getContainer().setVisibility(GONE);
                }
            }
            if (!mMediaPlayerView.isFullScreenMode()) {
                setAppBrightness(-1);
            }
        }
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

    private String volumesPercentFormat(int curVolume, int maxVolume) {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(0);
        float tmp = (float) curVolume / maxVolume;
        return numberFormat.format(tmp);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(PineConstants.DEFAULT_SHOW_TIMEOUT);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        releasePlugin();
        //  If null, all callbacks and messages will be removed.
        mHandler.removeCallbacksAndMessages(null);
        removeAllViews();
        mMediaPlayerView = null;
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (mControllersActionListener != null) {
            mControllersActionListener.onScreenDown(e);
        }
        mStartDragging = false;
        mPreX = e.getX();
        mPreY = e.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        if (mControllersActionListener != null) {
            mControllersActionListener.onScreenShowPress(e);
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mControllersActionListener == null
                || !mControllersActionListener.onScreenSingleTapUp(e)) {
            if (mPlayer.isPlaying() || mPlayer.isPause()) {
                toggleMediaControlsVisibility();
            } else {
                show(0);
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent curEvent, float distanceX, float distanceY) {
        if (mControllersActionListener == null
                || !mControllersActionListener.onScreenScroll(downEvent, curEvent, distanceX, distanceY)) {
            if (mPlayer.isInPlaybackState() && mMediaPlayerView.isFullScreenMode()) {
                float downX = downEvent.getX();
                float downY = downEvent.getY();
                float curX = curEvent.getX();
                float curY = curEvent.getY();
                if (Math.abs(curY - mPreY) < INSTANCE_DEVIATION) {
                    mDraggingX = mStartDragging ? mDraggingX : true;
                } else {
                    mDraggingX = false;
                }
                if (Math.abs(curX - mPreX) < INSTANCE_DEVIATION) {
                    mDraggingY = mStartDragging ? mDraggingY : true;
                } else {
                    mDraggingY = false;
                }
                if (mDraggingX != mDraggingY) {
                    if (mDraggingX) {
                        if (!mStartDragging) {
                            mStartVolumeByDragging = getCurVolumes();
                        }
                        onScrollAction(true, curX - downX);
                    } else if (mDraggingY) {
                        if (!mStartDragging) {
                            float appBright = getAppBrightness();
                            int systemBrightness = getSystemBrightness();
                            if (appBright < 0.0f) {
                                mStartBrightnessByDragging = systemBrightness;
                            } else {
                                int appBrightness = ((int) (appBright * 255));
                                mStartBrightnessByDragging = appBrightness > 255 ? 255 : appBrightness;
                            }
                        }
                        onScrollAction(false, downY - curY);
                    }
                }
                mStartDragging = true;
                mPreX = curX;
                mPreY = curY;
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mControllersActionListener != null) {
            mControllersActionListener.onScreenLongPress(e);
        }
    }

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent upEvent, float velocityX, float velocityY) {
        if (mControllersActionListener == null
                || !mControllersActionListener.onScreenScroll(downEvent, upEvent, velocityX, velocityY)) {

        }
        return true;
    }

    private void onScrollAction(boolean isXDragging, float changeDistance) {
        if (isXDragging) {
            int amount = (int) (changeDistance / INSTANCE_PER_VOLUME);
            if (amount != 0) {
                int newVolume = mStartVolumeByDragging + amount;
                if (newVolume >= 0 && newVolume <= mMaxVolumes) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    updateVolumesText();
                }
            }
        } else {
            int amount = (int) (changeDistance / INSTANCE_PER_BRIGHTNESS);
            if (amount != 0) {
                int newBrightness = mStartBrightnessByDragging + amount;
                if (newBrightness >= 0 && newBrightness <= 255) {
                    setAppBrightness(newBrightness);
                    updateBrightnessText(newBrightness);
                }
            }
        }
    }

    private void releasePlugin() {
        mHandler.removeMessages(MSG_PLUGIN_REFRESH);
        if (needInitPlugin()) {
            Iterator iterator = mPinePluginMap.entrySet().iterator();
            IPinePlayerPlugin pinePlayerPlugin = null;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                pinePlayerPlugin = (IPinePlayerPlugin) entry.getValue();
                pinePlayerPlugin.onRelease();
            }
        }
        mPinePluginMap = null;
    }

    private boolean needInitPlugin() {
        return mPinePluginMap != null && mPinePluginMap.size() > 0 && mPlayer != null &&
                mPlayer.isSurfaceViewEnable();
    }

    private boolean needActivePlugin() {
        return needInitPlugin() && mPlayer.getSurfaceView() != null;
    }

    /**
     * ----------------   DefaultMediaController begin   --------------------
     **/
    public interface IControllersActionListener {

        /**
         * @param playPauseBtn 播放暂停按键
         * @param player       播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onPlayPauseBtnClick(View playPauseBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param fastForwardBtn 快进按键
         * @param player         播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onFastForwardBtnClick(View fastForwardBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param fatsBackwardBtn 后退按键
         * @param player          播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onFastBackwardBtnClick(View fatsBackwardBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param preBtn 播放前一个按键
         * @param player 播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onPreBtnClick(View preBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param nextBtn 播放后一个按键
         * @param player  播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onNextBtnClick(View nextBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param volumesBtn 音量按键
         * @param player     播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onVolumesBtnClick(View volumesBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param speedBtn 倍速按键
         * @param player   播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onSpeedBtnClick(View speedBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param fullScreenBtn 全屏按键
         * @param player        播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onFullScreenBtnClick(View fullScreenBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param goBackBtn        回退按键
         * @param player           播放器
         * @param isFullScreenMode
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onGoBackBtnClick(View goBackBtn, PineMediaWidget.IPineMediaPlayer player,
                                 boolean isFullScreenMode);

        /**
         * @param viewBtn                 点击按键
         * @param rightViewControlBtnList 控制右侧Views的按键列表
         * @param rightViewHolderList     右侧Views的ViewHolder列表
         * @param player                  播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onRightViewControlBtnClick(View viewBtn, List<View> rightViewControlBtnList,
                                           List<PineRightViewHolder> rightViewHolderList, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param lockControllerBtn 锁定按键
         * @param controller
         * @param player            播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onLockControllerBtnClick(View lockControllerBtn,
                                         PineMediaWidget.IPineMediaController controller,
                                         PineMediaWidget.IPineMediaPlayer player);

        /**
         * 控件窗口ACTION_DOWN事件
         *
         * @param event
         * @return
         */
        boolean onScreenDown(MotionEvent event);

        /**
         * 控件窗口onShowPress手势
         *
         * @param event
         * @return
         */
        boolean onScreenShowPress(MotionEvent event);

        /**
         * 控件窗口onSingleTapUp手势
         *
         * @param event
         * @return
         */
        boolean onScreenSingleTapUp(MotionEvent event);

        /**
         * 控件窗口onLongPress手势
         *
         * @param event
         * @return
         */
        boolean onScreenLongPress(MotionEvent event);

        /**
         * 控件窗口onScroll手势
         *
         * @param downEvent
         * @param curEvent
         * @param distanceX
         * @param distanceY
         * @return
         */
        boolean onScreenScroll(MotionEvent downEvent, MotionEvent curEvent, float distanceX, float distanceY);

        /**
         * 控件窗口onFling手势
         *
         * @param downEvent
         * @param upEvent
         * @param velocityX
         * @param velocityY
         * @return
         */
        boolean onScreenFling(MotionEvent downEvent, MotionEvent upEvent, float velocityX, float velocityY);
    }

    /**
     * ----------------   DefaultMediaController end   --------------------
     **/

    /**
     * PineMediaController适配器，使用者通过此适配器客制化自己的视频播放控制器界面
     */
    public abstract static class AbstractMediaControllerAdapter {

        /**
         * 适配器初始化
         *
         * @param player
         * @return
         */
        protected boolean init(PineMediaWidget.IPineMediaPlayer player) {
            return true;
        }

        /**
         * 背景布局，会被添加到PineMediaPlayerView布局中，
         * 覆盖在MediaView上。用于播放切换过程中的背景布置，或者播放音频时的背景图
         *
         * @param player
         * @param isFullScreenMode
         * @return
         */
        protected abstract PineBackgroundViewHolder onCreateBackgroundViewHolder(
                PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode);

        /**
         * Controller内置控件布局的view holder，会被添加到PineMediaPlayerView布局中，
         * 覆盖在SubtitleView上，请使用透明背景
         * 需要在该方法中绑定布局的相应控件到ViewHolder中，对应的控件功能才能被激活
         *
         * @param player
         * @param isFullScreenMode
         * @return
         */
        protected abstract PineControllerViewHolder onCreateInRootControllerViewHolder(
                PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode);

        /**
         * Controller外置控件布局的view holder，会被添加到PineMediaPlayerView布局中，
         * 不会被添加到播放器布局中（由用户自己任意布局）
         * 需要在该方法中绑定布局的相应控件到ViewHolder中，对应的控件功能才能被激活
         *
         * @param player
         * @param isFullScreenMode
         * @return
         */
        protected abstract PineControllerViewHolder onCreateOutRootControllerViewHolder(
                PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode);

        /**
         * 播放准备过程中的等待界面的view holder，会被添加到PineMediaPlayerView布局中，
         * 覆盖在ControllerView上
         *
         * @param player
         * @param isFullScreenMode
         * @return
         */
        protected abstract PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(
                PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode);

        /**
         * 内置的右侧view holder List，会被添加到PineMediaPlayerView布局中，
         * 覆盖在WaitingProgressView上
         *
         * @param player
         * @param isFullScreenMode
         * @return
         */
        protected abstract List<PineRightViewHolder> onCreateRightViewHolderList(
                PineMediaWidget.IPineMediaPlayer player, boolean isFullScreenMode);

        /**
         * Controller各个显示部件及显示状态更新回调器
         *
         * @return
         */
        protected ControllerMonitor onCreateControllerMonitor() {
            return new ControllerMonitor();
        }

        /**
         * Controller各个控制部件的事件的listener
         *
         * @return
         */
        protected ControllersActionListener onCreateControllersActionListener() {
            return new ControllersActionListener();
        }

        /**
         * 设置当前播放媒体在播放列表中的位置
         *
         * @param position
         */
        public void setCurrentMediaPosition(int position) {

        }
    }

    /**
     * 默认控制器状态更新器。使用者通过继承覆写该类客制化控制器的显示需求
     */
    public static class ControllerMonitor {

        /**
         * 播放器建议控制器的显示状态需要改变时回调（显示需求可由用户自行处理）
         *
         * @param needShow   当前播放器建议控制器是否应处于显示状态
         * @param controller 播放控制器
         * @param player     播放器
         * @param viewHolder 控制器ViewHolder
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        public boolean onControllerVisibilityUpdate(
                boolean needShow, PineMediaWidget.IPineMediaController controller,
                PineMediaWidget.IPineMediaPlayer player, PineControllerViewHolder viewHolder) {
            return false;
        }

        /**
         * 播放器建议设备方向需要改变时回调（改变需求可由用户自行处理）
         *
         * @param context
         * @param controller  播放控制器
         * @param player      播放器
         * @param mediaWidth  播放器宽度
         * @param mediaHeight 播放器高度
         * @param mediaType   播放媒体类别
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        public boolean judgeAndChangeRequestedOrientation(
                Activity context, PineMediaWidget.IPineMediaController controller,
                PineMediaWidget.IPineMediaPlayer player, int mediaWidth,
                int mediaHeight, int mediaType) {
            return false;
        }

        /**
         * 播放器全屏模式切换时回调
         *
         * @param player      播放器
         * @param isFullScreenMode
         * @return
         */
        public boolean onFullScreenModeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                              boolean isFullScreenMode) {
            return false;
        }

        /**
         * 播放器播放状态发生改变时回调
         *
         * @param player      播放器
         * @param speedBtn 播放倍速控件
         */
        public boolean onSpeedUpdate(PineMediaWidget.IPineMediaPlayer player, View speedBtn) {
            return false;
        }

        /**
         * 播放器播放状态发生改变时回调
         *
         * @param player      播放器
         * @param pausePlayBtn 播放暂停控件
         */
        public boolean onPausePlayUpdate(PineMediaWidget.IPineMediaPlayer player,
                                         View pausePlayBtn) {
            return false;
        }

        /**
         * 播放器当前播放时间发生改变时回调
         *
         * @param player      播放器
         * @param currentTimeText 播放时间显示控件
         * @param currentTime     当前播放器播放时间
         */
        public boolean onCurrentTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                           View currentTimeText, int currentTime) {
            return false;
        }

        /**
         * 播放器总播放时长发生更新回调
         *
         * @param player      播放器
         * @param endTimeText 播放总时长显示控件
         * @param endTime     播放总时长
         */
        public boolean onEndTimeUpdate(PineMediaWidget.IPineMediaPlayer player,
                                       View endTimeText, int endTime) {
            return false;
        }

        /**
         * 播放器播放音量发生改变时回调
         *
         * @param player      播放器
         * @param volumesText 音量显示控件
         * @param curVolumes  当前音量
         * @param maxVolumes  最大音量
         */
        public boolean onVolumesUpdate(PineMediaWidget.IPineMediaPlayer player,
                                       View volumesText, int curVolumes, int maxVolumes) {
            return false;
        }

        /**
         * 播放器media名称发生改变时回调
         *
         * @param player      播放器
         * @param mediaNameText media名称显示控件
         * @param mediaEntity   media实体
         */
        public boolean onMediaNameUpdate(PineMediaWidget.IPineMediaPlayer player,
                                         View mediaNameText, PineMediaPlayerBean mediaEntity) {
            return false;
        }

        /**
         * 播放器亮度发生改变时回调
         *
         * @param player      播放器
         * @param brightValue 0（暗）～255（亮）(screenBrightness = -1.0f表示恢复为系统亮度)
         * @return
         */
        public boolean onBrightnessUpdate(PineMediaWidget.IPineMediaPlayer player, int brightValue) {
            return false;
        }
    }

    /**
     * 默认控制器点击事件监听器。使用者通过继承覆写该类客制化控制器各个功能部件的点击事件需求
     */
    public static class ControllersActionListener implements IControllersActionListener {

        @Override
        public boolean onPlayPauseBtnClick(View playPauseBtn,
                                           PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onFastForwardBtnClick(View fastForwardBtn,
                                             PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onFastBackwardBtnClick(View fatsBackwardBtn,
                                              PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onPreBtnClick(View preBtn,
                                     PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onNextBtnClick(View nextBtn,
                                      PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onVolumesBtnClick(View volumesBtn,
                                         PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onSpeedBtnClick(View speedBtn,
                                       PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onFullScreenBtnClick(View fullScreenBtn,
                                            PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onGoBackBtnClick(View goBackBtn, PineMediaWidget.IPineMediaPlayer player,
                                        boolean isFullScreenMode) {
            return false;
        }

        @Override
        public boolean onRightViewControlBtnClick(View viewBtn, List<View> rightViewControlBtnList,
                                                  List<PineRightViewHolder> rightViewHolderList,
                                                  PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onLockControllerBtnClick(View lockControllerBtn,
                                                PineMediaWidget.IPineMediaController controller,
                                                PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onScreenDown(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onScreenShowPress(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onScreenSingleTapUp(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onScreenLongPress(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onScreenScroll(MotionEvent downEvent, MotionEvent curEvent, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onScreenFling(MotionEvent downEvent, MotionEvent upEvent, float velocityX, float velocityY) {
            return false;
        }
    }
}
