package com.pine.pineplayer.applet;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pine.pineplayer.R;
import com.pine.player.applet.IPinePlayerPlugin;
import com.pine.player.component.PineMediaWidget;
import com.pine.player.widget.viewholder.PinePluginViewHolder;

import java.util.List;

public class PlayerSpeedPlugin implements IPinePlayerPlugin {
    private TextView speed_tv1;
    private TextView speed_tv2;
    private TextView speed_tv3;
    private TextView speed_tv4;
    private TextView speed_tv5;
    private TextView speed_x_1;
    private TextView speed_x_2;
    private TextView speed_x_3;
    private TextView speed_x_4;
    private TextView speed_x_5;

    private Context mContext;
    private PineMediaWidget.IPineMediaPlayer mPlayer;
    private PinePluginViewHolder mFullPluginViewHolder, mPluginViewHolder, mCurViewHolder;
    private boolean mIsOpen = true;

    @Override
    public PinePluginViewHolder createViewHolder(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            if (mFullPluginViewHolder == null) {
                mFullPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.player_speed_plugin_full, null);
                mFullPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mFullPluginViewHolder;
        } else {
            if (mPluginViewHolder == null) {
                mPluginViewHolder = new PinePluginViewHolder();
                ViewGroup view = (ViewGroup) View.inflate(context,
                        R.layout.player_speed_plugin, null);
                mPluginViewHolder.setContainer(view);
            }
            mCurViewHolder = mPluginViewHolder;
        }
        initSpeedView();
        closePlugin();
        return mCurViewHolder;
    }

    private void initSpeedView() {
        View container = mCurViewHolder.getContainer();
        speed_tv1 = container.findViewById(R.id.speed_tv1);
        speed_tv2 = container.findViewById(R.id.speed_tv2);
        speed_tv3 = container.findViewById(R.id.speed_tv3);
        speed_tv4 = container.findViewById(R.id.speed_tv4);
        speed_tv5 = container.findViewById(R.id.speed_tv5);
        speed_x_1 = container.findViewById(R.id.speed_x_1);
        speed_x_2 = container.findViewById(R.id.speed_x_2);
        speed_x_3 = container.findViewById(R.id.speed_x_3);
        speed_x_4 = container.findViewById(R.id.speed_x_4);
        speed_x_5 = container.findViewById(R.id.speed_x_5);
        container.findViewById(R.id.speed_btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setSpeed(0.5f);
                setSpeedBtnState();
            }
        });
        container.findViewById(R.id.speed_btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setSpeed(1f);
                setSpeedBtnState();
            }
        });
        container.findViewById(R.id.speed_btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setSpeed(1.2f);
                setSpeedBtnState();
            }
        });
        container.findViewById(R.id.speed_btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setSpeed(1.5f);
                setSpeedBtnState();
            }
        });
        container.findViewById(R.id.speed_btn5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setSpeed(2f);
                setSpeedBtnState();
            }
        });
    }

    private void setSpeedBtnState() {
        float speed = mPlayer != null ? mPlayer.getSpeed() : 1;
        speed_tv1.setTextColor(speed == 0.5f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_tv2.setTextColor(speed == 1f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_tv3.setTextColor(speed == 1.2f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_tv4.setTextColor(speed == 1.5f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_tv5.setTextColor(speed == 2f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_x_1.setTextColor(speed == 0.5f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_x_2.setTextColor(speed == 1f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_x_3.setTextColor(speed == 1.2f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_x_4.setTextColor(speed == 1.5f ? Color.parseColor("#2895E1") : Color.WHITE);
        speed_x_5.setTextColor(speed == 2f ? Color.parseColor("#2895E1") : Color.WHITE);
    }

    @Override
    public void onInit(Context context, PineMediaWidget.IPineMediaPlayer player,
                       PineMediaWidget.IPineMediaController controller, boolean isPlayerReset, boolean isResumeState) {
        mContext = context;
        mPlayer = player;
        setSpeedBtnState();
    }

    @Override
    public void setData(List data) {

    }

    @Override
    public void addData(List data) {

    }

    @Override
    public int getContainerType() {
        return IPinePlayerPlugin.TYPE_MATCH_CONTROLLER;
    }

    @Override
    public void onMediaPlayerPrepared() {

    }

    @Override
    public void onMediaPlayerStart() {

    }

    @Override
    public void onMediaPlayerInfo(int what, int extra) {

    }

    @Override
    public void onMediaPlayerPause() {

    }

    @Override
    public void onMediaPlayerComplete() {

    }

    @Override
    public void onAbnormalComplete() {

    }

    @Override
    public void onMediaPlayerError(int framework_err, int impl_err) {

    }

    @Override
    public void onTime(long position) {

    }

    @Override
    public void onRelease() {
        closePlugin();
    }

    @Override
    public void openPlugin() {
        mIsOpen = true;
        mCurViewHolder.getContainer().setVisibility(View.VISIBLE);
    }

    @Override
    public void closePlugin() {
        mIsOpen = false;
        mCurViewHolder.getContainer().setVisibility(View.GONE);
    }

    @Override
    public boolean isOpen() {
        return mIsOpen;
    }
}
