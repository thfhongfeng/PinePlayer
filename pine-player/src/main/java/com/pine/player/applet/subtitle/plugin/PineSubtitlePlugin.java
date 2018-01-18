package com.pine.player.applet.subtitle.plugin;

import android.content.Context;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public abstract class PineSubtitlePlugin implements IPinePlayerPlugin {

    private Context mContext;
    private String mSubtitleFilePath;
    private int mSubtitleFileType;
    private String mCharset;

    private List<PineSubtitleBean> mSubtitleBeanList;
    private List<PineSubtitleBean> mSubtitleBeanIteratorList;
    private PineSubtitleBean mPreSubtitleBean;
    private int mPreSubtitleBeanIndex = -1;

    private PineMediaWidget.IPineMediaPlayer mPlayer;

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

    @Override
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller,
                       boolean isPlayerReset, boolean isResumeState) {
        if (mSubtitleFilePath == null && mSubtitleFilePath == "") {
            return;
        }
        mContext = context;
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
            mSubtitleBeanIteratorList = new ArrayList<>(mSubtitleBeanList);
            mPlayer = player;
        } catch (FileNotFoundException e) {
            mSubtitleBeanList = null;
            mSubtitleBeanIteratorList = null;
            e.printStackTrace();
        } catch (IOException e) {
            mSubtitleBeanList = null;
            mSubtitleBeanIteratorList = null;
            e.printStackTrace();
        }
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
        if (mSubtitleBeanList == null) {
            return;
        }
        boolean isFound = false;
        if (mPreSubtitleBean == null || position > mPreSubtitleBean.getEndTime()) {
            PineSubtitleBean tmpBean = null;
            for (Iterator<PineSubtitleBean> iterator = mSubtitleBeanIteratorList.iterator(); iterator.hasNext(); ) {
                tmpBean = iterator.next();
                if (position > tmpBean.getBeginTime()
                        && position < tmpBean.getEndTime()) {
                    iterator.remove();
                    mPreSubtitleBeanIndex = mSubtitleBeanList.indexOf(tmpBean);
                    mPreSubtitleBean = tmpBean;
                    isFound = true;
                    break;
                }
            }
        } else if (position < mPreSubtitleBean.getBeginTime()) {
            PineSubtitleBean tmpBean = null;
            for (int i = mPreSubtitleBeanIndex; i >= 0; i--) {
                tmpBean = mSubtitleBeanList.get(i);
                mSubtitleBeanIteratorList.add(0, tmpBean);
                if (position > tmpBean.getBeginTime() && position < tmpBean.getEndTime()) {
                    mPreSubtitleBean = mSubtitleBeanList.get(i);
                    mPreSubtitleBeanIndex = i;
                    isFound = true;
                    break;
                }
            }
        } else {
            isFound = true;
        }
        updateSubtitleText(isFound ? mPreSubtitleBean : null);
    }

    @Override
    public void onRelease() {
        updateSubtitleText(null);
        mContext = null;
    }

    public abstract List<PineSubtitleBean> parseSubtitleBufferedReader(BufferedReader bufferedReader);

    public abstract void updateSubtitleText(PineSubtitleBean subtitle);
}
