package com.pine.player.applet.barrage;

import com.pine.player.applet.barrage.bean.PineBarrageBean;

import java.util.List;

/**
 * Created by tanghongfeng on 2017/9/20.
 */

/**
 * 弹幕解析器
 */
public interface IPineBarrageParser {
    List<PineBarrageBean> parse();
}
