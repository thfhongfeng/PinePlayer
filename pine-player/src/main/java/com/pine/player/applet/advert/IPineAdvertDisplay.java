package com.pine.player.applet.advert;

import android.view.View;

/**
 * Created by tanghongfeng on 2017/9/20.
 */

/**
 * 播放广告解析
 */
public interface IPineAdvertDisplay {
    /**
     * 播放开始前需要播放的广告对象
     * @return
     */
    void advertBeforeStart(View advertView);

    /**
     * 播放暂停时需要播放的广告对象
     * @return
     */
    void advertOnPause(View advertView, boolean isFullScreen);

    /**
     * 播放结束后需要播放的广告对象
     * @return
     */
    void advertAfterComplete(View advertView);
}
