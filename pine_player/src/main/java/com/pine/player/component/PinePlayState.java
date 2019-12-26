package com.pine.player.component;

public enum PinePlayState {
    // 播放器状态
    STATE_IDLE,
    STATE_PREPARING,
    STATE_PREPARED,
    STATE_PLAYING,
    STATE_PAUSED,
    STATE_PLAYBACK_COMPLETED,
    STATE_ERROR,
    // 本地播放流服务状态，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况
    SERVICE_STATE_DISCONNECTED,
    SERVICE_STATE_CONNECTING,
    SERVICE_STATE_CONNECTED
}
