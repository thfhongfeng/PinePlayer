package com.pine.player.widget.viewholder;

import android.view.View;
import android.view.ViewGroup;

import com.pine.player.applet.IPinePlayerPlugin;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PinePluginViewHolder {
    // 插件根View
    protected int containerType;
    // 插件根View
    protected ViewGroup container;

    public int getContainerType() {
        return containerType == IPinePlayerPlugin.TYPE_MATCH_SURFACE ?
                IPinePlayerPlugin.TYPE_MATCH_SURFACE : IPinePlayerPlugin.TYPE_MATCH_CONTROLLER;
    }

    public void setContainerType(int containerType) {
        this.containerType = containerType;
    }

    public ViewGroup getContainer() {
        return container;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }

}