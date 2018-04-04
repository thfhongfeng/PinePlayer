package com.pine.player.component;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.service.IPineMediaSocketService;
import com.pine.player.service.PineMediaSocketService;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineSurfaceView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanghongfeng on 2017/8/14.
 */

public class PineMediaPlayerComponent implements PineMediaWidget.IPineMediaSurfaceListener {
    // 播放器状态
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    private final static String TAG = LogUtil.makeLogTag(PineMediaPlayerComponent.class);

    // 是否使用5.0之后的新API，该API支持本地流播放
    private static final boolean USE_NEW_API = true;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    // 本地播放流服务状态，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况
    public static final int SERVICE_STATE_DISCONNECTED = 1;
    public static final int SERVICE_STATE_CONNECTING = 2;
    public static final int SERVICE_STATE_CONNECTED = 3;
    private boolean mIsBackgroundPlayerMode;
    private boolean mEnableSurfaceView;
    private Context mContext;
    // 准备播放的多媒体对象
    private PineMediaPlayerBean mMediaBean;
    // 准备播放的多媒体头部信息
    private Map<String, String> mHeaders;
    // 播放本地视频文件时，是否需要对流进行处理（比如播放加密视频时，需要设置为true）
    private boolean mIsLocalStreamMedia;
    // 本地播放流服务，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况（模拟网络流进行流播放）
    private IPineMediaSocketService mLocalService;
    private int mLocalServiceState = SERVICE_STATE_DISCONNECTED;
    private boolean mIsDelayOpenMedia;
    private boolean mIsDelayStart;
    private PineSurfaceView mSurfaceView = null;
    private PineMediaPlayerView mMediaPlayerView = null;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mMediaWidth, mMediaHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    // 播放器的控制器
    private PineMediaWidget.IPineMediaController mMediaController;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mMediaWidth = mp.getVideoWidth();
                    mMediaHeight = mp.getVideoHeight();
                    if (mSurfaceView != null && mMediaWidth != 0 && mMediaHeight != 0) {
                        mSurfaceView.getHolder().setFixedSize(mMediaWidth, mMediaHeight);
                        mSurfaceView.requestLayout();
                    }
                }
            };
    private PineMediaWidget.PineMediaPlayerListener mMediaPlayerListener;
    private int mCurrentBufferPercentage;
    // 记录播放位置，在界面切换等情况下，自动恢复到之前的播放位置
    private int mSeekWhenPrepared;
    // 记录播放状态，在界面切换等情况下，自动恢复到之前的播放状态
    private boolean mShouldPlayWhenPrepared;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private boolean mIsFullScreenMode;
    private float mSpeed = 1.0f;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            LogUtil.d(TAG, "onPrepared");
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            setMetaData(mp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(mSpeed));
            }
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onPrepared();
            }
            if (isAttachToFrontMode() && mMediaController != null) {
                mMediaController.onMediaPlayerPrepared();
            }
            mMediaWidth = mp.getVideoWidth();
            mMediaHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mMediaWidth != 0 && mMediaHeight != 0) {
                //LogUtil.i("@@@@", "media size: " + mMediaWidth +"/"+ mMediaHeight);
                if (isAttachToFrontMode() && mSurfaceView != null) {
                    mSurfaceView.getHolder().setFixedSize(mMediaWidth, mMediaHeight);
                }
                if (mSurfaceWidth == mMediaWidth && mSurfaceHeight == mMediaHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the media here instead of in the callback.
                    if (mTargetState == STATE_PLAYING || mShouldPlayWhenPrepared) {
                        mShouldPlayWhenPrepared = false;
                        start();
                        if (isAttachToFrontMode() && mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (isAttachToFrontMode() && mMediaController != null) {
                            // Show the media controls when we're paused into a media and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the media size yet, but should start anyway.
                // The media size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
            if (isAttachToFrontMode() && mSurfaceView != null) {
                mSurfaceView.requestFocus();
            }
        }
    };
    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    int currentPos = getCurrentPosition();
                    int duration = getDuration();
                    int bufferPercentage = getBufferPercentage();
                    LogUtil.d(TAG, "onCompletion currentPos:" + currentPos
                            + ", duration:" + duration
                            + ", bufferPercentage:" + bufferPercentage);
                    if (currentPos != duration && bufferPercentage > 0 && bufferPercentage < 100) {
                        mCurrentState = STATE_ERROR;
                        mTargetState = STATE_PLAYING;
                        mSeekWhenPrepared = currentPos;
                        mShouldPlayWhenPrepared = true;
                        if (isAttachToFrontMode() && mMediaController != null) {
                            mMediaController.onAbnormalComplete();
                        }
                        if (mMediaPlayerListener != null) {
                            mMediaPlayerListener.onAbnormalComplete();
                        }
                    } else {
                        mCurrentState = STATE_PLAYBACK_COMPLETED;
                        mTargetState = STATE_PLAYBACK_COMPLETED;
                        if (isAttachToFrontMode() && mMediaController != null) {
                            mMediaController.onMediaPlayerComplete();
                        }
                        if (mMediaPlayerListener != null) {
                            mMediaPlayerListener.onCompletion();
                        }
                    }
                }
            };
    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogUtil.d(TAG, "onInfo what: " + what + ", extra:" + extra);
                    if (isAttachToFrontMode() && mMediaController != null) {
                        mMediaController.onMediaPlayerInfo(what, extra);
                    }
                    if (mMediaPlayerListener != null) {
                        mMediaPlayerListener.onInfo(what, extra);
                    }
                    return true;
                }
            };
    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    LogUtil.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (isAttachToFrontMode() && mMediaController != null) {
                        mMediaController.onMediaPlayerError(framework_err, impl_err);
                    }
                    release(true);
                    /* If an error handler has been supplied, use it and finish. */
                    if (mMediaPlayerListener != null) {
                        if (mMediaPlayerListener.onError(framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (isAttachToFrontMode() && mSurfaceView != null && mSurfaceView.getWindowToken() != null) {
                        Resources r = mContext.getResources();
                        int messageId;

                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = Resources.getSystem()
                                    .getIdentifier("VideoView_error_text_invalid_progressive_playback",
                                            "string", "android");
                        } else {
                            messageId = Resources.getSystem()
                                    .getIdentifier("VideoView_error_text_unknown",
                                            "string", "android");
                        }
                        Toast.makeText(mContext, messageId, Toast.LENGTH_SHORT).show();
                        if (mMediaPlayerListener != null) {
                            mMediaPlayerListener.onCompletion();
                        }
                    }
                    return true;
                }
            };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    if (isAttachToFrontMode() && mMediaController != null) {
                        mMediaController.onBufferingUpdate(percent);
                    }
                }
            };
    // 本地播放流服务（用于低于M版本的本地流播放方案）
    protected ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "Local service connected");
            mLocalService = (IPineMediaSocketService) service;
            mLocalServiceState = SERVICE_STATE_CONNECTED;
            if (mIsDelayOpenMedia) {
                mIsDelayOpenMedia = false;
                mLocalService.setPlayerDecryptor(mMediaBean.getPlayerDecryptor());
                openMedia(false);
                if (isAttachToFrontMode() && mSurfaceView != null) {
                    mSurfaceView.requestLayout();
                    mSurfaceView.invalidate();
                }
            }
            if (mIsDelayStart) {
                mIsDelayStart = false;
                start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "Local service disconnected!");
            mLocalService = null;
            mLocalServiceState = SERVICE_STATE_DISCONNECTED;
        }
    };

    public PineMediaPlayerComponent(Context context) {
        mContext = context;
        initMediaView();
    }

    private void initMediaView() {
        mMediaWidth = 0;
        mMediaHeight = 0;
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setMediaPlayerView(PineMediaPlayerView playerView) {
        mMediaPlayerView = playerView;
    }

    /**
     * 挂载控制器界面
     *
     * @param isPlayerReset 本此attach是否重置了MediaPlayer
     * @param isResumeState 本此attach是否是为了恢复状态
     */
    private void attachMediaController(boolean isPlayerReset, boolean isResumeState) {
        if (mMediaPlayer != null && isAttachToFrontMode() && mMediaController != null &&
                mMediaBean != null) {
            mMediaController.setPlayingMedia(mMediaBean, "PineMediaView");
            mMediaController.attachToParentView(isPlayerReset, isResumeState);
        }
    }

    /**
     * 设置是否本地流播放需求
     *
     * @param isLocalStream 是否本地播放流
     */
    public void setLocalStreamMode(boolean isLocalStream) {
        // 设置是否本地流播放需求
        mIsLocalStreamMedia = isLocalStream;
        if (isNeedLocalService() && mLocalServiceState == SERVICE_STATE_DISCONNECTED) {
            mLocalServiceState = SERVICE_STATE_CONNECTING;
            Intent intent = new Intent("media.socket.server");
            intent.setPackage(mContext.getPackageName());
            LogUtil.d(TAG, "Bind local service");
            mContext.bindService(intent, mServiceConnection, mContext.BIND_AUTO_CREATE);
        }
    }

    /**
     * 设置是否为后台播放模式（退出界面，不会停止播放）
     *
     * @param isBackgroundPlayerMode 是否为后台播放模式
     */
    public void setBackgroundPlayerMode(boolean isBackgroundPlayerMode) {
        mIsBackgroundPlayerMode = isBackgroundPlayerMode;
    }

    /**
     * 设置多媒体播放参数
     *
     * @param pineMediaPlayerBean    多媒体播放参数对象
     * @param headers                多媒体播放信息头
     * @param resumeState            此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
     */
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                                Map<String, String> headers, boolean resumeState) {
        setPlayingMedia(pineMediaPlayerBean, headers, resumeState, mIsBackgroundPlayerMode);
    }

    /**
     * 设置多媒体播放参数
     *
     * @param pineMediaPlayerBean    多媒体播放参数对象
     * @param headers                多媒体播放信息头
     * @param resumeState            此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
     * @param isBackgroundPlayerMode 是否后台播放模式
     */
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                                Map<String, String> headers, boolean resumeState, boolean isBackgroundPlayerMode) {
        mMediaBean = pineMediaPlayerBean;
        mHeaders = headers;
        mIsBackgroundPlayerMode = isBackgroundPlayerMode;
        if (mLocalService != null) {
            mLocalService.setPlayerDecryptor(pineMediaPlayerBean.getPlayerDecryptor());
        }
        if (!resumeState) {
            mSeekWhenPrepared = 0;
            mShouldPlayWhenPrepared = false;
        }
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            openMedia(resumeState);
            if (isAttachToFrontMode() && mSurfaceView != null) {
                mSurfaceView.requestLayout();
                mSurfaceView.invalidate();
            }
        } else {
            // 如果需要使用到本地播放流服务，若流服务还未启动，则延时到服务启动完成后在打开Media
            mIsDelayOpenMedia = true;
        }
    }

    /**
     * 打开多媒体
     *
     * @param isResumeState 是否是为了恢复状态而重新打开
     */
    protected void openMedia(boolean isResumeState) {
        if (mMediaBean == null) {
            return;
        }
        Uri mediaUri = mMediaBean.getMediaUriByDefinition(mMediaBean.getCurrentDefinition());
        if (mediaUri == null) {
            return;
        }
        if (isNeedLocalService() && mLocalServiceState != SERVICE_STATE_CONNECTED) {
            return;
        }

        LogUtil.d(TAG, "Open Media mUri:" + mediaUri);
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (!isResumeState) {
            mSpeed = 1.0f;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            if (mIsLocalStreamMedia) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && USE_NEW_API) {
                    // 使用新版本的流方式API设置DataSource
                    File file = new File(mediaUri.getPath());
                    setPineDataSource(file);
                } else {
                    if (mHeaders == null) {
                        mHeaders = new HashMap<String, String>();
                    }
                    mHeaders.put("Path", mediaUri.getPath());
                    mMediaPlayer.setDataSource(mContext,
                            Uri.parse(PineMediaSocketService.getMediaLocalSocketUrl()), mHeaders);
                }
            } else {
                mMediaPlayer.setDataSource(mContext, mediaUri, mHeaders);
            }
            setDisplaySurface(mSurfaceView);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController(true, isResumeState);
        } catch (IOException ex) {
            LogUtil.w(TAG, "Unable to open content: " + mediaUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            LogUtil.w(TAG, "Unable to open content: " + mediaUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {
        }
    }

    // 大于M版本的本地流播放方式
    @TargetApi(Build.VERSION_CODES.M)
    private void setPineDataSource(final File file) {
        mMediaPlayer.setDataSource(new MediaDataSource() {
            RandomAccessFile mRandomAccessFile;
            byte[] mMediaBytes;
            long mSize = -1;

            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                if (mRandomAccessFile == null) {
                    mRandomAccessFile = new RandomAccessFile(file.getPath(), "r");
                    mSize = file.length();
                }
                if (mRandomAccessFile == null || position + 1 >= mSize) {
                    return -1;
                }
                mMediaBytes = new byte[size];
                // 从position位置（文件位置）开始读取size字节的数据到mMediaBytes中
                mRandomAccessFile.seek(position);
                mRandomAccessFile.read(mMediaBytes);
                // 对mMediaBytes中的数据进行解密
                if (mMediaBean.getPlayerDecryptor() != null) {
                    mMediaBean.getPlayerDecryptor().decrypt(mMediaBytes, position, size);
                }
                // 将mMediaBytes拷贝到buffer中
                System.arraycopy(mMediaBytes, 0, buffer, offset, size);
                return size;
            }

            @Override
            public long getSize() throws IOException {
                if (mSize < 0) {
                    if (file.exists()) {
                        mSize = file.length();
                    }
                }
                return mSize;
            }

            @Override
            public void close() throws IOException {
                if (mRandomAccessFile != null) {
                    mRandomAccessFile.close();
                }
                mRandomAccessFile = null;
                mMediaBytes = null;
            }
        });
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSpeed = speed;
            mShouldPlayWhenPrepared = isPlaying();
            mSeekWhenPrepared = getCurrentPosition();
            openMedia(true);
        }
    }

    public void start() {
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            if (isInPlaybackState()) {
                LogUtil.d(TAG, "Start media player");
                mMediaPlayer.start();
                if (isAttachToFrontMode() && mMediaController != null) {
                    mMediaController.onMediaPlayerStart();
                }
                mCurrentState = STATE_PLAYING;
            }
            mTargetState = STATE_PLAYING;
        } else {
            mIsDelayStart = true;
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                LogUtil.d(TAG, "Pause media player");
                mMediaPlayer.pause();
                if (isAttachToFrontMode() && mMediaController != null) {
                    mMediaController.onMediaPlayerPause();
                }
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        if (!isBackgroundPlayerMode()) {
            openMedia(true);
        }
    }

    public void release() {
        release(true);
    }

    public PineMediaWidget.IPineMediaController getMediaController() {
        return mMediaController;
    }

    public void setMediaController(PineMediaWidget.IPineMediaController controller) {
        if (isAttachToFrontMode() && mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        if (isBackgroundPlayerMode()) {
            attachMediaController(false, true);
        }
    }

    public void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                           Map<String, String> headers) {
        setPlayingMedia(pineMediaPlayerBean, headers, true, mIsBackgroundPlayerMode);
    }

    public void savePlayerState() {
        mShouldPlayWhenPrepared = isPlaying();
        mSeekWhenPrepared = getCurrentPosition();
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public PineMediaPlayerView getMediaPlayerView() {
        return mMediaPlayerView;
    }

    public PineSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout() {
        return mSurfaceView == null ? null : mSurfaceView.getMediaAdaptionLayout();
    }

    public int getMediaViewWidth() {
        return mMediaWidth;
    }

    public int getMediaViewHeight() {
        return mMediaHeight;
    }

    public PineMediaPlayerBean getMediaPlayerBean() {
        return mMediaBean;
    }

    public MediaPlayer.TrackInfo[] getTrackInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && mMediaPlayer.isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LogUtil.d(TAG, "mediaPlayerParams getSelectedTrack MEDIA_TRACK_TYPE_AUDIO: " +
                        mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) +
                        ", MEDIA_TRACK_TYPE_VIDEO:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) +
                        ", MEDIA_TRACK_TYPE_TIMEDTEXT:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) +
                        ", MEDIA_TRACK_TYPE_SUBTITLE:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE));
            }
            MediaPlayer.TrackInfo[] trackInfoArr = mMediaPlayer.getTrackInfo();
            MediaPlayer.TrackInfo trackInfo = null;
            for (int i = 0; i < trackInfoArr.length; i++) {
                trackInfo = trackInfoArr[i];
                LogUtil.d(TAG, "mediaPlayerParams trackInfo.getTrackType:" + trackInfo.getTrackType() +
                        ", trackInfo.getLanguage():" + trackInfo.getLanguage() +
                        ", trackInfo.describeContents():" + trackInfo.describeContents());
            }
            return mMediaPlayer.getTrackInfo();
        } else {
            return null;
        }
    }

    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msc);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msc;
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public boolean isPause() {
        return isInPlaybackState() && mCurrentState == STATE_PAUSED;
    }

    public void enableSurfaceView(boolean enableSurface) {
        mEnableSurfaceView = enableSurface;
    }

    public boolean isSurfaceViewEnable() {
        return mEnableSurfaceView;
    }

    public void toggleFullScreenMode(boolean isLocked) {
        mIsFullScreenMode = !mIsFullScreenMode;
        if (isAttachToFrontMode()) {
            ViewGroup.LayoutParams layoutParams;
            if (isFullScreenMode()) {
                if (mMediaPlayerView.getParent() instanceof RelativeLayout) {
                    layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else if (mMediaPlayerView.getParent() instanceof LinearLayout) {
                    layoutParams = new LinearLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else if (mMediaPlayerView.getParent() instanceof FrameLayout) {
                    layoutParams = new FrameLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                } else {
                    layoutParams = new ViewGroup.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                }
            } else {
                layoutParams = mMediaPlayerView.getHalfAnchorLayout();
            }
            mMediaPlayerView.setLayoutParams(layoutParams);
            mMediaController.updateFullScreenMode();
        }
    }

    public boolean isFullScreenMode() {
        return mIsFullScreenMode;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public boolean isAttachToFrontMode() {
        return mMediaPlayerView != null && (mMediaController != null || mSurfaceView != null);
    }

    public boolean isBackgroundPlayerMode() {
        return mIsBackgroundPlayerMode;
    }

    public int getMediaPlayerState() {
        return mCurrentState;
    }

    public void setMediaPlayerListener(PineMediaWidget.PineMediaPlayerListener listener) {
        mMediaPlayerListener = listener;
    }

    @Override
    public void onSurfaceChanged(SurfaceView surfaceView, int format, int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
        boolean isValidState = (mTargetState == STATE_PLAYING);
        boolean hasValidSize = (mMediaWidth == w && mMediaHeight == h);
        if (mMediaPlayer != null && isValidState && hasValidSize) {
            if (mSeekWhenPrepared != 0) {
                seekTo(mSeekWhenPrepared);
            }
            start();
        }
    }

    @Override
    public void onSurfaceCreated(SurfaceView surfaceView) {
        mSurfaceView = (PineSurfaceView) surfaceView;
        if (mCurrentState != STATE_PLAYING) {
            openMedia(true);
        } else {
            setDisplaySurface(mSurfaceView);
        }
    }

    @Override
    public void onSurfaceDestroyed(SurfaceView surfaceView) {
        // after we return from this we can't use the surface any more
        mSurfaceView = null;
        if (mMediaController != null) {
            mMediaController.hide();
        }
        if (isBackgroundPlayerMode()) {
            setDisplaySurface(null);
        } else {
            release();
        }
    }

    public void setDisplaySurface(PineSurfaceView pineSurfaceView) {
        if (mMediaPlayer != null) {
            if (isAttachToFrontMode() && pineSurfaceView != null) {
                mMediaPlayer.setDisplay(pineSurfaceView.getHolder());
            } else {
                mMediaPlayer.setDisplay(null);
            }
        }
    }

    public void onMediaPlayerViewDetached() {
        mSurfaceView = null;
        mMediaController = null;
        mMediaPlayerView = null;
        mIsFullScreenMode = false;
        mMediaWidth = 0;
        mMediaHeight = 0;
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
    }

    protected void stopPlayback() {
        LogUtil.d(TAG, "stopPlayback");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    /*
    * release the media player in any state
    */
    public void release(boolean clearTargetState) {
        LogUtil.d(TAG, "release clearTargetState:" + clearTargetState);
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (clearTargetState) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
            if (isAttachToFrontMode() && mMediaController != null) {
                mMediaController.onMediaPlayerRelease(clearTargetState);
            }
        }
    }

    private boolean isNeedLocalService() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !USE_NEW_API)
                && mIsLocalStreamMedia;
    }

    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        release();
        if (mLocalServiceState != SERVICE_STATE_DISCONNECTED) {
            LogUtil.d(TAG, "Unbind local service");
            mContext.unbindService(mServiceConnection);
            mLocalServiceState = SERVICE_STATE_DISCONNECTED;
        }
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && isAttachToFrontMode() && mMediaController != null) {
            mMediaController.toggleMediaControlsVisibility();
        }
        return false;
    }

    private void setMetaData(MediaPlayer mediaPlayer) {
        mCanPause = true;
        mCanSeekBack = mCanSeekForward = false;
        try {
            // Class clazzMediaPlayer = mediaPlayer.getClass();
            Class clazzMediaPlayer = Class.forName("android.media.MediaPlayer");
            Method methodMpGetMetadata = clazzMediaPlayer.getMethod("getMetadata",
                    boolean.class, boolean.class);
            Field fieldMPAll = clazzMediaPlayer.getField("METADATA_ALL");
            Field fieldMPFilter = clazzMediaPlayer.getField("BYPASS_METADATA_FILTER");

            Class clazzMetadata = Class.forName("android.media.Metadata");
            Method methodMdHas = clazzMetadata.getMethod("has", int.class);
            Method methodMdGetBoolean = clazzMetadata.getMethod("getBoolean", int.class);
            Field fieldMdPause = clazzMetadata.getField("PAUSE_AVAILABLE");
            Field fieldMdBack = clazzMetadata.getField("SEEK_BACKWARD_AVAILABLE");
            Field fieldMdForward = clazzMetadata.getField("SEEK_FORWARD_AVAILABLE");

            Object metadataObj = methodMpGetMetadata.invoke(mediaPlayer,
                    fieldMPAll.getBoolean(null), fieldMPFilter.getBoolean(null));
            if (metadataObj != null) {
                mCanPause = !(boolean) methodMdHas.invoke(metadataObj, fieldMdPause.getInt(null))
                        || (boolean) methodMdGetBoolean.invoke(metadataObj, fieldMdPause.getInt(null));
                mCanSeekBack = !(boolean) methodMdHas.invoke(metadataObj, fieldMdBack.getInt(null))
                        || (boolean) methodMdGetBoolean.invoke(metadataObj, fieldMdBack.getInt(null));
                mCanSeekForward = !(boolean) methodMdHas.invoke(metadataObj, fieldMdForward.getInt(null))
                        || (boolean) methodMdGetBoolean.invoke(metadataObj, fieldMdForward.getInt(null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
