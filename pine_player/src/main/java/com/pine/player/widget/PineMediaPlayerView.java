package com.pine.player.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pine.player.PineConstants;
import com.pine.player.R;
import com.pine.player.component.PineMediaPlayerComponent;
import com.pine.player.component.PineMediaPlayerProxy;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.service.PineMediaPlayerService;
import com.pine.player.util.LogUtil;

/**
 * Created by tanghongfeng on 2017/9/15.
 * <p>
 * 注意事项：
 * 1、若要保证全屏效果正常，请将该控件置于具有全屏布局能力的父布局中，
 * 且该父全屏布局必须是RelativeLayout,FrameLayout,LinearLayout中的一种
 */

public class PineMediaPlayerView extends RelativeLayout {
    private final static String TAG = LogUtil.makeLogTag(PineMediaPlayerView.class);
    private static final long BACK_PRESSED_EXIT_TIME = 2000;
    private Context mContext;
    private String mMediaPlayerTag;
    private boolean mIsInit;
    private boolean mSaveMediaStateWhenHide = true;
    private boolean mIsBoundToPlayer;
    private boolean mIsFullScreenMode;
    private PineSurfaceView mPineSurfaceView;
    private PineMediaWidget.IPineMediaController mMediaController;
    private PineMediaPlayerProxy mMediaPlayerProxy;
    private ViewGroup.LayoutParams mHalfAnchorLayout;
    ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (mMediaPlayerProxy != null) {
                // 在第一次绘制之前保存布局layoutParams
                if (!isFullScreenMode() && mHalfAnchorLayout == null) {
                    mHalfAnchorLayout = getLayoutParams();
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
            }
            return true;
        }
    };
    // 点击回退按键时，使用两次点击的时间间隔限定回退行为
    private long mExitTime = -1l;
    private boolean mIsViewShown;
    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mIsViewShown == isShown()) {
                        return;
                    }
                    LogUtil.d(TAG, "OnGlobalLayoutListener visibility from " + mIsViewShown
                            + " to " + isShown() + ", view:" + this);
                    mIsViewShown = isShown();
                    if (mIsViewShown) {
                        attachToMediaPlayerComponent();
                    } else {
                        detachFromMediaPlayerComponent(false);
                    }
                }
            };

    public PineMediaPlayerView(Context context) {
        super(context);
        mContext = context;
    }

    public PineMediaPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public PineMediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void init(String mediaPlayerTag) {
        init(mediaPlayerTag, null, true, mSaveMediaStateWhenHide);
    }

    public void init(String mediaPlayerTag, PineMediaWidget.IPineMediaController controller) {
        init(mediaPlayerTag, controller, true, mSaveMediaStateWhenHide);
    }

    public void init(String mediaPlayerTag, PineMediaWidget.IPineMediaController controller,
                     boolean enableSurface) {
        init(mediaPlayerTag, controller, enableSurface, mSaveMediaStateWhenHide);
    }

    /**
     * 播放器控件初始化
     * @param mediaPlayerTag   播放器唯一标识，如果mediaPlayerTag标识的播放器已经初始化，
     *                         则将控件绑定到该播放器上，否则初始化一个新的mediaPlayerTag标识的播放器，
     *                         并绑定控件到播放器上。
     * @param controller   IPineMediaController 实例，即播放器控制器
     * @param enableSurface   是否需要SurfaceView来呈现播放画面（默认为true），对于音频则可以设置为false
     * @param saveMediaStateWhenHide   当控件View隐藏时（比如控件所在Activity被pause，或者失去焦点）是否自动保存当前播放状态，
     *                                 用于再次显示之后的恢复到之前的播放状态（默认为true）。
     */
    public void init(String mediaPlayerTag, PineMediaWidget.IPineMediaController controller,
                     boolean enableSurface, boolean saveMediaStateWhenHide) {
        if (!mIsInit) {
            LogUtil.d(TAG, "init view:" + this);
            mIsInit = true;
            mMediaPlayerTag = mediaPlayerTag;
            mSaveMediaStateWhenHide = saveMediaStateWhenHide;
            mMediaPlayerProxy = (PineMediaPlayerProxy) PineMediaPlayerService
                    .getMediaPlayerByTag(mMediaPlayerTag);
            if (mMediaPlayerProxy == null) {
                mMediaPlayerProxy = new PineMediaPlayerProxy(mContext.getApplicationContext(),
                        mMediaPlayerTag);
                PineMediaPlayerService.setMediaPlayerByTag(mMediaPlayerTag, mMediaPlayerProxy);
            }
            mMediaPlayerProxy.setMediaPlayerView(this, false);
            if (enableSurface) {
                mPineSurfaceView = new PineSurfaceView(mContext);
                mPineSurfaceView.setMediaPlayer(this, mMediaPlayerProxy);
                LayoutParams layoutParams = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                layoutParams.addRule(CENTER_IN_PARENT);
                addView(mPineSurfaceView, layoutParams);
            }
            getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
            getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            }
            getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

            if (mMediaPlayerProxy.isInPlaybackState()) {
                resetMediaController(controller, false, true);
            } else {
                resetMediaController(controller, true, false);
            }

            mIsViewShown = isShown();
        }
    }

    private boolean checkIsInit() {
        if (!mIsInit) {
            Toast.makeText(mContext, R.string.init_method_need_call_toast, Toast.LENGTH_SHORT);
        }
        return mIsInit;
    }

    public boolean hasSurfaceView() {
        return mPineSurfaceView != null;
    }

    public PineSurfaceView getMediaSurfaceView() {
        return mPineSurfaceView;
    }

    public ViewGroup.LayoutParams getHalfAnchorLayout() {
        return mHalfAnchorLayout;
    }

    public PineMediaWidget.IPineMediaController getMediaController() {
        return mMediaController;
    }

    public void resetMediaController(PineMediaWidget.IPineMediaController controller,
                                     boolean isPlayerReset, boolean isResumeState) {
        if (checkIsInit()) {
            LogUtil.d(TAG, "resetMediaController view:" + this);
            if (mMediaController != null && mMediaController != controller) {
                mMediaController.resetOutRootControllerIdleState();
            }
            mMediaController = controller;
            if (controller != null) {
                controller.setMediaPlayerView(this);
                controller.setMediaPlayer(mMediaPlayerProxy);
            }
            if (mMediaPlayerProxy.isAttachViewMode() && mMediaController != null) {
                mMediaController.hide();
            }
            mMediaPlayerProxy.attachMediaController(isPlayerReset, isResumeState);
        }
    }

    public PineMediaWidget.IPineMediaPlayer getMediaPlayer() {
        if (!checkIsInit()) {
            return null;
        }
        return (PineMediaWidget.IPineMediaPlayer) mMediaPlayerProxy;
    }

    public void toggleFullScreenMode(boolean isLocked) {
        mIsFullScreenMode = !mIsFullScreenMode;
        if (mMediaPlayerProxy.isAttachViewMode()) {
            ViewGroup.LayoutParams layoutParams;
            if (isFullScreenMode()) {
                if (getParent() instanceof RelativeLayout) {
                    layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else if (getParent() instanceof LinearLayout) {
                    layoutParams = new LinearLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else if (getParent() instanceof FrameLayout) {
                    layoutParams = new FrameLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else {
                    layoutParams = new ViewGroup.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                }
            } else {
                layoutParams = getHalfAnchorLayout();
            }
            setLayoutParams(layoutParams);
            mMediaController.updateFullScreenMode();
        }
    }

    public boolean isFullScreenMode() {
        return mIsFullScreenMode;
    }

    public void attachToMediaPlayerComponent() {
        if (mMediaPlayerProxy == null || mIsBoundToPlayer || !isShown()) {
            return;
        }
        LogUtil.d(TAG, "attachToMediaPlayerComponent view:" + this);
        mMediaPlayerProxy.setMediaPlayerView(this, true);
        getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
        getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
        resetMediaController(mMediaController, false, true);
        mMediaPlayerProxy.resume();
    }

    public void onMediaComponentAttach() {
        LogUtil.d(TAG, "onMediaComponentAttach view:" + this);
        mIsBoundToPlayer = true;
    }

    public void detachFromMediaPlayerComponent(boolean viewDestroy) {
        if (mMediaPlayerProxy == null || (!viewDestroy && !mIsBoundToPlayer)) {
            return;
        }
        LogUtil.d(TAG, "detachFromMediaPlayerComponent viewDestroy: " + viewDestroy +
                ", saveState:" + mSaveMediaStateWhenHide + ", view:" + this);
        if (mMediaController != null) {
            mMediaController.resetOutRootControllerIdleState();
            mMediaController.hide();
        }
        if (!mMediaPlayerProxy.isAutocephalyPlayMode()) {
            if (mSaveMediaStateWhenHide) {
                mMediaPlayerProxy.savePlayMediaState();
            }
            if (viewDestroy) {
                if (mMediaPlayerProxy.shouldDestroyPlayerWhenDetach()) {
                    PineMediaPlayerService.destroyMediaPlayerByTag(mMediaPlayerTag);
                } else {
                    mMediaPlayerProxy.release();
                }
            } else {
                if (!(mContext instanceof Activity) || ((Activity) mContext).isFinishing()) {
                    mMediaPlayerProxy.release();
                } else {
                    mMediaPlayerProxy.pause();
                }
            }
        }
        mMediaPlayerProxy.detachMediaPlayerView(this);
    }

    public void onMediaComponentDetach() {
        LogUtil.d(TAG, "onMediaComponentDetach view:" + this);
        mIsBoundToPlayer = false;
    }

    // 因为Activity onPause和onResume时不会触发OnGlobalLayout，此处用来对OnGlobalLayoutListener进行补充
    @Override
    public void onWindowFocusChanged(boolean isFocus) {
        if (!mIsInit) {
            return;
        }
        mIsViewShown = isShown();
        LogUtil.d(TAG, "onWindowFocusChanged isFocus:" + isFocus + ", mIsViewShown:" +
                mIsViewShown + " , view:" + this);
        if (isFocus) {
            attachToMediaPlayerComponent();
        } else {
            detachFromMediaPlayerComponent(false);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        LogUtil.d(TAG, "Attached to window view:" + this);
        if (mIsInit) {
            attachToMediaPlayerComponent();
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LogUtil.d(TAG, "Detach from window view:" + this);
        if (mIsInit) {
            detachFromMediaPlayerComponent(true);
            mIsFullScreenMode = false;
            mIsBoundToPlayer = false;
            getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mMediaController != null) {
            int keyCode = event.getKeyCode();
            final boolean uniqueDown = event.getRepeatCount() == 0
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (uniqueDown) {
                    mMediaController.doPauseResume();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                    mMediaController.pausePlayBtnRequestFocus();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (uniqueDown && !mMediaPlayerProxy.isPlaying()) {
                    mMediaPlayerProxy.start();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (uniqueDown && mMediaPlayerProxy.isPlaying()) {
                    mMediaPlayerProxy.pause();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                    || keyCode == KeyEvent.KEYCODE_CAMERA) {
                // don't show the controls for volume adjustment
                mMediaController.updateVolumesText();
                return super.dispatchKeyEvent(event);
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                if (uniqueDown && mMediaPlayerProxy.getMediaPlayerState() ==
                        PineMediaPlayerComponent.STATE_PLAYING
                        && System.currentTimeMillis() - mExitTime > BACK_PRESSED_EXIT_TIME) {
                    if (isFullScreenMode()) {
                        toggleFullScreenMode(mMediaController.isLocked());
                    } else {
                        mMediaController.hide();
                        mExitTime = System.currentTimeMillis();
                        Toast.makeText(mContext, R.string.pine_media_back_pressed_toast,
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (mMediaPlayerProxy.isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayerProxy.isPlaying()) {
                    mMediaPlayerProxy.pause();
                    mMediaController.show();
                } else {
                    mMediaPlayerProxy.start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayerProxy.isPlaying()) {
                    mMediaPlayerProxy.start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayerProxy.isPlaying()) {
                    mMediaPlayerProxy.pause();
                    mMediaController.show();
                }
                return true;
            } else {
                mMediaController.toggleMediaControlsVisibility();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mMediaController != null) {
                    if (mMediaPlayerProxy.isPlaying() || mMediaPlayerProxy.isPause()) {
                        mMediaController.toggleMediaControlsVisibility();
                    } else {
                        mMediaController.show(0);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    public static final class PineMediaViewLayout {
        public int width;
        public int height;
        public int left;
        public int right;
        public int top;
        public int bottom;

        @Override
        public String toString() {
            return "{width:" + width
                    + ", height:" + height
                    + ", left:" + left
                    + ", right:" + right
                    + ", top:" + top
                    + ", bottom:" + bottom + "}";
        }
    }
}
