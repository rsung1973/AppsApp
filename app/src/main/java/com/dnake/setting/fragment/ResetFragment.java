package com.dnake.setting.fragment;

import android.os.RecoverySystem;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.tuya.utils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResetFragment extends BaseFragment {

    private LinearLayout layoutDesc;
    private LinearLayout layoutPopupConfirm;
    private TextView btnReset, btnCancel, btnConfirm;

    public static ResetFragment newInstance() {
        return new ResetFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_reset;
    }

    @Override
    protected void initView() {
        layoutDesc = (LinearLayout) rootView.findViewById(R.id.layout_desc);
        layoutPopupConfirm = (LinearLayout) rootView.findViewById(R.id.layout_popup_confirm);
        btnReset = (TextView) rootView.findViewById(R.id.btn_reset);
        btnCancel = (TextView) rootView.findViewById(R.id.btn_cancel);
        btnConfirm = (TextView) rootView.findViewById(R.id.btn_confirm);
        layoutDesc.setVisibility(View.VISIBLE);
        layoutPopupConfirm.setVisibility(View.GONE);
        btnReset.setVisibility(View.VISIBLE);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDesc.setVisibility(View.GONE);
                layoutPopupConfirm.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.GONE);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDesc.setVisibility(View.VISIBLE);
                layoutPopupConfirm.setVisibility(View.GONE);
                btnReset.setVisibility(View.VISIBLE);
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDevModel();
                utils.deleteDirectory("/dnake/data/tuya");
                doDataClear();
                sys.resetDesktopMain();
                //设置模型model, tuya相关
                setDevModel();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                doReboot();
                Thread thr = new Thread("Reboot") {
                    @Override
                    public void run() {
                        try {
                            RecoverySystem.rebootWipeUserData(getActivity());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thr.start();
            }
        });
    }

    private static List<String> dataFilePathList;

    public static void doDataClear() {
        dataFilePathList = new ArrayList<>();
        dataFilePathList.add("/dnake/data/contact/logger.xml");
        dataFilePathList.add("/dnake/data/avi/logger.xml");
        dataFilePathList.add("/dnake/data/jpeg/logger.xml");
        dataFilePathList.add("/dnake/data/talk/logger.xml");
        dataFilePathList.add("/dnake/data/sys.xml");
        dataFilePathList.add("/dnake/data/security.xml");
        dataFilePathList.add("/dnake/data/tuya_ipcs.xml");
        dataFilePathList.add("/dnake/cfg/sys_locale.xml");
        dataFilePathList.add("/dnake/cfg/setup.xml");
        dataFilePathList.add("/dnake/cfg/sys.xml");
        dataFilePathList.add("/dnake/cfg/date_ntp.xml");
        dataFilePathList.add("/dnake/cfg/desktop_bg_path");
        dataFilePathList.add("/dnake/cfg/ipc.xml");
        dataFilePathList.add("/dnake/cfg/security.xml");
        dataFilePathList.add("/dnake/cfg/unlock_set.xml");
        dataFilePathList.add("/dnake/cfg/third_app_num.xml");
        dataFilePathList.add("/dnake/cfg/is_brightness_default");
        dataFilePathList.add("/dnake/cfg/apps.xml");
        dataFilePathList.add("/dnake/cfg/sys_lan.xml");
        dataFilePathList.add("/dnake/data/text/logger.xml");

        for (String path : dataFilePathList) {
            clearFileData(path);
        }
        sys.saveTuya();
//        dmsg req = new dmsg();
//        dxml p = new dxml();
//        p.setInt("/params/dhcp", 0);
//        p.setText("/params/ip", "192.168.68.90");
//        p.setText("/params/mask", "255.255.255.0");
//        p.setText("/params/gateway", "192.168.68.1");
//        p.setText("/params/dns", "8.8.8.8");
//        req.to("/settings/lan/setup", p.toString());
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void stopView() {
        layoutDesc.setVisibility(View.VISIBLE);
        layoutPopupConfirm.setVisibility(View.GONE);
        btnReset.setVisibility(View.VISIBLE);
    }

    private void doReboot() {
        try {
            FileOutputStream out = new FileOutputStream("/var/etc/watchdog");
            String s = "1";
            out.write(s.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearFileData(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDevModel() {
        dxml p = new dxml();
        boolean result = p.load("/dnake/cfg/sys.xml");
        if (result) {
            sys.tuya.TUYA_UUID = p.getText("/sys/tuya/uuid", sys.tuya.TUYA_UUID);
            sys.tuya.TUYA_AUTHKEY = p.getText("/sys/tuya/key", sys.tuya.TUYA_AUTHKEY);
            sys.tuya.qr = p.getText("/sys/tuya/qr", sys.tuya.qr);
            sys.special_advanced.model = p.getText("/sys/special_advanced/model", "Android Indoor Monitor");
        } else {
            sys.special_advanced.model = "Android Indoor Monitor";
        }
    }

    private static void setDevModel() {
        dxml p = new dxml();
        p.setText("/sys/tuya/uuid", sys.tuya.TUYA_UUID);
        p.setText("/sys/tuya/key", sys.tuya.TUYA_AUTHKEY);
        p.setText("/sys/tuya/qr", sys.tuya.qr);
        p.setText("/sys/special_advanced/model", sys.special_advanced.model);
        p.save("/dnake/cfg/sys.xml");
    }
}
