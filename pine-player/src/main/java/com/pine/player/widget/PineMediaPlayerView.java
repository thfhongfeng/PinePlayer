package com.pine.player.widget;

import android.content.Context;
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
    private final static String TAG = "PineMediaPlayerView";
    private static final long BACK_PRESSED_EXIT_TIME = 2000;
    private Context mContext;
    private String mMediaPlayerTag;
    private boolean mIsInit;
    private boolean mIsBoundToPlayer;
    private boolean mIsFullScreenMode;
    private PineSurfaceView mPineSurfaceView;
    private PineMediaWidget.IPineMediaController mMediaController;
    private PineMediaPlayerComponent mMediaPlayerComponent;
    private ViewGroup.LayoutParams mHalfAnchorLayout;
    ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (mMediaPlayerComponent != null) {
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
        init(mediaPlayerTag, null, true);
    }

    public void init(String mediaPlayerTag, PineMediaWidget.IPineMediaController controller) {
        init(mediaPlayerTag, controller, true);
    }

    public void init(String mediaPlayerTag,
                     PineMediaWidget.IPineMediaController controller, boolean enableSurface) {
        if (!mIsInit) {
            mIsInit = true;
            mMediaPlayerTag = mediaPlayerTag;
            PineMediaPlayerProxy mediaPlayer = (PineMediaPlayerProxy) PineMediaPlayerService
                    .getMediaPlayerByTag(mMediaPlayerTag);
            if (mediaPlayer != null) {
                mMediaPlayerComponent = mediaPlayer.getPineMediaPlayerComponent();
            } else {
                mMediaPlayerComponent = new PineMediaPlayerComponent(mContext.getApplicationContext());
                mediaPlayer = new PineMediaPlayerProxy(mMediaPlayerTag, mMediaPlayerComponent);
                PineMediaPlayerService.setMediaPlayerByTag(mMediaPlayerTag, mediaPlayer);
            }
            mMediaPlayerComponent.setMediaPlayerView(this);
            if (enableSurface) {
                mPineSurfaceView = new PineSurfaceView(mContext);
                mPineSurfaceView.setMediaPlayerComponent(mMediaPlayerComponent);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                layoutParams.addRule(CENTER_IN_PARENT);
                addView(mPineSurfaceView, layoutParams);
            }
            getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
            getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);

            resetMediaController(controller);
            mIsBoundToPlayer = true;
        }
    }

    private boolean checkIsInit() {
        if (!mIsInit) {
            Toast.makeText(mContext, R.string.init_method_need_call_toast, Toast.LENGTH_SHORT);
        }
        return mIsInit;
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

    public void resetMediaController(PineMediaWidget.IPineMediaController controller) {
        if (checkIsInit()) {
            mMediaController = controller;
            if (controller != null) {
                controller.setMediaPlayerView(this);
                controller.setMediaPlayer(mMediaPlayerComponent);
            }
            if (mMediaPlayerComponent.isAttachViewMode() && mMediaController != null) {
                mMediaController.hide();
            }
            mMediaPlayerComponent.attachMediaController(false, true);
        }
    }

    public PineMediaWidget.IPineMediaPlayer getMediaPlayer() {
        if (!checkIsInit()) {
            return null;
        }
        return (PineMediaWidget.IPineMediaPlayer) mMediaPlayerComponent;
    }

    public void toggleFullScreenMode(boolean isLocked) {
        mIsFullScreenMode = !mIsFullScreenMode;
        if (mMediaPlayerComponent.isAttachViewMode()) {
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

    @Override
    public void onWindowFocusChanged(boolean isFocus) {
        LogUtil.d(TAG, "onWindowFocusChanged isFocus:" + isFocus + ", view:" + this);
        if (mMediaPlayerComponent == null) {
            return;
        }
        if (isFocus) {
            if (!mIsBoundToPlayer) {
                mMediaPlayerComponent.setMediaPlayerView(this);
                getViewTreeObserver().removeOnPreDrawListener(mOnPreDrawListener);
                getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
                resetMediaController(mMediaController);
                mIsBoundToPlayer = true;
            }
            mMediaPlayerComponent.resume();
        } else {
            if (!mMediaPlayerComponent.isAutocephalyPlayMode()) {
                PineMediaPlayerService.destroyMediaPlayerByTag(mMediaPlayerTag);
            }
            mMediaPlayerComponent.detachMediaPlayerView(this);
            mIsBoundToPlayer = false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        LogUtil.d(TAG, "Attached to window");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LogUtil.d(TAG, "Detach from window");
        mIsFullScreenMode = false;
        mIsBoundToPlayer = false;
        mMediaPlayerComponent = null;
        mPineSurfaceView = null;
        mMediaController = null;
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
                if (uniqueDown && !mMediaPlayerComponent.isPlaying()) {
                    mMediaPlayerComponent.start();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (uniqueDown && mMediaPlayerComponent.isPlaying()) {
                    mMediaPlayerComponent.pause();
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
                if (uniqueDown && mMediaPlayerComponent.getMediaPlayerState() ==
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
        if (mMediaPlayerComponent.isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayerComponent.isPlaying()) {
                    mMediaPlayerComponent.pause();
                    mMediaController.show();
                } else {
                    mMediaPlayerComponent.start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayerComponent.isPlaying()) {
                    mMediaPlayerComponent.start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayerComponent.isPlaying()) {
                    mMediaPlayerComponent.pause();
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
                    if (mMediaPlayerComponent.isPlaying() || mMediaPlayerComponent.isPause()) {
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
