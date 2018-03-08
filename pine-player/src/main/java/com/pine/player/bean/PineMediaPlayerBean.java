package com.pine.player.bean;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.decrytor.IPineMediaDecryptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private ArrayList<PineMediaUriSource> mediaUriSourceList;
    private int mediaType = MEDIA_TYPE_VIDEO;
    private Uri mediaImgUri;
    private HashMap<Integer, IPinePlayerPlugin> playerPluginMap;
    private IPineMediaDecryptor playerDecryptor;
    private int currentDefinition = PineMediaUriSource.MEDIA_DEFINITION_SD;

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull Uri mediaUri) {
        this(mediaCode, mediaName, mediaUri, MEDIA_TYPE_VIDEO, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull Uri mediaUri, int mediaType) {
        this(mediaCode, mediaName, mediaUri, mediaType, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull PineMediaUriSource mediaUriSource) {
        this(mediaCode, mediaName, mediaUriSource, MEDIA_TYPE_VIDEO, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull PineMediaUriSource mediaUriSource, int mediaType) {
        this(mediaCode, mediaName, mediaUriSource, mediaType, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull ArrayList<PineMediaUriSource> mediaUriSourceList) {
        this(mediaCode, mediaName, mediaUriSourceList, MEDIA_TYPE_VIDEO, null, null, null);
    }

    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull ArrayList<PineMediaUriSource> mediaUriSourceList, int mediaType) {
        this(mediaCode, mediaName, mediaUriSourceList, mediaType, null, null, null);
    }

    /**
     * @param mediaCode        media标识编码 不可为null，用于区分不同的视频
     * @param mediaName        media名称 不可为null
     * @param mediaImgUri     media文件Uri 不可为null
     * @param mediaType        media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri      media图片Uri 可为null
     * @param playerPluginMap media插件map集合 可为null
     * @param playerDecryptor  media解密器 可为null
     */
    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull Uri mediaUri,
                               int mediaType, Uri mediaImgUri,
                               HashMap<Integer, IPinePlayerPlugin> playerPluginMap,
                               IPineMediaDecryptor playerDecryptor) {
        this.mediaCode = mediaCode;
        this.mediaName = mediaName;
        this.mediaUriSourceList = new ArrayList<PineMediaUriSource>();
        this.mediaUriSourceList.add(new PineMediaUriSource(mediaUri));
        if (mediaType == MEDIA_TYPE_VIDEO || mediaType == MEDIA_TYPE_AUDIO) {
            this.mediaType = mediaType;
        } else {
            this.mediaType = MEDIA_TYPE_VIDEO;
        }
        this.mediaImgUri = mediaImgUri;
        this.playerPluginMap = playerPluginMap;
        this.playerDecryptor = playerDecryptor;
    }

    /**
     * @param mediaCode        media标识编码 不可为null，用于区分不同的视频
     * @param mediaName        media名称 不可为null
     * @param mediaUriSource   media文件UriSource 不可为null
     * @param mediaType        media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri      media图片Uri 可为null
     * @param playerPluginMap media插件map集合  可为null
     * @param playerDecryptor  media解密器 可为null
     */
    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull PineMediaUriSource mediaUriSource,
                               int mediaType, Uri mediaImgUri,
                               HashMap<Integer, IPinePlayerPlugin> playerPluginMap,
                               IPineMediaDecryptor playerDecryptor) {
        this.mediaCode = mediaCode;
        this.mediaName = mediaName;
        this.mediaUriSourceList = new ArrayList<PineMediaUriSource>();
        this.mediaUriSourceList.add(mediaUriSource);
        if (mediaType == MEDIA_TYPE_VIDEO || mediaType == MEDIA_TYPE_AUDIO) {
            this.mediaType = mediaType;
        } else {
            this.mediaType = MEDIA_TYPE_VIDEO;
        }
        this.mediaImgUri = mediaImgUri;
        this.playerPluginMap = playerPluginMap;
        this.playerDecryptor = playerDecryptor;
    }

    /**
     * @param mediaCode          media标识编码 不可为null，用于区分不同的视频
     * @param mediaName          media名称 不可为null
     * @param mediaUriSourceList media各个清晰度对应的文件mediaUriSource列表 不可为null
     * @param mediaType          media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri        media图片Uri 可为null
     * @param playerPluginMap  media插件map集合 可为null
     * @param playerDecryptor    media解密器 可为null
     */
    public PineMediaPlayerBean(@NonNull String mediaCode, @NonNull String mediaName,
                               @NonNull ArrayList<PineMediaUriSource> mediaUriSourceList,
                               int mediaType, Uri mediaImgUri,
                               HashMap<Integer, IPinePlayerPlugin> playerPluginMap,
                               IPineMediaDecryptor playerDecryptor) {
        this.mediaCode = mediaCode;
        this.mediaName = mediaName;
        this.mediaUriSourceList = mediaUriSourceList;
        if (mediaType == MEDIA_TYPE_VIDEO || mediaType == MEDIA_TYPE_AUDIO) {
            this.mediaType = mediaType;
        } else {
            this.mediaType = MEDIA_TYPE_VIDEO;
        }
        this.mediaImgUri = mediaImgUri;
        this.playerPluginMap = playerPluginMap;
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

    public Uri getMediaUriByDefinition(int definition) {
        PineMediaUriSource pineMediaUriSource =
                getMediaUriSourceByDefinition(definition);
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaUri();
        } else {
            return null;
        }
    }

    public PineMediaUriSource getMediaUriSourceByDefinition(int definition) {
        PineMediaUriSource pineMediaUriSource = null;
        for (int i = 0; i < mediaUriSourceList.size(); i++) {
            pineMediaUriSource = mediaUriSourceList.get(i);
            if (pineMediaUriSource.getMediaDefinition() == definition) {
                return pineMediaUriSource;
            }
        }
        return null;
    }

    public Uri getMediaUriByPosition(int index) {
        PineMediaUriSource pineMediaUriSource =
                getMediaUriSourceByPosition(index);
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaUri();
        } else {
            return null;
        }
    }

    public PineMediaUriSource getMediaUriSourceByPosition(int index) {
        if (index >= mediaUriSourceList.size()) {
            return null;
        } else {
            return mediaUriSourceList.get(index);
        }
    }

    public ArrayList<PineMediaUriSource> getMediaUriSourceList() {
        return mediaUriSourceList;
    }

    public void setMediaUriSourceList(ArrayList<PineMediaUriSource> mediaUriSourceList) {
        this.mediaUriSourceList = mediaUriSourceList;
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

    public HashMap<Integer, IPinePlayerPlugin> getPlayerPluginMap() {
        return playerPluginMap;
    }

    public void setPlayerPluginMap(HashMap<Integer, IPinePlayerPlugin> playerPluginMap) {
        this.playerPluginMap = playerPluginMap;
    }

    public IPineMediaDecryptor getPlayerDecryptor() {
        return this.playerDecryptor;
    }

    public void setPlayerDecryptor(IPineMediaDecryptor playerDecryptor) {
        this.playerDecryptor = playerDecryptor;
    }

    public void setCurrentDefinitionByPosition(int position) {
        if (position < mediaUriSourceList.size() && position >= 0) {
            currentDefinition = mediaUriSourceList.get(position).getMediaDefinition();
        }
    }

    public int getCurrentDefinitionPosition() {
        PineMediaUriSource pineMediaUriSource = null;
        for (int i = 0; i < mediaUriSourceList.size(); i++) {
            pineMediaUriSource = mediaUriSourceList.get(i);
            if (pineMediaUriSource.getMediaDefinition() == currentDefinition) {
                return i;
            }
        }
        return -1;
    }

    public int getCurrentDefinition() {
        return currentDefinition;
    }

    public void setCurrentDefinition(int currentDefinition) {
        this.currentDefinition = currentDefinition;
    }

    @Override
    public String toString() {
        String mediaUriListStr = null;
        if (mediaUriSourceList != null && mediaUriSourceList.size() > 0) {
            mediaUriListStr = "[" + mediaUriSourceList.get(0);
            for (int i = 1; i < mediaUriSourceList.size(); i++) {
                mediaUriListStr += "," + mediaUriSourceList.get(i);
            }
            mediaUriListStr += "]";
        }
        return "PineMediaPlayerBean:{" +
                "mediaCode:" + mediaCode +
                ",mediaName:" + mediaName +
                ",mediaUriSourceList:" + mediaUriListStr +
                ",mediaType:" + mediaType +
                ",mediaImgUri:" + mediaImgUri +
                ",playerPluginMap:" + playerPluginMap +
                ",playerDecryptor:" + playerDecryptor +
                ",currentDefinition:" + currentDefinition;
    }
}
