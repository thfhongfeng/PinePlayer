package com.pine.pineplayer.ui.Activity;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pine.pineplayer.ui.view.AdvanceDecoration;
import com.pine.pineplayer.decrytor.PineMediaDecryptor;
import com.pine.player.applet.subtitle.parser.PineLrcParser;
import com.pine.player.applet.subtitle.parser.PineSrtParser;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.utils.FileUtil;
import com.pine.player.widget.PineMediaWidget;
import com.pine.player.widget.viewholder.PineBackgroundViewHolder;
import com.pine.player.widget.viewholder.PineMediaListViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PinePlayerActivity extends AppCompatActivity {
    private static final String TAG = "PinePlayerActivity";

    private static final int GET_MEDIA_LIST_DONE = 1;

    private PineMediaPlayerView mVideoView;
    private ViewGroup mVideoListContainerInPlayer;
    private RecyclerView mVideoListInPlayerRv;

    private PineMediaController mController;
    private VideoListAdapter mVideoListInPlayerAdapter;

    private int mCurrentVideoPosition = -1;
    private List<PineMediaPlayerBean> mMediaList;
    private Handler mHandler;
    private String mBasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pine_player);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mBasePath = getExternalCacheDir().getPath().toString();
        } else {
            mBasePath = getCacheDir().getPath().toString();
        }

        initHandler();
        mVideoView = (PineMediaPlayerView) findViewById(R.id.video_view);
        mVideoListContainerInPlayer = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.video_recycler_view_full, null);
        mVideoListInPlayerRv = mVideoListContainerInPlayer
                .findViewById(R.id.video_recycler_view_in_player);
        mController = new PineMediaController(this);
        mController.setMediaControllerAdapter(mController.new DefaultMediaControllerAdapter(
                this) {
            @Override
            public PineMediaListViewHolder onCreateFullScreenMediaListViewHolder() {
                PineMediaListViewHolder viewHolder = new PineMediaListViewHolder();
                viewHolder.setContainer(mVideoListContainerInPlayer);
                return viewHolder;
            }

            @Override
            public PineBackgroundViewHolder onCreateBackgroundViewHolder(boolean isFullMode) {
                Uri imgUri = mMediaList.get(mCurrentVideoPosition).getMediaImgUri();
                if (imgUri == null) {
                    return null;
                }
                ImageView mediaBackgroundView = new ImageView(PinePlayerActivity.this);
                mediaBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.getInstance().displayImage("file://" + imgUri.getPath(),
                        mediaBackgroundView);
                RelativeLayout relativeLayout = new RelativeLayout(PinePlayerActivity.this);
                relativeLayout.addView(mediaBackgroundView,
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                PineBackgroundViewHolder viewHolder = new PineBackgroundViewHolder();
                viewHolder.setContainer(relativeLayout);
                return viewHolder;
            }
        });
        mVideoView.setMediaController(mController);
        mVideoView.setLocalStreamMode(true);
        mVideoView.setMediaPlayerListener(new PineMediaWidget.PineMediaPlayerListener() {
            @Override
            public boolean onError(int framework_err, int impl_err) {
                return false;
            }
        });

        mCurrentVideoPosition = 0;
        initVideoRecycleView();
        copyAssets();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.onActivityPaused();
    }

    @Override
    public void onDestroy() {
        mHandler.removeMessages(GET_MEDIA_LIST_DONE);
        super.onDestroy();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GET_MEDIA_LIST_DONE:
                        videoSelected(mCurrentVideoPosition);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void copyAssets() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "video.zip",
                        mBasePath, true, "GBK");
                FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "audio.zip",
                        mBasePath, true, "GBK");
                mHandler.sendEmptyMessage(GET_MEDIA_LIST_DONE);
            }
        }).start();
    }

    private void initMediaList() {
        mMediaList = new ArrayList<PineMediaPlayerBean>();
        PineMediaPlayerBean pineMediaBean;
        int count = 1000;
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaHorizontal",
                Uri.parse(mBasePath + "/video/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaVertical",
                Uri.parse(mBasePath + "/video/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaWebm",
                Uri.parse(mBasePath + "/video/Webm.webm"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioMp3",
                Uri.parse(mBasePath + "/audio/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "EncryptMedia",
                Uri.parse(mBasePath + "/video/Horse_Encrypt.a"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null, null, null,
                new PineMediaDecryptor());
        pineMediaBean.setMediaCode(String.valueOf(count++));
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaHorizontalSubtitle",
                Uri.parse(mBasePath + "/video/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                new PineSrtParser(this, mBasePath + "/video/StarryNight.srt", "UTF-8")
                , null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaVerticalSubtitle",
                Uri.parse(mBasePath + "/video/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null,
                new PineSrtParser(this, mBasePath + "/video/Spout.srt", "UTF-8")
                , null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaAudioLyric",
                Uri.parse(mBasePath + "/audio/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO, null,
                new PineLrcParser(this, mBasePath + "/audio/yesterday once more.lrc", "GBK")
                , null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaAudioImg",
                Uri.parse(mBasePath + "/audio/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(mBasePath + "/audio/HometownScenery.jpg"), null, null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "高铁",
                Uri.parse(mBasePath + "/video/HighSpeedRail.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(mBasePath + "/video/HighSpeedRail.jpg"), null, null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaAudioAll",
                Uri.parse(mBasePath + "/audio/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(mBasePath + "/audio/yesterday once more.jpg"),
                new PineLrcParser(this, mBasePath + "/audio/yesterday once more.lrc", "GBK"),
                null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaVideoAll",
                Uri.parse(mBasePath + "/video/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(mBasePath + "/video/Spout.jpg"),
                new PineSrtParser(this, mBasePath + "/video/Spout.srt", "UTF-8")
                , null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++),
                "MediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongName",
                Uri.parse(mBasePath + "/video/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                null, null, null, null, null);
        mMediaList.add(pineMediaBean);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "MediaNoFile",
                Uri.parse(mBasePath + "/video/MediaNoFile.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                null, null, null, null, null);
        mMediaList.add(pineMediaBean);
    }

    private void initVideoRecycleView() {
        initMediaList();
        // 播放器内置播放列表初始化
        // 设置固定大小
        mVideoListInPlayerRv.setHasFixedSize(true);
        // 创建线性布局管理器
        LinearLayoutManager llm = new LinearLayoutManager(PinePlayerActivity.this);
        // 设置垂直方向
        llm.setOrientation(OrientationHelper.VERTICAL);
        // 给RecyclerView设置布局管理器
        mVideoListInPlayerRv.setLayoutManager(llm);
        // 给RecyclerView添加装饰（比如divider）
        mVideoListInPlayerRv.addItemDecoration(
                new AdvanceDecoration(PinePlayerActivity.this,
                        R.drawable.rv_divider, 2, AdvanceDecoration.VERTICAL, true));
        // 设置适配器
        mVideoListInPlayerAdapter = new VideoListAdapter(mVideoListInPlayerRv);
        mVideoListInPlayerRv.setAdapter(mVideoListInPlayerAdapter);
        mVideoListInPlayerAdapter.setData(mMediaList);
        mVideoListInPlayerAdapter.notifyDataSetChanged();
    }

    private void videoSelected(int position) {
        String path = null;
        String name = "";
        PineMediaPlayerBean pineMediaPlayerBean = null;
        if (position >= 0 && position < mMediaList.size()) {
            pineMediaPlayerBean = mMediaList.get(position);
        } else {
            return;
        }
        mCurrentVideoPosition = position;
        mVideoView.setMedia(pineMediaPlayerBean);
        mVideoView.start();
        if (mVideoListInPlayerAdapter != null) {
            mVideoListInPlayerAdapter.itemSelected(mCurrentVideoPosition);
        }
    }

    private int findPositionInList(String id) {
        if (mMediaList != null && id != null) {
            for (int i = 0; i < mMediaList.size(); i++) {
                String mediaCode = mMediaList.get(i).getMediaCode();
                if (id.equals(mediaCode)) {
                    return i;
                }
            }
        }
        return -1;
    }

    // 自定义RecyclerView的数据Adapter
    class VideoListAdapter extends RecyclerView.Adapter {

        private View mPreSelectedView;
        private List<PineMediaPlayerBean> mData;
        private RecyclerView mRecyclerView;

        public VideoListAdapter(RecyclerView view) {
            this.mRecyclerView = view;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(PinePlayerActivity.this)
                    .inflate(R.layout.item_video_in_player, parent, false);
            // 为RecyclerView的item view设计事件监听机制
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mVideoView != null) {
                        int position = (int) view.getTag();
                        videoSelected((int) view.getTag());
                    }
                }
            });
            VideoViewHolder viewHolder = new VideoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            VideoViewHolder myHolder = (VideoViewHolder) holder;
            PineMediaPlayerBean itemData = mData.get(position);
            if (myHolder.mItemTv != null) {
                myHolder.mItemTv.setText(itemData.getMediaName());
            }
            myHolder.itemView.setTag(position);
            if (mCurrentVideoPosition == position) {
                itemSelected(myHolder.itemView);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<PineMediaPlayerBean> data) {
            this.mData = data;
        }

        private void itemSelected(View item) {
            if (mPreSelectedView != null) {
                mPreSelectedView.setSelected(false);
                TextView preItemText = (TextView) mPreSelectedView
                        .findViewById(R.id.rv_video_item_text);
                preItemText.setSelected(false);
                TextPaint preTextPaint = preItemText.getPaint();
                preTextPaint.setFakeBoldText(false);
            }
            item.setSelected(true);
            TextView itemText = (TextView) item.findViewById(R.id.rv_video_item_text);
            itemText.setSelected(true);
            TextPaint textPaint = itemText.getPaint();
            textPaint.setFakeBoldText(true);
            mPreSelectedView = item;
        }

        public void itemSelected(int position) {
            View item = mRecyclerView.findViewWithTag(position);
            if (item != null) {
                itemSelected(item);
            }
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView mItemTv;
        public ImageView mItemImg;

        public VideoViewHolder(View view) {
            super(view);
            mItemTv = (TextView) view.findViewById(R.id.rv_video_item_text);
        }
    }
}
