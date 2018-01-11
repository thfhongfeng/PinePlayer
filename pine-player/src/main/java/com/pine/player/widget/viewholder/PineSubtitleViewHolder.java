package com.pine.player.widget.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PineSubtitleViewHolder {
    // 外挂字幕根View
    protected ViewGroup container;
    // 外挂字幕显示控件
    private View subtitleText;

    public ViewGroup getContainer() {
        return container;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }

    public View getSubtitleText() {
        return subtitleText;
    }

    public void setSubtitleText(View subtitleText) {
        this.subtitleText = subtitleText;
    }
}