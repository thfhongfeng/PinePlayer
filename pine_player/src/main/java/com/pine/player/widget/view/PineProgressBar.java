package com.pine.player.widget.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tanghongfeng on 2018/4/16.
 */

public abstract class PineProgressBar extends View {

    public PineProgressBar(Context context) {
        super(context);
    }

    public PineProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PineProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract int getMax();

    public abstract void setMax(int max);

    public abstract int getProgress();

    public abstract void setProgress(int progress);

    public abstract int getSecondaryProgress();

    public abstract void setSecondaryProgress(int progress);

    public abstract void setOnProgressBarChangeListener(OnProgressBarChangeListener listener);

    public interface OnProgressBarChangeListener {
        void onProgressChanged(PineProgressBar progressBar, int progress, boolean fromUser);

        void onStartTrackingTouch(PineProgressBar progressBar);

        void onStopTrackingTouch(PineProgressBar progressBar);
    }
}
