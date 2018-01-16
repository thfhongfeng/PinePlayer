package com.pine.player.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.pine.player.bean.PineMediaPlayerBean;

import java.util.Map;

/**
 * Created by tanghongfeng on 2017/9/15.
 * <p>
 * 注意事项：
 * 1、若要保证全屏效果正常，请将该控件置于具有全屏布局能力的父布局中，
 * 且该父全屏布局必须是RelativeLayout,FrameLayout,LinearLayout中的一种
 */

public class PineMediaPlayerView extends RelativeLayout
        implements PineMediaWidget.IPineMediaPlayer {
    private final static String TAG = "PineMediaPlayerView";

    private Context mContext;
    private PineSurfaceView mPineSurfaceView;
    private ViewGroup.LayoutParams mHalfAnchorLayout;

    public PineMediaPlayerView(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    public PineMediaPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    public PineMediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        mPineSurfaceView = new PineSurfaceView(context);
        mPineSurfaceView.setProxy(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mPineSurfaceView, layoutParams);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // 在第一次绘制之前保存布局layoutParams
                if (!mPineSurfaceView.isFullScreenMode() && mHalfAnchorLayout == null) {
                    mHalfAnchorLayout = getLayoutParams();

                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    public void setMediaController(PineMediaWidget.IPineMediaController controller) {
        mPineSurfaceView.setMediaController(controller);
    }

    public void setLocalStreamMode(boolean isLocalStream) {
        mPineSurfaceView.setLocalStreamMode(isLocalStream, 0);
    }

    public void setLocalStreamMode(boolean isLocalStream, int port) {
        mPineSurfaceView.setLocalStreamMode(isLocalStream, port);
    }

    public void setMedia(PineMediaPlayerBean pineMediaPlayerBean) {
        mPineSurfaceView.setMedia(pineMediaPlayerBean, null);
    }

    public void setMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers) {
        mPineSurfaceView.setMedia(pineMediaPlayerBean, headers);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * ----------------   PineMediaWidget.IPineMediaPlayer impl begin   --------------
     **/

    @Override
    public float getSpeed() {
        return mPineSurfaceView.getSpeed();
    }

    @Override
    public void setSpeed(float speed) {
        mPineSurfaceView.setSpeed(speed);
    }

    @Override
    public void start() {
        mPineSurfaceView.start();
    }

    @Override
    public void pause() {
        mPineSurfaceView.pause();
    }

    @Override
    public void suspend() {
        mPineSurfaceView.suspend();
    }

    @Override
    public void resume() {
        mPineSurfaceView.resume();
    }

    @Override
    public void release() {
        mPineSurfaceView.release(true);
    }

    @Override
    public void resetMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                    Map<String, String> headers) {
        mPineSurfaceView.resetMediaAndResume(pineMediaPlayerBean, headers);
    }

    @Override
    public void onActivityPaused() {
        mPineSurfaceView.onActivityPaused();
    }

    @Override
    public int getDuration() {
        return mPineSurfaceView.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mPineSurfaceView.getCurrentPosition();
    }

    @Override
    public int getMediaViewWidth() {
        return mPineSurfaceView.getMediaViewWidth();
    }

    @Override
    public int getMediaViewHeight() {
        return mPineSurfaceView.getMediaViewHeight();
    }

    @Override
    public PineMediaPlayerBean getMediaPlayerBean() {
        return mPineSurfaceView.getMediaPlayerBean();
    }

    @Override
    public void seekTo(int pos) {
        mPineSurfaceView.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mPineSurfaceView.isPlaying();
    }

    @Override
    public boolean isPause() {
        return mPineSurfaceView.isPause();
    }

    @Override
    public void toggleFullScreenMode(boolean isLocked) {
        mPineSurfaceView.toggleFullScreenMode();
        ViewGroup.LayoutParams layoutParams;
        if (mPineSurfaceView.isFullScreenMode()) {
            if (getParent() instanceof RelativeLayout) {
                layoutParams = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
            } else if (getParent() instanceof LinearLayout) {
                layoutParams = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
            } else if (getParent() instanceof FrameLayout) {
                layoutParams = new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
            } else {
                layoutParams = new ViewGroup.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
            }
        } else {
            layoutParams = mHalfAnchorLayout;
        }
        setLayoutParams(layoutParams);
    }

    @Override
    public boolean isFullScreenMode() {
        return mPineSurfaceView.isFullScreenMode();
    }

    @Override
    public int getBufferPercentage() {
        return mPineSurfaceView.getBufferPercentage();
    }

    @Override
    public boolean canPause() {
        return mPineSurfaceView.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return mPineSurfaceView.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return mPineSurfaceView.canSeekForward();
    }

    @Override
    public int getAudioSessionId() {
        return mPineSurfaceView.getAudioSessionId();
    }

    @Override
    public boolean isInPlaybackState() {
        return mPineSurfaceView.isInPlaybackState();
    }

    @Override
    public int getMediaPlayerState() {
        return mPineSurfaceView.getMediaPlayerState();
    }

    @Override
    public PineMediaViewLayout getMediaAdaptionLayout() {
        return mPineSurfaceView.getMediaAdaptionLayout();
    }

    @Override
    public void setMediaPlayerListener(PineMediaWidget.PineMediaPlayerListener listener) {
        mPineSurfaceView.setMediaPlayerListener(listener);
    }
    /** ----------------   PineMediaWidget.IPineMediaPlayer impl end   -------------- **/

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
                    + ", bottom:" + bottom +"}";
        }
    }
}
