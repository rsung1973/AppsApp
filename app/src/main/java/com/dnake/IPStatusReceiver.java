package com.dnake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.dnake.utils.EthernetUtils;
import com.dnake.utils.Utils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPStatusReceiver extends BroadcastReceiver {
    private boolean isStart = false;
    private boolean isReloading = false;

    public IPStatusReceiver(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "android.net.ethernet.STATE_CHANGE":
            case "android.net.conn.CONNECTIVITY_CHANGE":
            case "android.net.ethernet.ETHERNET_STATE_CHANGED":
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                if (!Utils.hasValidIP()) {
                    if (!isStart) {
                        isStart = true;
                        if (!isReloading) {
                            isReloading = true;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            dmsg req = new dmsg();
                            dxml p = new dxml();
                            req.to("/apps/web/ethernet/query", null);
                            p.parse(req.mBody);
                            int is_dhcp = p.getInt("/params/dhcp", 0);
                            if (is_dhcp == 1) {
//                                setEthernetConfig(true, "", "", "", "", "");
                                EthernetUtils.setDynamicIp(apps.ctx);
                            } else {
                                String static_ip = p.getText("/params/ip", "");
                                String static_mask = p.getText("/params/mask", "");
                                String static_gateway = p.getText("/params/gateway", "");
                                String static_dns1 = p.getText("/params/dns1", "");
                                String static_dns2 = p.getText("/params/dns2", "");
                                setEthernetConfig(false, static_ip, static_mask, static_gateway, static_dns1, static_dns2);
                            }
                        }
                    }
                } else {
//                    isReloading = false;
                    if (isStart) {
                        isStart = false;
                    }
                }
                break;
        }
    }

    private void setEthernetConfig(boolean isDhcp, String address, String mask, String gate, String dns1, String dns2) {
        if (Utils.ipValidate(address) && Utils.ipValidate(mask) && Utils.ipValidate(gate) && ipMatch(address, mask, gate)) {
            dxml p = new dxml();
            dmsg req = new dmsg();
            p.setInt("/params/dhcp", isDhcp ? 1 : 0);
            if (!isDhcp) {
                p.setText("/params/ip", address);
                p.setText("/params/mask", mask);
                p.setText("/params/gateway", gate);
                p.setText("/params/dns1", dns1);
                p.setText("/params/dns2", dns2);
            }
            req.to("/apps/web/ethernet/setup", p.toString());
        }
    }

    private Boolean ipMatch(String ip, String mask, String gateway) {
        try {
            byte[] _ip = InetAddress.getByName(ip).getAddress();
            byte[] _mask = InetAddress.getByName(mask).getAddress();
            byte[] _gateway = InetAddress.getByName(gateway).getAddress();
            for (int i = 0; i < 4; i++) {
                _ip[i] &= _mask[i];
                _gateway[i] &= _mask[i];
                if (_ip[i] != _gateway[i])
                    return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return true;
    }
}
