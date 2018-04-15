package com.pine.player.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.pine.player.component.PineMediaPlayerComponent;
import com.pine.player.util.LogUtil;

/**
 * Created by tanghongfeng on 2017/8/14.
 * <p>
 * 注意事项：
 * 1、使用此控件必须在外层包一层RelativeLayout
 * 2、若要保证全屏效果正常，请将外包的RelativeLayout一起置于具有全屏布局能力的父布局中，
 * 且该全屏布局必须是RelativeLayout,FrameLayout,LinearLayout中的一种
 */

public class PineSurfaceView extends SurfaceView {
    private final static String TAG = "PineSurfaceView";
    private Context mContext;
    private PineMediaPlayerComponent mMediaPlayerComponent = null;
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            LogUtil.d(TAG, "surfaceChanged");
            if (mMediaPlayerComponent != null) {
                mMediaPlayerComponent.onSurfaceChanged(PineSurfaceView.this, format, w, h);
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceCreated");
            if (mMediaPlayerComponent != null) {
                mMediaPlayerComponent.onSurfaceCreated(PineSurfaceView.this);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceDestroyed");
            if (mMediaPlayerComponent != null) {
                mMediaPlayerComponent.onSurfaceDestroyed(PineSurfaceView.this);
            }
        }
    };
    private int mMediaWidth, mMediaHeight;
    // MediaView在onMeasure中调整后的布局属性，只有在onMeasure之后获取才有效
    private PineMediaPlayerView.PineMediaViewLayout mAdaptionMediaLayout =
            new PineMediaPlayerView.PineMediaViewLayout();

    protected PineSurfaceView(Context context) {
        super(context);
        mContext = context;
        initMediaView();
    }

    protected PineSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected PineSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initMediaView();
    }

    private void initMediaView() {
        mMediaWidth = 0;
        mMediaHeight = 0;
        getHolder().removeCallback(mSHCallback);
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setMediaPlayerComponent(PineMediaPlayerComponent mediaPlayerComponent) {
        mMediaPlayerComponent = mediaPlayerComponent;
    }

    public PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout() {
        return mAdaptionMediaLayout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMediaPlayerComponent == null) {
            return;
        }
        mMediaWidth = mMediaPlayerComponent.getMediaViewWidth();
        mMediaHeight = mMediaPlayerComponent.getMediaViewHeight();
        int width = getDefaultSize(mMediaWidth, widthMeasureSpec);
        int height = getDefaultSize(mMediaHeight, heightMeasureSpec);
        if (mMediaWidth > 0 && mMediaHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mMediaWidth * height < width * mMediaHeight) {
                    //LogUtil.i("@@@", "image too wide, correcting");
                    width = height * mMediaWidth / mMediaHeight;
                } else if (mMediaWidth * height > width * mMediaHeight) {
                    //LogUtil.i("@@@", "image too tall, correcting");
                    height = width * mMediaHeight / mMediaWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mMediaHeight / mMediaWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mMediaWidth / mMediaHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual media size
                width = mMediaWidth;
                height = mMediaHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mMediaWidth / mMediaHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mMediaHeight / mMediaWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mAdaptionMediaLayout.width = getMeasuredWidth();
        mAdaptionMediaLayout.height = getMeasuredHeight();
        mAdaptionMediaLayout.left = getLeft();
        mAdaptionMediaLayout.right = getRight();
        mAdaptionMediaLayout.top = getTop();
        mAdaptionMediaLayout.bottom = getBottom();
    }

    @Override
    public void draw(Canvas canvas) {
        LogUtil.d(TAG, "draw");
        super.draw(canvas);
    }
}
