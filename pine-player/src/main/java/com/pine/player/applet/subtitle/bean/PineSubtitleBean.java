package com.pine.player.applet.subtitle.bean;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * 外挂字幕
 */
public class PineSubtitleBean {
    // 艺人名
    public static final int ARTIST_ZONE = 0;
    // 标题
    public static final int TITLE_ZONE = 1;
    // 专辑名
    public static final int ALBUM_ZONE = 2;
    // 编者（指编辑LRC歌词的人）
    public static final int AUTHOR_ZONE = 3;
    // 时间补偿
    public static final int OFFSET_ZONE = 4;
    // 内容
    public static final int TEXT_ZONE = 5;
    // 翻译
    public static final int TRANS_ZONE = 6;

    // 字幕类别
    private int type;
    // 字幕标识
    private int order;
    // 字幕文字
    private String textBody;
    // 字幕翻译
    private String transBody;
    // 字幕开始时间戳
    private long beginTime;
    // 字幕开始结束戳
    private long endTime;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public String getTransBody() {
        return transBody;
    }

    public void setTransBody(String transBody) {
        this.transBody = transBody;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public PineSubtitleBean clone() {
        PineSubtitleBean pineSubtitleBean = null;
        try {
            pineSubtitleBean = (PineSubtitleBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return pineSubtitleBean;
    }
}
