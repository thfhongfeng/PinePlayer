package com.pine.player.applet.barrage.plugin;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.barrage.BarrageCanvasView;
import com.pine.player.applet.barrage.bean.PineBarrageBean;
import com.pine.player.util.LogUtil;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/1/12.
 */

public class PineBarragePlugin implements IPinePlayerPlugin {
    private final static String TAG = "PineBarragePlugin";
    private final Object LIST_LOCK = new Object();
    private Context mContext;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PinePluginViewHolder mCurViewHolder;
    private BarrageCanvasView mBarrageCanvasView;
    private int mDisplayTotalHeight = 200;
    private boolean mIsOpen = true;
    // 弹幕列表，按时间升序排列
    private List<PineBarrageBean> mBarrageList;
    private List<PineBarrageBean> mShownBarrageList;
    private ArrayList<PineBarrageBean> mDelayShowBarrageList;
    private int mPreFirstPDBIndex = -1;
    private int mPreLastPDBIndex = -1;
    private long mPrePosition = -1;

    public PineBarragePlugin(int displayTotalHeight, List<PineBarrageBean> barrageList) {
        setBarrageData(barrageList);
        mDisplayTotalHeight = displayTotalHeight;
        mShownBarrageList = new LinkedList<PineBarrageBean>();
        mDelayShowBarrageList = new ArrayList<PineBarrageBean>();
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        mBarrageCanvasView = new BarrageCanvasView(context, mDisplayTotalHeight);
        mCurViewHolder = new PinePluginViewHolder();
        mCurViewHolder.setContainer(mBarrageCanvasView);
        mBarrageCanvasView.setBarrageItemViewListener(new BarrageCanvasView.IBarrageItemViewListener() {
            @Override
            public void onItemViewAnimatorEnd(PineBarrageBean pineBarrageBean) {
                clearShownPineBarrageBean(pineBarrageBean);
            }

            @Override
            public void onAnimationCancel(PineBarrageBean pineBarrageBean) {
                clearShownPineBarrageBean(pineBarrageBean);
            }
        });
        return mCurViewHolder;
    }

    @Override
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller,
                       boolean isPlayerReset, boolean isResumeState) {
        clear();
        mContext = context;
        mPlayer = player;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (int i = 0; i < mShownBarrageList.size(); i++) {
                ObjectAnimator animator = mShownBarrageList.get(i).getAnimator();
                if (animator != null && animator.isPaused()) {
                    mShownBarrageList.get(i).getAnimator().resume();
                }
            }
        }
    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (int i = 0; i < mShownBarrageList.size(); i++) {
                ObjectAnimator animator = mShownBarrageList.get(i).getAnimator();
                if (animator != null && animator.isRunning()) {
                    mShownBarrageList.get(i).getAnimator().pause();
                }
            }
        }
    }

    @Override
    public void onMediaPlayerComplete() {

    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {
        clear();
    }

    @Override
    public void onTime(long position) {
        if (mBarrageCanvasView == null) {
            return;
        }
        if (mBarrageCanvasView == null) {
            mPrePosition = position;
            return;
        }
        if (mPrePosition > -1 && Math.abs(position - mPrePosition) >
                (PineConstants.PLUGIN_REFRESH_TIME_DELAY << 2) * mPlayer.getSpeed()) {
            resetBarrage();
        }
        ArrayList<PineBarrageBean> positionBarrageList = findNeedAddBarrages(position, mPlayer.getSpeed());
        mPrePosition = position;
        if (positionBarrageList == null && mDelayShowBarrageList.size() < 1) {
            return;
        }
        synchronized (LIST_LOCK) {
            if (mDelayShowBarrageList.size() > 0) {
                if (positionBarrageList == null) {
                    positionBarrageList = new ArrayList<PineBarrageBean>();
                }
                positionBarrageList.addAll(mDelayShowBarrageList);
                mDelayShowBarrageList.clear();
            }
            PineBarrageBean pineBarrageBean = null;
            for (int i = 0; i < positionBarrageList.size(); i++) {
                pineBarrageBean = positionBarrageList.get(i);
                LogUtil.d(TAG, "onTime prepare to add barrage text:" + pineBarrageBean.getTextBody());
                if (mBarrageCanvasView.addBarrageItemView(pineBarrageBean, mPlayer.getSpeed())) {
                    LogUtil.d(TAG, "onTime barrage added text:" + pineBarrageBean.getTextBody());
                    pineBarrageBean.setShow(true);
                    mShownBarrageList.add(pineBarrageBean);
                } else {
                    mDelayShowBarrageList.add(pineBarrageBean);
                }
            }
        }
    }

    @Override
    public void onRelease() {
        clear();
    }

    @Override
    public void openPlugin() {
        mIsOpen = true;
        mCurViewHolder.getContainer().setVisibility(View.VISIBLE);
    }

    @Override
    public void closePlugin() {
        mIsOpen = false;
        mCurViewHolder.getContainer().setVisibility(View.GONE);
    }

    @Override
    public boolean isOpen() {
        return mIsOpen;
    }

    public void setBarrageData(List<PineBarrageBean> barrageList) {
        if (barrageList == null) {
            return;
        }
        synchronized (LIST_LOCK) {
            mBarrageList = new ArrayList<PineBarrageBean>(barrageList);
            mPreFirstPDBIndex = -1;
            mPreLastPDBIndex = -1;
        }
        LogUtil.d(TAG, "setBarrageData mBarrageList:" + mBarrageList);
    }

    private void clear() {
        resetBarrage();
        mPreFirstPDBIndex = -1;
        mPreLastPDBIndex = -1;
        mContext = null;
        mPlayer = null;
    }

    private void resetBarrage() {
        LogUtil.d(TAG, "resetBarrage");
        mDelayShowBarrageList.clear();
        while (mShownBarrageList.size() > 0) {
            PineBarrageBean pineBarrageBean = mShownBarrageList.get(0);
            ObjectAnimator animator = pineBarrageBean.getAnimator();
            if (animator != null && (animator.isRunning() ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                            && animator.isPaused())) {
                animator.cancel();
            }
            clearShownPineBarrageBean(pineBarrageBean);
        }
        mShownBarrageList.clear();
        if (mBarrageCanvasView != null) {
            mBarrageCanvasView.clear();
        }
    }

    private void clearShownPineBarrageBean(PineBarrageBean pineBarrageBean) {
        if (pineBarrageBean != null && pineBarrageBean.isShow()) {
            LogUtil.d(TAG, "clearPineBarrageBeanState pineBarrageBean:" + pineBarrageBean.getTextBody());
            pineBarrageBean.setPartialDisplayBarrageNode(null);
            pineBarrageBean.setShow(false);
            pineBarrageBean.setItemView(null);
            pineBarrageBean.setAnimator(null);
        }
        mShownBarrageList.remove(pineBarrageBean);
    }

    private ArrayList<PineBarrageBean> findNeedAddBarrages(long position, float speed) {
        if (mBarrageList == null) {
            return null;
        }
        long startPosition = position - (long) Math.ceil(speed * 3 * PineConstants.PLUGIN_REFRESH_TIME_DELAY);
        synchronized (LIST_LOCK) {
            ArrayList<PineBarrageBean> resultList = new ArrayList<PineBarrageBean>();
            PineBarrageBean tmpBean;
            long preFirstPosition = -1;
            long preLastPosition = -1;
            if (mPreFirstPDBIndex > -1 && mPreFirstPDBIndex < mBarrageList.size()) {
                preFirstPosition = mBarrageList.get(mPreFirstPDBIndex).getBeginTime();
            }
            if (mPreLastPDBIndex > -1 && mPreLastPDBIndex < mBarrageList.size()) {
                preLastPosition = mBarrageList.get(mPreLastPDBIndex).getBeginTime();
            }
            int lastFoundIndex = -1;
            int countMatchSize = 0;
            if (preLastPosition <= position) {
                for (int i = mPreLastPDBIndex + 1; i < mBarrageList.size(); i++) {
                    tmpBean = mBarrageList.get(i);
                    if (tmpBean.getBeginTime() >= startPosition && tmpBean.getBeginTime() <= position) {
                        if (!tmpBean.isShow()) {
                            resultList.add(tmpBean);
                        }
                        countMatchSize++;
                        lastFoundIndex = i;
                    } else if (lastFoundIndex > 0) {
                        break;
                    }
                }
                if (lastFoundIndex >= 0) {
                    LogUtil.d(TAG, "findNeedAddBarrageList after index " + mPreLastPDBIndex +
                            ", found index rang firstPDBIndex:" + (lastFoundIndex - countMatchSize + 1) +
                            ", lastPDBIndex:" + lastFoundIndex +
                            ", preLastPosition:" + preLastPosition +
                            ", startPosition:" + startPosition + ", position:" + position +
                            ". actual found size (exclude is already show):" + resultList.size());
                    mPreFirstPDBIndex = lastFoundIndex - countMatchSize + 1;
                    mPreLastPDBIndex = lastFoundIndex;
                }
            } else if (preFirstPosition >= position) {
                for (int i = mPreFirstPDBIndex - 1; i >= 0; i--) {
                    tmpBean = mBarrageList.get(i);
                    if (tmpBean.getBeginTime() >= startPosition && tmpBean.getBeginTime() <= position) {
                        if (!tmpBean.isShow()) {
                            resultList.add(tmpBean);
                        }
                        countMatchSize++;
                        lastFoundIndex = i;
                    } else if (lastFoundIndex > 0) {
                        break;
                    }
                }
                if (lastFoundIndex >= 0) {
                    LogUtil.d(TAG, "findNeedAddBarrageList before index " + mPreFirstPDBIndex +
                            ", found index rang firstPDBIndex:" + (lastFoundIndex + countMatchSize - 1) +
                            ", lastPDBIndex:" + lastFoundIndex +
                            ", preLastPosition:" + preLastPosition +
                            ", startPosition:" + startPosition + ", position:" + position +
                            ". actual found size (exclude is already show):" + resultList.size());
                    mPreLastPDBIndex = lastFoundIndex + countMatchSize - 1;
                    mPreFirstPDBIndex = lastFoundIndex;
                }
                Collections.reverse(resultList);
            }
            return resultList.size() > 0 ? resultList : null;
        }
    }
}
