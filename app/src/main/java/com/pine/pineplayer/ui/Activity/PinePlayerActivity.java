package com.pine.pineplayer.ui.Activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.decrytor.PineMediaDecryptor;
import com.pine.pineplayer.ui.view.AdvanceDecoration;
import com.pine.pineplayer.utils.FileUtil;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.applet.advert.plugin.PineAdvertPlugin;
import com.pine.player.applet.advert.plugin.PineImageAdvertPlugin;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.applet.subtitle.plugin.PineSrtParserPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.widget.PineMediaController;
import com.pine.player.widget.PineMediaPlayerView;
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
    private RecyclerView mVideoListInDetailRv;

    private PineMediaController mController;
    private VideoListAdapter mVideoListInPlayerAdapter;
    private VideoListAdapter mVideoListInDetailAdapter;

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
        mVideoListInDetailRv = (RecyclerView) findViewById(R.id.media_list_rv);
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
                ImageView mediaBackgroundView = new ImageView(PinePlayerActivity.this);
                mediaBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (imgUri == null) {
                    mediaBackgroundView.setBackgroundResource(android.R.color.darker_gray);
                } else {
                    ImageLoader.getInstance().displayImage("file://" + imgUri.getPath(),
                            mediaBackgroundView);
                }
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
        mVideoView.savePlayerState();
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
                FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "resource.zip",
                        mBasePath, true, "GBK");
                mHandler.sendEmptyMessage(GET_MEDIA_LIST_DONE);
            }
        }).start();
    }

    private void initMediaList() {
        mMediaList = new ArrayList<PineMediaPlayerBean>();
        PineMediaPlayerBean pineMediaBean;
        int count = 1000;
        // 横屏视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Horizontal",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        // 竖屏视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Vertical",
                Uri.parse(mBasePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        // Webm格式视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Webm",
                Uri.parse(mBasePath + "/resource/Webm.webm"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        // mp3
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioMp3",
                Uri.parse(mBasePath + "/resource/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO);
        mMediaList.add(pineMediaBean);
        // 加密视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "EncryptMedia",
                Uri.parse(mBasePath + "/resource/Horse_Encrypt.a"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null,
                new PineMediaDecryptor());
        pineMediaBean.setMediaCode(String.valueOf(count++));
        mMediaList.add(pineMediaBean);
        // 横屏+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins1 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins1.add(new PineSrtParserPlugin(this, mBasePath + "/resource/StarryNight.srt", "UTF-8"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "HorizontalSrt",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins1, null);
        mMediaList.add(pineMediaBean);
        // 竖屏+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins2 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins2.add(new PineSrtParserPlugin(this, mBasePath + "/resource/Spout.srt", "UTF-8"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VerticalSrt",
                Uri.parse(mBasePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins2, null);
        mMediaList.add(pineMediaBean);
        // lrc字幕的音频
        List<IPinePlayerPlugin> pinePlayerPlugins3 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins3.add(new PineLrcParserPlugin(this, mBasePath + "/resource/yesterday once more.lrc", "GBK"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioLrc",
                Uri.parse(mBasePath + "/resource/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO, null, pinePlayerPlugins3, null);
        mMediaList.add(pineMediaBean);
        // 有背景图的音频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioImg",
                Uri.parse(mBasePath + "/resource/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(mBasePath + "/resource/HometownScenery.jpg"), null, null);
        mMediaList.add(pineMediaBean);
        // 有背景图的视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "高铁",
                Uri.parse(mBasePath + "/resource/HighSpeedRail.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(mBasePath + "/resource/HighSpeedRail.jpg"), null, null);
        mMediaList.add(pineMediaBean);

        List<PineAdvertBean> pineAdvertBeans;
        List<PineAdvertBean> pineAllImgAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertPlugin pineAllImgAdvertPlugin;
        // 暂停图片广告
        pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImagePauseAdvertBean = getAdvertBean(1, PineAdvertBean.TYPE_PAUSE,
                PineAdvertBean.CONTENT_IMAGE, true,
                Uri.parse(mBasePath + "/resource/ImgPauseAdvert.jpg"),
                0, 0);
        pineAdvertBeans.add(pineImagePauseAdvertBean);
        PineAdvertPlugin pineImagePauseAdvertPlugin = new PineImageAdvertPlugin(this, pineAdvertBeans);
        pineAllImgAdvertBeans.add(pineImagePauseAdvertBean);
        // 开头图片广告
        pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageHeadAdvertBean = getAdvertBean(2, PineAdvertBean.TYPE_HEAD,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(mBasePath + "/resource/ImgHeadAdvert.jpg"),
                0, 8000);
        pineAdvertBeans.add(pineImageHeadAdvertBean);
        PineAdvertPlugin pineImageHeadAdvertPlugin = new PineImageAdvertPlugin(this, pineAdvertBeans);
        pineAllImgAdvertBeans.add(pineImageHeadAdvertBean);
        // 结尾图片广告
        pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageCompleteAdvertBean = getAdvertBean(3, PineAdvertBean.TYPE_COMPLETE,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(mBasePath + "/resource/ImgCompleteAdvert.jpg"),
                0, 8000);
        pineAdvertBeans.add(pineImageCompleteAdvertBean);
        PineAdvertPlugin pineImageCompleteAdvertPlugin = new PineImageAdvertPlugin(this, pineAdvertBeans);
        pineAllImgAdvertBeans.add(pineImageCompleteAdvertBean);
        // 定时图片广告
        pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageTimeAdvertBean1 = getAdvertBean(4, PineAdvertBean.TYPE_TIME,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(mBasePath + "/resource/ImgTimeAdvert1.jpg"),
                5000, 8000);
        pineAdvertBeans.add(pineImageTimeAdvertBean1);
        pineAllImgAdvertBeans.add(pineImageTimeAdvertBean1);
        PineAdvertBean pineImageTimeAdvertBean2 = getAdvertBean(5, PineAdvertBean.TYPE_TIME,
                PineAdvertBean.CONTENT_IMAGE, true,
                Uri.parse(mBasePath + "/resource/ImgTimeAdvert2.jpg"),
                20000, 8000);
        pineAdvertBeans.add(pineImageTimeAdvertBean2);
        pineAllImgAdvertBeans.add(pineImageTimeAdvertBean2);
        PineAdvertPlugin pineImageTimeAdvertPlugin = new PineImageAdvertPlugin(this, pineAdvertBeans);

        pineAllImgAdvertPlugin = new PineImageAdvertPlugin(this, pineAllImgAdvertBeans);

        // 有暂停图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins4 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins4.add(pineImagePauseAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgPauseAdvert",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins4, null);
        mMediaList.add(pineMediaBean);
        // 有开头图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins5 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins5.add(pineImageHeadAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgHeadAdvert",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins5, null);
        mMediaList.add(pineMediaBean);
        // 有结尾图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins6 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins6.add(pineImageCompleteAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgCompleteAdvert",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins6, null);
        mMediaList.add(pineMediaBean);
        // 有定时图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins7 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins7.add(pineImageTimeAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgTimeAdvert",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins7, null);
        mMediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+定时图片广告+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins8 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins8.add(new PineSrtParserPlugin(this, mBasePath + "/resource/StarryNight.srt", "UTF-8"));
        pinePlayerPlugins8.add(pineAllImgAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgPluginAllSrt",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins8, null);
        mMediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+lrc字幕+背景图的音频
        List<IPinePlayerPlugin> pinePlayerPluginsAudioAll = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPluginsAudioAll.add(new PineLrcParserPlugin(this, mBasePath + "/resource/yesterday once more.lrc", "GBK"));
        pinePlayerPluginsAudioAll.add(pineAllImgAdvertPlugin);
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioAll",
                Uri.parse(mBasePath + "/resource/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(mBasePath + "/resource/yesterday once more.jpg"), pinePlayerPluginsAudioAll, null);
        mMediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+srt字幕+背景图的视频
        List<IPinePlayerPlugin> pinePlayerPluginsVideoAll = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPluginsVideoAll.add(new PineSrtParserPlugin(this, mBasePath + "/resource/Spout.srt", "UTF-8"));
        pinePlayerPluginsVideoAll.add(pineAllImgAdvertPlugin);
        ;
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VideoAll",
                Uri.parse(mBasePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(mBasePath + "/resource/Spout.jpg"), pinePlayerPluginsVideoAll, null);
        mMediaList.add(pineMediaBean);
        // 超长名字的视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++),
                "MediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongName",
                Uri.parse(mBasePath + "/resource/StarryNight.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
        // 空视频（视频文件不存在）
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "NullFile",
                Uri.parse(mBasePath + "/resource/MediaNoFile.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mMediaList.add(pineMediaBean);
    }

    private PineAdvertBean getAdvertBean(int order, int type, int contentType, boolean isRepeat,
                                         Uri uri, int time, int duration) {
        PineAdvertBean pinePauseAdvertBean = new PineAdvertBean();
        pinePauseAdvertBean.setOrder(order);
        pinePauseAdvertBean.setType(type);
        pinePauseAdvertBean.setContentType(contentType);
        pinePauseAdvertBean.setRepeat(isRepeat);
        pinePauseAdvertBean.setUri(uri);
        pinePauseAdvertBean.setDurationTime(duration);
        pinePauseAdvertBean.setPositionTime(time);
        return pinePauseAdvertBean;
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
        mVideoListInPlayerAdapter = new VideoListAdapter(mVideoListInPlayerRv,
                VideoListAdapter.LIST_IN_PLAYER);
        mVideoListInPlayerRv.setAdapter(mVideoListInPlayerAdapter);
        mVideoListInPlayerAdapter.setData(mMediaList);
        mVideoListInPlayerAdapter.notifyDataSetChanged();

        mVideoListInDetailRv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mVideoListInDetailRv.setLayoutManager(llm);
        mVideoListInDetailAdapter = new VideoListAdapter(mVideoListInDetailRv,
                VideoListAdapter.LIST_IN_DETAIL);
        mVideoListInDetailRv.setAdapter(mVideoListInDetailAdapter);
        mVideoListInDetailAdapter.setData(mMediaList);
        mVideoListInDetailAdapter.notifyDataSetChanged();
        mVideoListInDetailRv.setFocusable(false);
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
        private static final int LIST_IN_PLAYER = 1;
        private static final int LIST_IN_DETAIL = 2;
        private int mListType;
        private VideoViewHolder mPreSelectedViewHolder;
        private List<PineMediaPlayerBean> mData;
        private RecyclerView mRecyclerView;

        public VideoListAdapter(RecyclerView view, int type) {
            this.mRecyclerView = view;
            this.mListType = type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (mListType) {
                case LIST_IN_DETAIL:
                    view = LayoutInflater.from(PinePlayerActivity.this)
                            .inflate(R.layout.item_video_in_detail, parent, false);
                    break;
                default:
                    view = LayoutInflater.from(PinePlayerActivity.this)
                            .inflate(R.layout.item_video_in_player, parent, false);
                    break;
            }
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
            if (myHolder.mItemImg != null) {
                myHolder.mItemImg.setSelected(isSelected);
            }
            myHolder.mItemTv.setSelected(isSelected);
            myHolder.mTextPaint.setFakeBoldText(isSelected);
            if (isSelected) {
                mPreSelectedViewHolder = myHolder;
            }
            // 为RecyclerView的item view设计事件监听机制
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mPreSelectedViewHolder != null) {
                        mPreSelectedViewHolder.itemView.setSelected(false);
                        if (mPreSelectedViewHolder.mItemImg != null) {
                            mPreSelectedViewHolder.mItemImg.setSelected(false);
                        }
                        mPreSelectedViewHolder.mItemTv.setSelected(false);
                        mPreSelectedViewHolder.mTextPaint.setFakeBoldText(false);
                    }
                    mPreSelectedViewHolder = myHolder;
                    myHolder.itemView.setSelected(true);
                    if (myHolder.mItemImg != null) {
                        myHolder.mItemImg.setSelected(true);
                    }
                    myHolder.mItemTv.setSelected(true);
                    myHolder.mTextPaint.setFakeBoldText(true);
                    videoSelected(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<PineMediaPlayerBean> data) {
            this.mData = data;
        }
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    class VideoViewHolder extends RecyclerView.ViewHolder {
        public ImageView mItemImg;
        public TextView mItemTv;
        public TextPaint mTextPaint;

        public VideoViewHolder(View view) {
            super(view);
            mItemImg = (ImageView) view.findViewById(R.id.rv_video_item_img);
            mItemTv = (TextView) view.findViewById(R.id.rv_video_item_text);
            mTextPaint = mItemTv.getPaint();
        }
    }
}
