package com.pine.player.applet.barrage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pine.player.applet.barrage.bean.PartialDisplayBarrageNode;
import com.pine.player.applet.barrage.bean.PineBarrageBean;
import com.pine.player.util.LogUtils;

import java.util.List;

/**
 * Created by tanghongfeng on 2018/2/1.
 */

public class BarrageCanvasView extends RelativeLayout {
    private final static String TAG = "BarrageCanvasView";

    private Context mContext;
    private IBarrageItemViewListener mBarrageItemViewListener;
    private int mDisplayTotalHeight = 200;

    private PartialDisplayBarrageNode mDisplayableHeadNode;
    private PartialDisplayBarrageNode mRecycleHeadNode;

    public BarrageCanvasView(Context context, int displayTotalHeight) {
        super(context);
        mContext = context;
        mDisplayTotalHeight = displayTotalHeight;
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
        mDisplayableHeadNode = new PartialDisplayBarrageNode(null, null, 0, 0, mDisplayTotalHeight);
    }

    public void setBarrageItemViewListener(IBarrageItemViewListener barrageItemViewListener) {
        mBarrageItemViewListener = barrageItemViewListener;
    }

    public boolean addBarrageItemView(final PineBarrageBean pineBarrageBean) {
        final TextView textView = new TextView(mContext);
        textView.setTextColor(Color.WHITE);
        textView.setText(Html.fromHtml(pineBarrageBean.getTextBody()));
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(w, h);
        int height = textView.getMeasuredHeight();
        final int width = textView.getMeasuredWidth();
        final PartialDisplayBarrageNode node = getMatchedNode(height);
        if (node == null) {
            return false;
        }
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.topMargin = node.getStartPixIndex();
        int translationX = getWidth() + width + 40;
        if (pineBarrageBean.getDirection() == PineBarrageBean.FROM_RIGHT_TO_LEFT) {
            translationX = -translationX;
            lp.leftMargin = getWidth() + 20;
        } else if (pineBarrageBean.getDirection() == PineBarrageBean.FROM_LEFT_TO_RIGHT) {
            lp.leftMargin = -width - 20;
        } else {
            return false;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationX", 0, translationX);
        textView.setLayoutParams(lp);
        addView(textView);

        animator.setDuration(Math.abs(pineBarrageBean.getDuration() / getWidth() * translationX));
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

    public void recycleShownNode(List<PineBarrageBean> shownBarrageList) {
        PineBarrageBean pineBarrageBean;
        View itemView;
        for (int i = 0; i < shownBarrageList.size(); i++) {
            pineBarrageBean = shownBarrageList.get(i);
            itemView = pineBarrageBean.getItemView();
            if (itemView != null && pineBarrageBean.getPartialDisplayBarrageNode() != null) {
                float translationX = Math.abs(itemView.getTranslationX());
                if (translationX > itemView.getWidth() + 20) {
                    freeNodeSpace(pineBarrageBean.getPartialDisplayBarrageNode());
                    pineBarrageBean.setPartialDisplayBarrageNode(null);
                }
            }
        }
    }

    public void clear() {
        removeAllViews();
        mDisplayableHeadNode = new PartialDisplayBarrageNode(null, null, 0, 0,
                mDisplayTotalHeight);
    }

    private PartialDisplayBarrageNode getMatchedNode(int height) {
        PartialDisplayBarrageNode node = mDisplayableHeadNode;
        while (node != null && node.getUntilNextRemainderPix() < height) {
            LogUtils.d(TAG, "node: " + node);
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
