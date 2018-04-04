package com.pine.pineplayer.ui.activity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pine.pineplayer.R;
import com.pine.pineplayer.ui.view.AdvanceDecoration;
import com.pine.pineplayer.util.MockDataUtil;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.bean.PineMediaUriSource;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineControllerViewHolder;
import com.pine.player.widget.viewholder.PineRightViewHolder;
import com.pine.player.widget.viewholder.PineWaitingProgressViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SimpleCustomPinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "SimpleCustomPinePlayerActivity";

    private PineMediaPlayerView mVideoView;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PineMediaController mController;
    private String mBasePath;
    private PineMediaController.AbstractMediaControllerAdapter mMediaControllerAdapter;

    private PineControllerViewHolder mFullControllerViewHolder, mControllerViewHolder;
    private ViewGroup mFullControllerView, mControllerView;
    private ViewGroup mDefinitionListContainerInPlayer;
    private RecyclerView mDefinitionListInPlayerRv;

    private String[] mDefinitionNameArr;
    private TextView mDefinitionBtn;
    private DefinitionListAdapter mDefinitionListInPlayerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_custom_pine_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mBasePath = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(mBasePath)) {
            finish();
            return;
        }
        mDefinitionNameArr = getResources().getStringArray(R.array.media_definition_text_arr);
        initRecycleView();
        mVideoView = (PineMediaPlayerView) findViewById(R.id.video_view);
        mVideoView.init(TAG);
        mController = new PineMediaController(this);

        mMediaControllerAdapter = new PineMediaController.AbstractMediaControllerAdapter() {

            @Override
            public boolean init(PineMediaWidget.IPineMediaPlayer player) {
                return true;
            }

            @Override
            public List<PineRightViewHolder> onCreateRightViewHolderList(PineMediaWidget.IPineMediaPlayer player) {
                List<PineRightViewHolder> viewHolderList = new ArrayList<PineRightViewHolder>();
                if (player.isFullScreenMode()) {
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
            public PineControllerViewHolder onCreateInRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
                if (player.isFullScreenMode()) {
                    if (mFullControllerViewHolder == null) {
                        mFullControllerViewHolder = new PineControllerViewHolder();
                        if (mFullControllerView == null) {
                            mFullControllerView = (ViewGroup) View.inflate(SimpleCustomPinePlayerActivity.this,
                                    R.layout.media_controller_full, null);
                        }
                        initControllerViewHolder(mFullControllerViewHolder, mFullControllerView);
                        mFullControllerViewHolder.setTopControllerView(
                                mFullControllerView.findViewById(R.id.top_controller));
                        mFullControllerViewHolder.setCenterControllerView(
                                mFullControllerView.findViewById(R.id.center_controller));
                        mFullControllerViewHolder.setBottomControllerView(
                                mFullControllerView.findViewById(R.id.bottom_controller));
                        mFullControllerViewHolder.setRightControllerView(
                                mFullControllerView.findViewById(R.id.right_controller));
                        mFullControllerViewHolder.setGoBackButton(
                                mFullControllerView.findViewById(R.id.go_back_btn));
                    }
                    mFullControllerView.findViewById(R.id.media_list_btn).setVisibility(View.GONE);
                    List<View> rightViewControlBtnList = new ArrayList<View>();
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
                            mControllerView = (ViewGroup) View.inflate(SimpleCustomPinePlayerActivity.this,
                                    R.layout.media_controller, null);
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
            protected PineControllerViewHolder onCreateOutRootControllerViewHolder(PineMediaWidget.IPineMediaPlayer player) {
                return null;
            }

            @Override
            protected PineWaitingProgressViewHolder onCreateWaitingProgressViewHolder(PineMediaWidget.IPineMediaPlayer player) {
                return null;
            }

            @Override
            public PineBackgroundViewHolder onCreateBackgroundViewHolder(PineMediaWidget.IPineMediaPlayer player) {
                PineMediaPlayerBean playerBean = player.getMediaPlayerBean();
                Uri imgUri = playerBean == null ? null : playerBean.getMediaImgUri();
                ImageView mediaBackgroundView = new ImageView(SimpleCustomPinePlayerActivity.this);
                mediaBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (imgUri == null) {
                    mediaBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                } else {
                    ImageLoader.getInstance().displayImage("file://" + imgUri.getPath(),
                            mediaBackgroundView);
                }
                RelativeLayout relativeLayout = new RelativeLayout(SimpleCustomPinePlayerActivity.this);
                relativeLayout.addView(mediaBackgroundView,
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                PineBackgroundViewHolder viewHolder = new PineBackgroundViewHolder();
                viewHolder.setContainer(relativeLayout);
                return viewHolder;
            }

            public PineMediaController.ControllerMonitor onCreateControllerMonitor() {
                return new PineMediaController.ControllerMonitor() {
                    @Override
                    public boolean onFullScreenModeUpdate(boolean isFullScreenMode) {
                        return false;
                    }
                };
            }

            @Override
            protected PineMediaController.ControllersActionListener onCreateControllersActionListener() {
                return new PineMediaController.ControllersActionListener() {
                    @Override
                    public boolean onGoBackBtnClick(View fullScreenBtn,
                                                    PineMediaWidget.IPineMediaPlayer player) {
                        if (player.isFullScreenMode()) {
                            mControllerViewHolder.getFullScreenButton().performClick();
                        } else {
                            finish();
                        }
                        return false;
                    }
                };
            }
        };

        mController.setMediaControllerAdapter(mMediaControllerAdapter);
        mVideoView.setMediaController(mController);
        mPlayer = mVideoView.getMediaPlayer();
        mPlayer.setLocalStreamMode(true);
        mPlayer.setBackgroundPlayerMode(false);
        mPlayer.setMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public boolean onError(int framework_err, int impl_err) {
                return false;
            }
        });

        PineMediaPlayerBean pineMediaBean = new PineMediaPlayerBean(String.valueOf(0), "VideoDefinitionSelect",
                MockDataUtil.getMediaUriSourceList(mBasePath), PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null, null);
        mDefinitionListInPlayerAdapter.setData(pineMediaBean);
        mDefinitionListInPlayerAdapter.notifyDataSetChanged();
        mPlayer.setPlayingMedia(pineMediaBean);
        mPlayer.start();
        if (mDefinitionBtn != null) {
            mDefinitionBtn.setText(getDefinitionName(pineMediaBean.getCurrentDefinition()));
        }
    }

    private boolean hasDefinitionList(PineMediaPlayerBean pineMediaPlayerBean) {
        return pineMediaPlayerBean != null && pineMediaPlayerBean.getMediaUriSourceList().size() > 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayer.resume();
    }

    @Override
    public void onPause() {
        mPlayer.savePlayerState();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        super.onDestroy();
    }

    private void initRecycleView() {
        mDefinitionListContainerInPlayer = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.definition_recycler_view, null);
        mDefinitionListInPlayerRv = mDefinitionListContainerInPlayer
                .findViewById(R.id.definition_recycler_view_in_player);

        // 播放器内置清晰度列表初始化
        // 设置固定大小
        mDefinitionListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager definitionListLlm = new LinearLayoutManager(SimpleCustomPinePlayerActivity.this);
        // 设置垂直方向
        definitionListLlm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mDefinitionListInPlayerRv.setLayoutManager(definitionListLlm);
        // 给RecyclerView添加装饰（比如divider）
        mDefinitionListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(SimpleCustomPinePlayerActivity.this,
                        R.drawable.rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mDefinitionListInPlayerAdapter = new DefinitionListAdapter(mDefinitionListInPlayerRv);
        mDefinitionListInPlayerRv.setAdapter(mDefinitionListInPlayerAdapter);
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
            view = LayoutInflater.from(SimpleCustomPinePlayerActivity.this)
                    .inflate(R.layout.item_definition_select_in_player, parent, false);
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
}
