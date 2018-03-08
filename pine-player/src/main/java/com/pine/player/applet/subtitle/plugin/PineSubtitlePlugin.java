package com.pine.player.applet.subtitle.plugin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public abstract class PineSubtitlePlugin<T extends List> implements IPinePlayerPlugin<T> {

    private Context mContext;
    private String mSubtitleFilePath;
    private int mSubtitleFileType;
    private String mCharset;

    // 字幕列表，按时间升序排列
    private List<PineSubtitleBean> mSubtitleBeanList;
    private int mPreSubtitleBeanIndex = 0;

    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private boolean mIsOpen = true;

    public PineSubtitlePlugin(Context context, String subtitleFilePath, String charset) {
        mContext = context;
        mSubtitleFilePath = subtitleFilePath;
        mSubtitleFileType = PineConstants.PATH_STORAGE;
        mCharset = charset;
    }

    public PineSubtitlePlugin(Context context, String subtitleFilePath, int pathType, String charset) {
        mContext = context;
        mSubtitleFilePath = subtitleFilePath;
        mSubtitleFileType = pathType;
        mCharset = charset;
    }

    public void setSubtitle(String subtitleFilePath, int pathType, String charset) {
        resetState();
        mSubtitleFilePath = subtitleFilePath;
        mSubtitleFileType = pathType;
        mCharset = charset;
        prepareSubtitle();
    }

    private void prepareSubtitle() {
        if (mSubtitleFilePath == null && mSubtitleFilePath == "") {
            return;
        }
        InputStream inputStream = null;
        try {
            switch (mSubtitleFileType) {
                case PineConstants.PATH_ASSETS:
                    inputStream = mContext.getAssets().open(mSubtitleFilePath);
                    break;
                case PineConstants.PATH_STORAGE:
                    inputStream = new FileInputStream(mSubtitleFilePath);
                    break;
            }
            if (inputStream == null) {
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, mCharset));
            mSubtitleBeanList = parseSubtitleBufferedReader(br);
        } catch (FileNotFoundException e) {
            mSubtitleBeanList = null;
            e.printStackTrace();
        } catch (IOException e) {
            mSubtitleBeanList = null;
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller,
                       boolean isPlayerReset, boolean isResumeState) {
        mContext = context;
        mPlayer = player;
        prepareSubtitle();
    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_MATCH_SURFACE;
    }

    @Override
    public void onMediaPlayerPrepared() {

    }

    @Override
    public void onMediaPlayerStart() {

    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {

    }

    @Override
    public void onMediaPlayerComplete() {

    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {

    }

    @Override
    public void onTime(long position) {
        if (!mIsOpen || mSubtitleBeanList == null || mSubtitleBeanList.size() < 1) {
            return;
        }
        boolean isFound = false;
        PineSubtitleBean preBean = mSubtitleBeanList.get(mPreSubtitleBeanIndex);
        PineSubtitleBean tmpBean = null;
        if (position > preBean.getEndTime()) {
            for (int i = mPreSubtitleBeanIndex; i < mSubtitleBeanList.size(); i++) {
                tmpBean = mSubtitleBeanList.get(i);
                if (position >= tmpBean.getBeginTime() && position < tmpBean.getEndTime()) {
                    mPreSubtitleBeanIndex = i;
                    isFound = true;
                    break;
                }
            }
        } else if (position < preBean.getBeginTime()) {
            for (int i = mPreSubtitleBeanIndex; i >= 0; i--) {
                tmpBean = mSubtitleBeanList.get(i);
                if (position > tmpBean.getBeginTime() && position < tmpBean.getEndTime()) {
                    mPreSubtitleBeanIndex = i;
                    isFound = true;
                    break;
                }
            }
        } else {
            isFound = true;
        }
        updateSubtitleText(isFound ? mSubtitleBeanList.get(mPreSubtitleBeanIndex) : null);
    }

    @Override
    public void onRelease() {
        resetState();
        mContext = null;
        mPlayer = null;
    }

    private void resetState() {
        updateSubtitleText(null);
    }

    public void openPlugin() {
        mIsOpen = true;
        getViewHolder().getContainer().setVisibility(View.VISIBLE);
    }

    @Override
    public void closePlugin() {
        mIsOpen = false;
        getViewHolder().getContainer().setVisibility(View.GONE);
    }

    @Override
    public boolean isOpen() {
        return mIsOpen;
    }


    public abstract List<PineSubtitleBean> parseSubtitleBufferedReader(BufferedReader bufferedReader);

    public abstract void updateSubtitleText(PineSubtitleBean subtitle);

    public abstract
    @NonNull
    PinePluginViewHolder getViewHolder();
}
