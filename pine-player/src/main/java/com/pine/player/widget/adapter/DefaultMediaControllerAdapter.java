package com.pine.player.widget.adapter;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pine.player.R;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.bean.PineMediaUriSource;
import com.pine.player.widget.AdvanceDecoration;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/3/7.
 */

/**
 * 参考此Adapter，继承此Adapter或者AbstractMediaControllerAdapter进行自定义controller的定制
 **/

public class DefaultMediaControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    protected Activity mContext;
    protected PineBackgroundViewHolder mBackgroundViewHolder;
    protected PineControllerViewHolder mFullControllerViewHolder, mControllerViewHolder;
    protected PineWaitingProgressViewHolder mWaitingProgressViewHolder;
    protected RelativeLayout mBackgroundView;
    protected ViewGroup mFullControllerView, mControllerView;
    protected LinearLayout mWaitingProgressView;
    private ViewGroup mDefinitionListContainerInPlayer;
    private ViewGroup mVideoListContainerInPlayer;
    private RecyclerView mDefinitionListInPlayerRv;
    private RecyclerView mVideoListInPlayerRv;
    private DefinitionListAdapter mDefinitionListInPlayerAdapter;
    private VideoListAdapter mVideoListInPlayerAdapter;
    private List<PineMediaPlayerBean> mMediaList;
    private String[] mDefinitionNameArr;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private int mCurrentVideoPosition = -1;
    private TextView mDefinitionBtn;

    public DefaultMediaControllerAdapter(Activity context, List<PineMediaPlayerBean> mediaList) {
        this.mContext = context;
        mDefinitionNameArr = mContext.getResources().getStringArray(R.array.pine_media_definition_text_arr);
        mMediaList = mediaList;
        if (hasMediaList()) {
            initVideoRecycleView();
        }
        initDefinitionRecycleView();
    }

    @Override
    public boolean init(PineMediaWidget.IPineMediaPlayer player) {
        mPlayer = player;
        return true;
    }

    @Override
    public PineBackgroundViewHolder onCreateBackgroundViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (mBackgroundViewHolder == null) {
            mBackgroundViewHolder = new PineBackgroundViewHolder();
            if (mBackgroundView == null) {
                ImageView backgroundView = new ImageView(mContext);
                backgroundView.setBackgroundResource(android.R.color.darker_gray);
                mBackgroundView = new RelativeLayout(mContext);
                mBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                mBackgroundView.setLayoutTransition(new LayoutTransition());
                RelativeLayout.LayoutParams backgroundParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mBackgroundView.addView(backgroundView, backgroundParams);
                mBackgroundViewHolder.setBackgroundImageView(backgroundView);
            }
        }
        mBackgroundViewHolder.setContainer(mBackgroundView);
        return mBackgroundViewHolder;
    }

    @Override
    public PineControllerViewHolder onCreateInRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (player.isFullScreenMode()) {
            if (mFullControllerViewHolder == null) {
                mFullControllerViewHolder = new PineControllerViewHolder();
                if (mFullControllerView == null) {
                    mFullControllerView = (ViewGroup) View.inflate(mContext,
                            R.layout.pine_player_media_controller_full, null);
                }
                initControllerViewHolder(mFullControllerViewHolder, mFullControllerView);
                mFullControllerViewHolder.setTopControllerView(
                        mFullControllerView.findViewById(R.id.top_controller));
                mFullControllerViewHolder.setCenterControllerView(
                        mFullControllerView.findViewById(R.id.center_controller));
                mFullControllerViewHolder.setBottomControllerView(
                        mFullControllerView.findViewById(R.id.bottom_controller));
                mFullControllerViewHolder.setGoBackButton(
                        mFullControllerView.findViewById(R.id.go_back_btn));
            }
            List<View> rightViewControlBtnList = new ArrayList<View>();
            View mediaListBtn = mFullControllerView.findViewById(R.id.media_list_btn);
            if (hasMediaList()) {
                rightViewControlBtnList.add(mediaListBtn);
                mediaListBtn.setVisibility(View.VISIBLE);
            } else {
                mediaListBtn.setVisibility(View.GONE);
            }
            mDefinitionBtn = mFullControllerView.findViewById(R.id.media_definition_text);
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean)) {
                rightViewControlBtnList.add(mDefinitionBtn);
                mDefinitionBtn.setVisibility(View.VISIBLE);
            } else {
                mDefinitionBtn.setVisibility(View.GONE);
            }
            mFullControllerViewHolder.setRightViewControlBtnList(rightViewControlBtnList);
            mFullControllerViewHolder.setContainer(mFullControllerView);
            return mFullControllerViewHolder;
        } else {
            if (mControllerViewHolder == null) {
                if (mControllerView == null) {
                    mControllerView = (ViewGroup) View.inflate(mContext,
                            R.layout.pine_player_media_controller, null);
                }
                mControllerViewHolder = new PineControllerViewHolder();
                initControllerViewHolder(mControllerViewHolder, mControllerView);
                mControllerViewHolder.setTopControllerView(mControllerView
                        .findViewById(R.id.top_controller));
                mControllerViewHolder.setCenterControllerView(mControllerView
                        .findViewById(R.id.center_controller));
                mControllerViewHolder.setBottomControllerView(mControllerView
                        .findViewById(R.id.bottom_controller));
            }
            mControllerViewHolder.setContainer(mControllerView);
            return mControllerViewHolder;
        }
    }

    @Override
    public PineControllerViewHolder onCreateOutRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        return null;
    }

    private void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.pause_play_btn));
        viewHolder.setPlayProgressBar((SeekBar) root.findViewById(R.id.media_progress));
        viewHolder.setCurrentTimeText(root.findViewById(R.id.cur_time_text));
        viewHolder.setEndTimeText(root.findViewById(R.id.end_time_text));
        viewHolder.setVolumesText(root.findViewById(R.id.volumes_text));
        viewHolder.setFullScreenButton(root.findViewById(R.id.full_screen_btn));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.setSpeedButton(root.findViewById(R.id.media_speed_text));
        } else {
            root.findViewById(R.id.media_speed_text).setVisibility(View.GONE);
        }
        viewHolder.setMediaNameText(root.findViewById(R.id.media_name_text));
        viewHolder.setLockControllerButton(root.findViewById(R.id.lock_screen_btn));
    }

    @Override
    public PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (mWaitingProgressViewHolder == null) {
            mWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
            if (mWaitingProgressView == null) {
                mWaitingProgressView = new LinearLayout(mContext);
                mWaitingProgressView.setGravity(Gravity.CENTER);
                mWaitingProgressView.setBackgroundColor(Color.argb(192, 256, 256, 256));
                ProgressBar progressBar = new ProgressBar(mContext);
                ViewGroup.LayoutParams progressBarParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressBar.setLayoutParams(progressBarParams);
                progressBar.setIndeterminateDrawable(mContext.getResources()
                        .getDrawable(R.drawable.pine_player_media_waiting_anim));
                progressBar.setIndeterminate(true);
                mWaitingProgressView.addView(progressBar, progressBarParams);
            }
        }
        mWaitingProgressViewHolder.setContainer(mWaitingProgressView);
        return mWaitingProgressViewHolder;
    }

    @Override
    public List<PineRightViewHolder> onCreateRightViewHolderList(PineMediaWidget.IPineMediaPlayer player) {
        List<PineRightViewHolder> viewHolderList = new ArrayList<PineRightViewHolder>();
        if (player.isFullScreenMode()) {
            if (hasMediaList()) {
                PineRightViewHolder mediaListViewHolder = new PineRightViewHolder();
                mediaListViewHolder.setContainer(mVideoListContainerInPlayer);
                viewHolderList.add(mediaListViewHolder);
            }
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean)) {
                PineRightViewHolder definitionViewHolder = new PineRightViewHolder();
                definitionViewHolder.setContainer(mDefinitionListContainerInPlayer);
                viewHolderList.add(definitionViewHolder);
            }
        }
        return viewHolderList.size() > 0 ? viewHolderList : null;
    }

    @Override
    public PineMediaController.ControllersActionListener onCreateControllersActionListener() {
        return new PineMediaController.ControllersActionListener() {
            @Override
            public boolean onGoBackBtnClick(View fullScreenBtn,
                                            PineMediaWidget.IPineMediaPlayer player) {
                if (player.isFullScreenMode()) {
                    mControllerViewHolder.getFullScreenButton().performClick();
                } else {
                    mContext.finish();
                }
                return false;
            }
        };
    }

    private boolean hasMediaList() {
        return mMediaList != null && mMediaList.size() > 0;
    }

    private boolean hasDefinitionList(PineMediaPlayerBean pineMediaPlayerBean) {
        return pineMediaPlayerBean != null && pineMediaPlayerBean.getMediaUriSourceList().size() > 1;
    }

    private void initVideoRecycleView() {
        mVideoListContainerInPlayer = (ViewGroup) mContext.getLayoutInflater()
                .inflate(R.layout.pine_player_media_list_recycler_view, null);
        mVideoListInPlayerRv = mVideoListContainerInPlayer
                .findViewById(R.id.video_recycler_view_in_player);

        // 播放器内置播放列表初始化
        // 设置固定大小
        mVideoListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager MediaListLlm = new LinearLayoutManager(mContext);
        // 设置垂直方向
        MediaListLlm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mVideoListInPlayerRv.setLayoutManager(MediaListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mVideoListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mVideoListInPlayerAdapter = new VideoListAdapter(mVideoListInPlayerRv);
        mVideoListInPlayerRv.setAdapter(mVideoListInPlayerAdapter);
        mVideoListInPlayerAdapter.setData(mMediaList);
        mVideoListInPlayerAdapter.notifyDataSetChanged();
    }

    private void initDefinitionRecycleView() {
        mDefinitionListContainerInPlayer = (ViewGroup) mContext.getLayoutInflater()
                .inflate(R.layout.pine_player_definition_recycler_view, null);
        mDefinitionListInPlayerRv = mDefinitionListContainerInPlayer
                .findViewById(R.id.definition_recycler_view_in_player);

        // 播放器内置清晰度列表初始化
        // 设置固定大小
        mDefinitionListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager definitionListLlm = new LinearLayoutManager(mContext);
        // 设置垂直方向
        definitionListLlm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mDefinitionListInPlayerRv.setLayoutManager(definitionListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mDefinitionListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mDefinitionListInPlayerAdapter = new DefinitionListAdapter(mDefinitionListInPlayerRv);
        mDefinitionListInPlayerRv.setAdapter(mDefinitionListInPlayerAdapter);
    }

    private String getDefinitionName(int definition) {
        String definitionName = null;
        switch (definition) {
            case PineMediaUriSource.MEDIA_DEFINITION_SD:
                definitionName = mDefinitionNameArr[0];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_HD:
                definitionName = mDefinitionNameArr[1];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_VHD:
                definitionName = mDefinitionNameArr[2];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_1080:
                definitionName = mDefinitionNameArr[3];
                break;
            default:
                break;
        }
        return definitionName;
    }

    public void setCurrentVideoPosition(int position) {
        mCurrentVideoPosition = position;
    }

    public void videoSelected(int position) {
        PineMediaPlayerBean pineMediaPlayerBean = null;
        if (hasMediaList()) {
            if (position >= 0 && position < mMediaList.size()) {
                pineMediaPlayerBean = mMediaList.get(position);
            } else {
                return;
            }
        } else {
            pineMediaPlayerBean = mPlayer.getMediaPlayerBean();
        }
        mCurrentVideoPosition = position;
        if (hasDefinitionList(pineMediaPlayerBean)) {
            mDefinitionListInPlayerAdapter.setData(pineMediaPlayerBean);
            mDefinitionListInPlayerAdapter.notifyDataSetChanged();
            mDefinitionBtn.setVisibility(View.VISIBLE);
            mDefinitionBtn.setText(getDefinitionName(pineMediaPlayerBean.getCurrentDefinition()));
        } else {
            mDefinitionBtn.setVisibility(View.GONE);
        }
        mPlayer.setPlayingMedia(pineMediaPlayerBean);
        mPlayer.start();
    }

    private void videoDefinitionSelected(PineMediaPlayerBean pineMediaPlayerBean) {
        if (pineMediaPlayerBean == null) {
            return;
        }
        mDefinitionListInPlayerAdapter.setData(pineMediaPlayerBean);
        mDefinitionListInPlayerAdapter.notifyDataSetChanged();
        mPlayer.resetPlayingMediaAndResume(pineMediaPlayerBean, null);
        if (mDefinitionBtn != null) {
            mDefinitionBtn.setText(getDefinitionName(pineMediaPlayerBean.getCurrentDefinition()));
        }
    }

    // 自定义RecyclerView的数据Adapter
    class DefinitionListAdapter extends RecyclerView.Adapter {
        private PineMediaPlayerBean pineMediaPlayerBean;
        private List<PineMediaUriSource> mData;
        private RecyclerView mRecyclerView;

        public DefinitionListAdapter(RecyclerView view) {
            this.mRecyclerView = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.pine_player_item_definition_select_in_player, parent, false);
            DefinitionViewHolder viewHolder = new DefinitionViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final DefinitionViewHolder myHolder = (DefinitionViewHolder) holder;
            PineMediaUriSource itemData = mData.get(position);
            int definition = itemData.getMediaDefinition();
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(getDefinitionName(definition));
            }
            boolean isSelected = position == pineMediaPlayerBean.getCurrentDefinitionPosition();
            myHolder.itemView.setSelected(isSelected);
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    pineMediaPlayerBean.setCurrentDefinitionByPosition(position);
                    mPlayer.savePlayerState();
                    videoDefinitionSelected(pineMediaPlayerBean);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 :mData.size();
        }

        public void setData(@NonNull PineMediaPlayerBean pineMediaPlayerBean) {
            this.pineMediaPlayerBean = pineMediaPlayerBean;
            this.mData = pineMediaPlayerBean.getMediaUriSourceList();
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class DefinitionViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public DefinitionViewHolder(View view) {
            super(view);
            mItemTv = (TextView) view.findViewById(R.id.rv_definition_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }

    // 自定义RecyclerView的数据Adapter
    class VideoListAdapter extends RecyclerView.Adapter {
        private List<PineMediaPlayerBean> mData;
        private RecyclerView mRecyclerView;

        public VideoListAdapter(RecyclerView view) {
            this.mRecyclerView = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.pine_player_item_video_in_player, parent, false);
            VideoViewHolder viewHolder = new VideoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final VideoViewHolder myHolder = (VideoViewHolder) holder;
            PineMediaPlayerBean itemData = mData.get(position);
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(itemData.getMediaName());
            }
            boolean isSelected = position == mCurrentVideoPosition;
            myHolder.itemView.setSelected(isSelected);
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    videoSelected(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<PineMediaPlayerBean> data) {
            this.mData = data;
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public VideoViewHolder(View view) {
            super(view);
            mItemTv = (TextView) view.findViewById(R.id.rv_video_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }
}
