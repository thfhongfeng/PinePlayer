package com.pine.player.applet.barrage.bean;

import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Created by tanghongfeng on 2017/9/20.
 */

public class PineBarrageBean {
    // 弹幕类别: 普通弹幕
    public static final int NORMAL_BARRAGE = 0;

    // 弹幕方向: 从右往左
    public static final int FROM_RIGHT_TO_LEFT = 0;
    // 弹幕方向: 从左往右
    public static final int FROM_LEFT_TO_RIGHT = 1;

    private int order;
    // 弹幕类别
    private int type;
    // 弹幕方向
    private int direction;
    // 弹幕持续时间，默认以动画效果为准
    private int duration;
    // 弹幕文字
    private String textBody;
    // 弹幕文字宽度
    private int textWidth;
    // 弹幕文字高度
    private int textHeight;
    // 弹幕开始时间戳
    private long beginTime;

    private PartialDisplayBarrageNode partialDisplayBarrageNode;

    private volatile boolean isShow;

    private View itemView;

    private ObjectAnimator animator;

    public PineBarrageBean(int order, int type, int direction, int duration,
                           @NonNull String textBody, @NonNull long beginTime) {
        this(order, type, direction, duration, textBody, 0, 0, beginTime);
    }

    public PineBarrageBean(int order, int type, int direction, int duration,
                           String textBody, int textWidth, int textHeight, long beginTime) {
        this.order = order;
        this.type = type;
        this.direction = direction;
        this.duration = duration;
        this.textBody = textBody;
        this.textWidth = textWidth;
        this.textHeight = textHeight;
        this.beginTime = beginTime;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public int getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(int textHeight) {
        this.textHeight = textHeight;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public PartialDisplayBarrageNode getPartialDisplayBarrageNode() {
        return partialDisplayBarrageNode;
    }

    public void setPartialDisplayBarrageNode(PartialDisplayBarrageNode partialDisplayBarrageNode) {
        this.partialDisplayBarrageNode = partialDisplayBarrageNode;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public View getItemView() {
        return itemView;
    }

    public void setItemView(View itemView) {
        this.itemView = itemView;
    }

    public ObjectAnimator getAnimator() {
        return animator;
    }

    public void setAnimator(ObjectAnimator animator) {
        this.animator = animator;
    }
}
