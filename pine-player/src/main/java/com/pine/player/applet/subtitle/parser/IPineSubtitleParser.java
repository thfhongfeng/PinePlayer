package com.pine.player.applet.subtitle.parser;

import com.pine.player.applet.subtitle.bean.PineSubtitleBean;

import java.util.List;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

public interface IPineSubtitleParser {
    List<PineSubtitleBean> parse();
}
