package com.pine.player.applet.advert.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public abstract class PineAdvertPlugin implements IPinePlayerPlugin {

    private static final int MSG_RESUME_PLAYER = 1;

    private Context mContext;
    private List<PineAdvertBean> mAdvertBeanList;
    private List<PineAdvertBean> mTimeAdvertList;
    private PineAdvertBean mStartAdvert;
    private PineAdvertBean mPauseAdvert;
    private PineAdvertBean mCompleteAdvert;

    private PineMediaWidget.IPineMediaPlayer mPlayer;

    private boolean mIsPauseByAdvert;
    private boolean mIsPlayingAdvert;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESUME_PLAYER:
                    mIsPlayingAdvert = false;
                    advertComplete();
                    if (mPlayer != null) {
                        mPlayer.start();
                    }
                    break;
            }
        }
    };

    public PineAdvertPlugin(Context context, List<PineAdvertBean> advertBeanList) {
        mContext = context;
        mAdvertBeanList = advertBeanList;
    }

    @Override
    public void onInit(PineMediaWidget.IPineMediaPlayer player) {
        if (mAdvertBeanList == null) {
            return;
        }
        mIsPauseByAdvert = false;
        mIsPlayingAdvert = false;
        PineAdvertBean pineAdvertBean;
        mTimeAdvertList = new ArrayList<PineAdvertBean>();
        for (int i = 0; i < mAdvertBeanList.size(); i++) {
            pineAdvertBean = mAdvertBeanList.get(i);
            switch (pineAdvertBean.getType()) {
                case PineAdvertBean.TYPE_TIME:
                    mTimeAdvertList.add(pineAdvertBean);
                    break;
                case PineAdvertBean.TYPE_START:
                    mStartAdvert = pineAdvertBean;
                    break;
                case PineAdvertBean.TYPE_PAUSE:
                    mPauseAdvert = pineAdvertBean;
                    break;
                case PineAdvertBean.TYPE_COMPLETE:
                    mCompleteAdvert = pineAdvertBean;
                    break;
            }
        }
        mPlayer = player;
    }

    @Override
    public PinePluginViewHolder createViewHolder(boolean isFullScreen) {
        return createViewHolder(mContext, isFullScreen);
    }

    @Override
    public void onMediaPlayerPrepared() {

    }

    @Override
    public void onMediaPlayerStart() {
        if (mStartAdvert != null && mPlayer.canPause() && !mIsPlayingAdvert) {
            mIsPauseByAdvert = true;
            mPlayer.pause();
            playAdvert(mContext, mPlayer, mStartAdvert, PineAdvertBean.TYPE_START);
            mHandler.sendEmptyMessageDelayed(MSG_RESUME_PLAYER, mStartAdvert.getDurationTime());
            mIsPlayingAdvert = true;
        }
    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {
        if (mPauseAdvert != null && !mIsPauseByAdvert && !mIsPlayingAdvert) {
            playAdvert(mContext, mPlayer, mPauseAdvert, PineAdvertBean.TYPE_PAUSE);
            mHandler.sendEmptyMessageDelayed(MSG_RESUME_PLAYER, mStartAdvert.getDurationTime());
            mIsPlayingAdvert = true;
        }
        mIsPauseByAdvert = false;
    }

    @Override
    public void onMediaPlayerComplete() {
        if (mCompleteAdvert != null && !mIsPlayingAdvert) {
            playAdvert(mContext, mPlayer, mCompleteAdvert, PineAdvertBean.TYPE_COMPLETE);
            mHandler.sendEmptyMessageDelayed(MSG_RESUME_PLAYER, mStartAdvert.getDurationTime());
            mIsPlayingAdvert = true;
        }
    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {

    }

    @Override
    public void onRefresh() {
        if (mTimeAdvertList == null && mIsPlayingAdvert) {
            return;
        }
        int position = mPlayer.getCurrentPosition();
        PineAdvertBean pineAdvertBean;
        for (int i = 0; i < mTimeAdvertList.size(); i++) {
            pineAdvertBean = mTimeAdvertList.get(i);
            if (pineAdvertBean.getPositionTime() >= position
                    && pineAdvertBean.getPositionTime() < position + 1000) {
                mIsPlayingAdvert = true;
                mPlayer.pause();
                playAdvert(mContext, mPlayer, pineAdvertBean, PineAdvertBean.TYPE_TIME);
                mHandler.sendEmptyMessageDelayed(MSG_RESUME_PLAYER, mStartAdvert.getDurationTime());
                return;
            }
        }

    }

    @Override
    public void onRelease() {
        mContext = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    public abstract PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen);

    public abstract void playAdvert(Context context, PineMediaWidget.IPineMediaPlayer player,
                                    PineAdvertBean advertBean, int advertType);

    public abstract void advertComplete();
}
