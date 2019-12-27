package com.pine.player.widget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.pine.player.R;

/**
 * Created by tanghongfeng on 2018/4/16.
 */

public class PineRingProgressBar extends PineProgressBar {
    private int mMax = 1000;
    private int mProgress = 10;
    private int mSecondaryProgress = 0;
    private OnProgressBarChangeListener mProgressChangeListener;
    private Paint mBgPaint;
    private BitmapShader mBitmapShader;
    private Paint mRingBgPaint;
    private Paint mRingPaint;
    private Paint mSecondaryRingPaint;
    private int mCircle;
    // 半径
    private int mRadius;
    // 圆环宽度
    private int mStrokeWidth;
    // 其实角度，-90为北
    private int mStartAngle = -90;
    private RectF mArcRectF;
    // 圆环进度条背景色
    private int mRingBgColor;
    // 圆环第一进度条颜色
    private int mRingColor;
    // 圆环第二进度条颜色
    private int mSecondaryRingColor;
    // 圆环中心背景图
    private Drawable mBgDrawable;
    private int mGravity;

    public PineRingProgressBar(Context context) {
        super(context);
    }

    public PineRingProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PineProgressBar);
        mGravity = typedArray.getInt(R.styleable.PineProgressBar_android_gravity, Gravity.CENTER);
        mRadius = typedArray.getDimensionPixelOffset(R.styleable.PineProgressBar_radius, -1);
        mStrokeWidth = typedArray.getDimensionPixelOffset(R.styleable.PineProgressBar_strokeWidth, 4);
        mRingBgColor = typedArray.getColor(R.styleable.PineProgressBar_backgroundColor, 0xFFFFFFFF);
        mRingColor = typedArray.getColor(R.styleable.PineProgressBar_progressColor, 0xFFFFFFFF);
        mSecondaryRingColor = typedArray.getColor(R.styleable.PineProgressBar_secondaryColor, 0xFFFFFFFF);
        mStartAngle = typedArray.getInt(R.styleable.PineProgressBar_startAngle, -90);
        mBgDrawable = typedArray.getDrawable(R.styleable.PineProgressBar_fillDrawable);
        typedArray.recycle();
        init(context);
    }

    public PineRingProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRingBgPaint = new Paint();
        mRingBgPaint.setAntiAlias(true);
        mRingBgPaint.setColor(mRingBgColor);
        mRingBgPaint.setStyle(Paint.Style.STROKE);
        mRingBgPaint.setStrokeWidth(mStrokeWidth);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(mStrokeWidth);
        //mRingPaint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式，有圆 有方

        mSecondaryRingPaint = new Paint();
        mSecondaryRingPaint.setAntiAlias(true);
        mSecondaryRingPaint.setColor(mSecondaryRingColor);
        mSecondaryRingPaint.setStyle(Paint.Style.STROKE);
        mSecondaryRingPaint.setStrokeWidth(mStrokeWidth);
        //mSecondaryRingPaint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式，有圆 有方

        if (mBgDrawable != null) {
            mBgPaint = new Paint();
            mSecondaryRingPaint.setAntiAlias(true);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measure(widthMeasureSpec);
        measureWidth = measureWidth % 2 == 0 ? measureWidth : measureWidth - 1;
        int measureHeight = measure(heightMeasureSpec);
        measureHeight = measureHeight % 2 == 0 ? measureHeight : measureHeight - 1;
        int measureRadius = Math.min(measureWidth, measureHeight);
        setMeasuredDimension(measureRadius, measureRadius);
    }

    // 当wrap_content的时候，view的大小根据半径大小改变，但最大不会超过屏幕
    private int measure(int measureSpec) {
        int result = 0;
        //1、先获取测量模式 和 测量大小
        //2、如果测量模式是MatchParent 或者精确值，则宽为测量的宽
        //3、如果测量模式是WrapContent ，则宽为 直径值 与 测量宽中的较小值；否则当直径大于测量宽时，会绘制到屏幕之外；
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY || mRadius < 0) {
            return specSize;
        } else {
            result = (int) (mRadius + mRingPaint.getStrokeWidth()) * 2;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //1、如果半径大于圆心的横坐标，需要手动缩小半径的值，否则画到屏幕之外；
        //2、改变了半径，则需要重新设置字体大小；
        //3、改变了半径，则需要重新设置外圆环的宽度
        //4、画背景圆的外接矩形，用来画圆环；
        int length = getMeasuredWidth();
        mCircle = length / 2;
        int strokeWidth = (int) mRingPaint.getStrokeWidth();
        int halfStroke = strokeWidth / 2;
        if (mRadius > mCircle) {
            strokeWidth = (int) 0.1 * mRadius;
            strokeWidth = strokeWidth < 1 ? 1 : strokeWidth;
            mRadius = mCircle - halfStroke;
            mRingBgPaint.setStrokeWidth(strokeWidth);
            mRingPaint.setStrokeWidth(strokeWidth);
            mSecondaryRingPaint.setStrokeWidth(strokeWidth);
        } else if (mRadius < 0) {
            mRadius = mCircle - halfStroke;
        }
        float leftX = halfStroke;
        float leftY = halfStroke;
        switch (mGravity) {
            case Gravity.LEFT:
                leftX = halfStroke;
                leftY = mCircle - mRadius;
                break;
            case Gravity.RIGHT:
                leftX = length - mRadius * 2 - halfStroke;
                leftY = mCircle - mRadius;
                break;
            case Gravity.TOP:
                leftX = mCircle - mRadius;
                leftY = halfStroke;
                break;
            case Gravity.BOTTOM:
                leftX = mCircle - mRadius;
                leftY = length - mRadius * 2 - halfStroke;
                break;
            default:
                leftX = mCircle - mRadius;
                leftY = mCircle - mRadius;
                break;
        }
        mArcRectF = new RectF(leftX, leftY,
                mRadius * 2 + leftX, mRadius * 2 + leftY);

        if (mBgDrawable != null) {
            Bitmap bitmap;
            if (mBgDrawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) mBgDrawable;
                bitmap = bd.getBitmap();
            } else {
                int w = mBgDrawable.getIntrinsicWidth();
                int h = mBgDrawable.getIntrinsicHeight();
                w = w <= 0 ? getMeasuredWidth() : w;
                h = h <= 0 ? getMeasuredWidth() : h;
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                mBgDrawable.setBounds(0, 0, w, h);
                mBgDrawable.draw(canvas);
            }
            //初始化BitmapShader，传入bitmap对象
            mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            //计算缩放比例
            float scale = getMeasuredWidth() / (float) Math.min(bitmap.getHeight(), bitmap.getWidth());
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            mBitmapShader.setLocalMatrix(matrix);
            mBgPaint.setShader(mBitmapShader);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1、画背景
        //2、画进度圆环背景
        //3、画进度圆环
        //4、画第二进度圆环
        //5、判断进度，重新绘制
        if (mBgPaint != null) {
            canvas.drawCircle(mCircle, mCircle, mCircle, mBgPaint);
        }
        canvas.drawArc(mArcRectF, mStartAngle, 360, false, mRingBgPaint);

        float currentAngle = getProgress() * 360.0f / getMax();
        canvas.drawArc(mArcRectF, mStartAngle, currentAngle, false, mRingPaint);
        float currentSecondAngle = getSecondaryProgress() * 360.0f / getMax();
        canvas.drawArc(mArcRectF, mStartAngle, currentSecondAngle, false, mSecondaryRingPaint);
    }

    @Override
    public int getMax() {
        return mMax;
    }

    @Override
    public void setMax(int max) {
        mMax = max;
    }

    @Override
    public int getProgress() {
        return mProgress;
    }

    @Override
    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();
    }

    @Override
    public int getSecondaryProgress() {
        return mSecondaryProgress;
    }

    @Override
    public void setSecondaryProgress(int progress) {
        mSecondaryProgress = progress;
        postInvalidate();
    }

    @Override
    public void setOnProgressBarChangeListener(OnProgressBarChangeListener listener) {
        mProgressChangeListener = listener;
    }
}
