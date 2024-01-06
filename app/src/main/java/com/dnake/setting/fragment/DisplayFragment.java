package com.dnake.setting.fragment;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.utils.DisplayUtils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.widget.SettingsItemLayout;

public class DisplayFragment extends BaseFragment {
    private int sleepIndex = 0;
    private int fontSizeIndex = 0;
    private SettingsItemLayout layoutSleep, layoutFontSize, layoutScreenLock;
    private SeekBar seekbarBrightnessLvl;
    private TextView btnHomeScreen;
    private AlertDialog alertDialog;
    private AlertDialog brightnessSetupDialog;

    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }

    @Override
    protected int getLayoutId() {
        if (!TextUtils.isEmpty(apps.version) && apps.version.contains("902")) {
            return R.layout.fragment_display_902;
        } else {
            return R.layout.fragment_display;
        }
    }

    @Override
    protected void initView() {
        if (DisplayUtils.getScreenMode(mContext) == 1) {
            DisplayUtils.setScreenMode(mContext, 0);
        }
        seekbarBrightnessLvl = (SeekBar) rootView.findViewById(R.id.seekbar_brightness_lvl);
        layoutSleep = (SettingsItemLayout) rootView.findViewById(R.id.layout_sleep);
        layoutFontSize = (SettingsItemLayout) rootView.findViewById(R.id.layout_font_size);
        layoutScreenLock = (SettingsItemLayout) rootView.findViewById(R.id.layout_screen_lock);
        btnHomeScreen = (TextView) rootView.findViewById(R.id.btn_home_screen);
        seekbarBrightnessLvl.setProgress(DisplayUtils.getScreenBrightness(mContext));
        seekbarBrightnessLvl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DisplayUtils.setScreenBrightness(mContext, seekBar.getProgress());
                submitDisplayConf();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
        layoutSleep.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.display_sleep)).setSingleChoiceItems(layoutSleep.getEntriesArr(), sleepIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sleepIndex = which;
                        int millisecond = Integer.parseInt(layoutSleep.getEntryValuesArr()[which]);
                        DisplayUtils.setScreenSleepTime(mContext, millisecond);
                        layoutSleep.getContentTv().setText(layoutSleep.getEntriesArr()[which]);
                        alertDialog.dismiss();
                        submitDisplayConf();
                    }
                }).create();
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                });
                alertDialog.show();
                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        layoutFontSize.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.display_font_size)).setSingleChoiceItems(layoutFontSize.getEntriesArr(), fontSizeIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fontSizeIndex = which;
                        alertDialog.dismiss();
                        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        DisplayUtils.setFontSize(mContext, which);
//                        layoutFontSize.getContentTv().setText(layoutFontSize.getEntriesArr()[which]);
                        initView();
                        initData();
                    }
                }).create();
                alertDialog.show();
                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        layoutScreenLock.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutScreenLock.getSwitchBtn().isChecked()) {
                    layoutScreenLock.getSwitchBtn().setChecked(false);
                } else {
                    layoutScreenLock.getSwitchBtn().setChecked(true);
                }
            }
        });
        btnHomeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent it = new Intent();
//                ComponentName comp = new ComponentName("com.dnake.desktop", "com.dnake.desktop.DesktopActivity");
//                it.setComponent(comp);
//                it.setAction("android.intent.action.VIEW");
//                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(it);
            }
        });
    }

    @Override
    protected void initData() {
        layoutScreenLock.getSwitchBtn().setChecked(sys.display.screen_lock == 1);
        for (int i = 0; i < layoutSleep.getEntryValuesArr().length; i++) {
            if (layoutSleep.getEntryValuesArr()[i].equals(DisplayUtils.getScreenSleepTime(mContext) + "")) {
                sleepIndex = i;
                layoutSleep.getContentTv().setText(layoutSleep.getEntriesArr()[i]);
            }
        }
//        float fScale = getResources().getConfiguration().fontScale;
//        for (int i = 0; i < layoutFontSize.getEntryValuesArr().length; i++) {
//            float tmp = Float.parseFloat(layoutFontSize.getEntryValuesArr()[i]);
//            if (tmp == fScale) {
//                fontSizeIndex = i;
//                layoutFontSize.getContentTv().setText(layoutFontSize.getEntriesArr()[i]);
//            }
//        }
    }

    @Override
    protected void stopView() {
        if (brightnessSetupDialog != null && brightnessSetupDialog.isShowing()) {
            brightnessSetupDialog.dismiss();
            brightnessSetupDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        brightnessSetupDialog = null;
        alertDialog = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (brightnessSetupDialog != null && brightnessSetupDialog.isShowing()) {
            brightnessSetupDialog.dismiss();
            brightnessSetupDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        brightnessSetupDialog = null;
        alertDialog = null;
    }

    private void submitDisplayConf() {
        dxml p = new dxml();
        dmsg req = new dmsg();
        p.setInt("/params/bright", DisplayUtils.getScreenBrightness(mContext));
        p.setInt("/params/sleep", Integer.parseInt(layoutSleep.getEntryValuesArr()[sleepIndex]) / 1000);
        req.to("/ui/web/basic/write", p.toString());
    }
}
