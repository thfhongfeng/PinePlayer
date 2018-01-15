package com.pine.player.applet.barrage.plugin;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public class PineBarragePlugin implements IPinePlayerPlugin {

    @Override
    public void onInit(PineMediaWidget.IPineMediaPlayer player) {

    }

    @Override
    public PinePluginViewHolder createViewHolder(boolean isFullScreen) {
        return null;
    }

    @Override
    public void onMediaPlayerPrepared() {

    }

    @Override
    public void onMediaPlayerStart() {

    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {

    }

    @Override
    public void onMediaPlayerComplete() {

    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onRelease() {

    }
}
