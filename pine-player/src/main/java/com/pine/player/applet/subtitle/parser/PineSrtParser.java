package com.pine.player.applet.subtitle.parser;

import android.content.Context;

import com.pine.player.util.FileUtils;
import com.pine.player.PineConstants;
import com.pine.player.applet.subtitle.bean.PineSubtitleBean;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanghongfeng on 2017/9/18.
 */

/**
 * SRT外挂字幕解析器
 */
public class PineSrtParser implements IPineSubtitleParser {

    private Context mContext;
    private String mSrtPath;
    private int mSrtPathType;
    private String mCharset;

    public PineSrtParser(Context context, String srtPath, String charset) {
        mContext = context;
        mSrtPath = srtPath;
        mSrtPathType = PineConstants.PATH_STORAGE;
        mCharset = charset;
    }

    public PineSrtParser(Context context, String srtPath, int pathType, String charset) {
        mContext = context;
        mSrtPath = srtPath;
        mSrtPathType = pathType;
        mCharset = charset;
    }

    @Override
    public List<PineSubtitleBean> parse() {
        if (mSrtPath == null && mSrtPath == "") {
            return null;
        }
        InputStream inputStream = null;
        List<PineSubtitleBean> retList = null;
        try {
            switch (mSrtPathType) {
                case PineConstants.PATH_ASSETS:
                    inputStream = mContext.getAssets().open(mSrtPath);
                    break;
                case PineConstants.PATH_STORAGE:
                    inputStream = new FileInputStream(mSrtPath);
                    break;
            }
            if (inputStream == null) {
                return null;
            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,
//                    FileUtils.getTextFileEncode(mContext, mSrtPath, mSrtPathType)));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, mCharset));
            String line = null;
            String[] subtitleBean = new String[3];
            subtitleBean[0] = subtitleBean[1] = subtitleBean[2] = "";
            int indexCount = 0;
            retList = new ArrayList<PineSubtitleBean>();
            PineSubtitleBean item;
            while ((line = br.readLine()) != null) {
                if ("".equals(line)) {
                    if (indexCount > 1) {
                        item = new PineSubtitleBean();
                        item.setOrder(Integer.parseInt(subtitleBean[0]));
                        String timeToTime = subtitleBean[1];
                        int beginHour = Integer.parseInt(timeToTime.substring(0, 2));
                        int beginMinute = Integer.parseInt(timeToTime.substring(3, 5));
                        int beginSecond = Integer.parseInt(timeToTime.substring(6, 8));
                        int beginMilli = Integer.parseInt(timeToTime.substring(9, 12));
                        int beginTime = (beginHour * 3600 + beginMinute * 60 + beginSecond)
                                * 1000 + beginMilli;
                        item.setBeginTime(beginTime);
                        int endHour = Integer.parseInt(timeToTime.substring(17, 19));
                        int endMinute = Integer.parseInt(timeToTime.substring(20, 22));
                        int endSecond = Integer.parseInt(timeToTime.substring(23, 25));
                        int endMilli = Integer.parseInt(timeToTime.substring(26, 29));
                        int endTime = (endHour * 3600 + endMinute * 60 + endSecond)
                                * 1000 + endMilli;
                        item.setEndTime(endTime);
                        item.setTextBody(subtitleBean[2].substring(0, subtitleBean[2].length() - 1));
                        retList.add(item);
                    }
                    subtitleBean[0] = "";
                    subtitleBean[1] = "";
                    subtitleBean[2] = "";
                    indexCount = 0;
                } else {
                    if (indexCount < 2) {
                        subtitleBean[indexCount++] = line;
                    } else {
                        subtitleBean[2] += line;
                        subtitleBean[2] += "\n";
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return retList;
    }
}
