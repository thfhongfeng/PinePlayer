package com.pine.player.widget.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PineWaitingProgressViewHolder {
    // 播放等待根View
    private ViewGroup container;

    public ViewGroup getContainer() {
        return container;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }
}