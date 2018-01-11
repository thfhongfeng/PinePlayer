package com.pine.player.widget.viewholder;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PineBackgroundViewHolder {
    // 背景根View
    private ViewGroup container;
    // 背景图
    private ImageView backgroundImageView;

    public ViewGroup getContainer() {
        return container;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }

    public ImageView getBackgroundImageView() {
        return backgroundImageView;
    }

    public void setBackgroundImageView(ImageView backgroundImageView) {
        this.backgroundImageView = backgroundImageView;
    }
}