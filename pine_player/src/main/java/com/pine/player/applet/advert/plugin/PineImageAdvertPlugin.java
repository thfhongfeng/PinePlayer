package com.pine.player.applet.advert.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pine.player.R;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public class PineImageAdvertPlugin<T extends List> extends PineAdvertPlugin<T> {
    private static final String TAG = LogUtils.makeLogTag(PineImageAdvertPlugin.class);

    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;
    private Handler mHandler = new Handler();
    private long mAdvertTime;
    private TextView mTimerTaskTv;

    public PineImageAdvertPlugin(Context context, T advertBeanList) {
        super(context, advertBeanList);
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.pine_player_media_image_advert_full, null);
                view.setVisibility(View.GONE);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.pine_player_media_image_advert, null);
                view.setVisibility(View.GONE);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        mTimerTaskTv = (TextView) mCurViewHolder.getContainer().findViewById(R.id.timer_tick_tv);
        mTimerTaskTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipAdvert();
            }
        });
        return mCurViewHolder;
    }

    @Override
    public void setData(T data) {

    }

    @Override
    public void addData(T data) {

    }

    @Override
    public void playAdvert(Context context, PineMediaWidget.IPineMediaPlayer player,
                           PineAdvertBean advertBean, int advertType) {
        if (mCurViewHolder == null || mCurViewHolder.getContainer() == null) {
            return;
        }
        if (advertType != PineAdvertBean.TYPE_PAUSE) {
            mAdvertTime = advertBean.getDurationTime();
            mTimerTaskTv.setText(String.valueOf("跳过 " + mAdvertTime / 1000));
            mTimerTaskTv.setVisibility(View.VISIBLE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdvertTime -= 1000;
                    mTimerTaskTv.setText(String.valueOf("跳过 " + mAdvertTime / 1000));
                    if (mAdvertTime > 0) {
                        mHandler.postDelayed(this, 1000);
                    } else {
                        mTimerTaskTv.setVisibility(View.GONE);
                    }
                }
            }, 1000);
        }
        mCurViewHolder.getContainer().setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(advertBean.getUri().getPath());
        ((ImageView) mCurViewHolder.getContainer().findViewById(R.id.advert_iv))
                .setImageBitmap(bitmap);
    }

    @Override
    public void advertComplete() {
        mHandler.removeCallbacksAndMessages(null);
        if (mCurViewHolder == null || mCurViewHolder.getContainer() == null) {
            return;
        }
        mCurViewHolder.getContainer().setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public PinePluginViewHolder getViewHolder() {
        return mCurViewHolder;
    }
}
