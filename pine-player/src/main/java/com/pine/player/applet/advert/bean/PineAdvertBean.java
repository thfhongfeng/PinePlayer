package com.pine.player.applet.advert.bean;

import android.net.Uri;

/**
 * Created by tanghongfeng on 2017/9/20.
 */

public class PineAdvertBean {
    // 广告类型-时间点广告
    public static final int TYPE_TIME = 0;
    // 广告类型-暂停广告
    public static final int TYPE_PAUSE = 1;
    // 广告类型-开头广告
    public static final int TYPE_START = 2;
    // 广告类型-结尾广告
    public static final int TYPE_COMPLETE = 3;

    // 广告内容类别-视频广告
    public static final int TYPE_VIDEO = 0;
    // 广告内容类别-图片广告
    public static final int TYPE_IMAGE = 1;

    // 广告类别
    private int type;
    // 广告标识
    private int order;
    // 广告Uri
    private Uri uri;
    // 广告内容类别
    private int contentType;
    // 广告时间点时间戳
    private long positionTime;
    // 广告时长（毫秒）
    private long durationTime;

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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public long getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(long positionTime) {
        this.positionTime = positionTime;
    }

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }
}
