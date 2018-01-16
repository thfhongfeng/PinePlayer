package com.pine.player.widget.viewholder;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Created by tanghongfeng on 2017/9/14.
 */

public final class PineControllerViewHolder {
    // 控制器根View
    private ViewGroup container;
    // 控制器View的上部View（一般包括退出，media名称，播放列表按键等功能控件，由用户自行定制）
    private View topControllerView;
    // 控制器View的中部View（一般包括锁定功能控件，由用户自行定制）
    private View centerControllerView;
    // 控制器View的下部View（一般包括播放，播放时间，播放进度条，音量，全屏切换等功能控件，
    // 由用户自行定制）
    private View bottomControllerView;

    // 退出按键
    private View goBackButton;
    // 播放media名称显示控件
    private View mediaNameText;
    // 倍速控件
    private View speedButton;
    // 播放列表按键
    private View mediaListButton;
    // 锁定按键
    private View lockControllerButton;
    // 播放按键
    private View pausePlayButton;
    // 播放进度条
    private ProgressBar playProgressBar;
    // 快速前进，快速后退按键
    private View fastForwardButton, fastBackwardButton;
    // 播放下一个，播放前一个按键
    private View nextButton, prevButton;
    // media总时长，当前播放时长显示控件
    private View endTimeText, currentTimeText;
    // 音量调整
    private View volumesButton;
    // 当前音量显示控件
    private View volumesText;
    // 全屏切换
    private View fullScreenButton;

    public ViewGroup getContainer() {
        return container;
    }

    public void setContainer(ViewGroup container) {
        this.container = container;
    }

    public View getTopControllerView() {
        return topControllerView;
    }

    public void setTopControllerView(View topControllerView) {
        this.topControllerView = topControllerView;
    }

    public View getCenterControllerView() {
        return centerControllerView;
    }

    public void setCenterControllerView(View centerControllerView) {
        this.centerControllerView = centerControllerView;
    }

    public View getBottomControllerView() {
        return bottomControllerView;
    }

    public void setBottomControllerView(View bottomControllerView) {
        this.bottomControllerView = bottomControllerView;
    }

    public View getGoBackButton() {
        return goBackButton;
    }

    public void setGoBackButton(View goBackButton) {
        this.goBackButton = goBackButton;
    }

    public View getMediaNameText() {
        return mediaNameText;
    }

    public void setMediaNameText(View mediaNameText) {
        this.mediaNameText = mediaNameText;
    }

    public View getSpeedButton() {
        return speedButton;
    }

    public void setSpeedButton(View speedButton) {
        this.speedButton = speedButton;
    }

    public View getMediaListButton() {
        return mediaListButton;
    }

    public void setMediaListButton(View mediaListButton) {
        this.mediaListButton = mediaListButton;
    }

    public View getLockControllerButton() {
        return lockControllerButton;
    }

    public void setLockControllerButton(View lockControllerButton) {
        this.lockControllerButton = lockControllerButton;
    }

    public View getPausePlayButton() {
        return pausePlayButton;
    }

    public void setPausePlayButton(View pausePlayButton) {
        this.pausePlayButton = pausePlayButton;
    }

    public ProgressBar getPlayProgressBar() {
        return playProgressBar;
    }

    public void setPlayProgressBar(ProgressBar playProgressBar) {
        this.playProgressBar = playProgressBar;
    }

    public View getFastForwardButton() {
        return fastForwardButton;
    }

    public void setFastForwardButton(View fastForwardButton) {
        this.fastForwardButton = fastForwardButton;
    }

    public View getFastBackwardButton() {
        return fastBackwardButton;
    }

    public void setFastBackwardButton(View fastBackwardButton) {
        this.fastBackwardButton = fastBackwardButton;
    }

    public View getNextButton() {
        return nextButton;
    }

    public void setNextButton(View nextButton) {
        this.nextButton = nextButton;
    }

    public View getPrevButton() {
        return prevButton;
    }

    public void setPrevButton(View prevButton) {
        this.prevButton = prevButton;
    }

    public View getEndTimeText() {
        return endTimeText;
    }

    public void setEndTimeText(View endTimeText) {
        this.endTimeText = endTimeText;
    }

    public View getCurrentTimeText() {
        return currentTimeText;
    }

    public void setCurrentTimeText(View currentTimeText) {
        this.currentTimeText = currentTimeText;
    }

    public View getVolumesButton() {
        return volumesButton;
    }

    public void setVolumesButton(View volumesButton) {
        this.volumesButton = volumesButton;
    }

    public View getVolumesText() {
        return volumesText;
    }

    public void setVolumesText(View volumesText) {
        this.volumesText = volumesText;
    }

    public View getFullScreenButton() {
        return fullScreenButton;
    }

    public void setFullScreenButton(View fullScreenButton) {
        this.fullScreenButton = fullScreenButton;
    }
}