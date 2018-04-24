package com.pine.player.bean;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by tanghongfeng on 2018/3/7.
 */

public class PineMediaUriSource {
    public static final int MEDIA_DEFINITION_SD = 1;
    public static final int MEDIA_DEFINITION_HD = 2;
    public static final int MEDIA_DEFINITION_VHD = 3;
    public static final int MEDIA_DEFINITION_1080 = 4;
    private ArrayList<Uri> mediaSectionUriList;
    private ArrayList<Long> sectionDurationList;
    private long duration;
    private int mediaDefinition;

    public PineMediaUriSource(@NonNull Uri mediaUri) {
        this(mediaUri, -1, MEDIA_DEFINITION_SD);
    }

    public PineMediaUriSource(@NonNull Uri mediaUri, long duration) {
        this(mediaUri, duration, MEDIA_DEFINITION_SD);
    }

    public PineMediaUriSource(@NonNull Uri mediaUri, long duration, int mediaDefinition) {
        this.mediaSectionUriList = new ArrayList<>();
        this.mediaSectionUriList.add(mediaUri);
        this.sectionDurationList = new ArrayList<>();
        this.sectionDurationList.add(duration);
        this.mediaDefinition = mediaDefinition;
    }

    public PineMediaUriSource(@NonNull ArrayList<Uri> mediaUriSectionList) {
        this(mediaUriSectionList, null, MEDIA_DEFINITION_SD);
    }

    public PineMediaUriSource(@NonNull ArrayList<Uri> mediaUriSectionList,
                              @NonNull ArrayList<Long> sectionDurationList) {
        this(mediaUriSectionList, sectionDurationList, MEDIA_DEFINITION_SD);
    }

    public PineMediaUriSource(@NonNull ArrayList<Uri> mediaSectionUriList,
                              @NonNull ArrayList<Long> sectionDurationList, int mediaDefinition) {
        if (mediaSectionUriList == null || sectionDurationList == null) {
            this.mediaSectionUriList = new ArrayList<>();
            this.sectionDurationList = new ArrayList<>();
        } else if (mediaSectionUriList.size() != sectionDurationList.size()) {
            this.mediaSectionUriList = mediaSectionUriList;
            this.sectionDurationList = new ArrayList<>();
            for (int i = 0; i < mediaSectionUriList.size(); i++) {
                this.sectionDurationList.add(-1l);
            }
        } else {
            this.mediaSectionUriList = mediaSectionUriList;
            this.sectionDurationList = sectionDurationList;
        }
        this.mediaDefinition = mediaDefinition;
    }

    public Uri getMediaUri() {
        return getMediaUriBySectionIndex(0);
    }

    public Uri getMediaUriBySectionIndex(int sectionIndex) {
        return sectionIndex >= 0 && sectionIndex < mediaSectionUriList.size() ?
                mediaSectionUriList.get(sectionIndex) : null;
    }

    public void setMediaSectionDuration(int sectionIndex, long duration) {
        if (sectionIndex >= 0 && sectionIndex < sectionDurationList.size()) {
            sectionDurationList.set(sectionIndex, duration);
        }
    }

    public long getMediaDurationBySectionIndex(int sectionIndex) {
        return sectionIndex >= 0 && sectionIndex < sectionDurationList.size() ?
                sectionDurationList.get(sectionIndex) : -1;
    }

    public ArrayList<Uri> getMediaSectionUriList() {
        return mediaSectionUriList;
    }

    public void setMediaSectionUriList(ArrayList<Uri> mediaSectionUriList) {
        this.mediaSectionUriList = mediaSectionUriList;
    }

    public ArrayList<Long> getSectionDurationList() {
        return sectionDurationList;
    }

    public void setSectionDurationList(ArrayList<Long> sectionDurationList) {
        this.sectionDurationList = sectionDurationList;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        if (sectionDurationList.size() == 1) {
            sectionDurationList.set(0, duration);
        }
    }

    public int getMediaDefinition() {
        return mediaDefinition;
    }

    public void setMediaDefinition(int mediaDefinition) {
        this.mediaDefinition = mediaDefinition;
    }

    @Override
    public String toString() {
        String uris = "";
        String duration = "";
        if (mediaSectionUriList.size() > 0) {
            uris = mediaSectionUriList.get(0).toString();
            duration = sectionDurationList.get(0).toString();
            for (int i = 1; i < mediaSectionUriList.size(); i++) {
                uris = uris + ", " + mediaSectionUriList.get(i).toString();
                duration = duration + ", " + sectionDurationList.get(i).toString();
            }
        }
        return "PineMediaUriSource:{" +
                "duration:" + duration +
                "mediaDefinition:" + mediaDefinition +
                ",mediaSectionUriList:[" + uris +
                "],sectionDurationList:[" + duration +
                "]}";
    }
}
