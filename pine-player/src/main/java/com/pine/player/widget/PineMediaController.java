package com.pine.player.widget;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pine.player.R;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineMediaListViewHolder;
import com.pine.player.widget.viewholder.PinePluginViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by tanghongfeng on 2017/8/16.
 */

public class PineMediaController extends RelativeLayout
        implements PineMediaWidget.IPineMediaController {
    private final static String TAG = "PineMediaController";

    private static final int MSG_FADE_OUT = 1;
    private static final int MSG_SHOW_PROGRESS = 2;
    private static final int MSG_BACKGROUND_FADE_OUT = 3;
    private static final int MSG_WAITING_FADE_OUT = 4;
    private static final int MSG_PLUGIN_REFRESH = 5;
    public static final int PLUGIN_REFRESH_TIME_DELAY = 200;
    public static final int DEFAULT_TIMEOUT = 6000;
    private final static String CONTROLLER_TAG = "Controller_Tag";

    private String mMediaViewTag;

    private final Activity mContext;
    private AudioManager mAudioManager;
    private int mMaxVolumes;

    // 播放器
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    // 控制器适配器
    private AbstractMediaControllerAdapter mAdapter;
    // 播放实体
    private PineMediaPlayerBean mMediaBean;
    private ControllersOnClickListener mControllersOnClickListener;
    private ControllerMonitor mControllerMonitor;

    private int mPreFadeOutTime;
    // 播放控制器自动隐藏时间
    private int mFadeOutTime = DEFAULT_TIMEOUT;
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
    private boolean mIsNeedResizeMediaList;
    private boolean mIsNeedResizePluginView;

    private PineBackgroundViewHolder mBackgroundViewHolder;
    private RelativeLayout mPluginViewContainer;
    private List<PinePluginViewHolder> mPluginViewHolderList;
    private PineControllerViewHolder mControllerViewHolder;
    private PineWaitingProgressViewHolder mWaitingProgressViewHolder;
    private PineMediaListViewHolder mMediaListViewHolder;

    // Controller控件的当前父布局
    private RelativeLayout mAnchor;
    // Controller控件本身
    private View mRoot;

    private List<IPinePlayerPlugin> mPinePluginList;

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
                    if (!mDragging && mPlayer.isPlaying()) {
                        msg = obtainMessage(MSG_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
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
                    break;
                // 每500毫秒刷新一次插件View
                case MSG_PLUGIN_REFRESH:
                    for (int i = 0; i < mPinePluginList.size(); i++) {
                        mPinePluginList.get(i).onTime(mPlayer.getCurrentPosition());
                    }
                    if (mPlayer.isPlaying() && !mHandler.hasMessages(MSG_PLUGIN_REFRESH)) {
                        msg = obtainMessage(MSG_PLUGIN_REFRESH);
                        sendMessageDelayed(msg, PLUGIN_REFRESH_TIME_DELAY);
                    }
                    break;
            }
        }
    };

    public PineMediaController(Activity context) {
        this(context, true);
    }

    public PineMediaController(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolumes = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public PineMediaController(Activity context, boolean useFastForward) {
        super(context);
        mRoot = this;
        mRoot.setTag(CONTROLLER_TAG);
        mContext = context;
        mUseFastForward = useFastForward;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolumes = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    ViewTreeObserver.OnPreDrawListener mMediaListPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {

        @Override
        public boolean onPreDraw() {
            // 在第一次绘制MediaList之前需要调整它的布局属性以适应Controller布局。
            if (mMediaListViewHolder.getContainer() != null
                    && mPlayer.isFullScreenMode() && mIsNeedResizeMediaList) {
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
                        mMediaListViewHolder.getContainer().getLayoutParams();
                if (oldLayoutParams.topMargin == topMargin
                        && oldLayoutParams.bottomMargin == bottomMargin) {
                    mIsNeedResizeMediaList = false;
                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.topMargin = topMargin;
                    layoutParams.bottomMargin = bottomMargin;
                    layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                    mMediaListViewHolder.getContainer().setLayoutParams(layoutParams);
                }
            }
            return true;
        }
    };

    ViewTreeObserver.OnPreDrawListener mPluginPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {

        @Override
        public boolean onPreDraw() {
            // 在绘制SubtitleView之前需要调整它的布局属性以适应Controller布局。
            if (mPluginViewContainer != null && mIsNeedResizePluginView) {
                PineMediaPlayerView.PineMediaViewLayout playerLayoutParams =
                        mPlayer.getMediaAdaptionLayout();
                if (playerLayoutParams != null) {
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
                            mPluginViewContainer.getLayoutParams();
                    if (oldLayoutParams.topMargin == topMargin &&
                            oldLayoutParams.bottomMargin == bottomMargin) {
                        mIsNeedResizePluginView = false;
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
                        mPluginViewContainer.setLayoutParams(layoutParams);
                    }
                }
            }
            return true;
        }
    };

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

    public View getControllerParentView() {
        return mAnchor;
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
     * ----------------   IPineMediaController begin   --------------------
     **/

    @Override
    public void setMediaPlayer(PineMediaWidget.IPineMediaPlayer player,
                               PineMediaPlayerBean pineMediaPlayerBean, String mediaViewTag) {
        mPlayer = player;
        mMediaBean = pineMediaPlayerBean;
        mMediaViewTag = mediaViewTag;
    }

    /**
     * 将PineMediaController各个部分挂载到PineMediaPlayerView中
     *
     * @param parent PineMediaPlayerView
     * @param isPlayerReset 本此attach是否重置了MediaPlayer
     * @param isResumeState 本此attach是否是为了恢复状态
     */
    @Override
    public void attachToParentView(RelativeLayout parent, boolean isPlayerReset, boolean isResumeState) {
        Log.d(TAG, "Attach to media view. isPlayerReset: " + isPlayerReset
                + ", isResumeState: " + isResumeState);
        mAnchor = parent;
        removeAllViews();
        if (mAdapter == null) {
            mAdapter = new DefaultMediaControllerAdapter(mContext);
        }
        mControllerMonitor = mAdapter.onCreateControllerMonitor();
        mControllersOnClickListener = mAdapter.onCreateControllersOnClickListener();
        mBackgroundViewHolder
                = mAdapter.onCreateBackgroundViewHolder(mPlayer.isFullScreenMode());
        mPinePluginList = mMediaBean.getPlayerPluginList();
        if (mPinePluginList != null && mPinePluginList.size() > 0) {
            mPluginViewHolderList = new ArrayList<PinePluginViewHolder>();
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPluginViewHolderList.add(mPinePluginList.get(i)
                        .createViewHolder(mPlayer.isFullScreenMode()));
            }
        }
        mControllerViewHolder
                = mAdapter.onCreateOutRootControllerViewHolder(mPlayer.isFullScreenMode());
        if (mControllerViewHolder == null) {
            mControllerContainerInRoot = true;
            mControllerViewHolder
                    = mAdapter.onCreateInRootControllerViewHolder(mPlayer.isFullScreenMode());
        }
        mWaitingProgressViewHolder
                = mAdapter.onCreateWaitingProgressViewHolder(mPlayer.isFullScreenMode());
        mMediaListViewHolder = mAdapter.onCreateFullScreenMediaListViewHolder();
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
        if (mPluginViewHolderList != null) {
            mPluginViewContainer = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams pluginContainerParams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            pluginContainerParams.addRule(CENTER_IN_PARENT);
            for (int i = 0; i < mPluginViewHolderList.size(); i++) {
                if (mPluginViewHolderList.get(i).getContainer() != null) {
                    mPluginViewContainer.addView(mPluginViewHolderList.get(i).getContainer(),
                            pluginContainerParams);
                }
            }
            addView(mPluginViewContainer, new RelativeLayout.LayoutParams(0, 0));
            addPreDrawListener(mPluginViewContainer, mPluginPreDrawListener);
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
        if (mWaitingProgressViewHolder != null || mWaitingProgressViewHolder.getContainer() != null) {
            addView(mWaitingProgressViewHolder.getContainer(), layoutParams);
            mWaitingProgressViewHolder.getContainer().setVisibility(VISIBLE);
        } else {
            mWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
        }
        // 全屏模式下内置播放列表View
        if (mMediaListViewHolder != null || mMediaListViewHolder.getContainer() != null) {
            mMediaListViewHolder.getContainer().setVisibility(GONE);
            addView(mMediaListViewHolder.getContainer(), new RelativeLayout.LayoutParams(0, 0));
            addPreDrawListener(mMediaListViewHolder.getContainer(), mMediaListPreDrawListener);
        } else {
            mMediaListViewHolder = new PineMediaListViewHolder();
        }
        if (mIsFirstAttach) {
            mAnchor.addView(mRoot, layoutParams);
        }
        if (mPluginViewContainer != null) {
            mIsNeedResizePluginView = true;
        }
        if (mMediaListViewHolder.getContainer() != null) {
            mIsNeedResizeMediaList = true;
        }
        initControllerView();
        mIsFirstAttach = false;

        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onInit(mContext, mPlayer, this, isPlayerReset, isResumeState);
            }
        }
    }

    private void initControllerView() {
        setEnabled(false);
        if (mControllerViewHolder.getGoBackButton() != null) {
            mControllerViewHolder.getGoBackButton().setOnClickListener(mGoBackListener);
            mControllerViewHolder.getGoBackButton().setEnabled(mGoBackListener != null);
            mControllerViewHolder.getGoBackButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getMediaListButton() != null) {
            mControllerViewHolder.getMediaListButton().setOnClickListener(mMediaListListener);
            mControllerViewHolder.getMediaListButton().setEnabled(mMediaListListener != null);
            mControllerViewHolder.getMediaListButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getLockControllerButton() != null) {
            mControllerViewHolder.getLockControllerButton().setOnClickListener(mLockControllerListener);
            mControllerViewHolder.getLockControllerButton().setEnabled(mLockControllerListener != null);
            mControllerViewHolder.getLockControllerButton().setSelected(mIsControllerLocked);
            mControllerViewHolder.getLockControllerButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getFullScreenButton() != null) {
            mControllerViewHolder.getFullScreenButton().setOnClickListener(mFullScreenListener);
            mControllerViewHolder.getFullScreenButton().setEnabled(mFullScreenListener != null);
            mControllerViewHolder.getFullScreenButton().setVisibility(View.VISIBLE);
        }
        show(0);
    }

    private void installClickListeners() {
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().requestFocus();
            mControllerViewHolder.getPausePlayButton().setOnClickListener(mPausePlayListener);
            mControllerViewHolder.getPausePlayButton().setEnabled(mPausePlayListener != null);
            mControllerViewHolder.getPausePlayButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getPlayProgressBar() != null) {
            if (mControllerViewHolder.getPlayProgressBar() instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mControllerViewHolder.getPlayProgressBar();
                seeker.setOnSeekBarChangeListener(mSeekListener);
                mControllerViewHolder.getPlayProgressBar().setEnabled(mSeekListener != null);
                mControllerViewHolder.getPlayProgressBar().setVisibility(View.VISIBLE);
            }
            mControllerViewHolder.getPlayProgressBar().setMax(1000);
        }
        if (mControllerViewHolder.getFastForwardButton() != null) {
            mControllerViewHolder.getFastForwardButton().setOnClickListener(mFfwdListener);
            mControllerViewHolder.getFastForwardButton().setEnabled(mFfwdListener != null);
            mControllerViewHolder.getFastForwardButton().setVisibility(
                    mUseFastForward ? View.VISIBLE : View.GONE);
        }
        if (mControllerViewHolder.getFastBackwardButton() != null) {
            mControllerViewHolder.getFastBackwardButton().setOnClickListener(mRewListener);
            mControllerViewHolder.getFastBackwardButton().setEnabled(mRewListener != null);
            mControllerViewHolder.getFastBackwardButton().setVisibility(
                    mUseFastForward ? View.VISIBLE : View.GONE);
        }
        if (mControllerViewHolder.getNextButton() != null) {
            mControllerViewHolder.getNextButton().setOnClickListener(mNextListener);
            mControllerViewHolder.getNextButton().setEnabled(mNextListener != null);
            mControllerViewHolder.getNextButton().setVisibility(View.VISIBLE);
        }
        if (mControllerViewHolder.getPrevButton() != null) {
            mControllerViewHolder.getPrevButton().setOnClickListener(mPrevListener);
            mControllerViewHolder.getPrevButton().setEnabled(mPrevListener != null);
            mControllerViewHolder.getPrevButton().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void doPauseResume() {
        if (mAnchor == null) {
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
        if (mAnchor == null || mControllerViewHolder.getContainer() == null) {
            return;
        }
        if (!(mControllerViewHolder.getContainer().getVisibility() == VISIBLE) ||
                timeout != mPreFadeOutTime) {
            mPreFadeOutTime = timeout;
            mIsNeedResizePluginView = true;
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
        mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
        mHandler.removeMessages(MSG_FADE_OUT);
        if (timeout > 0) {
            Message msg = mHandler.obtainMessage(MSG_FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        if (mAnchor == null || mControllerViewHolder.getContainer() == null) {
            return;
        }
        if (mControllerViewHolder.getContainer().getVisibility() == VISIBLE) {
            try {
                mIsNeedResizePluginView = true;
                mHandler.removeMessages(MSG_SHOW_PROGRESS);
                if (mControllerContainerInRoot) {
                    mControllerViewHolder.getContainer().setVisibility(GONE);
                }
                updateControllerVisibility(false);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
        }
    }

    @Override
    public void onMediaPlayerStart() {
        if (mAnchor == null) {
            return;
        }
        updatePausePlayButton();
        if (mPinePluginList != null && mPinePluginList.size() > 0) {
            // 启动插件刷新
            if (!mHandler.hasMessages(MSG_PLUGIN_REFRESH)) {
                mHandler.sendEmptyMessage(MSG_PLUGIN_REFRESH);
            }
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerStart();
            }
        }
    }

    @Override
    public void onMediaPlayerPause() {
        if (mAnchor == null) {
            return;
        }
        updatePausePlayButton();
        mHandler.removeMessages(MSG_PLUGIN_REFRESH);
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerPause();
            }
        }
    }

    @Override
    public void onMediaPlayerPrepared() {
        if (mAnchor == null) {
            return;
        }
        // 设置设备方向
        judgeAndChangeRequestedOrientation();
        updateMediaNameText(mPlayer.getMediaPlayerBean());
        installClickListeners();
        if (mPluginViewContainer != null) {
            mPluginViewContainer.setVisibility(VISIBLE);
        }
        show();
        mHandler.sendEmptyMessageDelayed(MSG_WAITING_FADE_OUT, 200);
        if (mMediaBean.getMediaType() == PineMediaPlayerBean.MEDIA_TYPE_VIDEO) {
            mHandler.sendEmptyMessageDelayed(MSG_BACKGROUND_FADE_OUT, 200);
        }
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerPrepared();
            }
        }
    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerInfo(what, extra);
            }
        }
    }

    private boolean mPausedByBufferingUpdate;

    @Override
    public void onBufferingUpdate(int percent) {
        float position = (float) mPlayer.getCurrentPosition();
        float duration = (float) mPlayer.getDuration();
        Log.v(TAG, "onBufferingUpdate percent: " + percent + ", duration:" + duration
                + ", position:" + position);
        if (mPlayer.getMediaPlayerState() == PineSurfaceView.STATE_PLAYBACK_COMPLETED
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
                    hide();
                }
                if (mWaitingProgressViewHolder.getContainer() != null) {
                    mWaitingProgressViewHolder.getContainer().setVisibility(GONE);
                }
            }
        }
    }

    @Override
    public void onMediaPlayerComplete() {
        if (mAnchor == null) {
            return;
        }
        show(0);
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerComplete();
            }
        }
    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {
        if (mAnchor == null) {
            return;
        }
        if (mWaitingProgressViewHolder.getContainer() != null) {
            mWaitingProgressViewHolder.getContainer().setVisibility(GONE);
        }
        if (mPluginViewContainer != null) {
            mPluginViewContainer.setVisibility(GONE);
        }
        if (mControllerViewHolder != null) {
            setEnabled(false);
            show(0);
        }
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onMediaPlayerError(framework_err, impl_err);
            }
        }
    }

    @Override
    public void onAbnormalComplete() {
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onAbnormalComplete();
            }
        }
    }

    @Override
    public void onMediaPlayerRelease(boolean clearTargetState) {
        mHandler.removeMessages(MSG_PLUGIN_REFRESH);
        if (mPinePluginList != null) {
            for (int i = 0; i < mPinePluginList.size(); i++) {
                mPinePluginList.get(i).onRelease();
            }
        }
    }

    @Override
    public void updateVolumes() {
        if (mAnchor == null) {
            return;
        }
        updateVolumesText(getCurVolumes(), mMaxVolumes);
    }

    @Override
    public void pausePlayBtnRequestFocus() {
        if (mAnchor == null) {
            return;
        }
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().requestFocus();
        }
    }

    @Override
    public boolean isShowing() {
        if (mAnchor == null) {
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
        Log.d(TAG, "setProgress position:" + position + ", duration:" + duration);
        if (mPlayer.getMediaPlayerState() == PineSurfaceView.STATE_PLAYBACK_COMPLETED) {
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
        updateEndTimeText(duration);
        updateCurrentTimeText(position);

        return position;
    }

    @Override
    public void setControllerEnabled(boolean enabled) {
        setControllerEnabled(enabled, enabled, enabled, enabled, enabled, enabled, enabled, enabled);
    }

    @Override
    public void setControllerEnabled(boolean enabledPlayerPause, boolean enabledProgressBar,
                                     boolean enabledToggleFullScreen, boolean enabledLock,
                                     boolean enabledFastForward, boolean enabledFastBackward,
                                     boolean enabledNext, boolean enabledPrev) {
        if (mAnchor == null) {
            return;
        }
        Log.d(TAG, "setControllerEnabled enabledPlayerPause: " + enabledPlayerPause
                + ", enabledProgressBar: " + enabledProgressBar
                + ", enabledToggleFullScreen: " + enabledToggleFullScreen
                + ", enabledLock: " + enabledLock
                + ", enabledFastForward: " + enabledFastForward
                + ", enabledFastBackward: " + enabledFastBackward
                + ", enabledNext: " + enabledNext
                + ", enabledPrev: " + enabledPrev);
        if (mControllerViewHolder.getPausePlayButton() != null) {
            mControllerViewHolder.getPausePlayButton().setEnabled(enabledPlayerPause);
        }
        if (mControllerViewHolder.getPlayProgressBar() != null) {
            mControllerViewHolder.getPlayProgressBar().setEnabled(enabledProgressBar);
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
            if (mControllerViewHolder.getPlayProgressBar() != null && !mPlayer.canSeekBackward() && !mPlayer.canSeekForward()) {
                mControllerViewHolder.getPlayProgressBar().setEnabled(false);
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
        removeAllViewsInLayout();
        if (mPluginViewContainer != null) {
            mPluginViewContainer.removeAllViewsInLayout();
        }
        requestLayout();
        invalidate();
    }

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
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
            updateCurrentTimeText((int) newPosition);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlayButton();

            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS);
        }
    };

    private final View.OnClickListener mPausePlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onPlayPauseBtnClick(v, mPlayer)) {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
                updatePausePlayButton();
            }
        }
    };

    private final View.OnClickListener mRewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onFastBackwardBtnClick(v, mPlayer)) {
                int pos = mPlayer.getCurrentPosition();
                pos -= 5000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();
                show(DEFAULT_TIMEOUT);
            }
        }
    };

    private final View.OnClickListener mFfwdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onFastForwardBtnClick(v, mPlayer)) {
                int pos = mPlayer.getCurrentPosition();
                pos += 15000; // milliseconds
                mPlayer.seekTo(pos);
                setProgress();
                show(DEFAULT_TIMEOUT);
            }
        }
    };

    private final View.OnClickListener mNextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onNextBtnClick(v, mPlayer)) {

            }
        }
    };

    private final View.OnClickListener mPrevListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onPreBtnClick(v, mPlayer)) {

            }
        }
    };

    private final View.OnClickListener mVolumesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onVolumesBtnClick(v, mPlayer)) {
                mControllerViewHolder.getVolumesButton().setSelected(
                        !mControllerViewHolder.getVolumesButton().isSelected());
            }
        }
    };

    private final View.OnClickListener mFullScreenListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onFullScreenBtnClick(v, mPlayer)) {
                mPlayer.toggleFullScreenMode(mIsControllerLocked);
                mControllerViewHolder.getFullScreenButton().setSelected(!mPlayer.isFullScreenMode());
                attachToParentView(mAnchor, false, true);
                judgeAndChangeRequestedOrientation();
                updateMediaNameText(mPlayer.getMediaPlayerBean());
                if (mPlayer.isInPlaybackState()) {
                    installClickListeners();
                    show();
                    if (mWaitingProgressViewHolder.getContainer() != null) {
                        mWaitingProgressViewHolder.getContainer().setVisibility(GONE);
                    }
                    if (mMediaBean.getMediaType() == PineMediaPlayerBean.MEDIA_TYPE_VIDEO
                            && mBackgroundViewHolder.getContainer() != null) {
                        mBackgroundViewHolder.getContainer().setVisibility(GONE);
                    }
                }
            }
        }
    };

    private final View.OnClickListener mGoBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onGoBackBtnClick(v, mPlayer)) {

            }
        }
    };

    private final View.OnClickListener mMediaListListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onMediaListBtnClick(v,
                    mMediaListViewHolder.getContainer(), mPlayer)) {
                boolean isMediaListLastSelected = mControllerViewHolder.getMediaListButton().isSelected();
                mControllerViewHolder.getMediaListButton().setSelected(!isMediaListLastSelected);
                if (mMediaListViewHolder.getContainer() != null) {
                    mMediaListViewHolder.getContainer()
                            .setVisibility(!isMediaListLastSelected ? VISIBLE : GONE);
                }
                show(!isMediaListLastSelected || !mPlayer.isInPlaybackState() ? 0 : DEFAULT_TIMEOUT);
            }
        }
    };

    private final View.OnClickListener mLockControllerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllersOnClickListener == null
                    || !mControllersOnClickListener.onLockControllerBtnClick(v,
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

    /**
     * 更新控制器及其子控件显示状态(默认方式)
     *
     * @param needShow
     */
    private void updateControllerVisibility(boolean needShow) {
        if (mControllerMonitor == null
                || !mControllerMonitor.updateControllerVisibility(needShow,
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
                if (mMediaListViewHolder.getContainer() != null
                        && mControllerViewHolder.getMediaListButton() != null) {
                    mMediaListViewHolder.getContainer().setVisibility(
                            !mIsControllerLocked
                                    && mControllerViewHolder.getMediaListButton().isSelected()
                                    && mPlayer.isFullScreenMode()
                                    ? View.VISIBLE : View.GONE);
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
                if (mMediaListViewHolder.getContainer() != null
                        && mControllerViewHolder.getMediaListButton() != null) {
                    mMediaListViewHolder.getContainer().setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 通过综合判断改变设备方向(默认方式)
     */
    private void judgeAndChangeRequestedOrientation() {
        if (mAnchor == null) {
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
            if (!mPlayer.isFullScreenMode()) {
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
    }

    /**
     * 更新播放/暂停按键显示状态(默认方式)
     */
    private void updatePausePlayButton() {
        if (mControllerMonitor == null
                || !mControllerMonitor.updatePausePlayButton(mControllerViewHolder
                .getPausePlayButton(), mPlayer)) {
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
                || !mControllerMonitor.updateCurrentTimeText(mControllerViewHolder
                .getCurrentTimeText(), position)) {
            if (mControllerViewHolder.getCurrentTimeText() == null) {
                return;
            }
            ((TextView) mControllerViewHolder.getCurrentTimeText()).setText(stringForTime(position));
        }
    }

    /**
     * 更新总时长显示状态(默认方式)
     */
    public void updateEndTimeText(int duration) {
        if (mControllerMonitor == null
                || !mControllerMonitor.updateEndTimeText(mControllerViewHolder
                .getEndTimeText(), duration)) {
            if (mControllerViewHolder.getEndTimeText() == null) {
                return;
            }
            ((TextView) mControllerViewHolder.getEndTimeText()).setText(stringForTime(duration));
        }
    }

    /**
     * 更新音量显示状态(默认方式)
     */
    public void updateVolumesText(int curVolumes, int maxVolumes) {
        if (mControllerMonitor == null
                || !mControllerMonitor.updateVolumesText(mControllerViewHolder
                .getVolumesText(), curVolumes, maxVolumes)) {
            if (mControllerViewHolder.getVolumesText() == null) {
                return;
            }
            ((TextView) mControllerViewHolder.getVolumesText())
                    .setText(volumesPercentFormat(curVolumes, maxVolumes));
        }
    }

    /**
     * 更新Media name(默认方式)
     */
    public void updateMediaNameText(PineMediaPlayerBean pineMediaPlayerBean) {
        if (mControllerMonitor == null
                || !mControllerMonitor.updateMediaNameText(mControllerViewHolder
                .getMediaNameText(), pineMediaPlayerBean)) {
            if (mControllerViewHolder.getMediaNameText() == null) {
                return;
            }
            ((TextView) mControllerViewHolder.getMediaNameText())
                    .setText(pineMediaPlayerBean.getMediaName());
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

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                show(0); // show until hide is called
//                break;
//            case MotionEvent.ACTION_UP:
//                toggleMediaControlsVisibility();
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                hide();
//                break;
//            default:
//                break;
//        }
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPlayer.isInPlaybackState()) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        //  If null, all callbacks and messages will be removed.
        mHandler.removeCallbacksAndMessages(null);
        mAnchor = null;
        super.onDetachedFromWindow();
    }

    /** ----------------   DefaultMediaController begin   -------------------- **/
    /**
     * 参考此Adapter，继承此Adapter或者AbstractMediaControllerAdapter进行自定义controller的定制
     **/

    public class DefaultMediaControllerAdapter extends AbstractMediaControllerAdapter {
        private Activity mDContext;
        private PineBackgroundViewHolder mDBackgroundViewHolder;
        private PineControllerViewHolder mDFullControllerViewHolder, mDControllerViewHolder;
        private PineWaitingProgressViewHolder mDWaitingProgressViewHolder;
        private PineMediaListViewHolder mDMediaListViewHolder;
        private RelativeLayout mDBackgroundView;
        private ViewGroup mDFullControllerView, mDControllerView;
        private LinearLayout mDWaitingProgressView;

        public DefaultMediaControllerAdapter(Activity context) {
            this.mDContext = context;
        }

        @Override
        public PineBackgroundViewHolder onCreateBackgroundViewHolder(boolean isFullMode) {
            if (mDBackgroundViewHolder == null) {
                mDBackgroundViewHolder = new PineBackgroundViewHolder();
                if (mDBackgroundView == null) {
                    ImageView backgroundView = new ImageView(mContext);
                    backgroundView.setBackgroundResource(android.R.color.black);
                    mDBackgroundView = new RelativeLayout(mContext);
                    mDBackgroundView.setBackgroundResource(android.R.color.black);
                    mDBackgroundView.setLayoutTransition(new LayoutTransition());
                    RelativeLayout.LayoutParams backgroundParams = new RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    backgroundParams.addRule(CENTER_IN_PARENT);
                    mDBackgroundView.addView(backgroundView, backgroundParams);
                    mDBackgroundViewHolder.setBackgroundImageView(backgroundView);
                }
            }
            mDBackgroundViewHolder.setContainer(mDBackgroundView);
            return mDBackgroundViewHolder;
        }

        @Override
        public PineControllerViewHolder onCreateInRootControllerViewHolder(boolean isFullMode) {
            if (isFullMode) {
                if (mDFullControllerViewHolder == null) {
                    mDFullControllerViewHolder = new PineControllerViewHolder();
                    if (mDFullControllerView == null) {
                        mDFullControllerView = (ViewGroup) View.inflate(mDContext,
                                R.layout.media_controller_full, null);
                    }
                    initControllerViewHolder(mDFullControllerViewHolder, mDFullControllerView);
                    mDFullControllerViewHolder.setTopControllerView(
                            mDFullControllerView.findViewById(R.id.top_controller));
                    mDFullControllerViewHolder.setCenterControllerView(
                            mDFullControllerView.findViewById(R.id.center_controller));
                    mDFullControllerViewHolder.setBottomControllerView(
                            mDFullControllerView.findViewById(R.id.bottom_controller));
                    mDFullControllerViewHolder.setGoBackButton(
                            mDFullControllerView.findViewById(R.id.go_back_btn));
                    mDFullControllerViewHolder.setMediaListButton(
                            mDFullControllerView.findViewById(R.id.media_list_btn));
                }
                mDFullControllerViewHolder.setContainer(mDFullControllerView);
                return mDFullControllerViewHolder;
            } else {
                if (mDControllerViewHolder == null) {
                    if (mDControllerView == null) {
                        mDControllerView = (ViewGroup) View.inflate(mDContext,
                                R.layout.media_controller, null);
                    }
                    mDControllerViewHolder = new PineControllerViewHolder();
                    initControllerViewHolder(mDControllerViewHolder, mDControllerView);
                    mDControllerViewHolder.setTopControllerView(mDControllerView
                            .findViewById(R.id.top_controller));
                    mDControllerViewHolder.setCenterControllerView(mDControllerView
                            .findViewById(R.id.center_controller));
                    mDControllerViewHolder.setBottomControllerView(mDControllerView
                            .findViewById(R.id.bottom_controller));
                }
                mDControllerViewHolder.setContainer(mDControllerView);
                return mDControllerViewHolder;
            }
        }

        @Override
        public PineControllerViewHolder onCreateOutRootControllerViewHolder(boolean isFullMode) {
            return null;
        }

        private void initControllerViewHolder(
                PineControllerViewHolder viewHolder, View root) {
            viewHolder.setPausePlayButton(root.findViewById(R.id.pause_play_btn));
            viewHolder.setPlayProgressBar((SeekBar) root.findViewById(R.id.media_progress));
            viewHolder.setCurrentTimeText(root.findViewById(R.id.cur_time_text));
            viewHolder.setEndTimeText(root.findViewById(R.id.end_time_text));
            viewHolder.setVolumesText(root.findViewById(R.id.volumes_text));
            viewHolder.setFullScreenButton(root.findViewById(R.id.full_screen_btn));
            viewHolder.setMediaNameText(root.findViewById(R.id.media_name_text));
            viewHolder.setLockControllerButton(root.findViewById(R.id.lock_screen_btn));
        }

        @Override
        public PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(boolean isFullMode) {
            if (mDWaitingProgressViewHolder == null) {
                mDWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
                if (mDWaitingProgressView == null) {
                    mDWaitingProgressView = new LinearLayout(mDContext);
                    mDWaitingProgressView.setGravity(Gravity.CENTER);
                    mDWaitingProgressView.setBackgroundColor(Color.argb(192, 256, 256, 256));
                    ProgressBar progressBar = new ProgressBar(mDContext);
                    ViewGroup.LayoutParams progressBarParams = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    progressBar.setLayoutParams(progressBarParams);
                    progressBar.setIndeterminateDrawable(getResources()
                            .getDrawable(R.drawable.media_waiting_anim));
                    progressBar.setIndeterminate(true);
                    mDWaitingProgressView.addView(progressBar, progressBarParams);
                }
            }
            mDWaitingProgressViewHolder.setContainer(mDWaitingProgressView);
            return mDWaitingProgressViewHolder;
        }

        @Override
        public PineMediaListViewHolder onCreateFullScreenMediaListViewHolder() {
            return null;
        }

        @Override
        public PineMediaController.ControllersOnClickListener onCreateControllersOnClickListener() {
            return new PineMediaController.ControllersOnClickListener() {
                @Override
                public boolean onGoBackBtnClick(View fullScreenBtn,
                                                PineMediaWidget.IPineMediaPlayer player) {
                    if (mPlayer.isFullScreenMode()) {
                        mControllerViewHolder.getFullScreenButton().performClick();
                    } else {
                        mDContext.finish();
                    }
                    return false;
                }
            };
        }
    }

    /**
     * ----------------   DefaultMediaController end   --------------------
     **/

    /**
     * PineMediaController适配器，使用者通过此适配器客制化自己的视频播放控制器界面
     */
    public abstract static class AbstractMediaControllerAdapter {
        /**
         * 背景布局，会被添加到PineMediaPlayerView布局中，
         * 覆盖在MediaView上。用于播放切换过程中的背景布置，或者播放音频时的背景图
         *
         * @param isFullMode
         * @return
         */
        public abstract PineBackgroundViewHolder onCreateBackgroundViewHolder(boolean isFullMode);

        /**
         * Controller内置控件布局的view holder，会被添加到PineMediaPlayerView布局中，
         * 覆盖在SubtitleView上，请使用透明背景
         * 需要在该方法中绑定布局的相应控件到ViewHolder中，对应的控件功能才能被激活
         *
         * @param isFullMode 是否全屏模式（可根据该参数设置全屏和非全屏状态下各自的布局）
         * @return
         */
        public abstract PineControllerViewHolder onCreateInRootControllerViewHolder(boolean isFullMode);

        /**
         * Controller外置控件布局的view holder，会被添加到PineMediaPlayerView布局中，
         * 不会被添加到播放器布局中（由用户自己任意布局）
         * 需要在该方法中绑定布局的相应控件到ViewHolder中，对应的控件功能才能被激活
         *
         * @param isFullMode 是否全屏模式（可根据该参数设置全屏和非全屏状态下各自的布局）
         * @return
         */
        public abstract PineControllerViewHolder onCreateOutRootControllerViewHolder(boolean isFullMode);

        /**
         * 播放准备过程中的等待界面的view holder，会被添加到PineMediaPlayerView布局中，
         * 覆盖在ControllerView上
         *
         * @param isFullMode 是否全屏模式（可根据该参数设置全屏和非全屏状态下各自的布局）
         * @return
         */
        public abstract PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(boolean isFullMode);

        /**
         * 全屏状态下内置的播放列表的view holder，会被添加到PineMediaPlayerView布局中，
         * 覆盖在WaitingProgressView上
         *
         * @return
         */
        public abstract PineMediaListViewHolder onCreateFullScreenMediaListViewHolder();

        /**
         * Controller各个显示部件及显示状态更新回调器
         *
         * @return
         */
        public ControllerMonitor onCreateControllerMonitor() {
            return new ControllerMonitor();
        }

        /**
         * Controller各个控制部件的点击事件的listener
         *
         * @return
         */
        public ControllersOnClickListener onCreateControllersOnClickListener() {
            return new ControllersOnClickListener();
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
        public boolean updateControllerVisibility(
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
         * 播放器播放状态发生改变时回调
         *
         * @param pausePlayBtn 播放暂停控件
         * @param player       播放器
         */
        public boolean updatePausePlayButton(View pausePlayBtn, PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        /**
         * 播放器当前播放时间发生改变时回调
         *
         * @param currentTimeText 播放时间显示控件
         * @param currentTime     当前播放器播放时间
         */
        public boolean updateCurrentTimeText(View currentTimeText, int currentTime) {
            return false;
        }

        /**
         * 播放器总播放时长发生更新回调
         *
         * @param endTimeText 播放总时长显示控件
         * @param endTime     播放总时长
         */
        public boolean updateEndTimeText(View endTimeText, int endTime) {
            return false;
        }

        /**
         * 播放器播放音量发生改变时回调
         *
         * @param volumesText 音量显示控件
         * @param curVolumes  当前音量
         * @param maxVolumes  最大音量
         */
        public boolean updateVolumesText(View volumesText, int curVolumes, int maxVolumes) {
            return false;
        }

        /**
         * 播放器media名称发生改变时回调
         *
         * @param mediaNameText media名称显示控件
         * @param mediaEntity   media实体
         */
        public boolean updateMediaNameText(View mediaNameText, PineMediaPlayerBean mediaEntity) {
            return false;
        }
    }

    /**
     * 默认控制器点击事件监听器。使用者通过继承覆写该类客制化控制器各个功能部件的点击事件需求
     */
    public static class ControllersOnClickListener implements IControllersOnClickListener {

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
        public boolean onFullScreenBtnClick(View fullScreenBtn,
                                            PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onGoBackBtnClick(View goBackBtn,
                                        PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onMediaListBtnClick(View mediaListBtn, View mediaListContainerView,
                                           PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }

        @Override
        public boolean onLockControllerBtnClick(View lockControllerBtn,
                                                PineMediaWidget.IPineMediaController controller,
                                                PineMediaWidget.IPineMediaPlayer player) {
            return false;
        }
    }


    public interface IControllersOnClickListener {

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
         * @param fullScreenBtn 全屏按键
         * @param player        播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onFullScreenBtnClick(View fullScreenBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param goBackBtn 回退按键
         * @param player    播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onGoBackBtnClick(View goBackBtn, PineMediaWidget.IPineMediaPlayer player);

        /**
         * @param mediaListBtn           播放列表显示/隐藏按键
         * @param mediaListContainerView
         * @param player                 播放器
         * @return true-消耗了该事件，阻止播放控制器默认的行为;
         * false-没有消耗该事件，用户事件处理完后会继续执行播放器默认行为
         */
        boolean onMediaListBtnClick(View mediaListBtn, View mediaListContainerView,
                                    PineMediaWidget.IPineMediaPlayer player);

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
    }
}
