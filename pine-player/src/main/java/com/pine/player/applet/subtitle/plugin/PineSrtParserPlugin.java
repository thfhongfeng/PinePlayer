package com.pine.player.applet.subtitle.plugin;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pine.player.R;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.PineConstants;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
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
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * SRT外挂字幕解析器
 */
public class PineSrtParserPlugin implements IPinePlayerPlugin {

    private Context mContext;
    private String mSrtPath;
    private int mSrtPathType;
    private String mCharset;

    private List<PineSubtitleBean> mSubtitleBeanList;
    private PineSubtitleBean mPrePineSubtitleBean;
    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;

    public PineSrtParserPlugin(Context context, String srtPath, String charset) {
        mContext = context;
        mSrtPath = srtPath;
        mSrtPathType = PineConstants.PATH_STORAGE;
        mCharset = charset;
    }

    public PineSrtParserPlugin(Context context, String srtPath, int pathType, String charset) {
        mContext = context;
        mSrtPath = srtPath;
        mSrtPathType = pathType;
        mCharset = charset;
    }

    @Override
    public void onInit() {
        if (mSrtPath == null && mSrtPath == "") {
            return;
        }
        InputStream inputStream = null;
        List<PineSubtitleBean> retList = null;
        try {
            switch (mSrtPathType) {
                case PineConstants.PATH_ASSETS:
                    inputStream = mContext.getAssets().open(mSrtPath);
                    break;
                case PineConstants.PATH_STORAGE:
                    inputStream = new FileInputStream(mSrtPath);
                    break;
            }
            if (inputStream == null) {
                return;
            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,
//                    FileUtils.getTextFileEncode(mContext, mSrtPath, mSrtPathType)));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, mCharset));
            String line = null;
            String[] subtitleBean = new String[3];
            subtitleBean[0] = subtitleBean[1] = subtitleBean[2] = "";
            int indexCount = 0;
            retList = new ArrayList<PineSubtitleBean>();
            PineSubtitleBean item;
            while ((line = br.readLine()) != null) {
                if ("".equals(line)) {
                    if (indexCount > 1) {
                        item = new PineSubtitleBean();
                        item.setOrder(Integer.parseInt(subtitleBean[0]));
                        String timeToTime = subtitleBean[1];
                        int beginHour = Integer.parseInt(timeToTime.substring(0, 2));
                        int beginMinute = Integer.parseInt(timeToTime.substring(3, 5));
                        int beginSecond = Integer.parseInt(timeToTime.substring(6, 8));
                        int beginMilli = Integer.parseInt(timeToTime.substring(9, 12));
                        int beginTime = (beginHour * 3600 + beginMinute * 60 + beginSecond)
                                * 1000 + beginMilli;
                        item.setBeginTime(beginTime);
                        int endHour = Integer.parseInt(timeToTime.substring(17, 19));
                        int endMinute = Integer.parseInt(timeToTime.substring(20, 22));
                        int endSecond = Integer.parseInt(timeToTime.substring(23, 25));
                        int endMilli = Integer.parseInt(timeToTime.substring(26, 29));
                        int endTime = (endHour * 3600 + endMinute * 60 + endSecond)
                                * 1000 + endMilli;
                        item.setEndTime(endTime);
                        item.setTextBody(subtitleBean[2].substring(0, subtitleBean[2].length() - 1));
                        retList.add(item);
                    }
                    subtitleBean[0] = "";
                    subtitleBean[1] = "";
                    subtitleBean[2] = "";
                    indexCount = 0;
                } else {
                    if (indexCount < 2) {
                        subtitleBean[indexCount++] = line;
                    } else {
                        subtitleBean[2] += line;
                        subtitleBean[2] += "\n";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mSubtitleBeanList = retList;
    }

    @Override
    public PinePluginViewHolder createViewHolder(boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(mContext,
                        R.layout.media_subtitle_full, null);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(mContext,
                        R.layout.media_subtitle, null);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        return mCurViewHolder;
    }

    @Override
    public void onRefresh(int position) {
        if (mSubtitleBeanList == null) {
            return;
        }
        if (mPrePineSubtitleBean != null && position > mPrePineSubtitleBean.getBeginTime()
                && position < mPrePineSubtitleBean.getEndTime()) {
            updateSubtitleText(mPrePineSubtitleBean);
        } else {
            for (Iterator<PineSubtitleBean> iterator = mSubtitleBeanList.iterator(); iterator.hasNext(); ) {
                mPrePineSubtitleBean = iterator.next();
                if (position > mPrePineSubtitleBean.getBeginTime()
                        && position < mPrePineSubtitleBean.getEndTime()) {
                    updateSubtitleText(mPrePineSubtitleBean);
                }
            }
        }
    }

    @Override
    public void onRelease() {
        updateSubtitleText(null);
    }

    /**
     * 更新字幕
     */
    private void updateSubtitleText(PineSubtitleBean subtitle) {
        if (mCurViewHolder == null || mCurViewHolder.getContainer() == null) {
            return;
        }
        String text = "";
        if (subtitle != null) {
            text = subtitle.getTextBody();
            if (subtitle.getTransBody() != null && !subtitle.getTransBody().isEmpty()) {
                text += "<br />" + subtitle.getTransBody();
            }
        }
        ((TextView) mCurViewHolder.getContainer().findViewById(R.id.subtitle_text)).setText(Html.fromHtml(text));
    }
}
