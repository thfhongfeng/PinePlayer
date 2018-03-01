package com.pine.player.util;

import android.util.Log;

/**
 * Created by tanghongfeng on 2018/3/1.
 */

public class LogUtils {
    //各个Log级别定义的值，级别越高值越大
    /*
        public static final int VERBOSE = 2;
        public static final int DEBUG = 3;
        public static final int INFO = 4;
        public static final int WARN = 5;
        public static final int ERROR = 6;
        public static final int ASSERT = 7;
    */
    private static final boolean DEBUG = true;
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(Class clz) {
        String str = clz.getSimpleName();
        if (str.length() > MAX_LOG_TAG_LENGTH) {
        return str.substring(0, MAX_LOG_TAG_LENGTH - 1);
    }
        return str;
}

    public static void v(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable cause) {
        if (DEBUG || Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, msg, cause);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable cause) {
        if (DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg, cause);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable cause) {
        if (DEBUG || Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, msg, cause);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable cause) {
        if (DEBUG || Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, msg, cause);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG || Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable cause) {
        if (DEBUG || Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg, cause);
        }
    }
}
