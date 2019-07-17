package com.pine.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.pine.player.component.PineMediaPlayerProxy;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tanghongfeng on 2018/3/30.
 */

public class PineMediaPlayerService extends Service {
    public final static String SERVICE_MEDIA_PLAYER_TAG = "ServiceMediaPlayer";
    private final static String TAG = LogUtil.makeLogTag(PineMediaPlayerService.class);
    private static HashMap<String, PineMediaPlayerProxy> mMediaPlayerProxyMap =
            new HashMap<>();
    private static HashMap<String, Boolean> mMediaShouldPlayWhenPreparedMap =
            new HashMap<>();
    private static HashMap<String, Integer> mMediaSeekWhenPreparedMap =
            new HashMap<>();

    public synchronized static PineMediaWidget.IPineMediaPlayer getMediaPlayerByTag(String tag) {
        return mMediaPlayerProxyMap.get(tag);
    }

    public synchronized static void setMediaPlayerByTag(String tag,
                                                        PineMediaPlayerProxy mediaPlayerProxy) {
        LogUtil.d(TAG, "setMediaPlayerByTag tag:" + tag + ",mediaPlayerProxy:" + mediaPlayerProxy);
        mMediaPlayerProxyMap.put(tag, mediaPlayerProxy);
    }

    public synchronized static void destroyAllMediaPlayer() {
        LogUtil.d(TAG, "destroyAllMediaPlayer");
        Iterator iterator = mMediaPlayerProxyMap.entrySet().iterator();
        PineMediaPlayerProxy pineMediaPlayer = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            pineMediaPlayer = (PineMediaPlayerProxy) entry.getValue();
            if (pineMediaPlayer != null) {
                pineMediaPlayer.onDestroy();
            }
        }
        mMediaPlayerProxyMap.clear();
    }

    public synchronized static void destroyMediaPlayerByTag(String tag) {
        LogUtil.d(TAG, "destroyMediaPlayerByTag tag:" + tag);
        PineMediaPlayerProxy pineMediaPlayer = mMediaPlayerProxyMap.remove(tag);
        if (pineMediaPlayer != null) {
            pineMediaPlayer.onDestroy();
        }
    }

    public synchronized static void clearAllPlayMediaState() {
        LogUtil.d(TAG, "clearAllPlayMediaState");
        mMediaShouldPlayWhenPreparedMap.clear();
        mMediaSeekWhenPreparedMap.clear();
    }

    public synchronized static void clearPlayMediaState(String mediaCode) {
        LogUtil.d(TAG, "clearPlayMediaState mediaCode:" + mediaCode);
        mMediaShouldPlayWhenPreparedMap.remove(mediaCode);
        mMediaSeekWhenPreparedMap.remove(mediaCode);
    }

    public synchronized static boolean isShouldPlayWhenPrepared(String mediaCode) {
        return mMediaShouldPlayWhenPreparedMap.containsKey(mediaCode) ?
                mMediaShouldPlayWhenPreparedMap.get(mediaCode) : false;
    }

    public synchronized static int getSeekWhenPrepared(String mediaCode) {
        return mMediaSeekWhenPreparedMap.containsKey(mediaCode) ?
                mMediaSeekWhenPreparedMap.get(mediaCode) : 0;
    }

    public synchronized static void setShouldPlayWhenPrepared(String mediaCode, boolean isPlaying) {
        LogUtil.d(TAG, "setShouldPlayWhenPrepared mediaCode:" + mediaCode + ",isPlaying:" + isPlaying);
        mMediaShouldPlayWhenPreparedMap.put(mediaCode, isPlaying);
    }

    public synchronized static void setSeekWhenPrepared(String mediaCode, int currentPosition) {
        LogUtil.d(TAG, "setSeekWhenPrepared mediaCode:" + mediaCode + ",currentPosition:" + currentPosition);
        mMediaSeekWhenPreparedMap.put(mediaCode, currentPosition);
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        if (getMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG) == null) {
            setMediaPlayerByTag(SERVICE_MEDIA_PLAYER_TAG, new PineMediaPlayerProxy(
                    getApplicationContext(), SERVICE_MEDIA_PLAYER_TAG));
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
