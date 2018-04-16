package com.pine.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.pine.player.decrytor.IPineMediaDecryptor;
import com.pine.player.util.LogUtil;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by tanghongfeng on 2017/8/18.
 */

public class PineMediaSocketService extends Service {
    public static final String MEDIA_LOCAL_SOCKET_URL = "http://127.0.0.1:";
    // 本地播放流服务状态，用于兼容5.0以下版本的mediaPlayer不支持本地流播放的情况
    public static final int SERVICE_STATE_IDLE = 0;
    public static final int SERVICE_STATE_DISCONNECTED = 1;
    public static final int SERVICE_STATE_CONNECTING = 2;
    public static final int SERVICE_STATE_CONNECTED = 3;
    private static final String TAG = LogUtil.makeLogTag(PineMediaSocketService.class);
    private static int mSocketPort;
    private static PineMediaSocketThread mPineMediaServerThread;

    static {
        mSocketPort = 18888 + new Random().nextInt(100);
    }

    private ExecutorService mThreads;

    public static String getMediaLocalSocketUrl() {
        return MEDIA_LOCAL_SOCKET_URL + mSocketPort;
    }

    public static int getMediaServiceState() {
        return mPineMediaServerThread == null ? SERVICE_STATE_IDLE :
                mPineMediaServerThread.getMediaSocketState();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mSocketPort <= 0) {
            return null;
        }
        if (getMediaServiceState() != SERVICE_STATE_CONNECTED ||
                getMediaServiceState() != SERVICE_STATE_CONNECTING) {
            if (mPineMediaServerThread != null) {
                mPineMediaServerThread.release();
            }
            mPineMediaServerThread = new PineMediaSocketThread(mSocketPort);
            mThreads.submit(mPineMediaServerThread);
        }
        return new MediaSocketBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent); // 返回false
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        super.onCreate();
        mThreads = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ServerThread");
            }
        });
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        if (mPineMediaServerThread != null) {
            mPineMediaServerThread.release();
            mPineMediaServerThread = null;
        }
        mThreads.shutdownNow();
        super.onDestroy();
    }

    public class MediaSocketBinder extends Binder implements IPineMediaSocketService {
        @Override
        public void setPlayerDecryptor(IPineMediaDecryptor pinePlayerDecryptor) {
            mPineMediaServerThread.setPlayerDecryptor(pinePlayerDecryptor);
        }
    }
}