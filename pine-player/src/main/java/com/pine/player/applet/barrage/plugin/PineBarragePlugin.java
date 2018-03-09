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

public class PineBarragePlugin<T extends List> implements IPinePlayerPlugin<T> {
    private final static String TAG = "PineBarragePlugin";
    private final Object LIST_LOCK = new Object();
    private Context mContext;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PinePluginViewHolder mCurViewHolder;
    private BarrageCanvasView mBarrageCanvasView;
    private int mDisplayStartPx = 0;
    private int mDisplayTotalHeight = -1;
    private float mDisplayStartHeightPercent = -1f;
    private float mDisplayEndHeightPercent = -1f;
    private boolean mIsOpen = true;
    // 弹幕列表，按时间升序排列
    private List<PineBarrageBean> mBarrageList;
    private List<PineBarrageBean> mShownBarrageList;
    private ArrayList<PineBarrageBean> mDelayShowBarrageList;
    private int mPreFirstPDBIndex = -1;
    private int mPreLastPDBIndex = -1;
    private long mPrePosition = -1;

    public PineBarragePlugin(int displayTotalHeight, T barrageList) {
        this(0, displayTotalHeight, barrageList);
    }

    /**
     * @param displayStartPx     单位px
     * @param displayTotalHeight 单位px
     * @param barrageList
     */
    public PineBarragePlugin(int displayStartPx, int displayTotalHeight, T barrageList) {
        setData(barrageList);
        mDisplayStartPx = displayStartPx;
        mDisplayTotalHeight = displayTotalHeight;
        mShownBarrageList = new LinkedList<PineBarrageBean>();
        mDelayShowBarrageList = new ArrayList<PineBarrageBean>();
    }

    public PineBarragePlugin(float displayEndPercent, T barrageList) {
        this(0.0f, displayEndPercent, barrageList);
    }

    /**
     * @param displayStartPercent [0.0-displayEndPercent)
     * @param displayEndPercent   [displayStartPercent-1.0]
     * @param barrageList
     */
    public PineBarragePlugin(float displayStartPercent, float displayEndPercent, T barrageList) {
        setData(barrageList);
        mDisplayStartHeightPercent = displayStartPercent;
        mDisplayEndHeightPercent = displayEndPercent;
        mShownBarrageList = new LinkedList<PineBarrageBean>();
        mDelayShowBarrageList = new ArrayList<PineBarrageBean>();
    }

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (mDisplayTotalHeight != -1) {
            mBarrageCanvasView = new BarrageCanvasView(context, mDisplayStartPx, mDisplayTotalHeight);
        } else {
            mBarrageCanvasView = new BarrageCanvasView(context, mDisplayStartHeightPercent, mDisplayEndHeightPercent);
        }
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
    public void setData(T data) {
        if (data == null) {
            return;
        }
        synchronized (LIST_LOCK) {
            mBarrageList = new ArrayList<PineBarrageBean>(data);
            mPreFirstPDBIndex = -1;
            mPreLastPDBIndex = -1;
        }
        LogUtil.d(TAG, "setBarrageData mBarrageList.size():" + mBarrageList.size());
    }

    /**
     * @param data 添加的列表必须是按BeginTime升序排列
     */
    @Override
    public void addData(T data) {
        if (data == null || data.size() < 1) {
            return;
        }
        ArrayList<PineBarrageBean> barrageList = new ArrayList<PineBarrageBean>(data);
        long startPosition = barrageList.get(0).getBeginTime();
        long endPosition = barrageList.get(barrageList.size() - 1).getBeginTime();
        if (startPosition > endPosition) {
            return;
        }
        LogUtil.d(TAG, "before addBarrageData total size:" + mBarrageList.size());
        if (startPosition < mBarrageList.get(mBarrageList.size() - 1).getBeginTime()) {
            int startSearchIndex = mPreFirstPDBIndex < 0 ? 0 : mPreFirstPDBIndex;
            int startIndex = 0, endIndex = mBarrageList.size() - 1;
            boolean isStartIndexSet = false, isEndIndexSet = false;
            PineBarrageBean tmpBean = null;
            ArrayList<PineBarrageBean> mergeList = new ArrayList<PineBarrageBean>();
            for (int i = startSearchIndex; i >= 0; i--) {
                tmpBean = mBarrageList.get(i);
                if (tmpBean.getBeginTime() > endPosition) {
                    continue;
                } else if (tmpBean.getBeginTime() <= startPosition) {
                    startIndex = i + 1;
                    break;
                }
                isStartIndexSet = true;
                mergeList.add(0, tmpBean);
                LogUtil.d(TAG, "mergeList start mBarrageList(" + i + "):" + tmpBean.getTextBody());
            }
            for (int i = startSearchIndex + 1; i < mBarrageList.size(); i++) {
                tmpBean = mBarrageList.get(i);
                if (tmpBean.getBeginTime() < startPosition) {
                    continue;
                } else if (tmpBean.getBeginTime() >= endPosition) {
                    endIndex = i - 1;
                    break;
                }
                isEndIndexSet = true;
                mergeList.add(tmpBean);
                LogUtil.d(TAG, "mergeList end mBarrageList(" + i + "):" + tmpBean.getTextBody());
            }
            LogUtil.d(TAG, "mergeList mBarrageList 1" + ", startIndex:" + startIndex + ", endIndex:" + endIndex +
                    ", isStartIndexSet:" + isStartIndexSet + ", isEndIndexSet:" + isEndIndexSet);
            if (isStartIndexSet) {
                endIndex = startIndex + mergeList.size() - 1;
            } else if (isEndIndexSet) {
                startIndex = endIndex - mergeList.size() + 1;
            }
            LogUtil.d(TAG, "mergeList mBarrageList 2" + ", startIndex:" + startIndex + ", endIndex:" + endIndex);
            ArrayList<PineBarrageBean> resultList = mergeList(barrageList, mergeList);
            LogUtil.d(TAG, "mergeList mBarrageList 3" + ", startIndex:" + startIndex + ", endIndex:" + endIndex);
            synchronized (LIST_LOCK) {
                int k = 0;
                for (int i = startIndex; i <= endIndex; i++) {
                    LogUtil.d(TAG, "mBarrageList(" + i + "):" + mBarrageList.get(i).getTextBody());
                    mBarrageList.set(i, resultList.get(k++));
                }
                mBarrageList.addAll(endIndex + 1, resultList.subList(k, resultList.size()));
            }
            LogUtil.d(TAG, "after addBarrageData merge func size:" + data.size() + ", total size:" +
                    mBarrageList.size() + ", startPosition:" + startPosition + ", endPosition:" + endPosition +
                    ", resultList.size():" + resultList.size() + ", startIndex:" + startIndex + ", endIndex:" + endIndex);
        } else {
            synchronized (LIST_LOCK) {
                mBarrageList.addAll(barrageList);
                LogUtil.d(TAG, "after addBarrageData addAll func size:" + data.size() + ", total size:" +
                        mBarrageList.size() + ", startPosition:" + startPosition + ", endPosition:" + endPosition);
            }
        }
    }

    private ArrayList<PineBarrageBean> mergeList(ArrayList<PineBarrageBean> firstList, ArrayList<PineBarrageBean> secondList) {
        ArrayList<PineBarrageBean> resultList = new ArrayList<PineBarrageBean>();
        int i = 0, j = 0;
        int firstListSize = firstList.size();
        int secondListSize = secondList.size();
        while (i < firstListSize && j < secondListSize) {
            if (firstList.get(i).getBeginTime() <= secondList.get(j).getBeginTime()) {
                resultList.add(firstList.get(i++));
            } else {
                resultList.add(secondList.get(j++));
            }
        }
        while (i < firstListSize) {
            resultList.add(firstList.get(i++));
        }
        while (j < secondListSize) {
            resultList.add(secondList.get(j++));
        }
        return resultList;
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
                } else if (pineBarrageBean.getBeginTime() < position + 60000) {
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
