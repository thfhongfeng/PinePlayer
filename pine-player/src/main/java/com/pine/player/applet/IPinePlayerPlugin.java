package com.pine.player.applet;

import com.pine.player.widget.viewholder.PinePluginViewHolder;

/**
 * Created by tanghongfeng on 2018/1/11.
 */

public interface IPinePlayerPlugin {
    void onInit();
    PinePluginViewHolder createViewHolder(boolean isFullScreen);
    void onRefresh(int position);
    void onRelease();
}
