package com.pine.pineplayer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pine.pineplayer.PinePlayerApp;
import com.pine.pineplayer.R;
import com.pine.pineplayer.util.FileUtil;

/**
 * Created by tanghongfeng on 2018/3/8.
 */

public class MainActivity extends AppCompatActivity {
    private String mBasePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mBasePath = getExternalCacheDir().getPath().toString();
        } else {
            mBasePath = getCacheDir().getPath().toString();
        }
        findViewById(R.id.label_tv).setVisibility(View.VISIBLE);
        copyAssets();
    }

    private void copyAssets() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.unZipAssets(PinePlayerApp.getAppContext(), "resource.zip",
                        mBasePath, true, "GBK");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.label_tv).setVisibility(View.GONE);

                        findViewById(R.id.simple_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, SimplePinePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.simple_default_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_default_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, SimpleDefaultPinePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.simple_custom_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.simple_custom_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, SimpleCustomPinePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.list_default_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.list_default_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, ListDefaultPinePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                        findViewById(R.id.list_custom_tv).setVisibility(View.VISIBLE);
                        findViewById(R.id.list_custom_tv).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, ListCustomPinePlayerActivity.class);
                                intent.putExtra("path", mBasePath);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        }).start();
    }
}
