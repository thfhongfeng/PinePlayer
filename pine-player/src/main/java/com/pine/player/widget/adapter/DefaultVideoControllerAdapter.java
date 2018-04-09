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
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.AdvanceDecoration;
import com.pine.player.widget.PineMediaController;
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
 * 参考此Adapter，继承AbstractMediaControllerAdapter进行自定义controller的定制
 **/

public class DefaultVideoControllerAdapter extends PineMediaController.AbstractMediaControllerAdapter {
    private Activity mDContext;
    private PineBackgroundViewHolder mDBackgroundViewHolder;
    private PineControllerViewHolder mDFullControllerViewHolder, mDControllerViewHolder;
    private PineWaitingProgressViewHolder mDWaitingProgressViewHolder;
    private RelativeLayout mDBackgroundView;
    private ViewGroup mDFullControllerView, mDControllerView;
    private LinearLayout mDWaitingProgressView;
    private ViewGroup mDDefinitionListContainerInPlayer;
    private ViewGroup mDVideoListContainerInPlayer;
    private RecyclerView mDDefinitionListInPlayerRv;
    private RecyclerView mDVideoListInPlayerRv;
    private DefinitionListAdapter mDDefinitionListInPlayerAdapter;
    private VideoListAdapter mDVideoListInPlayerAdapter;
    private List<PineMediaPlayerBean> mDMediaList;
    private String[] mDDefinitionNameArr;
    private PineMediaWidget.IPineMediaPlayer mDPlayer;
    private int mDCurrentVideoPosition = -1;
    private TextView mDDefinitionBtn;
    private boolean mDEnableSpeed, mDEnableMediaList, mDEnableDefinition;
    private boolean mDEnableCurTime, mDEnableProgressBar, mDEnableTotalTime;
    private boolean mDEnableVolumeText, mDEnableFullScreen;

    public DefaultVideoControllerAdapter(Activity context) {
        this(context, null, true, true, true, true, true, true, true, true);
    }

    public DefaultVideoControllerAdapter(Activity context, List<PineMediaPlayerBean> mediaList) {
        this(context, mediaList, true, true, true, true, true, true, true, true);
    }

    public DefaultVideoControllerAdapter(Activity context, List<PineMediaPlayerBean> mediaList,
                                         boolean enableSpeed, boolean enableMediaList,
                                         boolean enableDefinition, boolean enableCurTime,
                                         boolean enableProgressBar, boolean enableTotalTime,
                                         boolean enableVolumeText, boolean enableFullScreen) {
        mDContext = context;
        mDDefinitionNameArr = mDContext.getResources().getStringArray(R.array.pine_media_definition_text_arr);
        mDMediaList = mediaList;
        mDEnableSpeed = enableSpeed;
        mDEnableMediaList = enableMediaList;
        mDEnableDefinition = enableDefinition;
        mDEnableCurTime = enableCurTime;
        mDEnableProgressBar = enableProgressBar;
        mDEnableTotalTime = enableTotalTime;
        mDEnableVolumeText = enableVolumeText;
        mDEnableFullScreen = enableFullScreen;
        if (hasMediaList() && mDEnableMediaList) {
            initVideoRecycleView();
        }
        if (mDEnableDefinition) {
            initDefinitionRecycleView();
        }
    }

    @Override
    protected final boolean init(PineMediaWidget.IPineMediaPlayer player) {
        mDPlayer = player;
        return true;
    }

    @Override
    protected final PineBackgroundViewHolder onCreateBackgroundViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (mDBackgroundViewHolder == null) {
            mDBackgroundViewHolder = new PineBackgroundViewHolder();
            if (mDBackgroundView == null) {
                ImageView backgroundView = new ImageView(mDContext);
                backgroundView.setBackgroundResource(android.R.color.darker_gray);
                mDBackgroundView = new RelativeLayout(mDContext);
                mDBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                mDBackgroundView.setLayoutTransition(new LayoutTransition());
                RelativeLayout.LayoutParams backgroundParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                backgroundParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mDBackgroundView.addView(backgroundView, backgroundParams);
                mDBackgroundViewHolder.setBackgroundImageView(backgroundView);
            }
        }
        mDBackgroundViewHolder.setContainer(mDBackgroundView);
        return mDBackgroundViewHolder;
    }

    @Override
    protected final PineControllerViewHolder onCreateInRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (player.isFullScreenMode()) {
            if (mDFullControllerViewHolder == null) {
                mDFullControllerViewHolder = new PineControllerViewHolder();
                if (mDFullControllerView == null) {
                    mDFullControllerView = (ViewGroup) View.inflate(mDContext,
                            R.layout.pine_player_media_controller_full, null);
                }
                initControllerViewHolder(mDFullControllerViewHolder, mDFullControllerView);
                mDFullControllerViewHolder.setTopControllerView(
                        mDFullControllerView.findViewById(R.id.top_controller));
                mDFullControllerViewHolder.setCenterControllerView(
                        mDFullControllerView.findViewById(R.id.center_controller));
                mDFullControllerViewHolder.setBottomControllerView(
                        mDFullControllerView.findViewById(R.id.bottom_controller));
                mDFullControllerViewHolder.setGoBackButton(
                        mDFullControllerView.findViewById(R.id.go_back_btn));
            }
            List<View> rightViewControlBtnList = new ArrayList<View>();
            View mediaListBtn = mDFullControllerView.findViewById(R.id.media_list_btn);
            if (hasMediaList() && mDEnableMediaList) {
                rightViewControlBtnList.add(mediaListBtn);
                mediaListBtn.setVisibility(View.VISIBLE);
            } else {
                mediaListBtn.setVisibility(View.GONE);
            }
            mDDefinitionBtn = mDFullControllerView.findViewById(R.id.media_definition_text);
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean) && mDEnableDefinition) {
                rightViewControlBtnList.add(mDDefinitionBtn);
                mDDefinitionBtn.setVisibility(View.VISIBLE);
            } else {
                mDDefinitionBtn.setVisibility(View.GONE);
            }
            if (rightViewControlBtnList.size() > 0) {
                mDFullControllerViewHolder.setRightViewControlBtnList(rightViewControlBtnList);
            }
            mDFullControllerViewHolder.setContainer(mDFullControllerView);
            return mDFullControllerViewHolder;
        } else {
            if (mDControllerViewHolder == null) {
                if (mDControllerView == null) {
                    mDControllerView = (ViewGroup) View.inflate(mDContext,
                            R.layout.pine_player_media_controller, null);
                }
                mDControllerViewHolder = new PineControllerViewHolder();
                initControllerViewHolder(mDControllerViewHolder, mDControllerView);
                mDControllerViewHolder.setTopControllerView(mDControllerView
                        .findViewById(R.id.top_controller));
                mDControllerViewHolder.setCenterControllerView(mDControllerView
                        .findViewById(R.id.center_controller));
                mDControllerViewHolder.setBottomControllerView(mDControllerView
                        .findViewById(R.id.bottom_controller));
            }
            mDControllerViewHolder.setContainer(mDControllerView);
            return mDControllerViewHolder;
        }
    }

    @Override
    protected PineControllerViewHolder onCreateOutRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        return null;
    }

    private final void initControllerViewHolder(
            PineControllerViewHolder viewHolder, View root) {
        viewHolder.setPausePlayButton(root.findViewById(R.id.pause_play_btn));
        SeekBar seekBar = (SeekBar) root.findViewById(R.id.media_progress);
        if (mDEnableProgressBar) {
            viewHolder.setPlayProgressBar(seekBar);
        } else {
            seekBar.setVisibility(View.GONE);
        }
        View curTimeTv = root.findViewById(R.id.cur_time_text);
        if (mDEnableCurTime) {
            viewHolder.setCurrentTimeText(curTimeTv);
        } else {
            curTimeTv.setVisibility(View.GONE);
        }
        View endTimeTv = root.findViewById(R.id.end_time_text);
        if (mDEnableTotalTime) {
            viewHolder.setEndTimeText(endTimeTv);
        } else {
            endTimeTv.setVisibility(View.GONE);
        }
        View VolumesTv = root.findViewById(R.id.volumes_text);
        if (mDEnableVolumeText) {
            viewHolder.setVolumesText(VolumesTv);
        } else {
            VolumesTv.setVisibility(View.GONE);
        }
        View fullScreenTv = root.findViewById(R.id.full_screen_btn);
        if (mDEnableFullScreen) {
            viewHolder.setFullScreenButton(fullScreenTv);
        } else {
            fullScreenTv.setVisibility(View.GONE);
        }
        View speedTv = root.findViewById(R.id.media_speed_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mDEnableSpeed) {
            viewHolder.setSpeedButton(speedTv);
        } else {
            speedTv.setVisibility(View.GONE);
        }
        viewHolder.setMediaNameText(root.findViewById(R.id.media_name_text));
        viewHolder.setLockControllerButton(root.findViewById(R.id.lock_screen_btn));
    }

    @Override
    protected final PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(PineMediaWidget.IPineMediaPlayer player) {
        if (mDWaitingProgressViewHolder == null) {
            mDWaitingProgressViewHolder = new PineWaitingProgressViewHolder();
            if (mDWaitingProgressView == null) {
                mDWaitingProgressView = new LinearLayout(mDContext);
                mDWaitingProgressView.setGravity(Gravity.CENTER);
                mDWaitingProgressView.setBackgroundColor(Color.argb(192, 256, 256, 256));
                ProgressBar progressBar = new ProgressBar(mDContext);
                ViewGroup.LayoutParams progressBarParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                progressBar.setLayoutParams(progressBarParams);
                progressBar.setIndeterminateDrawable(mDContext.getResources()
                        .getDrawable(R.drawable.pine_player_media_waiting_anim));
                progressBar.setIndeterminate(true);
                mDWaitingProgressView.addView(progressBar, progressBarParams);
            }
        }
        mDWaitingProgressViewHolder.setContainer(mDWaitingProgressView);
        return mDWaitingProgressViewHolder;
    }

    @Override
    protected final List<PineRightViewHolder> onCreateRightViewHolderList(PineMediaWidget.IPineMediaPlayer player) {
        List<PineRightViewHolder> viewHolderList = new ArrayList<>();
        if (player.isFullScreenMode()) {
            if (hasMediaList() && mDEnableMediaList) {
                PineRightViewHolder mediaListViewHolder = new PineRightViewHolder();
                mediaListViewHolder.setContainer(mDVideoListContainerInPlayer);
                viewHolderList.add(mediaListViewHolder);
            }
            PineMediaPlayerBean pineMediaPlayerBean = player.getMediaPlayerBean();
            if (hasDefinitionList(pineMediaPlayerBean) && mDEnableDefinition) {
                PineRightViewHolder definitionViewHolder = new PineRightViewHolder();
                definitionViewHolder.setContainer(mDDefinitionListContainerInPlayer);
                viewHolderList.add(definitionViewHolder);
                mDDefinitionListInPlayerAdapter.setData(pineMediaPlayerBean);
                mDDefinitionListInPlayerAdapter.notifyDataSetChanged();
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
                if (player.isFullScreenMode() && mDEnableFullScreen) {
                    mDControllerViewHolder.getFullScreenButton().performClick();
                } else {
                    mDContext.finish();
                }
                return false;
            }
        };
    }

    private boolean hasMediaList() {
        return mDMediaList != null && mDMediaList.size() > 0;
    }

    private boolean hasDefinitionList(PineMediaPlayerBean pineMediaPlayerBean) {
        return pineMediaPlayerBean != null && pineMediaPlayerBean.getMediaUriSourceList().size() > 1;
    }

    private void initVideoRecycleView() {
        mDVideoListContainerInPlayer = (ViewGroup) mDContext.getLayoutInflater()
                .inflate(R.layout.pine_player_media_list_recycler_view, null);
        mDVideoListInPlayerRv = mDVideoListContainerInPlayer
                .findViewById(R.id.video_recycler_view_in_player);

        // 播放器内置播放列表初始化
        // 设置固定大小
        mDVideoListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager MediaListLlm = new LinearLayoutManager(mDContext);
        // 设置垂直方向
        MediaListLlm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mDVideoListInPlayerRv.setLayoutManager(MediaListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mDVideoListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mDContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mDVideoListInPlayerAdapter = new VideoListAdapter(mDVideoListInPlayerRv);
        mDVideoListInPlayerRv.setAdapter(mDVideoListInPlayerAdapter);
        mDVideoListInPlayerAdapter.setData(mDMediaList);
        mDVideoListInPlayerAdapter.notifyDataSetChanged();
    }

    private void initDefinitionRecycleView() {
        mDDefinitionListContainerInPlayer = (ViewGroup) mDContext.getLayoutInflater()
                .inflate(R.layout.pine_player_definition_recycler_view, null);
        mDDefinitionListInPlayerRv = mDDefinitionListContainerInPlayer
                .findViewById(R.id.definition_recycler_view_in_player);

        // 播放器内置清晰度列表初始化
        // 设置固定大小
        mDDefinitionListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager definitionListLlm = new LinearLayoutManager(mDContext);
        // 设置垂直方向
        definitionListLlm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mDDefinitionListInPlayerRv.setLayoutManager(definitionListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mDDefinitionListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(mDContext,
                        R.drawable.pine_player_rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mDDefinitionListInPlayerAdapter = new DefinitionListAdapter(mDDefinitionListInPlayerRv);
        mDDefinitionListInPlayerRv.setAdapter(mDDefinitionListInPlayerAdapter);
    }

    private String getDefinitionName(int definition) {
        String definitionName = null;
        switch (definition) {
            case PineMediaUriSource.MEDIA_DEFINITION_SD:
                definitionName = mDDefinitionNameArr[0];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_HD:
                definitionName = mDDefinitionNameArr[1];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_VHD:
                definitionName = mDDefinitionNameArr[2];
                break;
            case PineMediaUriSource.MEDIA_DEFINITION_1080:
                definitionName = mDDefinitionNameArr[3];
                break;
            default:
                break;
        }
        return definitionName;
    }

    private void videoSelected(int position) {
        PineMediaPlayerBean pineMediaPlayerBean = null;
        if (hasMediaList()) {
            if (position >= 0 && position < mDMediaList.size()) {
                pineMediaPlayerBean = mDMediaList.get(position);
            } else {
                return;
            }
        } else {
            pineMediaPlayerBean = mDPlayer.getMediaPlayerBean();
        }
        mDCurrentVideoPosition = position;
        if (hasDefinitionList(pineMediaPlayerBean) && mDDefinitionBtn != null) {
            mDDefinitionListInPlayerAdapter.setData(pineMediaPlayerBean);
            mDDefinitionListInPlayerAdapter.notifyDataSetChanged();
            mDDefinitionBtn.setVisibility(View.VISIBLE);
            mDDefinitionBtn.setText(getDefinitionName(pineMediaPlayerBean.getCurrentDefinition()));
        } else {
            mDDefinitionBtn.setVisibility(View.GONE);
        }
        mDPlayer.setPlayingMedia(pineMediaPlayerBean);
        mDPlayer.start();
    }

    private void videoDefinitionSelected(PineMediaPlayerBean pineMediaPlayerBean) {
        if (pineMediaPlayerBean == null) {
            return;
        }
        mDDefinitionListInPlayerAdapter.notifyDataSetChanged();
        mDPlayer.resetPlayingMediaAndResume(pineMediaPlayerBean, null);
        if (mDDefinitionBtn != null) {
            mDDefinitionBtn.setText(getDefinitionName(pineMediaPlayerBean.getCurrentDefinition()));
        }
    }

    @Override
    public final void setCurrentMediaPosition(int position) {
        mDCurrentVideoPosition = position;
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
            view = LayoutInflater.from(mDContext)
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
                    mDPlayer.savePlayerState();
                    videoDefinitionSelected(pineMediaPlayerBean);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
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
            view = LayoutInflater.from(mDContext)
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
            boolean isSelected = position == mDCurrentVideoPosition;
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
