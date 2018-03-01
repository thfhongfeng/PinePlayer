package com.pine.player.applet.barrage.plugin;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;

import com.pine.player.PineConstants;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.barrage.BarrageCanvasView;
import com.pine.player.applet.barrage.bean.PineBarrageBean;
import com.pine.player.util.LogUtils;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.ArrayList;
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
    private BarrageCanvasView mBarrageCanvasView;
    private int mDisplayTotalHeight = 200;

    // 弹幕列表，按时间升序排列
    private List<PineBarrageBean> mBarrageList;
    private List<PineBarrageBean> mShownBarrageList;
    private int mPreFirstPDBIndex = 0;
    private int mPreLastPDBIndex = 0;
    private long mPrePosition = 0;

    public PineBarragePlugin(int displayTotalHeight, List<PineBarrageBean> barrageList) {
        setBarrageData(barrageList);
        mDisplayTotalHeight = displayTotalHeight;
        mShownBarrageList = new LinkedList<PineBarrageBean>();
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        mBarrageCanvasView = new BarrageCanvasView(context, mDisplayTotalHeight);
        PinePluginViewHolder viewHolder = new PinePluginViewHolder();
        viewHolder.setContainer(mBarrageCanvasView);
        mBarrageCanvasView.setBarrageItemViewListener(new BarrageCanvasView.IBarrageItemViewListener() {
            @Override
            public void onItemViewAnimatorEnd(PineBarrageBean pineBarrageBean) {
                clearPineBarrageBean(pineBarrageBean);
            }

            @Override
            public void onAnimationCancel(PineBarrageBean pineBarrageBean) {
                clearPineBarrageBean(pineBarrageBean);
            }
        });
        return viewHolder;
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
        if (Math.abs(position - mPrePosition) >
                (PineConstants.PLUGIN_REFRESH_TIME_DELAY << 2) * mPlayer.getSpeed()) {
            resetBarrage();
        }
        mPrePosition = position;
        List<PineBarrageBean> positionBarrageList = findNeedAddBarrageList(position);
        if (positionBarrageList == null) {
            return;
        }
        synchronized (LIST_LOCK) {
//            mBarrageCanvasView.recycleShownNode(mShownBarrageList);
            PineBarrageBean pineBarrageBean = null;
            for (int i = 0; i < positionBarrageList.size(); i++) {
                pineBarrageBean = positionBarrageList.get(i);
                LogUtils.d(TAG, "onTime prepare to add barrage text:" + pineBarrageBean.getTextBody());
                if (mBarrageCanvasView.addBarrageItemView(pineBarrageBean)) {
                    LogUtils.d(TAG, "onTime barrage added text:" + pineBarrageBean.getTextBody());
                    pineBarrageBean.setShow(true);
                    mShownBarrageList.add(pineBarrageBean);
                }
            }
        }
    }

    @Override
    public void onRelease() {
        clear();
    }

    public void setBarrageData(List<PineBarrageBean> barrageList) {
        if (barrageList == null) {
            return;
        }
        synchronized (LIST_LOCK) {
            mBarrageList = new ArrayList<PineBarrageBean>(barrageList);
            mPreFirstPDBIndex = 0;
            mPreLastPDBIndex = 0;
        }
        LogUtils.d(TAG, "setBarrageData mBarrageList:" + mBarrageList);
    }

    private void clear() {
        resetBarrage();
        mPreFirstPDBIndex = 0;
        mPreLastPDBIndex = 0;
        mContext = null;
        mPlayer = null;
    }

    private void resetBarrage() {
        LogUtils.d(TAG, "resetBarrage");
        while (mShownBarrageList.size() > 0) {
            PineBarrageBean pineBarrageBean = mShownBarrageList.get(0);
            ObjectAnimator animator = pineBarrageBean.getAnimator();
            if (animator != null && (animator.isRunning() ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                            && animator.isPaused())) {
                animator.cancel();
            }
            clearPineBarrageBean(pineBarrageBean);
        }
        mShownBarrageList.clear();
        if (mBarrageCanvasView != null) {
            mBarrageCanvasView.clear();
        }
    }

    private void clearPineBarrageBean(PineBarrageBean pineBarrageBean) {
        if (pineBarrageBean != null && pineBarrageBean.isShow()) {
            LogUtils.d(TAG, "clearPineBarrageBeanState pineBarrageBean:" + pineBarrageBean.getTextBody());
            pineBarrageBean.setPartialDisplayBarrageNode(null);
            pineBarrageBean.setShow(false);
            pineBarrageBean.setItemView(null);
            pineBarrageBean.setAnimator(null);
        }
        mShownBarrageList.remove(pineBarrageBean);
    }

    private List<PineBarrageBean> findNeedAddBarrageList(long position) {
        if (mBarrageList == null) {
            return null;
        }
        long startPosition = position - PineConstants.PLUGIN_REFRESH_TIME_DELAY;
        synchronized (LIST_LOCK) {
            List<PineBarrageBean> resultList = new ArrayList<PineBarrageBean>();
            PineBarrageBean tmpBean;
            long preFirstPosition = mBarrageList.get(mPreFirstPDBIndex).getBeginTime();
            long preLastPosition = mBarrageList.get(mPreLastPDBIndex).getBeginTime();
            int lastFoundIndex = -1;
            int countMatchSize = 0;
            if (preLastPosition <= position) {
                for (int i = mPreLastPDBIndex; i < mBarrageList.size(); i++) {
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
                    mPreLastPDBIndex = lastFoundIndex;
                    mPreFirstPDBIndex = mPreLastPDBIndex - countMatchSize + 1;
                    LogUtils.d(TAG, "findNeedAddBarrageList after index " + lastFoundIndex +
                            ", found index rang firstPDBIndex :" + mPreFirstPDBIndex +
                            ", lastPDBIndex:" + mPreLastPDBIndex +
                            ", position preLastPosition:" + preLastPosition +
                            ", position:" + position +
                            ". actual found size (exclude is already show):" + resultList.size());
                }
            } else if (preFirstPosition >= position) {
                for (int i = mPreFirstPDBIndex; i >= 0; i--) {
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
                    mPreFirstPDBIndex = lastFoundIndex;
                    mPreLastPDBIndex = mPreFirstPDBIndex + countMatchSize - 1;
                    LogUtils.d(TAG, "findNeedAddBarrageList before index " + lastFoundIndex +
                            ", found index rang firstPDBIndex :" + mPreFirstPDBIndex +
                            ", lastPDBIndex:" + mPreLastPDBIndex +
                            ", position preLastPosition:" + preLastPosition +
                            ", position:" + position +
                            ". actual found size (exclude is already show):" + resultList.size());
                }
                int resultSize = resultList.size();
                for (int i = 0; i < resultSize / 2; i++) {
                    tmpBean = resultList.get(i);
                    resultList.set(i, resultList.get(resultSize - i - 1));
                    resultList.set(resultSize - i - 1, tmpBean);
                }
            }
            return resultList.size() > 0 ? resultList : null;
        }
    }
}
