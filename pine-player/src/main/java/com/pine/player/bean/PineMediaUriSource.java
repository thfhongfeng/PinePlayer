package com.pine.player.bean;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by tanghongfeng on 2018/3/7.
 */

public class PineMediaUriSource {
    public static final int MEDIA_DEFINITION_SD = 1;
    public static final int MEDIA_DEFINITION_HD = 2;
    public static final int MEDIA_DEFINITION_VHD = 3;
    public static final int MEDIA_DEFINITION_1080 = 4;
    private Uri mediaUri;
    private int mediaDefinition;

    public PineMediaUriSource(@NonNull Uri mediaUri) {
        this(mediaUri, MEDIA_DEFINITION_SD);
    }

    public PineMediaUriSource(@NonNull Uri mediaUri, int mediaDefinition) {
        this.mediaUri = mediaUri;
        this.mediaDefinition = mediaDefinition;
    }

    public Uri getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(Uri mediaUri) {
        this.mediaUri = mediaUri;
    }

    public int getMediaDefinition() {
        return mediaDefinition;
    }

    public void setMediaDefinition(int mediaDefinition) {
        this.mediaDefinition = mediaDefinition;
    }

    @Override
    public String toString() {
        return "PineMediaUriSource:{" +
                "mediaUri:" + mediaUri +
                ",mediaDefinition:" + mediaDefinition;
    }
}
