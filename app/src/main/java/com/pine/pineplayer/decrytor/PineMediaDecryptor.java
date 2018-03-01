package com.pine.pineplayer.decrytor;

import com.pine.pineplayer.util.IOUtil;
import com.pine.player.decrytor.IPineMediaDecryptor;

import java.nio.ByteBuffer;

/**
 * Created by tanghongfeng on 2017/9/13.
 */

public class PineMediaDecryptor implements IPineMediaDecryptor {
    @Override
    public void decrypt(ByteBuffer byteBuffer, long offset, long size) {
        if (offset < IOUtil.getReverseLength()) {
            long count = IOUtil.getReverseLength() - offset;
            count = count < size ? count : size;
            IOUtil.decrypt(byteBuffer, offset, count);
        }
    }

    @Override
    public void decrypt(byte[] buffer, long offset, long size) {
        if (offset < IOUtil.getReverseLength()) {
            long count = IOUtil.getReverseLength() - offset;
            count = count < size ? count : size;
            IOUtil.decrypt(buffer, offset, count);
        }
    }
}
