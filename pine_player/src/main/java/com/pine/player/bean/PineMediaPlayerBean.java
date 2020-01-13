package com.pine.player.bean;

import android.content.ContentResolver;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.decrytor.IPineMediaDecryptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * 播放参数
 */
public class PineMediaPlayerBean {
    public final static int MEDIA_TYPE_VIDEO = 1;
    public final static int MEDIA_TYPE_AUDIO = 2;

    /**
     * media唯一标识符
     */
    @NonNull
    private String mediaCode;
    @NonNull
    private String mediaName;
    private String mediaDesc;
    
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
     * @param mediaCode       media标识编码 不可为null，用于区分不同的视频
     * @param mediaName       media名称 不可为null
     * @param mediaUriSource  media文件UriSource 不可为null
     * @param mediaType       media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri     media图片Uri 可为null
     * @param playerPluginMap media插件map集合  可为null
     * @param playerDecryptor media解密器 可为null
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
     * @param mediaCode       media标识编码 不可为null，用于区分不同的视频
     * @param mediaName       media名称 不可为null
     * @param mediaImgUri     media文件Uri 不可为null
     * @param mediaType       media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri     media图片Uri 可为null
     * @param playerPluginMap media插件map集合 可为null
     * @param playerDecryptor media解密器 可为null
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
     * @param mediaCode          media标识编码 不可为null，用于区分不同的视频
     * @param mediaName          media名称 不可为null
     * @param mediaUriSourceList media各个清晰度对应的文件mediaUriSource列表 不可为null
     * @param mediaType          media类型 默认为MEDIA_TYPE_VIDEO
     * @param mediaImgUri        media图片Uri 可为null
     * @param playerPluginMap    media插件map集合 可为null
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

    public String getMediaDesc() {
        return mediaDesc;
    }

    public void setMediaDesc(String mediaDesc) {
        this.mediaDesc = mediaDesc;
    }

    /**
     * 获取当前清晰度的uri（用于一个清晰度的media只有一个uri的情况）
     *
     * @return
     */
    public Uri getMediaUri() {
        PineMediaUriSource pineMediaUriSource = getMediaUriSource();
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaUri();
        } else {
            return null;
        }
    }

    /**
     * 获取指定清晰度的uri（用于一个清晰度的media只有一个uri的情况）
     *
     * @return
     */
    public Uri getMediaUriByDefinition(int definition) {
        PineMediaUriSource pineMediaUriSource =
                getMediaUriSourceByDefinition(definition);
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaUri();
        } else {
            return null;
        }
    }

    /**
     * 获取当前清晰度的uri分段列表（用于一个清晰度的media有多个分段的url的情况）
     *
     * @return
     */
    public ArrayList<Uri> getMediaSectionUris() {
        PineMediaUriSource pineMediaUriSource = getMediaUriSource();
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaSectionUriList();
        } else {
            return null;
        }
    }

    /**
     * 获取指定清晰度的uri分段列表（用于一个清晰度的media有多个分段的url的情况）
     *
     * @return
     */
    public ArrayList<Uri> getMediaSectionUrisByDefinition(int definition) {
        PineMediaUriSource pineMediaUriSource = getMediaUriSourceByDefinition(definition);
        if (pineMediaUriSource != null) {
            return pineMediaUriSource.getMediaSectionUriList();
        } else {
            return null;
        }
    }

    /**
     * 获取当前清晰度的PineMediaUriSource
     *
     * @return
     */
    public PineMediaUriSource getMediaUriSource() {
        return getMediaUriSourceByDefinition(getCurrentDefinition());
    }

    /**
     * 获取指定清晰度的PineMediaUriSource
     *
     * @return
     */
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

    public boolean isLocalMediaBean() {
        String scheme = getMediaUriByDefinition(currentDefinition).getScheme();
        return TextUtils.isEmpty(scheme) || scheme.equals(ContentResolver.SCHEME_FILE);
    }

    public boolean isLocalStreamBean() {
        return isLocalMediaBean() && playerDecryptor != null;
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
                ",currentDefinition:" + currentDefinition + "}";
    }
}
