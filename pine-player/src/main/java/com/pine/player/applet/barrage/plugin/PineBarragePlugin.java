package com.pine.player.applet.barrage.plugin;

import android.content.Context;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public class PineBarragePlugin implements IPinePlayerPlugin {

    @Override
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller,
                       boolean isPlayerReset, boolean isResumeState) {

    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        return null;
    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_MATCH_SURFACE;
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
    public void onTime(long position) {

    }

    @Override
    public void onRelease() {

    }
}
