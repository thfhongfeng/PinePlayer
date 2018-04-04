package com.pine.player.service;

import com.pine.player.decrytor.IPineMediaDecryptor;

/**
 * Created by tanghongfeng on 2018/4/4.
 */

public interface IPineMediaSocketService {
    void setPlayerDecryptor(IPineMediaDecryptor pinePlayerDecryptor);
}
