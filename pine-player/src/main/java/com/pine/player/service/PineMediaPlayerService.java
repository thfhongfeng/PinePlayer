package com.pine.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.component.PineMediaPlayerComponent;
import com.pine.player.util.LogUtil;
import com.pine.player.component.PineMediaPlayerProxy;
import com.pine.player.component.PineMediaWidget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tanghongfeng on 2018/3/30.
 */

public class PineMediaPlayerService extends Service {
    public final static String SERVICE_MEDIA_PLAYER_TAG = "ServiceMediaPlayer";
    private final static String TAG = LogUtil.makeLogTag(PineMediaPlayerService.class);
    private static HashMap<String, PineMediaWidget.IPineMediaPlayer> mMediaPlayerProxyMap =
            new HashMap<>();

    public synchronized static PineMediaWidget.IPineMediaPlayer getMediaPlayerByTag(String tag) {
        return mMediaPlayerProxyMap.get(tag);
    }

    public synchronized static void setMediaPlayerByTag(String tag, PineMediaWidget.IPineMediaPlayer mediaPlayerProxy) {
        mMediaPlayerProxyMap.put(tag, mediaPlayerProxy);
    }

    public synchronized static void destroyAllMediaPlayer() {
        Iterator iterator = mMediaPlayerProxyMap.entrySet().iterator();
        PineMediaWidget.IPineMediaPlayer pineMediaPlayer = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            pineMediaPlayer = (PineMediaWidget.IPineMediaPlayer) entry.getValue();
            if (pineMediaPlayer != null) {
                pineMediaPlayer.onDestroy();
            }
        }
        mMediaPlayerProxyMap.clear();
    }

    public synchronized static void destroyMediaPlayerByTag(String tag) {
        PineMediaWidget.IPineMediaPlayer pineMediaPlayer = mMediaPlayerProxyMap.remove(tag);
        if (pineMediaPlayer != null) {
            pineMediaPlayer.onDestroy();
        }
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        if (getMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG) == null) {
            setMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG, new PineMediaPlayerProxy(
                    new PineMediaPlayerComponent(getApplicationContext())));
        }
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent); // 返回false
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        destroyMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MediaPlayerBinder();
    }

    private class MediaPlayerBinder extends Binder implements IPineMediaPlayerService {
        public PineMediaWidget.IPineMediaPlayer getMediaPlayer() {
            return getMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG);
        }
    }
}
