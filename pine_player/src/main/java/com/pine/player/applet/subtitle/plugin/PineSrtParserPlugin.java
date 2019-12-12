package com.pine.player.applet.subtitle.plugin;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pine.player.R;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * SRT外挂字幕解析器
 */
public class PineSrtParserPlugin<T extends List> extends PineSubtitlePlugin<T> {

    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;

    public PineSrtParserPlugin(Context context, String subtitleFilePath, String charset) {
        super(context, subtitleFilePath, charset);
    }

    public PineSrtParserPlugin(Context context, String subtitleFilePath, int pathType, String charset) {
        super(context, subtitleFilePath, pathType, charset);
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.pine_player_media_subtitle_full, null);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.pine_player_media_subtitle, null);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        return mCurViewHolder;
    }

    @Override
    public void setData(T data) {

    }

    @Override
    public void addData(T data) {

    }

    @Override
    public List<PineSubtitleBean> parseSubtitleBufferedReader(BufferedReader bufferedReader) {
        List<PineSubtitleBean> retList = new ArrayList<PineSubtitleBean>();
        String line = null;
        String[] subtitleBean = new String[3];
        subtitleBean[0] = subtitleBean[1] = subtitleBean[2] = "";
        int indexCount = 0;
        PineSubtitleBean item;
        try {
            while ((line = bufferedReader.readLine()) != null) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return retList;
    }

    /**
     * 更新字幕
     */
    @Override
    public void updateSubtitleText(PineSubtitleBean subtitle) {
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

    @NonNull
    @Override
    public PinePluginViewHolder getViewHolder() {
        return mCurViewHolder;
    }
}
