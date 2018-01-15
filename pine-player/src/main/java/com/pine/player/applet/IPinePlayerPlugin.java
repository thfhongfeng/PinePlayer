package com.pine.player.applet;

import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

/**
 * Created by tanghongfeng on 2018/1/11.
 */

public interface IPinePlayerPlugin {
    void onInit(PineMediaWidget.IPineMediaPlayer player);
    PinePluginViewHolder createViewHolder(boolean isFullScreen);
    void onMediaPlayerPrepared();
    void onMediaPlayerStart();
    void onMediaPlayerInfo(int what, int extra);
    void onMediaPlayerPause();
    void onMediaPlayerComplete();
    void onAbnormalComplete();
    void onMediaPlayerError(int framework_err, int impl_err);
    void onRefresh();
    void onRelease();
}
