package com.pine.pineplayer.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by tanghongfeng on 2017/8/10.
 */

public class AdvanceDecoration extends RecyclerView.ItemDecoration {

    public final static int VERTICAL = 1;
    public final static int HORIZONTAL = 2;

    private Context mContext;
    private Rect mItemOutRect;
    private Drawable mDivider;
    private BoundDrawables mBoundDrawables;
    private CornerDrawables mCornerDrawables;
    private boolean mIsDrawStartDivider;

    public AdvanceDecoration(Context context) {
        this(context, new Rect(0, 0, 0, 0), null, null, null);
    }

    public AdvanceDecoration(Context context, int dividerResId, int dividerWidth,
                             int orientation, boolean isDrawStartDivider) {
        this(context, context.getResources().getDrawable(dividerResId), dividerWidth,
                orientation, isDrawStartDivider);
    }

    public AdvanceDecoration(Context context, Drawable divider, int dividerWidth,
                             int orientation, boolean isDrawStartDivider) {
        this(context, null, null, null, null);
        switch (orientation) {
            case HORIZONTAL:
                this.mItemOutRect = new Rect(0, 0, dividerWidth, 0);
                this.mDivider = divider;
                break;
            default:
                this.mItemOutRect = new Rect(0, 0, 0, dividerWidth);
                this.mDivider = divider;
                break;
        }
        mIsDrawStartDivider = isDrawStartDivider;
    }

    public AdvanceDecoration(Context context, int dividerWidth, Drawable gridDivider) {
        this(context, null, null, null, null);
        int tmp = dividerWidth / 2;
        this.mItemOutRect = new Rect(tmp, tmp, tmp, tmp);
        this.mBoundDrawables = new BoundDrawables(gridDivider, gridDivider, gridDivider, gridDivider);
        this.mCornerDrawables = new CornerDrawables(gridDivider, gridDivider, gridDivider, gridDivider);
    }

    public AdvanceDecoration(Context context, Rect outRect) {
        this(context, outRect, null, null, null);
    }

    public AdvanceDecoration(Context context, Rect outRect, BoundDrawables boundDrawables) {
        this(context, outRect, null, boundDrawables, null);
    }

    public AdvanceDecoration(Context context, Rect outRect, BoundDrawables boundDrawables,
                             CornerDrawables cornerDrawables) {
        this(context, outRect, null, boundDrawables, cornerDrawables);
    }

    private AdvanceDecoration(Context context, Rect itemOutRect, Drawable divider,
                              BoundDrawables boundDrawables, CornerDrawables cornerDrawables) {
        this.mContext = context;
        this.mItemOutRect = itemOutRect;
        this.mDivider = divider;
        this.mBoundDrawables = boundDrawables;
        this.mCornerDrawables = cornerDrawables;
    }

    // 设置item之间的边距（只是设置边距Rec大小，边距的divider图片等需要在onDraw或者onDrawOver单独画出来）
    // getItemOffsets通过outRect.set(l,t,r,b)指定
    // item view的left，top，right，bottom方向与其它item view的边距
    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAt(0) == view && mDivider != null && mIsDrawStartDivider) {
            outRect.set(new Rect(mItemOutRect.right, mItemOutRect.bottom,
                    mItemOutRect.right, mItemOutRect.bottom));
        } else {
            outRect.set(mItemOutRect);
        }
    }

    // 在draw item之前先draw装饰（比如画边距的图片等）
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mItemOutRect.left <= 0 && mItemOutRect.right <= 0 && mItemOutRect.top <= 0
                && mItemOutRect.bottom <= 0 || (mBoundDrawables == null &&
                mCornerDrawables == null && mDivider == null)) {
            return;
        }
        int count = parent.getChildCount();
        if (count > 0 && mIsDrawStartDivider) {
            View view = parent.getChildAt(0);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemLeft = view.getLeft() - layoutParams.leftMargin;
            int itemRight = view.getRight() + layoutParams.rightMargin;
            int itemTop = view.getTop() - layoutParams.topMargin;
            int itemBottom = view.getBottom() + layoutParams.bottomMargin;
            if (mDivider != null) {
                if (mItemOutRect.right > 0) {
                    mDivider.setBounds(itemLeft - mItemOutRect.right, itemTop,
                            itemLeft, itemBottom);
                    mDivider.draw(c);
                } else if (mItemOutRect.bottom > 0) {
                    mDivider.setBounds(itemLeft, itemTop - mItemOutRect.bottom, itemRight,
                            itemTop);
                    mDivider.draw(c);
                }
            }
        }
        for (int i = 0; i < count; i++) {
            View view = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemLeft = view.getLeft() - layoutParams.leftMargin;
            int itemRight = view.getRight() + layoutParams.rightMargin;
            int itemTop = view.getTop() - layoutParams.topMargin;
            int itemBottom = view.getBottom() + layoutParams.bottomMargin;
            if (mDivider != null) {
                if (mItemOutRect.right > 0) {
                    mDivider.setBounds(itemRight, itemTop,
                            itemRight + mItemOutRect.right, itemBottom);
                    mDivider.draw(c);
                } else if (mItemOutRect.bottom > 0) {
                    mDivider.setBounds(itemLeft, itemBottom, itemRight,
                            itemBottom + mItemOutRect.bottom);
                    mDivider.draw(c);
                }
            }
            if (mBoundDrawables != null) {
                if (mBoundDrawables.mLeft != null) {
                    mBoundDrawables.mLeft.setBounds(itemLeft - mItemOutRect.left,
                            itemTop, itemLeft, itemBottom);
                    mBoundDrawables.mLeft.draw(c);
                }
                if (mBoundDrawables.mRight != null) {
                    mBoundDrawables.mRight.setBounds(itemRight, itemTop,
                            itemRight + mItemOutRect.right, itemBottom);
                    mBoundDrawables.mRight.draw(c);
                }
                if (mBoundDrawables.mTop != null) {
                    mBoundDrawables.mTop.setBounds(itemLeft,
                            itemTop - mItemOutRect.top, itemRight, itemTop);
                    mBoundDrawables.mTop.draw(c);
                }
                if (mBoundDrawables.mBottom != null) {
                    mBoundDrawables.mBottom.setBounds(itemLeft, itemBottom, itemRight,
                            itemBottom + mItemOutRect.bottom);
                    mBoundDrawables.mBottom.draw(c);
                }
            }
            if (mCornerDrawables != null) {
                if (mCornerDrawables.mLeftTop != null) {
                    mCornerDrawables.mLeftTop.setBounds(itemLeft - mItemOutRect.left,
                            itemTop - mItemOutRect.top, itemLeft, itemTop);
                    mCornerDrawables.mLeftTop.draw(c);
                }
                if (mCornerDrawables.mLeftBottom != null) {
                    mCornerDrawables.mLeftBottom.setBounds(itemLeft - mItemOutRect.left,
                            itemBottom, itemLeft, itemBottom + mItemOutRect.bottom);
                    mCornerDrawables.mLeftBottom.draw(c);
                }
                if (mCornerDrawables.mRightTop != null) {
                    mCornerDrawables.mRightTop.setBounds(itemRight,
                            itemTop - mItemOutRect.top, itemRight + mItemOutRect.right, itemTop);
                    mCornerDrawables.mRightTop.draw(c);
                }
                if (mCornerDrawables.mRightBottom != null) {
                    mCornerDrawables.mRightBottom.setBounds(itemRight, itemBottom,
                            itemRight + mItemOutRect.right, itemBottom + mItemOutRect.bottom);
                    mCornerDrawables.mRightBottom.draw(c);
                }
            }
        }
    }

    // 在draw item之后再draw装饰
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

    }

    public class BoundDrawables {
        Drawable mLeft;
        Drawable mTop;
        Drawable mRight;
        Drawable mBottom;

        public BoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
        }
    }

    public class CornerDrawables {
        Drawable mLeftTop;
        Drawable mRightTop;
        Drawable mLeftBottom;
        Drawable mRightBottom;

        public CornerDrawables(Drawable leftTop, Drawable rightTop,
                               Drawable leftBottom, Drawable rightBottom) {
            mLeftTop = leftTop;
            mRightTop = rightTop;
            mLeftBottom = leftBottom;
            mRightBottom = rightBottom;
        }
    }
}
