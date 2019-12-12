package com.pine.pineplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.ui.activity.autocephaly.AutocephalyMainActivity;
import com.pine.pineplayer.ui.activity.mediaservice.MediaServiceMainActivity;
import com.pine.pineplayer.util.FileUtil;
import com.pine.player.util.LogUtil;

import java.io.File;

/**
 * Created by tanghongfeng on 2018/3/8.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);
    private String mBasePath;
    private boolean mNeedCopyAssets = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mBasePath = getExternalCacheDir().getPath().toString();
        } else {
            mBasePath = getCacheDir().getPath().toString();
        }
        findViewById(R.id.label_tv).setVisibility(View.GONE);
        copyAssets();
    }

    private void copyAssets() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(mBasePath + File.separator + "resource");
                if (!file.exists() || mNeedCopyAssets) {
                    findViewById(R.id.label_tv).setVisibility(View.VISIBLE);
                    FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "resource.zip",
                            mBasePath, true, "GBK");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.label_tv).setVisibility(View.GONE);

                        findViewById(R.id.simple_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, SimplePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.simple_default_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_default_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, DefaultPlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.simple_custom_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_custom_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, CustomPlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.list_default_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.list_default_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, DefaultMediaListPlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.list_custom_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.list_custom_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, CustomMediaListPlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.multi_controller_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.multi_controller_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, MultiControllerSwitchActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.autocephaly_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.autocephaly_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, AutocephalyMainActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.media_service_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.media_service_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, MediaServiceMainActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
