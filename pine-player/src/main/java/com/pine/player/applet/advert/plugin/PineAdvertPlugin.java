package com.pine.player.applet.advert.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.PineMediaWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public abstract class PineAdvertPlugin implements IPinePlayerPlugin {
    private static final String TAG = "PineAdvertPlugin";

    private static final int MSG_HEAD_ADVERT_FINISH = 1;
    private static final int MSG_COMPLETE_ADVERT_FINISH = 2;
    private static final int MSG_TIME_ADVERT_FINISH = 3;

    private Context mContext;
    private List<PineAdvertBean> mAdvertBeanList;
    private List<PineAdvertBean> mTimeAdvertList;
    private PineAdvertBean mHeadAdvert;
    private PineAdvertBean mPauseAdvert;
    private PineAdvertBean mCompleteAdvert;

    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaWidget.IPineMediaController mController;

    private boolean mNotPlayWhenResumeState = false;

    private boolean mIsPauseByAdvert;
    private boolean mIsPlayingHeadAdvert;
    private boolean mIsPlayingPauseAdvert;
    private boolean mIsPlayingCompleteAdvert;
    private boolean mIsPlayingTimeAdvert;

    private PineAdvertBean mPreTimeAdvert;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HEAD_ADVERT_FINISH:
                    LogUtils.d(TAG, "handleMessage MSG_HEAD_ADVERT_FINISH");
                    advertComplete();
                    mIsPauseByAdvert = false;
                    mIsPlayingHeadAdvert = false;
                    if (mPlayer != null) {
                        mPlayer.start();
                    }
                    mController.setControllerEnabled(true);
                    if (!mHeadAdvert.isRepeat()) {
                        mHeadAdvert = null;
                    }
                    break;
                case MSG_COMPLETE_ADVERT_FINISH:
                    LogUtils.d(TAG, "handleMessage MSG_COMPLETE_ADVERT_FINISH");
                    advertComplete();
                    mIsPauseByAdvert = false;
                    mIsPlayingCompleteAdvert = false;
                    mController.setControllerEnabled(true);
                    if (!mCompleteAdvert.isRepeat()) {
                        mCompleteAdvert = null;
                    }
                    break;
                case MSG_TIME_ADVERT_FINISH:
                    LogUtils.d(TAG, "handleMessage MSG_TIME_ADVERT_FINISH");
                    advertComplete();
                    mIsPauseByAdvert = false;
                    mIsPlayingTimeAdvert = false;
                    if (mPlayer != null) {
                        mPlayer.start();
                    }
                    mController.setControllerEnabled(true);
                    if (!mPreTimeAdvert.isRepeat()) {
                        mTimeAdvertList.remove(mPreTimeAdvert);
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
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller,
                       boolean isPlayerReset, boolean isResumeState) {
        if (mAdvertBeanList == null) {
            return;
        }
        mContext = context;
        mIsPauseByAdvert = false;
        mIsPlayingHeadAdvert = false;
        mIsPlayingPauseAdvert = false;
        mIsPlayingCompleteAdvert = false;
        mIsPlayingTimeAdvert = false;
        if (isPlayerReset && !isResumeState) {
            constructAdvertBeans();
        }
        mNotPlayWhenResumeState = !isPlayerReset || isResumeState;
        mPlayer = player;
        mController = controller;
        if (mHeadAdvert != null && !isPlayingAdvert() && !mNotPlayWhenResumeState) {
            LogUtils.d(TAG, "play head image advert");
            mIsPlayingHeadAdvert = true;
            playAdvert(mContext, mPlayer, mHeadAdvert, PineAdvertBean.TYPE_HEAD);
            mHandler.sendEmptyMessageDelayed(MSG_HEAD_ADVERT_FINISH, mHeadAdvert.getDurationTime());
        }
    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_MATCH_CONTROLLER;
    }

    @Override
    public void onMediaPlayerPrepared() {

    }

    @Override
    public void onMediaPlayerStart() {
        if (mHeadAdvert != null && mIsPlayingHeadAdvert) {
            mIsPauseByAdvert = true;
            mController.setControllerEnabled(false);
            mPlayer.pause();
        } else if (mCompleteAdvert != null && mIsPlayingCompleteAdvert) {
            mIsPlayingCompleteAdvert = false;
            advertComplete();
        } else if (mPauseAdvert != null && mIsPlayingPauseAdvert) {
            mIsPlayingPauseAdvert = false;
            advertComplete();
            mController.setControllerEnabled(true);
            if (!mPauseAdvert.isRepeat()) {
                mPauseAdvert = null;
            }
        } else if (mTimeAdvertList != null && mIsPlayingTimeAdvert) {
            mIsPlayingTimeAdvert = false;
            advertComplete();
            mController.setControllerEnabled(true);
        }
        mNotPlayWhenResumeState = false;
    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {
        if (mPauseAdvert != null && !isPlayingAdvert() && !mIsPauseByAdvert
                && !mNotPlayWhenResumeState) {
            LogUtils.d(TAG, "play pause image advert");
            mIsPlayingPauseAdvert = true;
            mController.setControllerEnabled(false, true, false, false, false, false, false, false, false);
            playAdvert(mContext, mPlayer, mPauseAdvert, PineAdvertBean.TYPE_PAUSE);
        }
    }

    @Override
    public void onMediaPlayerComplete() {
        if (mCompleteAdvert != null && !isPlayingAdvert()) {
            LogUtils.d(TAG, "play complete image advert");
            mIsPlayingCompleteAdvert = true;
            mController.setControllerEnabled(false, true, false, false, false, false, false, false, false);
            playAdvert(mContext, mPlayer, mCompleteAdvert, PineAdvertBean.TYPE_COMPLETE);
            mHandler.sendEmptyMessageDelayed(MSG_COMPLETE_ADVERT_FINISH, mCompleteAdvert.getDurationTime());
        }
    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {

    }

    @Override
    public void onTime(long position) {
        if (mTimeAdvertList == null && isPlayingAdvert()) {
            return;
        }
        PineAdvertBean pineAdvertBean = null;
        boolean isFound = false;
        for (int i = 0; i < mTimeAdvertList.size(); i++) {
            PineAdvertBean tmp = mTimeAdvertList.get(i);
            if (tmp.getPositionTime() >= position
                    && tmp.getPositionTime() < position + 1000) {
                pineAdvertBean = tmp;
                isFound = true;
            }
        }
        if (isFound) {
            if (mPreTimeAdvert != pineAdvertBean) {
                LogUtils.d(TAG, "play time image advert");
                mIsPlayingTimeAdvert = true;
                mIsPauseByAdvert = true;
                mController.setControllerEnabled(false);
                mPlayer.pause();
                mPreTimeAdvert = pineAdvertBean;
                playAdvert(mContext, mPlayer, pineAdvertBean, PineAdvertBean.TYPE_TIME);
                mHandler.sendEmptyMessageDelayed(MSG_TIME_ADVERT_FINISH, pineAdvertBean.getDurationTime());
            }
        } else {
            mPreTimeAdvert = null;
        }
    }

    @Override
    public void onRelease() {
        mContext = null;
        mHandler.removeCallbacksAndMessages(null);
        advertComplete();
    }

    private void constructAdvertBeans() {
        PineAdvertBean pineAdvertBean;
        mTimeAdvertList = new ArrayList<PineAdvertBean>();
        for (int i = 0; i < mAdvertBeanList.size(); i++) {
            pineAdvertBean = mAdvertBeanList.get(i);
            switch (pineAdvertBean.getType()) {
                case PineAdvertBean.TYPE_TIME:
                    mTimeAdvertList.add(pineAdvertBean);
                    break;
                case PineAdvertBean.TYPE_HEAD:
                    mHeadAdvert = pineAdvertBean;
                    break;
                case PineAdvertBean.TYPE_PAUSE:
                    mPauseAdvert = pineAdvertBean;
                    break;
                case PineAdvertBean.TYPE_COMPLETE:
                    mCompleteAdvert = pineAdvertBean;
                    break;
            }
        }
    }

    private boolean isPlayingAdvert() {
        return mIsPlayingHeadAdvert || mIsPlayingPauseAdvert ||
                mIsPlayingCompleteAdvert || mIsPlayingTimeAdvert;
    }

    public abstract void playAdvert(Context context, PineMediaWidget.IPineMediaPlayer player,
                                    PineAdvertBean advertBean, int advertType);

    public abstract void advertComplete();
}
