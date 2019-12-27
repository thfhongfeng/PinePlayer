package com.pine.pineplayer.decrytor;

import com.pine.pineplayer.util.IOUtils;
import com.pine.player.decrytor.IPineMediaDecryptor;

import java.nio.ByteBuffer;

/**
 * Created by tanghongfeng on 2017/9/13.
 */

public class PineMediaDecryptor implements IPineMediaDecryptor {
    @Override
    public void decrypt(ByteBuffer byteBuffer, long offset, long size) {
        if (offset < IOUtils.getReverseLength()) {
            long count = IOUtils.getReverseLength() - offset;
            count = count < size ? count : size;
            IOUtils.decrypt(byteBuffer, offset, count);
        }
    }

    @Override
    public void decrypt(byte[] buffer, long offset, long size) {
        if (offset < IOUtils.getReverseLength()) {
            long count = IOUtils.getReverseLength() - offset;
            count = count < size ? count : size;
            IOUtils.decrypt(buffer, offset, count);
        }
    }
}
