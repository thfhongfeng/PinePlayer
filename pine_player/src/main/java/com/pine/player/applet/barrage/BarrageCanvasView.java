package com.pine.player.applet.barrage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pine.player.applet.barrage.bean.PartialDisplayBarrageNode;
import com.pine.player.applet.barrage.bean.PineBarrageBean;
import com.pine.player.util.LogUtils;

/**
 * Created by tanghongfeng on 2018/2/1.
 */

public class BarrageCanvasView extends RelativeLayout {
    private final static String TAG = LogUtils.makeLogTag(BarrageCanvasView.class);

    private Context mContext;
    private boolean mIsPrepare = false;
    private IBarrageItemViewListener mBarrageItemViewListener;
    private int mCurHeight = -1;
    private int mDisplayStartPx = 0;
    private int mDisplayTotalHeight = -1;
    private float mDisplayStartHeightPercent = -1f;
    private float mDisplayEndHeightPercent = -1f;
    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mCurHeight != getHeight()) {
                mCurHeight = getHeight();
                mDisplayStartPx = (int) (getHeight() * mDisplayStartHeightPercent);
                int newDisplayEndHeight = (int) (getHeight() * mDisplayEndHeightPercent);
                mDisplayTotalHeight = newDisplayEndHeight - mDisplayStartPx + 1;
            }
        }
    };
    private int mTextViewHeight = 0;
    // 双链表
    private PartialDisplayBarrageNode mDisplayableHeadNode;
    private PartialDisplayBarrageNode mRecycleHeadNode;

    public BarrageCanvasView(Context context, int displayStartPx, int displayTotalHeight) {
        super(context);
        mContext = context;
        mDisplayStartPx = displayStartPx < 0 ? 0 : displayStartPx;
        mDisplayTotalHeight = displayTotalHeight;
        init();
    }

    public BarrageCanvasView(Context context, float displayStartPercent, float displayEndPercent) {
        super(context);
        mContext = context;
        mDisplayStartHeightPercent = displayStartPercent < displayStartPercent ? 0.0f : displayStartPercent;
        mDisplayEndHeightPercent = displayEndPercent > 1.0f ? 1.0f : displayEndPercent < 0.0f ? 0.0f : displayEndPercent;
        init();
    }

    public BarrageCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public BarrageCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        if (mDisplayTotalHeight == -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            }
            getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    private void prepare() {
        clearPartialDisplayBarrageNode(mDisplayableHeadNode);
        clearPartialDisplayBarrageNode(mRecycleHeadNode);
        if (mDisplayTotalHeight > 0) {
            mDisplayableHeadNode = new PartialDisplayBarrageNode(null, null, mDisplayStartPx, 0, mDisplayTotalHeight);
        } else {
            mDisplayableHeadNode = new PartialDisplayBarrageNode(null, null, mDisplayStartPx, 0, 200);
        }
        LogUtils.d(TAG, "prepare StartPxIndex:" + mDisplayableHeadNode.getStartPixIndex()
                + ", UntilNextRemainderPix:" + mDisplayableHeadNode.getUntilNextRemainderPix());
        mIsPrepare = true;
    }

    public void setBarrageItemViewListener(IBarrageItemViewListener barrageItemViewListener) {
        mBarrageItemViewListener = barrageItemViewListener;
    }

    public boolean addBarrageItemView(final PineBarrageBean pineBarrageBean, float speed) {
        if (!mIsPrepare) {
            prepare();
        }
        if (getWidth() <= 0) {
            return false;
        }
        if (pineBarrageBean.getTextHeight() > 0) {
            mTextViewHeight = pineBarrageBean.getTextHeight();
        } else if (mTextViewHeight <= 0) {
            TextView textView = new TextView(mContext);
            textView.setTextColor(Color.WHITE);
            textView.setText(Html.fromHtml(pineBarrageBean.getTextBody()));
            int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            textView.measure(w, h);
            mTextViewHeight = textView.getMeasuredHeight();
        }
        final PartialDisplayBarrageNode node = getMatchedNode(mTextViewHeight);
        if (node == null) {
            return false;
        }
        final int width;
        final TextView textView = new TextView(mContext);
        textView.setTextColor(Color.WHITE);
        textView.setText(Html.fromHtml(pineBarrageBean.getTextBody()));
        if (pineBarrageBean.getTextWidth() > 0) {
            width = pineBarrageBean.getTextWidth();
        } else {
            // 单个TextView的measure不会太耗时，但是一次onTime时循环进行100个以上的measure会导致性能急剧下降
            // 要尽量减小每次onTime时的measure动作。为此目前只能将TextView的高度固定或者由弹幕文字提供者自行提供。
            int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            textView.measure(w, h);
            width = textView.getMeasuredWidth();
        }

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.topMargin = node.getStartPixIndex();
        int translationX = getWidth() + width + 40;
        if (pineBarrageBean.getDirection() == PineBarrageBean.FROM_RIGHT_TO_LEFT) {
            translationX = -translationX;
            lp.leftMargin = getWidth() + 20;
        } else {
            lp.leftMargin = -width - 20;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationX", 0, translationX);
        textView.setLayoutParams(lp);
        addView(textView);

        animator.setDuration(Math.abs(pineBarrageBean.getDuration() * translationX / (long) (getWidth() * speed)));
        final LinearInterpolator linearInterpolator = new LinearInterpolator();
        animator.setInterpolator(linearInterpolator);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                BarrageCanvasView.this.removeView(textView);
                if (mBarrageItemViewListener != null) {
                    mBarrageItemViewListener.onItemViewAnimatorEnd(pineBarrageBean);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                BarrageCanvasView.this.removeView(textView);
                if (mBarrageItemViewListener != null) {
                    mBarrageItemViewListener.onAnimationCancel(pineBarrageBean);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (pineBarrageBean.getPartialDisplayBarrageNode() != null) {
                    float changeValue = Math.abs((float) animation.getAnimatedValue());
                    if (changeValue > width + 20) {
                        freeNodeSpace(pineBarrageBean.getPartialDisplayBarrageNode());
                        pineBarrageBean.setPartialDisplayBarrageNode(null);
                    }
                }
            }
        });
        pineBarrageBean.setPartialDisplayBarrageNode(node);
        pineBarrageBean.setItemView(textView);
        pineBarrageBean.setAnimator(animator);
        return true;
    }

    public void clear() {
        removeAllViews();
        clearPartialDisplayBarrageNode(mDisplayableHeadNode);
        mDisplayableHeadNode = null;
        clearPartialDisplayBarrageNode(mRecycleHeadNode);
        mRecycleHeadNode = null;
        mIsPrepare = false;
    }

    private void clearPartialDisplayBarrageNode(PartialDisplayBarrageNode node) {
        PartialDisplayBarrageNode tmpNode = node;
        while (node != null) {
            node = node.getNextNode();
            tmpNode.setPreNode(null);
            tmpNode.setNextNode(null);
            tmpNode = node;
        }
    }

    private PartialDisplayBarrageNode getMatchedNode(int height) {
        PartialDisplayBarrageNode node = mDisplayableHeadNode;
        while (node != null && node.getUntilNextRemainderPix() < height) {
            node = node.getNextNode();
        }
        if (node != null) {
            PartialDisplayBarrageNode newNode;
            if (mRecycleHeadNode != null) {
                newNode = mRecycleHeadNode;
                mRecycleHeadNode = mRecycleHeadNode.getNextNode();
            } else {
                newNode = new PartialDisplayBarrageNode();
            }
            newNode.setPreNode(node);
            newNode.setNextNode(node.getNextNode());
            newNode.setNodeUsedPix(height);
            newNode.setUntilNextRemainderPix(node.getUntilNextRemainderPix() - height);
            newNode.setStartPixIndex(node.getStartPixIndex() + node.getNodeUsedPix());
            node.setNextNode(newNode);
            node.setUntilNextRemainderPix(0);
            if (newNode.getNextNode() != null) {
                newNode.getNextNode().setPreNode(newNode);
            }
            LogUtils.d(TAG, "matched node:" + newNode);
            return newNode;
        } else {
            return null;
        }
    }

    public void freeNodeSpace(PartialDisplayBarrageNode node) {
        if (node == null && mDisplayableHeadNode.equals(node)) {
            return;
        }
        PartialDisplayBarrageNode preNode = node.getPreNode();
        preNode.setUntilNextRemainderPix(preNode.getUntilNextRemainderPix() + node.getNodeUsedPix()
                + node.getUntilNextRemainderPix());
        preNode.setNextNode(node.getNextNode());
        if (node.getNextNode() != null) {
            node.getNextNode().setPreNode(preNode);
        }
        LogUtils.d(TAG, "freeNodeSpace node:" + node + ", after free, pre node:" + preNode);
        node.setStartPixIndex(-1);
        node.setNodeUsedPix(-1);
        node.setUntilNextRemainderPix(-1);
        node.setNextNode(null);
        node.setPreNode(null);
        if (mRecycleHeadNode != null) {
            node.setNextNode(mRecycleHeadNode);
            mRecycleHeadNode.setPreNode(node);
        }
        mRecycleHeadNode = node;
    }

    public static interface IBarrageItemViewListener {
        void onItemViewAnimatorEnd(PineBarrageBean pineBarrageBean);

        void onAnimationCancel(PineBarrageBean pineBarrageBean);
    }
}
