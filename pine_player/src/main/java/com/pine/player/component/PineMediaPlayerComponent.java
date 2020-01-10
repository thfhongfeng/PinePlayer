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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.service.IPineMediaSocketService;
import com.pine.player.service.PineMediaPlayerService;
import com.pine.player.service.PineMediaSocketService;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.widget.PineSurfaceView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.pine.player.component.PinePlayState.SERVICE_STATE_CONNECTED;
import static com.pine.player.component.PinePlayState.SERVICE_STATE_CONNECTING;
import static com.pine.player.component.PinePlayState.SERVICE_STATE_DISCONNECTED;
import static com.pine.player.component.PinePlayState.STATE_ERROR;
import static com.pine.player.component.PinePlayState.STATE_IDLE;
import static com.pine.player.component.PinePlayState.STATE_PAUSED;
import static com.pine.player.component.PinePlayState.STATE_PLAYBACK_COMPLETED;
import static com.pine.player.component.PinePlayState.STATE_PLAYING;
import static com.pine.player.component.PinePlayState.STATE_PREPARED;
import static com.pine.player.component.PinePlayState.STATE_PREPARING;

/**
 * Created by tanghongfeng on 2017/8/14.
 */

public class PineMediaPlayerComponent implements PineMediaWidget.IPineMediaPlayerComponent {
    private final static String TAG = LogUtils.makeLogTag(PineMediaPlayerComponent.class);
    // 是否使用5.0之后的新API，该API支持本地流播放
    private static final boolean USE_NEW_API = true;
    private final Object LISTENER_SET_LOCK = new Object();
    private final int MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT = 1;
    private final int MAX_RETRY_FOR_BUFFERING_START_TIMEOUT = 3;
    private PinePlayState mPreState = STATE_IDLE;
    private PinePlayState mCurrentState = STATE_IDLE;
    private PinePlayState mTargetState = STATE_IDLE;
    // 是否独立模式（独立模式下播放器不跟随Context的生命周期变化）
    private boolean mIsAutocephalyPlayMode;
    /**
     * 在非独立模式下，当Context销毁时，播放器是销毁(destroy)还是释放(release)
     * true: destroy模式下，Context销毁后，非独立播放器所有状态清除，对象释放，无法使用resume来恢复播放状态
     * false: release模式下，Context销毁后，非独立播放器对象不会释放，可以使用resume来恢复播放状态
     */
    private boolean mShouldDestroyWhenDetach;
    private Context mContext;
    // 上一个播放的多媒体对象
    private PineMediaPlayerBean mOldMediaBean;
    // 准备播放的多媒体对象
    private PineMediaPlayerBean mMediaBean;
    // 准备播放的多媒体头部信息
    private Map<String, String> mHeaders;
    // 播放本地视频文件时，是否需要对流进行处理（比如播放加密视频时，需要设置为true）
    private boolean mIsLocalStreamMedia;
    // 本地播放流服务，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况（模拟网络流进行流播放）
    private IPineMediaSocketService mLocalService;
    private PinePlayState mLocalServiceState = SERVICE_STATE_DISCONNECTED;
    private boolean mIsDelayOpenMedia;
    private boolean mIsDelayStart;
    private PineSurfaceView mSurfaceView = null;
    private PineMediaPlayerView mMediaPlayerView = null;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mMediaWidth, mMediaHeight;
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
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private HashSet<PineMediaWidget.IPineMediaPlayerListener> mMediaPlayerListenerSet =
            new HashSet<>();
    private int mCurrentBufferPercentage;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            setIgnoreTemporaryControllerState(false);
            LogUtils.d(TAG, "onPrepared Media mUri: " + mMediaBean.getMediaUri());
            mPreState = mCurrentState;
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            setMetaData(mp);
            if (canUpdateControllerPlayState()) {
                mMediaPlayerView.getMediaController().onMediaPlayerPrepared();
            }
            mMediaWidth = mp.getVideoWidth();
            mMediaHeight = mp.getVideoHeight();

            int seekToPosition = PineMediaPlayerService
                    .getSeekWhenPrepared(mMediaBean.getMediaCode());  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mMediaPlayerListenerSet.size() > 0) {
                synchronized (LISTENER_SET_LOCK) {
                    for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                        if (listener != null) {
                            listener.onStateChange(mMediaBean, mPreState, STATE_PREPARED);
                        }
                    }
                }
            }
            if (mMediaWidth != 0 && mMediaHeight != 0 && isAttachViewMode() && mSurfaceView != null) {
                //LogUtils.i("@@@@", "media size: " + mMediaWidth +"/"+ mMediaHeight);
                mSurfaceView.getHolder().setFixedSize(mMediaWidth, mMediaHeight);
                if (mSurfaceWidth == mMediaWidth && mSurfaceHeight == mMediaHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the media here instead of in the callback.
                    if (mTargetState == STATE_PLAYING ||
                            PineMediaPlayerService.isShouldPlayWhenPrepared(mMediaBean.getMediaCode())) {
                        start();
                        if (isAttachViewMode() && mMediaPlayerView.getMediaController() != null) {
                            mMediaPlayerView.getMediaController().show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (isAttachViewMode() && mMediaPlayerView.getMediaController() != null) {
                            // Show the media controls when we're paused into a media and make 'em stick.
                            mMediaPlayerView.getMediaController().show(0);
                        }
                    }
                }
            } else {
                // We don't know the media size yet, but should start anyway.
                // The media size might be reported to us later.
                if (mTargetState == STATE_PLAYING ||
                        PineMediaPlayerService.isShouldPlayWhenPrepared(mMediaBean.getMediaCode())) {
                    start();
                }
            }
            if (isAttachViewMode() && mSurfaceView != null) {
                mSurfaceView.requestFocus();
            }
        }
    };
    private float mSpeed = 1.0f;
    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    int currentPos = getCurrentPosition();
                    int duration = getDuration();
                    int bufferPercentage = getBufferPercentage();
                    LogUtils.d(TAG, "onCompletion currentPos:" + currentPos
                            + ", duration:" + duration
                            + ", bufferPercentage:" + bufferPercentage);
                    mPreState = mCurrentState;
                    if (currentPos != duration && bufferPercentage > 0 && bufferPercentage < 100) {
                        mCurrentState = STATE_ERROR;
                        mTargetState = STATE_PLAYING;
                        PineMediaPlayerService.setSeekWhenPrepared(mMediaBean.getMediaCode(), currentPos);
                        PineMediaPlayerService.setShouldPlayWhenPrepared(mMediaBean.getMediaCode(), true);
                        if (canUpdateControllerPlayState()) {
                            mMediaPlayerView.getMediaController().onAbnormalComplete();
                        }
                        if (mMediaPlayerListenerSet.size() > 0) {
                            synchronized (LISTENER_SET_LOCK) {
                                for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                                    if (listener != null) {
                                        listener.onAbnormalComplete(mMediaBean);
                                        listener.onStateChange(mMediaBean, mPreState, mCurrentState);
                                    }
                                }
                            }
                        }
                    } else {
                        mCurrentState = STATE_PLAYBACK_COMPLETED;
                        mTargetState = STATE_PLAYBACK_COMPLETED;
                        if (canUpdateControllerPlayState()) {
                            mMediaPlayerView.getMediaController().onMediaPlayerComplete();
                        }
                        if (mMediaPlayerListenerSet.size() > 0) {
                            synchronized (LISTENER_SET_LOCK) {
                                for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                                    if (listener != null) {
                                        listener.onComplete(mMediaBean);
                                        listener.onStateChange(mMediaBean, mPreState, mCurrentState);
                                    }
                                }
                            }
                        }
                    }
                }
            };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    if (canUpdateControllerPlayState()) {
                        mMediaPlayerView.getMediaController().onBufferingUpdate(percent);
                    }
                    if (mMediaPlayerListenerSet.size() > 0) {
                        synchronized (LISTENER_SET_LOCK) {
                            for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                                if (listener != null) {
                                    listener.onBufferingUpdate(mMediaBean, percent);
                                }
                            }
                        }
                    }
                }
            };
    private int mRetryForBufferingStartTimeout = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT:
                    release();
                    if (mRetryForBufferingStartTimeout < MAX_RETRY_FOR_BUFFERING_START_TIMEOUT) {
                        mRetryForBufferingStartTimeout++;
                        LogUtils.d(TAG, "Resume player when network bandwidth block, " +
                                "retry count:" + mRetryForBufferingStartTimeout);
                        if (mMediaBean != null) {
                            openMedia(true);
                        }
                    }
                    break;
            }
        }
    };
    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    LogUtils.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (canUpdateControllerPlayState()) {
                        mMediaPlayerView.getMediaController().onMediaPlayerError(framework_err, impl_err);
                    }
                    release(true, mMediaBean);
                    /* If an error handler has been supplied, use it and finish. */
                    if (mMediaPlayerListenerSet.size() > 0) {
                        synchronized (LISTENER_SET_LOCK) {
                            boolean result = true;
                            for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                                if (listener != null) {
                                    result = result && listener.onError(mMediaBean, framework_err, impl_err);
                                    listener.onStateChange(mMediaBean, mPreState, mCurrentState);
                                }
                            }
                            if (result) {
                                return true;
                            }
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (isAttachViewMode() && mSurfaceView != null && mSurfaceView.getWindowToken() != null) {
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
                    }
                    return true;
                }
            };
    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogUtils.d(TAG, "onInfo what: " + what + ", extra:" + extra);
                    if (canUpdateControllerPlayState()) {
                        mMediaPlayerView.getMediaController().onMediaPlayerInfo(what, extra);
                    }
                    if (mMediaPlayerListenerSet.size() > 0) {
                        synchronized (LISTENER_SET_LOCK) {
                            for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                                if (listener != null) {
                                    listener.onInfo(mMediaBean, what, extra);
                                }
                            }
                        }
                    }
                    if (what == 703) {
                        savePlayMediaState();
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        mHandler.removeMessages(MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT);
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        mHandler.removeMessages(MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT);
                        mHandler.sendEmptyMessageDelayed(MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT, 2000);
                    }
                    return true;
                }
            };
    // 本地播放流服务（用于低于M版本的本地流播放方案）
    protected ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "Local service connected");
            mLocalService = (IPineMediaSocketService) service;
            mLocalServiceState = SERVICE_STATE_CONNECTED;
            if (mIsDelayOpenMedia) {
                mIsDelayOpenMedia = false;
                mLocalService.setPlayerDecryptor(mMediaBean.getPlayerDecryptor());
                openMedia(false);
                if (isAttachViewMode() && mSurfaceView != null) {
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
            LogUtils.d(TAG, "Local service disconnected!");
            mLocalService = null;
            mLocalServiceState = SERVICE_STATE_DISCONNECTED;
        }
    };
    // media的uri分段数(一个media可能有多个分段uri，则需要无缝衔接)
    private int mMediaSectionUrisCount = -1;

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

    // 这个参数用来在player状态发生改变时暂时忽略其所引起的controller状态的变化，直到重新播放media bean。
    // （正常情况下，在切换controller的过程后，旧的media bean还未完全释放时，会将状态更新到新的controller中）
    // 适用场景：切换controller的同时可能会需要重置或者更换新的media bean，而新的controller不应该去表现旧的media bean的状态。
    // 这个时候可以通过设置mIgnoreControllerStateTemporary为true来避免这个问题。
    // 你不需要手动重置mIgnoreControllerStateTemporary为false，因为在重置或者更换新的media bean时，这个值会被重置为false。
    private boolean mIgnoreControllerStateTemporary;

    private boolean canUpdateControllerPlayState() {
        return isAttachViewMode() && mMediaPlayerView.getMediaController() != null &&
                !mIgnoreControllerStateTemporary;
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
        Uri mediaUri = mMediaBean.getMediaUri();
        if (mediaUri == null || TextUtils.isEmpty(mediaUri.toString())) {
            return;
        }
        if (isNeedLocalService() && mLocalServiceState != SERVICE_STATE_CONNECTED) {
            return;
        }
        setIgnoreTemporaryControllerState(false);
        LogUtils.d(TAG, "Open Media mUri:" + mediaUri + ", isResumeState:" + isResumeState);
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false, mOldMediaBean);

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(mSpeed));
                } catch (Exception e) {
                    // java.lang.SecurityException
                }
            }
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mPreState = mCurrentState;
            mCurrentState = STATE_PREPARING;
            attachMediaController(true, isResumeState);
            if (mMediaPlayerListenerSet.size() > 0) {
                synchronized (LISTENER_SET_LOCK) {
                    for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                        if (listener != null) {
                            listener.onStateChange(mMediaBean, mPreState, mCurrentState);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LogUtils.w(TAG, "Unable to open content: " + mediaUri, ex);
            mPreState = mCurrentState;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            LogUtils.w(TAG, "Unable to open content: " + mediaUri, ex);
            mPreState = mCurrentState;
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
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

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                                Map<String, String> headers, boolean resumeState) {
        setPlayingMedia(pineMediaPlayerBean, headers, resumeState, mIsAutocephalyPlayMode);
    }

    /**
     * 设置多媒体播放参数
     *
     * @param pineMediaPlayerBean   多媒体播放参数对象
     * @param headers               多媒体播放信息头
     * @param resumeState           此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
     * @param isAutocephalyPlayMode 是否独立播放模式
     */
    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                                Map<String, String> headers, boolean resumeState, boolean isAutocephalyPlayMode) {
        LogUtils.d(TAG, "setPlayingMedia resumeState:" + resumeState +
                ", isAutocephalyPlayMode:" + isAutocephalyPlayMode + ",pineMediaPlayerBean :" + pineMediaPlayerBean);
        mOldMediaBean = mMediaBean;
        mMediaBean = pineMediaPlayerBean;
        mRetryForBufferingStartTimeout = 0;
        if (!resumeState) {
            clearPlayMediaState();
        }
        mHeaders = headers;
        mIsAutocephalyPlayMode = isAutocephalyPlayMode;
        mMediaSectionUrisCount = mMediaBean != null ? mMediaBean.getMediaSectionUris().size() : -1;
        mIsLocalStreamMedia = mMediaBean != null && mMediaBean.isLocalStreamBean();
        if (mLocalService != null) {
            mLocalService.setPlayerDecryptor(mMediaBean != null ? mMediaBean.getPlayerDecryptor() : null);
        }
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            openMedia(resumeState);
            if (isAttachViewMode() && mSurfaceView != null) {
                mSurfaceView.requestLayout();
                mSurfaceView.invalidate();
            }
        } else {
            // 如果需要使用到本地播放流服务，若流服务还未启动，则延时到服务启动完成后在打开Media
            mIsDelayOpenMedia = true;
            mLocalServiceState = SERVICE_STATE_CONNECTING;
            Intent intent = new Intent("media.socket.server");
            intent.setPackage(mContext.getPackageName());
            LogUtils.d(TAG, "Bind local service");
            mContext.bindService(intent, mServiceConnection, mContext.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                           Map<String, String> headers) {
        LogUtils.d(TAG, "resetPlayingMediaAndResume pineMediaPlayerBean:" + pineMediaPlayerBean);
        setPlayingMedia(pineMediaPlayerBean, headers, true, mIsAutocephalyPlayMode);
    }

    @Override
    public float getSpeed() {
        return mSpeed;
    }

    @Override
    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSpeed = speed;
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(mSpeed));
                } else {
                    mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(mSpeed));
                    mMediaPlayer.pause();
                }
            } catch (IllegalStateException e) {
                // 播放本地视频时，setPlaybackParams第一次会报IllegalStateException异常。。。，只能重启播放
                if (mMediaBean != null) {
                    PineMediaPlayerService.setSeekWhenPrepared(mMediaBean.getMediaCode(), getCurrentPosition());
                    PineMediaPlayerService.setShouldPlayWhenPrepared(mMediaBean.getMediaCode(), isPlaying());
                }
                openMedia(true);
            }
            if (canUpdateControllerPlayState()) {
                mMediaPlayerView.getMediaController().onMediaPlayerSpeedChange();
            }
        }
    }

    @Override
    public void start() {
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            LogUtils.d(TAG, "Start media player");
            if (isInPlaybackState()) {
                mMediaPlayer.start();
                PineMediaPlayerService.setShouldPlayWhenPrepared(mMediaBean.getMediaCode(), false);
                if (canUpdateControllerPlayState()) {
                    mMediaPlayerView.getMediaController().onMediaPlayerStart();
                }
                mPreState = mCurrentState;
                mCurrentState = STATE_PLAYING;
                if (mMediaPlayerListenerSet.size() > 0) {
                    synchronized (LISTENER_SET_LOCK) {
                        for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                            if (listener != null) {
                                listener.onStateChange(mMediaBean, mPreState, STATE_PLAYING);
                            }
                        }
                    }
                }
            }
            mTargetState = STATE_PLAYING;
        } else {
            mIsDelayStart = true;
        }
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                LogUtils.d(TAG, "Pause media player");
                mMediaPlayer.pause();
                if (canUpdateControllerPlayState()) {
                    mMediaPlayerView.getMediaController().onMediaPlayerPause();
                }
                mPreState = mCurrentState;
                mCurrentState = STATE_PAUSED;
                if (mMediaPlayerListenerSet.size() > 0) {
                    synchronized (LISTENER_SET_LOCK) {
                        for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                            if (listener != null) {
                                listener.onStateChange(mMediaBean, mPreState, STATE_PAUSED);
                            }
                        }
                    }
                }
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public void suspend() {
        release(false, mMediaBean);
    }

    @Override
    public void resume() {
        if (isInPlaybackState()) {
            attachMediaController(false, true);
            if (PineMediaPlayerService
                    .getSeekWhenPrepared(mMediaBean.getMediaCode()) != 0) {
                seekTo(PineMediaPlayerService
                        .getSeekWhenPrepared(mMediaBean.getMediaCode()));
            }
            if (PineMediaPlayerService
                    .isShouldPlayWhenPrepared(mMediaBean.getMediaCode())) {
                start();
            }
        } else {
            openMedia(true);
        }
    }

    @Override
    public void release() {
        release(true, mMediaBean);
    }


    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        mRetryForBufferingStartTimeout = 0;
        release();
        mMediaPlayerListenerSet.clear();
        if (mLocalServiceState != SERVICE_STATE_DISCONNECTED) {
            LogUtils.d(TAG, "Unbind local service");
            mContext.unbindService(mServiceConnection);
            mLocalService = null;
            mLocalServiceState = SERVICE_STATE_DISCONNECTED;
        }
        detachMediaPlayerView(mMediaPlayerView);
        mMediaWidth = 0;
        mMediaHeight = 0;
        mOldMediaBean = null;
        mMediaBean = null;
    }

    @Override
    public void savePlayMediaState() {
        if (isInPlaybackState()) {
            PineMediaPlayerService.setSeekWhenPrepared(mMediaBean.getMediaCode(), getCurrentPosition());
            PineMediaPlayerService.setShouldPlayWhenPrepared(mMediaBean.getMediaCode(), isPlaying());
        }
    }

    @Override
    public void clearPlayMediaState() {
        if (mMediaBean != null) {
            PineMediaPlayerService.clearPlayMediaState(mMediaBean.getMediaCode());
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public PineMediaPlayerBean getMediaPlayerBean() {
        return mMediaBean;
    }

    @Override
    public MediaPlayer.TrackInfo[] getTrackInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && mMediaPlayer != null &&
                mMediaPlayer.isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LogUtils.d(TAG, "mediaPlayerParams getSelectedTrack MEDIA_TRACK_TYPE_AUDIO: " +
                        mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) +
                        ", MEDIA_TRACK_TYPE_VIDEO:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) +
                        ", MEDIA_TRACK_TYPE_TIMEDTEXT:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) +
                        ", MEDIA_TRACK_TYPE_SUBTITLE:" + mMediaPlayer.getSelectedTrack(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE));
            }
            MediaPlayer.TrackInfo[] trackInfoArr = mMediaPlayer.getTrackInfo();
            MediaPlayer.TrackInfo trackInfo = null;
            for (int i = 0; i < trackInfoArr.length; i++) {
                trackInfo = trackInfoArr[i];
                LogUtils.d(TAG, "mediaPlayerParams trackInfo.getTrackType:" + trackInfo.getTrackType() +
                        ", trackInfo.getLanguage():" + trackInfo.getLanguage() +
                        ", trackInfo.describeContents():" + trackInfo.describeContents());
            }
            return mMediaPlayer.getTrackInfo();
        } else {
            return null;
        }
    }

    @Override
    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            LogUtils.d(TAG, "seek to:" + msc);
            mMediaPlayer.seekTo(msc);
            PineMediaPlayerService.setSeekWhenPrepared(mMediaBean.getMediaCode(), 0);
        } else {
            if (mMediaBean != null) {
                PineMediaPlayerService.setSeekWhenPrepared(mMediaBean.getMediaCode(), msc);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPause() {
        return isInPlaybackState() && mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isSurfaceViewEnable() {
        return mSurfaceView != null;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    @Override
    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    /**
     * 是否本地视频流服务方式播放
     */
    @Override
    public boolean isLocalStreamMode() {
        return false;
    }

    @Override
    public boolean isAttachViewMode() {
        return mMediaPlayerView != null;
    }

    @Override
    public boolean isAttachViewShown() {
        return isAttachViewMode() && mMediaPlayerView.isShown();
    }

    @Override
    public boolean isAutocephalyPlayMode() {
        return mIsAutocephalyPlayMode;
    }

    @Override
    public boolean shouldDestroyPlayerWhenDetach() {
        return mShouldDestroyWhenDetach;
    }

    /**
     * 设置是否为独立播放模式（是否与播放界面共生命周期）
     *
     * @param isAutocephalyPlayMode   设置是否为独立播放模式
     * @param shouldDestroyWhenDetach 在非独立模式下，当控件View从上下文环境中（Context）移除时，
     *                                播放器是销毁(destroy)还是释放(release)
     *                                true: destroy模式下，从Context中移除后，非独立播放器所有状态清除，对象销毁，无法使用resume来恢复播放状态
     *                                false: release模式下，从Context中移除后，非独立播放器所有状态清除，对象不会销毁，可以使用resume来恢复播放状态
     */
    @Override
    public void setAutocephalyPlayMode(boolean isAutocephalyPlayMode, boolean shouldDestroyWhenDetach) {
        mIsAutocephalyPlayMode = isAutocephalyPlayMode;
        mShouldDestroyWhenDetach = shouldDestroyWhenDetach;
    }

    @Override
    public PinePlayState getMediaPlayerState() {
        return mCurrentState;
    }

    @Override
    public void setIgnoreTemporaryControllerState(boolean ignoreControllerStateTemporary) {
        this.mIgnoreControllerStateTemporary = ignoreControllerStateTemporary;
    }

    @Override
    public boolean ignoreTemporaryControllerState() {
        return mIgnoreControllerStateTemporary;
    }

    @Override
    public void removeMediaPlayerListener(PineMediaWidget.IPineMediaPlayerListener listener) {
        synchronized (LISTENER_SET_LOCK) {
            mMediaPlayerListenerSet.remove(listener);
        }
    }

    @Override
    public void addMediaPlayerListener(PineMediaWidget.IPineMediaPlayerListener listener) {
        synchronized (LISTENER_SET_LOCK) {
            mMediaPlayerListenerSet.add(listener);
        }
    }

    @Override
    public void setMediaPlayerView(PineMediaPlayerView playerView, boolean forResume) {
        if (mMediaPlayerView != null && playerView != mMediaPlayerView) {
            detachMediaPlayerView(mMediaPlayerView);
        }
        mMediaPlayerView = playerView;
        if (forResume) {
            setDisplaySurface(mSurfaceView);
        }
        if (mMediaPlayerView != null) {
            mMediaPlayerView.onMediaComponentAttach();
        }
    }

    @Override
    public PineMediaPlayerView getMediaPlayerView() {
        return mMediaPlayerView;
    }

    @Override
    public void detachMediaPlayerView(PineMediaPlayerView view) {
        if (view == mMediaPlayerView && mMediaPlayerView != null) {
            mMediaPlayerView.onMediaComponentDetach();
            mMediaPlayerView = null;
        }
    }

    /**
     * 挂载控制器界面
     *
     * @param isPlayerReset 本此attach是否重置了MediaPlayer
     * @param isResumeState 本此attach是否是为了恢复状态
     */
    @Override
    public void attachMediaController(boolean isPlayerReset, boolean isResumeState) {
        if (mMediaPlayer != null && isAttachViewMode() &&
                mMediaPlayerView.getMediaController() != null && mMediaBean != null) {
            mMediaPlayerView.getMediaController().setPlayingMedia(mMediaBean, "PineMediaView");
            mMediaPlayerView.getMediaController().attachToParentView(isPlayerReset, isResumeState);
        }
    }

    @Override
    public PineSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    @Override
    public PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout() {
        return mSurfaceView == null ? null : mSurfaceView.getMediaAdaptionLayout();
    }

    @Override
    public int getMediaViewWidth() {
        return mMediaWidth;
    }

    @Override
    public int getMediaViewHeight() {
        return mMediaHeight;
    }

    @Override
    public void onSurfaceChanged(PineMediaPlayerView mediaPlayerView,
                                 SurfaceView surfaceView, int format, int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
        boolean isValidState = (mTargetState == STATE_PLAYING);
        boolean hasValidSize = (mMediaWidth == w && mMediaHeight == h);
        if (mMediaPlayer != null && isValidState && hasValidSize) {
            if (PineMediaPlayerService
                    .getSeekWhenPrepared(mMediaBean.getMediaCode()) != 0) {
                seekTo(PineMediaPlayerService
                        .getSeekWhenPrepared(mMediaBean.getMediaCode()));
            }
            start();
        }
    }

    @Override
    public void onSurfaceCreated(PineMediaPlayerView mediaPlayerView, SurfaceView surfaceView) {
        mSurfaceView = (PineSurfaceView) surfaceView;
        setDisplaySurface(mSurfaceView);
    }

    @Override
    public void onSurfaceDestroyed(PineMediaPlayerView mediaPlayerView, SurfaceView surfaceView) {
        // after we return from this we can't use the surface any more
        mSurfaceView = null;
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
        if (isAttachViewMode() && mMediaPlayerView.getMediaController() != null) {
            mMediaPlayerView.getMediaController().hide();
        }
        setDisplaySurface(null);
    }

    public void setDisplaySurface(PineSurfaceView pineSurfaceView) {
        if (mMediaPlayer != null) {
            if (isAttachViewMode() && pineSurfaceView != null) {
                mMediaPlayer.setDisplay(pineSurfaceView.getHolder());
            } else {
                mMediaPlayer.setDisplay(null);
            }
        }
    }

    protected void stopPlayback() {
        LogUtils.d(TAG, "stopPlayback");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPreState = mCurrentState;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
            if (mMediaPlayerListenerSet.size() > 0) {
                synchronized (LISTENER_SET_LOCK) {
                    for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                        if (listener != null) {
                            listener.onStateChange(mMediaBean, mPreState, STATE_IDLE);
                        }
                    }
                }
            }
        }
    }

    /*
     * release the media player in any state
     */
    public void release(boolean clearTargetState, PineMediaPlayerBean mediaBean) {
        LogUtils.d(TAG, "release clearTargetState:" + clearTargetState);
        mHandler.removeMessages(MSG_MEDIA_INFO_BUFFERING_START_TIMEOUT);
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPreState = mCurrentState;
            mCurrentState = STATE_IDLE;
            if (clearTargetState) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
            if (canUpdateControllerPlayState()) {
                mMediaPlayerView.getMediaController().onMediaPlayerRelease(clearTargetState);
            }
            if (mMediaPlayerListenerSet.size() > 0 && mediaBean != null) {
                synchronized (LISTENER_SET_LOCK) {
                    for (PineMediaWidget.IPineMediaPlayerListener listener : mMediaPlayerListenerSet) {
                        if (listener != null) {
                            listener.onStateChange(mediaBean, mPreState, STATE_IDLE);
                        }
                    }
                }
            }
        }
    }

    private boolean isNeedLocalService() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !USE_NEW_API)
                && mIsLocalStreamMedia;
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && isAttachViewMode() && mMediaPlayerView.getMediaController() != null) {
            mMediaPlayerView.getMediaController().toggleMediaControllerVisibility();
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
