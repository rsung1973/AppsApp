package com.dnake.setting.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.setting.activity.SettingsActivity;
import com.dnake.utils.Utils;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.login;
import com.dnake.v700.sound;
import com.dnake.widget.Button2;
import com.dnake.widget.LastInputEditText;
import com.dnake.widget.SettingsItemLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EthernetFragment extends BaseFragment {
    private SettingsItemLayout layoutDhcpEnable;
    private LastInputEditText et_ip, et_mask, et_gateway, et_dns, etCmsIP, etPassword;
    private TextView tvCurIp;
    private int dhcpEnable = 0;
    private String cmsIPStr = "", passwordStr = "";

    public static EthernetFragment newInstance() {
        return new EthernetFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ethernet;
    }

    @Override
    protected void initView() {
        layoutDhcpEnable = (SettingsItemLayout) rootView.findViewById(R.id.layout_dhcp_enable);
        et_ip = (LastInputEditText) rootView.findViewById(R.id.et_ip);
        et_mask = (LastInputEditText) rootView.findViewById(R.id.et_mask);
        et_gateway = (LastInputEditText) rootView.findViewById(R.id.et_gateway);
        et_dns = (LastInputEditText) rootView.findViewById(R.id.et_dns);
        etCmsIP = (LastInputEditText) rootView.findViewById(R.id.et_cms_ip);
        etPassword = (LastInputEditText) rootView.findViewById(R.id.et_password);
        tvCurIp = (TextView) rootView.findViewById(R.id.tv_cur_ip);

        layoutDhcpEnable.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutDhcpEnable.getSwitchBtn().isChecked()) {
                    dhcpEnable = 0;
                    layoutDhcpEnable.getSwitchBtn().setChecked(false);
                    et_ip.setEnabled(true);
                    et_mask.setEnabled(true);
                    et_gateway.setEnabled(true);
                    et_dns.setEnabled(true);
                } else {
                    dhcpEnable = 1;
                    layoutDhcpEnable.getSwitchBtn().setChecked(true);
                    et_ip.setEnabled(false);
                    et_mask.setEnabled(false);
                    et_gateway.setEnabled(false);
                    et_dns.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void initData() {
        loadNetwork();
    }

    @Override
    protected void stopView() {
    }

    private void loadNetwork() {
        tvCurIp.setText(Utils.getLocalIp());
        dmsg req = new dmsg();
        dxml p = new dxml();

        req.to("/settings/lan/query", null);
        p.parse(req.mBody);
        dhcpEnable = p.getInt("/params/dhcp", 0);

        et_ip.setText(p.getText("/params/ip"));
        et_mask.setText(p.getText("/params/mask"));
        et_gateway.setText(p.getText("/params/gateway"));
        et_dns.setText(p.getText("/params/dns"));

        if (dhcpEnable == 0) {
            layoutDhcpEnable.getSwitchBtn().setChecked(false);
            et_ip.setEnabled(true);
            et_mask.setEnabled(true);
            et_gateway.setEnabled(true);
            et_dns.setEnabled(true);
        } else {
            layoutDhcpEnable.getSwitchBtn().setChecked(true);
            et_ip.setEnabled(false);
            et_mask.setEnabled(false);
            et_gateway.setEnabled(false);
            et_dns.setEnabled(false);
        }

        p = new dxml();
        req.to("/ui/web/room/read", null);
        p.parse(req.mBody);
        cmsIPStr = p.getText("/params/server", "");
        passwordStr = p.getText("/params/passwd", "");

        etCmsIP.setText(cmsIPStr);
        etPassword.setText(passwordStr);
    }

    private void doSave() {
        if (login.ok()) {
            String ip = et_ip.getText().toString().trim();
            String mask = et_mask.getText().toString().trim();
            String gateway = et_gateway.getText().toString().trim();
            String dns = et_dns.getText().toString().trim();
            cmsIPStr = etCmsIP.getText().toString().trim();
            passwordStr = etPassword.getText().toString().trim();
            if (ipValidate(ip) && ipValidate(mask) && ipValidate(gateway) && ipMatch(ip, mask, gateway) && !(TextUtils.isEmpty(cmsIPStr) || TextUtils.isEmpty(cmsIPStr) || cmsIPStr.length() >= 32)) {
                dxml p = new dxml();
                dmsg req = new dmsg();
                p.setInt("/params/dhcp", layoutDhcpEnable.getSwitchBtn().isChecked() ? 1 : 0);
                p.setText("/params/ip", ip);
                p.setText("/params/mask", mask);
                p.setText("/params/gateway", gateway);
                p.setText("/params/dns", dns);
                req.to("/settings/lan/setup", p.toString());
                p = new dxml();
                p.setText("/params/server", cmsIPStr);
                p.setText("/params/passwd", passwordStr);
                req.to("/ui/web/room/write", p.toString());
                sound.play(sound.modify_success);
            } else sound.play(sound.modify_failed);
        } else {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_login, (ViewGroup) rootView.findViewById(R.id.layout_login));
            EditText login_passwd = (EditText) layout.findViewById(R.id.et_login_pwd);

            AlertDialog.Builder b = new AlertDialog.Builder(mContext);
            b.setView(layout);
            b.setTitle(R.string.login_title);
            b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if (login.passwd(login_passwd.getText().toString())) {
                        login.ok = true;
                        doSave();
                    } else {
                        login.ok = false;
                        sound.play(sound.passwd_err);
                    }
                }
            });
            b.setNegativeButton(R.string.login_passwd_cancel, null);
            AlertDialog ad = b.create();
            ad.setCanceledOnTouchOutside(false);
            ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            });
            ad.show();
            ad.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private Boolean ipValidate(String addr) {
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        if (!addr.matches(REGX_IP)) return false;
        return true;
    }

    private Boolean ipMatch(String ip, String mask, String gateway) {
        try {
            byte[] _ip = InetAddress.getByName(ip).getAddress();
            byte[] _mask = InetAddress.getByName(mask).getAddress();
            byte[] _gateway = InetAddress.getByName(gateway).getAddress();
            for (int i = 0; i < 4; i++) {
                _ip[i] &= _mask[i];
                _gateway[i] &= _mask[i];
                if (_ip[i] != _gateway[i]) return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void saveCallback() {
        doSave();
    }
}
