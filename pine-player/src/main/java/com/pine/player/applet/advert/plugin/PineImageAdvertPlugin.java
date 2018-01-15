package com.pine.player.applet.advert.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pine.player.R;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public class PineImageAdvertPlugin extends PineAdvertPlugin {

    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;

    public PineImageAdvertPlugin(Context context, List<PineAdvertBean> advertBeanList) {
        super(context, advertBeanList);
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.media_image_advert_full, null);
                view.setVisibility(View.GONE);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.media_image_advert, null);
                view.setVisibility(View.GONE);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        return mCurViewHolder;
    }

    @Override
    public void playAdvert(Context context, PineMediaWidget.IPineMediaPlayer player,
                           PineAdvertBean advertBean, int advertType) {
        if (mCurViewHolder == null || mCurViewHolder.getContainer() == null) {
            return;
        }
        mCurViewHolder.getContainer().setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(advertBean.getUri().getPath());
        ((ImageView) mCurViewHolder.getContainer().findViewById(R.id.advert_iv))
                .setImageBitmap(bitmap);
    }

    @Override
    public void advertComplete() {
        if (mCurViewHolder == null || mCurViewHolder.getContainer() == null) {
            return;
        }
        mCurViewHolder.getContainer().setVisibility(View.GONE);
    }
}
