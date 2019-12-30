package com.pine.player.widget.viewholder;

import android.view.ViewGroup;

import com.pine.player.applet.IPinePlayerPlugin;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PinePluginViewHolder {
    // 插件根View
    protected int containerType = IPinePlayerPlugin.TYPE_MATCH_CONTROLLER;
    // 插件根View
    protected ViewGroup container;

    public int getContainerType() {
        return containerType;
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