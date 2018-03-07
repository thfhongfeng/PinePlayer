package com.pine.pineplayer.util;

import android.content.Context;
import android.net.Uri;

import com.pine.pineplayer.decrytor.PineMediaDecryptor;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.applet.advert.plugin.PineImageAdvertPlugin;
import com.pine.player.applet.barrage.bean.PineBarrageBean;
import com.pine.player.applet.barrage.plugin.PineBarragePlugin;
import com.pine.player.applet.subtitle.plugin.PineLrcParserPlugin;
import com.pine.player.applet.subtitle.plugin.PineSrtParserPlugin;
import com.pine.player.bean.PineMediaPlayerBean;
import com.pine.player.bean.PineMediaUriSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/2/5.
 */

public class MockDataUtil {

    public static List<PineMediaPlayerBean> getMediaList(Context context, String basePath) {
        List<PineMediaPlayerBean> mediaList = new ArrayList<PineMediaPlayerBean>();
        PineMediaPlayerBean pineMediaBean;
        int count = 1000;
        // 横屏视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Horizontal",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mediaList.add(pineMediaBean);
        // 竖屏视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Vertical",
                Uri.parse(basePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mediaList.add(pineMediaBean);
        // Webm格式视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "Webm",
                Uri.parse(basePath + "/resource/Webm.webm"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mediaList.add(pineMediaBean);
        // mp3
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioMp3",
                Uri.parse(basePath + "/resource/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO);
        mediaList.add(pineMediaBean);
        // 加密视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "EncryptMedia",
                Uri.parse(basePath + "/resource/Horse_Encrypt.a"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null,
                new PineMediaDecryptor());
        pineMediaBean.setMediaCode(String.valueOf(count++));
        mediaList.add(pineMediaBean);
        // 横屏+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins1 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins1.add(new PineSrtParserPlugin(context, basePath + "/resource/Scenery.srt", "UTF-8"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "HorizontalSrt",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins1, null);
        mediaList.add(pineMediaBean);
        // 竖屏+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins2 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins2.add(new PineSrtParserPlugin(context, basePath + "/resource/Spout.srt", "UTF-8"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VerticalSrt",
                Uri.parse(basePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins2, null);
        mediaList.add(pineMediaBean);
        // lrc字幕的音频
        List<IPinePlayerPlugin> pinePlayerPlugins3 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins3.add(new PineLrcParserPlugin(context, basePath + "/resource/yesterday once more.lrc", "GBK"));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioLrc",
                Uri.parse(basePath + "/resource/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO, null, pinePlayerPlugins3, null);
        mediaList.add(pineMediaBean);
        // 有背景图的音频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioImg",
                Uri.parse(basePath + "/resource/HometownScenery.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(basePath + "/resource/HometownScenery.jpg"), null, null);
        mediaList.add(pineMediaBean);
        // 有背景图的视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "高铁",
                Uri.parse(basePath + "/resource/HighSpeedRail.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(basePath + "/resource/HighSpeedRail.jpg"), null, null);
        mediaList.add(pineMediaBean);
        // 有暂停图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins4 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins4.add(new PineImageAdvertPlugin(context, MockDataUtil.getPauseAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgPauseAdvert",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins4, null);
        mediaList.add(pineMediaBean);
        // 有开头图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins5 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins5.add(new PineImageAdvertPlugin(context, MockDataUtil.getHeadAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgHeadAdvert",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins5, null);
        mediaList.add(pineMediaBean);
        // 有结尾图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins6 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins6.add(new PineImageAdvertPlugin(context, MockDataUtil.getCompleteAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgCompleteAdvert",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins6, null);
        mediaList.add(pineMediaBean);
        // 有定时图片广告的视频
        List<IPinePlayerPlugin> pinePlayerPlugins7 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins7.add(new PineImageAdvertPlugin(context, MockDataUtil.getTimeAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgTimeAdvert",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins7, null);
        mediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+定时图片广告+srt字幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins8 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins8.add(new PineSrtParserPlugin(context, basePath + "/resource/Scenery.srt", "UTF-8"));
        pinePlayerPlugins8.add(new PineImageAdvertPlugin(context, MockDataUtil.getAllAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "ImgPluginAllSrt",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins8, null);
        mediaList.add(pineMediaBean);
        // 有弹幕的视频
        List<IPinePlayerPlugin> pinePlayerPlugins9 = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPlugins9.add(new PineBarragePlugin(400, MockDataUtil.getBarrageList()));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VideoBarrage",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, pinePlayerPlugins9, null);
        mediaList.add(pineMediaBean);
        // 清晰度选择的视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VideoDefinitionSelect",
                getMediaUriSourceList(basePath), PineMediaPlayerBean.MEDIA_TYPE_VIDEO, null, null, null);
        mediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+lrc字幕+背景图的音频
        List<IPinePlayerPlugin> pinePlayerPluginsAudioAll = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPluginsAudioAll.add(new PineLrcParserPlugin(context, basePath + "/resource/yesterday once more.lrc", "GBK"));
        pinePlayerPluginsAudioAll.add(new PineImageAdvertPlugin(context, MockDataUtil.getAllAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "AudioAll",
                Uri.parse(basePath + "/resource/yesterday once more.mp3"),
                PineMediaPlayerBean.MEDIA_TYPE_AUDIO,
                Uri.parse(basePath + "/resource/yesterday once more.jpg"), pinePlayerPluginsAudioAll, null);
        mediaList.add(pineMediaBean);
        // 开头图片广告+暂停图片广告+结尾图片广告+srt字幕+背景图的视频
        List<IPinePlayerPlugin> pinePlayerPluginsVideoAll = new ArrayList<IPinePlayerPlugin>();
        pinePlayerPluginsVideoAll.add(new PineSrtParserPlugin(context, basePath + "/resource/Spout.srt", "UTF-8"));
        pinePlayerPluginsVideoAll.add(new PineImageAdvertPlugin(context, MockDataUtil.getAllAdvertList(basePath)));
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "VideoAll",
                Uri.parse(basePath + "/resource/Spout.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO,
                Uri.parse(basePath + "/resource/Spout.jpg"), pinePlayerPluginsVideoAll, null);
        mediaList.add(pineMediaBean);
        // 超长名字的视频
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++),
                "MediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongNameMediaLongName",
                Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mediaList.add(pineMediaBean);
        // 空视频（视频文件不存在）
        pineMediaBean = new PineMediaPlayerBean(String.valueOf(count++), "NullFile",
                Uri.parse(basePath + "/resource/MediaNoFile.mp4"),
                PineMediaPlayerBean.MEDIA_TYPE_VIDEO);
        mediaList.add(pineMediaBean);
        return mediaList;
    }

    public static ArrayList<PineMediaUriSource> getMediaUriSourceList(String basePath) {
        ArrayList<PineMediaUriSource> pinePlayerUriSourceList = new ArrayList<PineMediaUriSource>();
        PineMediaUriSource pineMediaUriSource;
        pineMediaUriSource = new PineMediaUriSource(Uri.parse(basePath + "/resource/Scenery_SD.mp4"),
                PineMediaUriSource.MEDIA_DEFINITION_SD);
        pinePlayerUriSourceList.add(pineMediaUriSource);
        pineMediaUriSource = new PineMediaUriSource(Uri.parse(basePath + "/resource/Scenery_HD.mp4"),
                PineMediaUriSource.MEDIA_DEFINITION_HD);
        pinePlayerUriSourceList.add(pineMediaUriSource);
        pineMediaUriSource = new PineMediaUriSource(Uri.parse(basePath + "/resource/Scenery.mp4"),
                PineMediaUriSource.MEDIA_DEFINITION_VHD);
        pinePlayerUriSourceList.add(pineMediaUriSource);
        return pinePlayerUriSourceList;
    }

    public static List<PineBarrageBean> getBarrageList() {
        List<PineBarrageBean> barrageList = new ArrayList<PineBarrageBean>();
        int count = 0;
        PineBarrageBean pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "哈哈", 20);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "还不错", 200);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000,
                "<b><u><font color='RED'>我是HTML格式</font></u></b>", 5000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕1", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕2", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕3", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕4", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕5", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕6", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕7", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000, "同时弹幕8", 6000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 8000,
                "我很长我很长我很长我很长我很长我很长我很长我很长我很长我很长我很长我很长我很长", 8000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 16000,
                "我很慢", 12000);
        barrageList.add(pineBarrageBean);
        pineBarrageBean = new PineBarrageBean(count++, PineBarrageBean.NORMAL_BARRAGE,
                PineBarrageBean.FROM_RIGHT_TO_LEFT, 4000,
                "我很快", 12000);
        barrageList.add(pineBarrageBean);
        return barrageList;
    }

    public static List<PineAdvertBean> getPauseAdvertList(String basePath) {
        List<PineAdvertBean> pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImagePauseAdvertBean = getAdvertBean(1, PineAdvertBean.TYPE_PAUSE,
                PineAdvertBean.CONTENT_IMAGE, true,
                Uri.parse(basePath + "/resource/ImgPauseAdvert.jpg"),
                0, 0);
        pineAdvertBeans.add(pineImagePauseAdvertBean);
        return pineAdvertBeans;
    }

    public static List<PineAdvertBean> getHeadAdvertList(String basePath) {
        List<PineAdvertBean> pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageHeadAdvertBean = getAdvertBean(1, PineAdvertBean.TYPE_HEAD,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(basePath + "/resource/ImgHeadAdvert.jpg"),
                0, 8000);
        pineAdvertBeans.add(pineImageHeadAdvertBean);
        return pineAdvertBeans;
    }

    public static List<PineAdvertBean> getCompleteAdvertList(String basePath) {
        List<PineAdvertBean> pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageCompleteAdvertBean = getAdvertBean(1, PineAdvertBean.TYPE_COMPLETE,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(basePath + "/resource/ImgCompleteAdvert.jpg"),
                0, 8000);
        pineAdvertBeans.add(pineImageCompleteAdvertBean);
        return pineAdvertBeans;
    }

    public static List<PineAdvertBean> getTimeAdvertList(String basePath) {
        List<PineAdvertBean> pineAdvertBeans = new ArrayList<PineAdvertBean>();
        PineAdvertBean pineImageTimeAdvertBean1 = getAdvertBean(1, PineAdvertBean.TYPE_TIME,
                PineAdvertBean.CONTENT_IMAGE, false,
                Uri.parse(basePath + "/resource/ImgTimeAdvert1.jpg"),
                5000, 8000);
        pineAdvertBeans.add(pineImageTimeAdvertBean1);
        PineAdvertBean pineImageTimeAdvertBean2 = getAdvertBean(2, PineAdvertBean.TYPE_TIME,
                PineAdvertBean.CONTENT_IMAGE, true,
                Uri.parse(basePath + "/resource/ImgTimeAdvert2.jpg"),
                20000, 8000);
        pineAdvertBeans.add(pineImageTimeAdvertBean2);
        return pineAdvertBeans;
    }

    public static List<PineAdvertBean> getAllAdvertList(String basePath) {
        List<PineAdvertBean> pineAdvertBeans = new ArrayList<PineAdvertBean>();
        pineAdvertBeans.addAll(getPauseAdvertList(basePath));
        pineAdvertBeans.addAll(getHeadAdvertList(basePath));
        pineAdvertBeans.addAll(getCompleteAdvertList(basePath));
        pineAdvertBeans.addAll(getTimeAdvertList(basePath));
        return pineAdvertBeans;
    }

    private static PineAdvertBean getAdvertBean(int order, int type, int contentType, boolean isRepeat,
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
}
