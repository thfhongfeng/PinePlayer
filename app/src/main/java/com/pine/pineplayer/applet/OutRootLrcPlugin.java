package com.pine.pineplayer.applet;

import android.content.Context;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.List;

public class OutRootLrcPlugin<T extends List> extends PineLrcParserPlugin<T> {
    private ISubtitleUpdateListener mSubtitleUpdateListener;

    public OutRootLrcPlugin(Context context, String subtitleFilePath, String charset) {
        super(context, subtitleFilePath, charset);
    }

    public OutRootLrcPlugin(Context context, String subtitleFilePath, int pathType, String charset) {
        super(context, subtitleFilePath, pathType, charset);
    }

    public void setSubtitleUpdateListener(ISubtitleUpdateListener subtitleUpdateListener) {
        mSubtitleUpdateListener = subtitleUpdateListener;
    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_OUT_ROOT;
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        return null;
    }

    @Override
    public void updateSubtitleText(PineSubtitleBean subtitle, int position) {
        if (mPlayer != null && mSubtitleUpdateListener != null) {
            mSubtitleUpdateListener.updateSubtitleText(mPlayer.getMediaPlayerBean(), subtitle);
        }
    }

    @Override
    public void clearSubtitleText() {
        if (mSubtitleUpdateListener != null) {
            mSubtitleUpdateListener.clearSubtitleText();
        }
    }

    public interface ISubtitleUpdateListener {
        void updateSubtitleText(PineMediaPlayerBean mediaBean, PineSubtitleBean subtitle);

        void clearSubtitleText();
    }
}
