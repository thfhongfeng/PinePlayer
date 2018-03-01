package com.pine.pineplayer.util;

import android.net.Uri;

import com.pine.player.applet.advert.bean.PineAdvertBean;
import com.pine.player.applet.barrage.bean.PineBarrageBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2018/2/5.
 */

public class MockDataUtil {
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

    public static List<PineAdvertBean>  getAllAdvertList(String basePath) {
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
