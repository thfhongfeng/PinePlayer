package com.pine.player.widget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.pine.player.R;

/**
 * Created by tanghongfeng on 2018/4/16.
 */

public class PineRingProgressBar extends PineProgressBar {
    private int mMax = 1000;
    private int mProgress = 50;
    private int mSecondaryProgress = 0;
    private OnProgressBarChangeListener mProgressChangeListener;

    private Paint mRingBgPaint;
    private Paint mRingPaint;
    private Paint mSecondaryRingPaint;

    private float mRadius;
    private float mStrokeWidth;
    private int mStartAngle = -90;
    private int mCircleX;
    private int mCircleY;
    private RectF mArcRectF;

    private int mRingBgColor;
    private int mRingColor;
    private int mSecondaryRingColor;

    public PineRingProgressBar(Context context) {
        super(context);
    }

    public PineRingProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PineProgressBar);
        mRadius = typedArray.getDimension(R.styleable.PineProgressBar_radius, 80);
        mStrokeWidth = typedArray.getDimension(R.styleable.PineProgressBar_strokeWidth, 10);
        mRingBgColor = typedArray.getColor(R.styleable.PineProgressBar_backgroundColor, 0xFFFFFFFF);
        mRingColor = typedArray.getColor(R.styleable.PineProgressBar_progressColor, 0xFFFFFFFF);
        mSecondaryRingColor = typedArray.getColor(R.styleable.PineProgressBar_secondaryColor, 0xFFFFFFFF);
        mStartAngle = typedArray.getInt(R.styleable.PineProgressBar_startAngle, -90);
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
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(widthMeasureSpec));
    }

    // 当wrap_content的时候，view的大小根据半径大小改变，但最大不会超过屏幕
    private int measure(int measureSpec) {
        int result = 0;
        //1、先获取测量模式 和 测量大小
        //2、如果测量模式是MatchParent 或者精确值，则宽为测量的宽
        //3、如果测量模式是WrapContent ，则宽为 直径值 与 测量宽中的较小值；否则当直径大于测量宽时，会绘制到屏幕之外；
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
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
        mCircleX = getMeasuredWidth() / 2;
        mCircleY = getMeasuredHeight() / 2;
        if (mRadius > mCircleX) {
            mRadius = mCircleX;
            mRadius = (int) (mCircleX - 0.075 * mRadius);
            mRingBgPaint.setStrokeWidth((float) (0.075 * mRadius));
            mRingPaint.setStrokeWidth((float) (0.075 * mRadius));
            mSecondaryRingPaint.setStrokeWidth((float) (0.075 * mRadius));
        }
        mArcRectF = new RectF(mCircleX - mRadius, mCircleY - mRadius,
                mCircleX + mRadius, mCircleY + mRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1、画圆环背景
        //2、画进度圆环
        //3、画第二进度圆环
        //4、判断进度，重新绘制
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
