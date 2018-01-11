package com.pine.player.bean;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.pine.player.applet.advert.IPineAdvertDisplay;
import com.pine.player.applet.barrage.IPineBarrageParser;
import com.pine.player.applet.subtitle.parser.IPineSubtitleParser;
import com.pine.player.decrytor.IPineMediaDecryptor;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * 播放参数
 */
public class PineMediaPlayerBean {
    public final static int MEDIA_TYPE_VIDEO = 1;
    public final static int MEDIA_TYPE_AUDIO = 2;

    @NonNull
    private String mediaCode;
    @NonNull
    private String mediaName;
    @NonNull
    private Uri mediaUri;
    private int mediaType = MEDIA_TYPE_VIDEO;
    private Uri mediaImgUri;
    private IPineSubtitleParser subtitleParser;
    private IPineBarrageParser barrageParser;
    private IPineAdvertDisplay advertParser;
    private IPineMediaDecryptor playerDecryptor;

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull Uri mediaUri) {
        this(mediaCode, mediaName, mediaUri, MEDIA_TYPE_VIDEO, null, null, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull Uri mediaUri, int mediaType) {
        this(mediaCode, mediaName, mediaUri, mediaType, null, null, null, null, null);
    }

    /**
     * @param mediaCode       media标识编码 不可为null，用于区分不同的视频
     * @param mediaName       media名称 不可为null
     * @param mediaUri        media文件Uri 不可为null
     * @param mediaType       media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri     media图片Uri 可为null
     * @param subtitleParser  media外挂字幕解析器（内置有SRT解析器PineSrtParser） 可为null
     * @param barrageParser   media弹幕解析器 可为null
     * @param advertParser    media广告解析器 可为null
     * @param playerDecryptor media解密器 可为null
     */
    public PineMediaPlayerBean(String mediaCode, String mediaName, Uri mediaUri,
                               int mediaType, Uri mediaImgUri,
                               IPineSubtitleParser subtitleParser,
                               IPineBarrageParser barrageParser,
                               IPineAdvertDisplay advertParser,
                               IPineMediaDecryptor playerDecryptor) {
        this.mediaCode = mediaCode;
        this.mediaName = mediaName;
        this.mediaUri = mediaUri;
        if (mediaType == MEDIA_TYPE_VIDEO || mediaType == MEDIA_TYPE_AUDIO) {
            this.mediaType = mediaType;
        } else {
            this.mediaType = MEDIA_TYPE_VIDEO;
        }
        this.mediaImgUri = mediaImgUri;
        this.subtitleParser = subtitleParser;
        this.barrageParser = barrageParser;
        this.advertParser = advertParser;
        this.playerDecryptor = playerDecryptor;
    }

    public String getMediaCode() {
        return mediaCode;
    }

    public void setMediaCode(String mediaCode) {
        this.mediaCode = mediaCode;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public Uri getMediaImgUri() {
        return mediaImgUri;
    }

    public void setMediaImgUri(Uri mediaImgUri) {
        this.mediaImgUri = mediaImgUri;
    }

    public IPineSubtitleParser getSubtitleParser() {
        return subtitleParser;
    }

    public void setSubtitleParser(IPineSubtitleParser subtitleParser) {
        this.subtitleParser = subtitleParser;
    }

    public IPineBarrageParser getBarrageParser() {
        return barrageParser;
    }

    public void setBarrageParser(IPineBarrageParser barrageParser) {
        this.barrageParser = barrageParser;
    }

    public IPineAdvertDisplay getAdvertParser() {
        return advertParser;
    }

    public void setAdvertParser(IPineAdvertDisplay advertParser) {
        this.advertParser = advertParser;
    }

    public IPineMediaDecryptor getPlayerDecryptor() {
        return playerDecryptor;
    }

    public void setPlayerDecryptor(IPineMediaDecryptor playerDecryptor) {
        this.playerDecryptor = playerDecryptor;
    }

    public String toString() {
        return "PineMediaPlayerBean{" +
                "mediaCode:" + mediaCode +
                ",mediaName:" + mediaName +
                ",mediaUri:" + mediaUri +
                ",mediaType:" + mediaType +
                ",mediaImgUri:" + mediaImgUri +
                ",subtitleParser:" + subtitleParser +
                ",barrageParser:" + barrageParser +
                ",advertParser:" + advertParser +
                ",playerDecryptor:" + playerDecryptor;
    }
}
