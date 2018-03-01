package com.pine.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.pine.player.decrytor.IPineMediaDecryptor;
import com.pine.player.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by tanghongfeng on 2017/8/18.
 */

public class PineMediaSocketService extends Service {

    public final static String PINE_MEDIA_SOCKET_PORT_KEY = "port_key";
    private final static String TAG = "PineMediaSocketService";
    private ExecutorService mThreads;
    private PineMediaServerThread mPineMediaServerThread;

    @Override
    public IBinder onBind(Intent intent) {
        int port = intent.getIntExtra(PINE_MEDIA_SOCKET_PORT_KEY, 0);
        if (mPineMediaServerThread == null && port > 0) {
            mPineMediaServerThread = new PineMediaServerThread(port);
            mThreads.submit(mPineMediaServerThread);
        }
        return new MyBinder();
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

    public void setPlayerDecryptor(IPineMediaDecryptor pinePlayerDecryptor) {
        mPineMediaServerThread.setPlayerDecryptor(pinePlayerDecryptor);
    }

    public class MyBinder extends Binder {
        public PineMediaSocketService getService() {
            return PineMediaSocketService.this;
        }
    }
}