package com.pine.player.component;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineSurfaceView;

import java.util.Map;

/**
 * Created by tanghongfeng on 2018/4/2.
 */

public class PineMediaPlayerProxy implements PineMediaWidget.IPineMediaPlayer {
    private PineMediaPlayerComponent mMediaPlayerComponent;

    public PineMediaPlayerProxy(@NonNull PineMediaPlayerComponent pineMediaPlayerComponent) {
        mMediaPlayerComponent = pineMediaPlayerComponent;
    }

    public PineMediaPlayerComponent getPineMediaPlayerComponent() {
        return mMediaPlayerComponent;
    }

    public void setPineMediaPlayerComponent(PineMediaPlayerComponent mediaPlayerComponent) {
        mMediaPlayerComponent = mediaPlayerComponent;
    }

    @Override
    public float getSpeed() {
        return mMediaPlayerComponent.getSpeed();
    }

    @Override
    public void setSpeed(float speed) {
        mMediaPlayerComponent.setSpeed(speed);
    }

    @Override
    public void start() {
        mMediaPlayerComponent.start();
    }

    @Override
    public void pause() {
        mMediaPlayerComponent.pause();
    }

    @Override
    public void suspend() {
        mMediaPlayerComponent.suspend();
    }

    @Override
    public void resume() {
        mMediaPlayerComponent.resume();
    }

    @Override
    public void release() {
        mMediaPlayerComponent.release();
    }

    @Override
    public void onDestroy() {
        mMediaPlayerComponent.onDestroy();
    }

    @Override
    public boolean isLocalStreamMode() {
        return mMediaPlayerComponent.isLocalStreamMode();
    }
    @Override
    public void setAutocephalyPlayMode(boolean isAutocephalyPlayMode) {
        mMediaPlayerComponent.setAutocephalyPlayMode(isAutocephalyPlayMode);
    }

    @Override
    public PineMediaWidget.IPineMediaController getMediaController() {
        return mMediaPlayerComponent.getMediaController();
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean) {
        mMediaPlayerComponent.setPlayingMedia(pineMediaPlayerBean, null, false);
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers) {
        mMediaPlayerComponent.setPlayingMedia(pineMediaPlayerBean, headers, false);
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, boolean isAutocephalyPlayMode) {
        mMediaPlayerComponent.setPlayingMedia(pineMediaPlayerBean, null, false, isAutocephalyPlayMode);
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers, boolean isAutocephalyPlayMode) {
        mMediaPlayerComponent.setPlayingMedia(pineMediaPlayerBean, headers, false, isAutocephalyPlayMode);
    }

    @Override
    public void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers) {
        mMediaPlayerComponent.resetPlayingMediaAndResume(pineMediaPlayerBean, headers);
    }

    @Override
    public void savePlayerState() {
        mMediaPlayerComponent.savePlayerState();
    }

    @Override
    public int getDuration() {
        return mMediaPlayerComponent.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayerComponent.getCurrentPosition();
    }

    @Override
    public PineMediaPlayerBean getMediaPlayerBean() {
        return mMediaPlayerComponent.getMediaPlayerBean();
    }

    @Override
    public MediaPlayer.TrackInfo[] getTrackInfo() {
        return mMediaPlayerComponent.getTrackInfo();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayerComponent.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayerComponent.isPlaying();
    }

    @Override
    public boolean isPause() {
        return mMediaPlayerComponent.isPause();
    }

    @Override
    public boolean isSurfaceViewEnable() {
        return mMediaPlayerComponent.isSurfaceViewEnable();
    }

    @Override
    public void toggleFullScreenMode(boolean isLocked) {
        mMediaPlayerComponent.toggleFullScreenMode(isLocked);
    }

    @Override
    public boolean isFullScreenMode() {
        return mMediaPlayerComponent.isFullScreenMode();
    }

    @Override
    public int getBufferPercentage() {
        return mMediaPlayerComponent.getBufferPercentage();
    }

    @Override
    public boolean canPause() {
        return mMediaPlayerComponent.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return mMediaPlayerComponent.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return mMediaPlayerComponent.canSeekForward();
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayerComponent.getAudioSessionId();
    }

    @Override
    public boolean isInPlaybackState() {
        return mMediaPlayerComponent.isInPlaybackState();
    }

    @Override
    public boolean isAttachToFrontMode() {
        return mMediaPlayerComponent.isAttachToFrontMode();
    }

    @Override
    public boolean isAutocephalyPlayMode() {
        return mMediaPlayerComponent.isAutocephalyPlayMode();
    }

    @Override
    public int getMediaPlayerState() {
        return mMediaPlayerComponent.getMediaPlayerState();
    }

    @Override
    public void setMediaPlayerListener(PineMediaWidget.PineMediaPlayerListener listener) {
        mMediaPlayerComponent.setMediaPlayerListener(listener);
    }

    @Override
    public PineMediaPlayerView getMediaPlayerView() {
        return mMediaPlayerComponent.getMediaPlayerView();
    }

    @Override
    public PineSurfaceView getSurfaceView() {
        return mMediaPlayerComponent.getSurfaceView();
    }

    @Override
    public PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout() {
        return mMediaPlayerComponent.getMediaAdaptionLayout();
    }

    @Override
    public int getMediaViewWidth() {
        return mMediaPlayerComponent.getMediaViewWidth();
    }

    @Override
    public int getMediaViewHeight() {
        return mMediaPlayerComponent.getMediaViewHeight();
    }
}
