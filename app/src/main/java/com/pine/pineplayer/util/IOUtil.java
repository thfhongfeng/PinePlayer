package com.pine.pineplayer.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by tanghongfeng on 2017/9/13.
 */

public class IOUtil {
    private final static int REVERSE_LENGTH = 100;

    /**
     * 加解密
     *
     * @param strFile 源文件绝对路径
     * @return
     */
    public static boolean encrypt(String strFile) {
        int len = REVERSE_LENGTH;
        try {
            File f = new File(strFile);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLen = raf.length();
            if (totalLen < REVERSE_LENGTH)
                len = (int) totalLen;
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, REVERSE_LENGTH);
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = buffer.get(i);
                tmp = (byte) (rawByte ^ i);
                buffer.put(i, tmp);
            }
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getReverseLength() {
        return REVERSE_LENGTH;
    }

    /**
     * 解密
     *
     * @param strFileChange
     * @return
     */
    public static Boolean decrypt(ByteBuffer strFileChange, long start, long count) {
        byte[] buffer = strFileChange.array();
        long len = count < buffer.length ? count : buffer.length;
        try {
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = strFileChange.get(i);
                tmp = (byte) (rawByte ^ start);
                strFileChange.put(i, tmp);
                start++;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解密
     *
     * @param strFileChange
     * @return
     */
    public static Boolean decrypt(byte[] strFileChange, long start, long count) {
        if (strFileChange.length < REVERSE_LENGTH) {
            return false;
        }
        long len = count < strFileChange.length ? count : strFileChange.length;
        try {
            byte tmp;
            for (int i = 0; i < len; ++i) {
                byte rawByte = strFileChange[i];
                tmp = (byte) (rawByte ^ start);
                strFileChange[i] = tmp;
                start++;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
