package com.dnake.setting.fragment;

import android.util.Log;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.utils.EthernetUtils;
import com.dnake.utils.SoundUtils;
import com.dnake.utils.Utils;
import com.dnake.utils.WifiUtils;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.widget.SettingsItemLayout;

import java.net.SocketException;

public class VersionFragment extends BaseFragment {

    private SettingsItemLayout layoutFW, layoutUI, layoutIP, layoutWifiIp, layoutMAC, layoutSipStatus;

    public static VersionFragment newInstance() {
        return new VersionFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_version;
    }

    @Override
    protected void initView() {
        layoutFW = (SettingsItemLayout) rootView.findViewById(R.id.layout_fw);
        layoutUI = (SettingsItemLayout) rootView.findViewById(R.id.layout_ui);
        layoutIP = (SettingsItemLayout) rootView.findViewById(R.id.layout_ip);
        layoutWifiIp = (SettingsItemLayout) rootView.findViewById(R.id.layout_wifi_ip);
        layoutMAC = (SettingsItemLayout) rootView.findViewById(R.id.layout_mac);
        layoutSipStatus = (SettingsItemLayout) rootView.findViewById(R.id.layout_sip_status);
    }

    @Override
    protected void initData() {
        dmsg req = new dmsg();
        try {
//            if (req.to("/talk/version", null) == 200) {
//                dxml p = new dxml();
//                p.parse(req.mBody);
//                layoutFW.getContentTv().setText(p.getText("/params/version"));
//            }
            if (req.to("/ui/version", null) == 200) {
                dxml p = new dxml();
                p.parse(req.mBody);
                layoutFW.getContentTv().setText(p.getText("/params/version"));
                layoutSipStatus.getContentTv().setText(p.getText("/params/proxy").equals("1") ? "Enabled" : "Disabled");
            }
            layoutIP.getContentTv().setText(EthernetUtils.getIpAddress("eth0"));
            WifiUtils mWifiUtils = new WifiUtils(mContext);
            layoutWifiIp.getContentTv().setText(mWifiUtils.getCurWifiIpAddress(mContext));
            layoutMAC.getContentTv().setText(Utils.getEth0Mac());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stopView() {
    }
}
