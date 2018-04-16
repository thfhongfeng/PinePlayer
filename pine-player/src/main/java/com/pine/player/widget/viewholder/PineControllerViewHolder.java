package com.pine.player.widget.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.pine.player.widget.view.PineProgressBar;

import java.util.List;

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
    // 控制器View的右部View（与RightViewContainer互斥显示，由用户自行定制）
    private View rightControllerView;
    // 控制器View的区块View列表（由用户自行定制）
    private List<View> blockControllerViewList;

    // 退出按键
    private View goBackButton;
    // 播放media名称显示控件
    private View mediaNameText;
    // 倍速控件
    private View speedButton;
    // 全屏模式下右侧View的控制按键列表（比如清晰度选择View的显示开关，播放列表View的显示开关，
    // 该List的Item与AbstractMediaControllerAdapter的getRightContainerViewHolderList的Item的为一一对应关系）
    private List<View> rightViewControlBtnList;
    // 锁定按键
    private View lockControllerButton;
    // 播放按键
    private View pausePlayButton;
    // 播放进度条（用于使用系统自带的ProgressBar）
    private ProgressBar playProgressBar;
    // 播放进度条（用于自定义的进度条）
    private PineProgressBar customProgressBar;
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

    public View getRightControllerView() {
        return rightControllerView;
    }

    public void setRightControllerView(View rightControllerView) {
        this.rightControllerView = rightControllerView;
    }

    public List<View> getBlockControllerViewList() {
        return blockControllerViewList;
    }

    public void setBlockControllerViewList(List<View> blockControllerViewList) {
        this.blockControllerViewList = blockControllerViewList;
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

    public List<View> getRightViewControlBtnList() {
        return rightViewControlBtnList;
    }

    public void setRightViewControlBtnList(List<View> rightViewControlBtnList) {
        this.rightViewControlBtnList = rightViewControlBtnList;
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

    public PineProgressBar getCustomProgressBar() {
        return customProgressBar;
    }

    public void setCustomProgressBar(PineProgressBar customProgressBar) {
        this.customProgressBar = customProgressBar;
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