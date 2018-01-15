package com.pine.player.applet.subtitle.plugin;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pine.player.R;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2017/9/28.
 */

public class PineLrcParserPlugin extends PineSubtitlePlugin {

    /**
     * [ar:艺人名] [ti:曲名] [al:专辑名] [by:编者（指编辑LRC歌词的人）] [offset:时间补偿值]
     * 其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
     */
    // parse target artist
    private final String TagAr = "[ar:";

    // parse target title
    private final String TagTi = "[ti:";

    // parse target album
    private final String TagAl = "[al:";

    // parse target author of the lrc
    private final String TagBy = "[by:";

    // parse target offset
    private final String TagOff = "[offset:";

    // parse target offset
    private final String TagXTrans = "[x-trans]";

    private PineSubtitleBean mLastTextBean;

    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;

    public PineLrcParserPlugin(Context context, String subtitleFilePath, String charset) {
        super(context, subtitleFilePath, charset);
    }

    public PineLrcParserPlugin(Context context, String subtitleFilePath, int pathType, String charset) {
        super(context, subtitleFilePath, pathType, charset);
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.media_subtitle_full, null);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.media_subtitle, null);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        return mCurViewHolder;
    }

    @Override
    public List<PineSubtitleBean> parseSubtitleBufferedReader(BufferedReader bufferedReader) {
        List<PineSubtitleBean> retList = new ArrayList<PineSubtitleBean>();
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.indexOf(TagXTrans) != -1 && retList.size() > 0) {
                    PineSubtitleBean item = retList.get(retList.size() - 1);
                    if (item.getType() == PineSubtitleBean.TEXT_ZONE) {
                        String[] cut = line.split("]");
                        if (cut.length == 2) {
                            item.setTransBody(cut[1]);
                        }
                    }
                } else {
                    PineSubtitleBean item = analyzeLine(line);
                    if (item != null) {
                        if (item.getType() == PineSubtitleBean.TEXT_ZONE) {
                            if (mLastTextBean != null) {
                                mLastTextBean.setEndTime(item.getBeginTime() - 100);
                            }
                            mLastTextBean = item;
                        }
                        retList.add(item);
                    }
                }
            }
            if (mLastTextBean != null) {
                mLastTextBean.setEndTime(mLastTextBean.getBeginTime() + 5 * 1000);
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

    private PineSubtitleBean analyzeLine(String line) {
        PineSubtitleBean item = null;
        if (line.indexOf(TagAr) != -1) {
            item = new PineSubtitleBean();
            item.setType(PineSubtitleBean.ARTIST_ZONE);
            item.setTextBody(line.substring(
                    line.indexOf(':') + 1, line.lastIndexOf(']')));
        } else if (line.indexOf(TagAl) != -1) {
            item = new PineSubtitleBean();
            item.setType(PineSubtitleBean.ALBUM_ZONE);
            item.setTextBody(line.substring(
                    line.indexOf(':') + 1, line.lastIndexOf(']')));
        } else if (line.indexOf(TagTi) != -1) {
            item = new PineSubtitleBean();
            item.setType(PineSubtitleBean.TITLE_ZONE);
            item.setTextBody(line.substring(
                    line.indexOf(':') + 1, line.lastIndexOf(']')));
        } else if (line.indexOf(TagBy) != -1) {
            item = new PineSubtitleBean();
            item.setType(PineSubtitleBean.AUTHOR_ZONE);
            item.setTextBody(line.substring(
                    line.indexOf(':') + 1, line.lastIndexOf(']')));
        } else if (line.indexOf(TagOff) != -1) {
            item = new PineSubtitleBean();
            item.setType(PineSubtitleBean.OFFSET_ZONE);
            item.setTextBody(line.substring(
                    line.indexOf(':') + 1, line.lastIndexOf(']')));
        } else if (line.indexOf(TagXTrans) != -1) {
            String[] cut = line.split("]");
            if (cut.length >= 2) {
                for (int i = 0; i < cut.length - 1; i++) {
                    item = new PineSubtitleBean();
                    item.setType(PineSubtitleBean.TRANS_ZONE);
                    item.setBeginTime(analyzeTimeStringToValue(cut[i]
                            .substring(line.indexOf('[') + 1)));
                    item.setTextBody(cut[cut.length - 1]);
                }
            }
        } else {
            String[] cut = line.split("]");
            if (cut.length >= 2) {
                for (int i = 0; i < cut.length - 1; i++) {
                    item = new PineSubtitleBean();
                    item.setType(PineSubtitleBean.TEXT_ZONE);
                    item.setBeginTime(analyzeTimeStringToValue(cut[i]
                            .substring(line.indexOf('[') + 1)));
                    item.setTextBody(cut[cut.length - 1]);
                }
            }
        }
        return item;
    }

    private long analyzeTimeStringToValue(String time) {
        long minute = Integer
                .parseInt(time.substring(0, time.lastIndexOf(":")));
        long second = Integer.parseInt(time.substring(time.indexOf(":") + 1,
                time.lastIndexOf(".")));
        long millisecond = Integer
                .parseInt(time.substring(time.indexOf(".") + 1));
        return (long) (minute * 60 * 1000 + second * 1000 + millisecond);
    }
}
