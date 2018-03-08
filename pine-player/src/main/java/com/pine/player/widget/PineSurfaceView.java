package com.pine.player.widget;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.pine.player.PineConstants;
import com.pine.player.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.service.PineMediaSocketService;
import com.pine.player.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by tanghongfeng on 2017/8/14.
 * <p>
 * 注意事项：
 * 1、使用此控件必须在外层包一层RelativeLayout
 * 2、若要保证全屏效果正常，请将外包的RelativeLayout一起置于具有全屏布局能力的父布局中，
 * 且该全屏布局必须是RelativeLayout,FrameLayout,LinearLayout中的一种
 */

public class PineSurfaceView extends SurfaceView implements PineMediaWidget.IPineMediaPlayer {
    // 播放器状态
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    private final static String TAG = "PineSurfaceView";
    private static final long BACK_PRESSED_EXIT_TIME = 2000;
    // 是否使用5.0之后的新API，该API支持本地流播放
    private static final boolean USE_NEW_API = true;
    // 本地播放流服务状态，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况
    private static final int SERVICE_STATE_DISCONNECTED = 1;
    private static final int SERVICE_STATE_CONNECTING = 2;
    private static final int SERVICE_STATE_CONNECTED = 3;
    private final int PINE_MEDIA_DEFAULT_PORT = 18888;
    private final String MEDIA_LOCAL_SOCKET_URL = "http://127.0.0.1:";
    private int mSocketPort = PINE_MEDIA_DEFAULT_PORT;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private Context mContext;

    // 多媒体播放列表对象
    private List<PineMediaPlayerBean> mMediaBeanList;
    // 多媒体头部信息列表对象
    private List<Map<String, String>> mHeaderList;
    // 准备播放的多媒体对象
    private PineMediaPlayerBean mMediaBean;
    // 准备播放的多媒体头部信息
    private Map<String, String> mHeaders;
    // 播放本地视频文件时，是否需要对流进行处理（比如播放加密视频时，需要设置为true）
    private boolean mIsLocalStreamMedia;

    // 本地播放流服务，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况（模拟网络流进行流播放）
    private PineMediaSocketService mLocalService;
    private int mLocalServiceState = SERVICE_STATE_DISCONNECTED;
    private boolean mIsDelayOpenMedia;
    private boolean mIsDelayStart;

    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mMediaWidth, mMediaHeight;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mMediaWidth = mp.getVideoWidth();
                    mMediaHeight = mp.getVideoHeight();
                    if (mMediaWidth != 0 && mMediaHeight != 0) {
                        getHolder().setFixedSize(mMediaWidth, mMediaHeight);
                        requestLayout();
                    }
                }
            };
    // MediaView在onMeasure中调整后的布局属性，只有在onMeasure之后获取才有效
    private PineMediaPlayerView.PineMediaViewLayout mAdaptionMediaLayout =
            new PineMediaPlayerView.PineMediaViewLayout();
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    // 播放器的控制器
    private PineMediaWidget.IPineMediaController mMediaController;
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
    // 点击回退按键时，使用两次点击的时间间隔限定回退行为
    private long mExitTime = -1l;
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
            if (mMediaController != null) {
                mMediaController.setControllerEnabled(true);
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
                getHolder().setFixedSize(mMediaWidth, mMediaHeight);
                if (mSurfaceWidth == mMediaWidth && mSurfaceHeight == mMediaHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the media here instead of in the callback.
                    if (mTargetState == STATE_PLAYING || mShouldPlayWhenPrepared) {
                        mShouldPlayWhenPrepared = false;
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
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
            requestFocus();
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
                        if (mMediaController != null) {
                            mMediaController.onAbnormalComplete();
                        }
                        if (mMediaPlayerListener != null) {
                            mMediaPlayerListener.onAbnormalComplete();
                        }
                    } else {
                        mCurrentState = STATE_PLAYBACK_COMPLETED;
                        mTargetState = STATE_PLAYBACK_COMPLETED;
                        if (mMediaController != null) {
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
                    if (mMediaController != null) {
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
                    if (mMediaController != null) {
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
                    if (getWindowToken() != null) {
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

                        new AlertDialog.Builder(mContext)
                                .setMessage(messageId)
                                .setPositiveButton(Resources.getSystem()
                                                .getIdentifier("VideoView_error_button",
                                                        "string", "android"),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                /* If we get here, there is no onError listener, so
                                                 * at least inform them that the media is over.
                                                 */
                                                if (mMediaPlayerListener != null) {
                                                    mMediaPlayerListener.onCompletion();
                                                }
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    if (mMediaController != null) {
                        mMediaController.onBufferingUpdate(percent);
                    }
                }
            };

    // 本地播放流服务（用于低于M版本的本地流播放方案）
    protected ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "Local service connected");
            mLocalService = ((PineMediaSocketService.MyBinder) service).getService();
            mLocalServiceState = SERVICE_STATE_CONNECTED;
            if (mIsDelayOpenMedia) {
                mIsDelayOpenMedia = false;
                mLocalService.setPlayerDecryptor(mMediaBean.getPlayerDecryptor());
                openMedia(false);
                requestLayout();
                invalidate();
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

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            LogUtil.d(TAG, "surfaceChanged");
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

        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceCreated");
            mSurfaceHolder = holder;
            openMedia(true);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceDestroyed");
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) {
                mMediaController.hide();
            }
            release(true);
        }
    };

    protected PineSurfaceView(Context context) {
        super(context);
        mContext = context;
        initMediaView();
    }

    protected PineSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected PineSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initMediaView();
    }

    private void initMediaView() {
        mMediaWidth = 0;
        mMediaHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    protected void setMediaController(PineMediaWidget.IPineMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController(true, false);
    }

    /**
     * 挂载控制器界面
     *
     * @param isPlayerReset 本此attach是否重置了MediaPlayer
     * @param isResumeState 本此attach是否是为了恢复状态
     */
    private void attachMediaController(boolean isPlayerReset, boolean isResumeState) {
        if (mMediaPlayer != null && mMediaController != null && mMediaBean != null) {
            this.setTag("PineMediaView");
            mMediaController.setPlayingMedia(mMediaBean, "PineMediaView");
            mMediaController.attachToParentView(isPlayerReset, isResumeState);
        }
    }

    /**
     * 设置是否本地流播放需求
     *
     * @param isLocalStream 是否本地播放流
     * @param port          本地播放流端口，如果设置为空或者小于等于0的端口，
     *                      则会使用程序自动生成的默认端口
     */
    protected void setLocalStreamMode(boolean isLocalStream, int port) {
        // 设置是否本地流播放需求
        mIsLocalStreamMedia = isLocalStream;
        mSocketPort = port;
        if (mSocketPort <= 0) {
            mSocketPort = PINE_MEDIA_DEFAULT_PORT + new Random().nextInt(100);
        }
        if (isNeedLocalService() && mLocalServiceState == SERVICE_STATE_DISCONNECTED) {
            mLocalServiceState = SERVICE_STATE_CONNECTING;
            Intent intent = new Intent("media.socket.server");
            intent.setPackage(mContext.getPackageName());
            intent.putExtra(PineMediaSocketService.PINE_MEDIA_SOCKET_PORT_KEY, mSocketPort);
            LogUtil.d(TAG, "Bind local service");
            mContext.bindService(intent, mServiceConnection, mContext.BIND_AUTO_CREATE);
        }
    }

    /**
     * 设置多媒体播放参数
     *
     * @param pineMediaPlayerBean 多媒体播放参数对象
     * @param headers             多媒体播放信息头
     * @param resumeState         此次播放是否恢复到之前的播放状态(用于被动中断后的恢复)
     */
    private void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean,
                            Map<String, String> headers, boolean resumeState) {
        mMediaBean = pineMediaPlayerBean;
        mHeaders = headers;
        if (mLocalService != null) {
            mLocalService.setPlayerDecryptor(pineMediaPlayerBean.getPlayerDecryptor());
        }
        if (!resumeState) {
            mSeekWhenPrepared = 0;
            mShouldPlayWhenPrepared = false;
        }
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            openMedia(resumeState);
            requestLayout();
            invalidate();
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
        if (mediaUri == null || mSurfaceHolder == null) {
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
                            Uri.parse(MEDIA_LOCAL_SOCKET_URL + mSocketPort), mHeaders);
                }
            } else {
                mMediaPlayer.setDataSource(mContext, mediaUri, mHeaders);
            }
            mMediaPlayer.setDisplay(mSurfaceHolder);
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

    @Override
    public float getSpeed() {
        return mSpeed;
    }

    @Override
    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSpeed = speed;
            mShouldPlayWhenPrepared = isPlaying();
            mSeekWhenPrepared = getCurrentPosition();
            openMedia(true);
        }
    }

    @Override
    public void start() {
        if (!isNeedLocalService() || mLocalServiceState == SERVICE_STATE_CONNECTED) {
            if (isInPlaybackState()) {
                LogUtil.d(TAG, "Start media player");
                mMediaPlayer.start();
                if (mMediaController != null) {
                    mMediaController.onMediaPlayerStart();
                }
                mCurrentState = STATE_PLAYING;
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
                LogUtil.d(TAG, "Pause media player");
                mMediaPlayer.pause();
                if (mMediaController != null) {
                    mMediaController.onMediaPlayerPause();
                }
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public void suspend() {
        release(false);
    }

    @Override
    public void resume() {
        openMedia(true);
    }

    @Override
    public void release() {
        release(true);
    }

    @Override
    public PineMediaWidget.IPineMediaController getController() {
        return mMediaController;
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean) {
        setPlayingMedia(pineMediaPlayerBean, null, false);
    }

    @Override
    public void setPlayingMedia(PineMediaPlayerBean pineMediaPlayerBean, Map<String, String> headers) {
        setPlayingMedia(pineMediaPlayerBean, headers, false);
    }

    @Override
    public void resetPlayingMediaAndResume(PineMediaPlayerBean pineMediaPlayerBean,
                                    Map<String, String> headers) {
        setPlayingMedia(pineMediaPlayerBean, headers, true);
    }

    @Override
    public void savePlayerState() {
        mShouldPlayWhenPrepared = isPlaying();
        mSeekWhenPrepared = getCurrentPosition();
        pause();
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
    public int getMediaViewWidth() {
        return mMediaWidth;
    }

    @Override
    public int getMediaViewHeight() {
        return mMediaHeight;
    }

    @Override
    public void setMediaList(List<PineMediaPlayerBean> pineMediaPlayerBeanList,
                                List<Map<String, String>> headerList) {
        mMediaBeanList = pineMediaPlayerBeanList;
        mHeaderList = headerList;
    }

    @Override
    public List<PineMediaPlayerBean> getMediaList() {
        return mMediaBeanList;
    }

    @Override
    public List<Map<String, String>> getMediaHeadList() {
        return mHeaderList;
    }

    @Override
    public PineMediaPlayerBean getMediaPlayerBean() {
        return mMediaBean;
    }

    @Override
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

    @Override
    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msc);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msc;
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
    public void toggleFullScreenMode(boolean isLocked) {
        mIsFullScreenMode = !mIsFullScreenMode;
    }

    @Override
    public boolean isFullScreenMode() {
        return mIsFullScreenMode;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public int getMediaPlayerState() {
        return mCurrentState;
    }

    // 获取MediaView在onMeasure中调整后的布局属性，只有在onMeasure之后获取才有效
    @Override
    public PineMediaPlayerView.PineMediaViewLayout getMediaAdaptionLayout() {
        return mAdaptionMediaLayout;
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
    public void setMediaPlayerListener(PineMediaWidget.PineMediaPlayerListener listener) {
        mMediaPlayerListener = listener;
    }

    @Override
    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
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
    protected void release(boolean clearTargetState) {
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
            if (mMediaController != null) {
                mMediaController.onMediaPlayerRelease(clearTargetState);
            }
        }
    }

    private boolean isNeedLocalService() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !USE_NEW_API)
                && mIsLocalStreamMedia;
    }

    @Override
    protected void onAttachedToWindow() {
        LogUtil.d(TAG, "Attached to window");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LogUtil.d(TAG, "Detach from window");
        if (mLocalServiceState != SERVICE_STATE_DISCONNECTED) {
            LogUtil.d(TAG, "Unbind local service");
            mContext.unbindService(mServiceConnection);
            mLocalServiceState = SERVICE_STATE_DISCONNECTED;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mMediaWidth, widthMeasureSpec);
        int height = getDefaultSize(mMediaHeight, heightMeasureSpec);
        if (mMediaWidth > 0 && mMediaHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mMediaWidth * height < width * mMediaHeight) {
                    //LogUtil.i("@@@", "image too wide, correcting");
                    width = height * mMediaWidth / mMediaHeight;
                } else if (mMediaWidth * height > width * mMediaHeight) {
                    //LogUtil.i("@@@", "image too tall, correcting");
                    height = width * mMediaHeight / mMediaWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mMediaHeight / mMediaWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mMediaWidth / mMediaHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual media size
                width = mMediaWidth;
                height = mMediaHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mMediaWidth / mMediaHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mMediaHeight / mMediaWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mAdaptionMediaLayout.width = getMeasuredWidth();
        mAdaptionMediaLayout.height = getMeasuredHeight();
        mAdaptionMediaLayout.left = getLeft();
        mAdaptionMediaLayout.right = getRight();
        mAdaptionMediaLayout.top = getTop();
        mAdaptionMediaLayout.bottom = getBottom();
    }

    @Override
    public void draw(Canvas canvas) {
        LogUtil.d(TAG, "draw");
        super.draw(canvas);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            mMediaController.toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mMediaController != null) {
            int keyCode = event.getKeyCode();
            final boolean uniqueDown = event.getRepeatCount() == 0
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (uniqueDown) {
                    mMediaController.doPauseResume();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                    mMediaController.pausePlayBtnRequestFocus();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (uniqueDown && !mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (uniqueDown && mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show(PineConstants.DEFAULT_SHOW_TIMEOUT);
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                    || keyCode == KeyEvent.KEYCODE_CAMERA) {
                // don't show the controls for volume adjustment
                mMediaController.updateVolumesText();
                return super.dispatchKeyEvent(event);
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                if (uniqueDown && mCurrentState == STATE_PLAYING
                        && System.currentTimeMillis() - mExitTime > BACK_PRESSED_EXIT_TIME
                        && !isFullScreenMode()) {
                    mMediaController.hide();
                    mExitTime = System.currentTimeMillis();
                    Toast.makeText(mContext, R.string.pine_media_back_pressed_toast,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                mMediaController.toggleMediaControlsVisibility();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mMediaController != null) {
                    if (isPlaying() || isPause()) {
                        mMediaController.toggleMediaControlsVisibility();
                    } else {
                        mMediaController.show(0);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
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
