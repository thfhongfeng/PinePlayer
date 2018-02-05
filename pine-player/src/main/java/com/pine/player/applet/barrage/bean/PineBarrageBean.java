package com.pine.player.applet.barrage.bean;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by tanghongfeng on 2017/9/20.
 */

public class PineBarrageBean {
    // 普通弹幕
    public static final int NORMAL_BARRAGE = 1;

    // 从左往右
    public static final int FROM_LEFT_TO_RIGHT = 1;
    // 从右往左
    public static final int FROM_RIGHT_TO_LEFT = 2;

    private int order;
    // 弹幕类别
    private int type;
    // 弹幕方向
    private int direction;
    // 弹幕持续时间，默认以动画效果为准
    private int duration;
    // 弹幕文字
    private String textBody;
    // 弹幕开始时间戳
    private long beginTime;

    private PartialDisplayBarrageNode partialDisplayBarrageNode;

    private boolean isShow;

    private View itemView;

    private ObjectAnimator animator;

    public PineBarrageBean(int order, int type, int direction, int duration,
                           String textBody, long beginTime) {
        this.order = order;
        this.type = type;
        this.direction = direction;
        this.duration = duration;
        this.textBody = textBody;
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
