package com.pine.player.widget;

import android.view.ViewGroup;

import com.pine.player.bean.PineMediaPlayerBean;

import java.util.Map;

/**
 * Created by tanghongfeng on 2017/8/28.
 */

public class PineMediaWidget {
    /**
     * 播放控制器接口，主要提供给播放器使用
     */
    public static interface IPineMediaController {

        /**
         * 设置播放器
         *
         * @param player 播放器
         */
        void setMediaPlayer(IPineMediaPlayer player);

        /**
         * 设置播放器控件Anchor View
         *
         * @param view
         */
        void setAnchorView(ViewGroup view);

        /**
         * 设置播放内容
         *
         * @param pineMediaPlayerBean
         * @param videoViewTag
         */
        void setMedia(PineMediaPlayerBean pineMediaPlayerBean,
                      String videoViewTag);

        /**
         * 挂载控制器到播放器上
         *
         * @param isPlayerReset 本此attach是否重置了MediaPlayer
         * @param isResumeState 本此attach是否是为了恢复状态
         */
        void attachToParentView(boolean isPlayerReset, boolean isResumeState);

        /**
         * 播放和暂停切换
         */
        void doPauseResume();

        /**
         * 切换控制器显示状态
         */
        void toggleMediaControlsVisibility();

        /**
         * 显示控制器
         */
        void show();

        /**
         * 显示控制器
         *
         * @param timeout 控制器自动隐藏时间
         */
        void show(int timeout);

        /**
         * 隐藏控制器
         */
        void hide();

        /**
         * 播放器播放回调
         */
        void onMediaPlayerStart();

        /**
         * 播放器暂停回调
         */
        void onMediaPlayerPause();

        /**
         * 播放器装备就绪回调
         */
        void onMediaPlayerPrepared();

        /**
         * 播放器信息更新回调
         *
         * @param what
         * @param extra
         */
        void onMediaPlayerInfo(int what, int extra);

        /**
         * 播放器缓冲更新回调
         *
         * @param percent 当前缓冲百分比
         */
        void onBufferingUpdate(int percent);

        /**
         * 播放器播放完毕回调
         */
        void onMediaPlayerComplete();

        /**
         * 播放器发生错误回调
         *
         * @param framework_err
         * @param impl_err
         */
        void onMediaPlayerError(int framework_err, int impl_err);

        /**
         * 播放器非正常播放结束回调
         */
        void onAbnormalComplete();

        /**
         * 播放器释放回调
         */
        void onMediaPlayerRelease(boolean clearTargetState);

        /**
         * 更新音量显示
         */
        void updateVolumesText();

        /**
         * 播放按键RequestFocus
         */
        void pausePlayBtnRequestFocus();

        /**
         * 设置控制器是否可用
         *
         * @param enabled
         */
        void setControllerEnabled(boolean enabled);

        /**
         * 分别设置各个控制器部件是否可用
         *
         * @param enabledSpeed
         * @param enabledPlayerPause
         * @param enabledProgressBar
         * @param enabledToggleFullScreen
         * @param enabledLock
         * @param enabledFastForward
         * @param enabledFastBackward
         * @param enabledNext
         * @param enabledPrev
         */
        void setControllerEnabled(boolean enabledSpeed, boolean enabledPlayerPause, boolean enabledProgressBar,
                                  boolean enabledToggleFullScreen, boolean enabledLock,
                                  boolean enabledFastForward, boolean enabledFastBackward,
                                  boolean enabledNext, boolean enabledPrev);

        /**
         * 控制器是否显示
         *
         * @return
         */
        boolean isShowing();

        /**
         * 当前控制器是否锁住
         *
         * @return
         */
        boolean isLocked();
    }

    /**
     * 播放器接口，主要提供给播放控制器使用
     */
    public static interface IPineMediaPlayer {

        /**
         * 获取播放倍速
         */
        float getSpeed();

        /**
         * 设置播放倍速
         *
         * @param speed
         */
        void setSpeed(float speed);

        /**
         * 开始播放
         */
        void start();

        /**
         * 暂停播放
         */
        void pause();

        /**
         * 挂起
         */
        void suspend();

        /**
         * 恢复播放
         */
        void resume();

        /**
         * 释放
         */
        void release();

        /**
         * 重新设置播放参数并恢复到之前的播放状态
         *
         * @param pineMediaPlayerBean 播放参数对象
         * @param headers             播放头
         */
        void resetMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                 Map<String, String> headers);

        /**
         * 保存播放状态和进度
         */
        void savePlayerState();

        /**
         * 获取播放总时长
         *
         * @return
         */
        int getDuration();

        /**
         * 获取当前播放位置
         *
         * @return
         */
        int getCurrentPosition();

        /**
         * 获取播放器控件宽度
         *
         * @return
         */
        int getMediaViewWidth();

        /**
         * 获取播放器控件高度
         *
         * @return
         */
        int getMediaViewHeight();

        /**
         * 获取播放的media实体
         *
         * @return
         */
        PineMediaPlayerBean getMediaPlayerBean();

        /**
         * 跳到指定的播放位置
         *
         * @param pos 指定的播放位置
         */
        void seekTo(int pos);

        /**
         * 当前是否处于播放状态
         *
         * @return
         */
        boolean isPlaying();

        /**
         * 当前是否处于播暂停状态
         *
         * @return
         */
        boolean isPause();

        /**
         * 全屏模式装换
         *
         * @param isLocked 当前是否锁定状态
         */
        void toggleFullScreenMode(boolean isLocked);

        /**
         * 当前是否全屏模式
         *
         * @return
         */
        boolean isFullScreenMode();

        /**
         * 获取缓冲百分比
         *
         * @return
         */
        int getBufferPercentage();

        /**
         * 播放的media是否支持暂停
         *
         * @return
         */
        boolean canPause();

        /**
         * 播放的media是否支持回退
         *
         * @return
         */
        boolean canSeekBackward();

        /**
         * 播放的media是否支持快进
         *
         * @return
         */
        boolean canSeekForward();

        /**
         * 获取AudioSessionId
         *
         * @return
         */
        int getAudioSessionId();

        /**
         * 播放器是否处于可播放状态
         *
         * @return
         */
        boolean isInPlaybackState();

        /**
         * 获取播放器具体状态
         *
         * @return
         */
        int getMediaPlayerState();

        /**
         * 获取MediaView在onMeasure中调整后的布局属性，只有在onMeasure之后获取才有效
         *
         * @return
         */
        PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout();

        /**
         * 设置播放状态监听器
         *
         * @param listener
         */
        void setMediaPlayerListener(PineMediaPlayerListener listener);
    }

    /**
     * 播放器播放状态监听器，主要提供给使用者使用，以对播放器播放状态进行监控
     */
    public interface IPineMediaPlayerListener {
        /**
         * 播放器播放准备就绪
         */
        void onPrepared();

        /**
         * 播放器播放完成
         */
        void onCompletion();

        /**
         * 播放器播放信息更新
         *
         * @param what
         * @param extra
         * @return
         */
        boolean onInfo(int what, int extra);

        /**
         * 播放器发生错误
         *
         * @param framework_err
         * @param impl_err
         * @return
         */
        boolean onError(int framework_err, int impl_err);

        /**
         * 播放器非正常播放结束（比如播放缓存或者暂停时session失效等问题）
         *
         * @return
         */
        boolean onAbnormalComplete();
    }

    /**
     * 默认播放器播放状态监听器，主要提供给使用者使用，以对播放器播放状态进行监控
     */
    public static class PineMediaPlayerListener implements IPineMediaPlayerListener {

        @Override
        public void onPrepared() {

        }

        @Override
        public void onCompletion() {

        }

        @Override
        public boolean onInfo(int what, int extra) {
            return true;
        }

        @Override
        public boolean onError(int framework_err, int impl_err) {
            return true;
        }

        @Override
        public boolean onAbnormalComplete() {
            return true;
        }
    }
}
