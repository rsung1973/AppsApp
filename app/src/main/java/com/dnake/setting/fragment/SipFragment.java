package com.dnake.setting.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.setting.EmojiFilter;
import com.dnake.utils.DisplayUtils;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sound;
import com.dnake.v700.sys;
import com.dnake.widget.SettingsItemLayout;
import com.hjq.toast.ToastUtils;

public class SipFragment extends BaseFragment {

    private SettingsItemLayout layoutSipEnable;
//    private EditText etProxy, etRealm, etUser, etUserId, etDisplayName, etPassword;

    private EditText etUser, etUserId, etDisplayName, etPassword;
    private EditText etServerHost, etServerHostPort, etOutboundProxy, etOutboundProxyPort, etVideoPayload;
    private SettingsItemLayout layoutTransportProtocol;
    private AlertDialog alertDialog;

    private int sipEnable = 0;
    private int mTransportProtocol = 0;
    private int h264_code = 102;
    private String proxyStr = "", proxyPortStr = "", realmStr = "", realmPortStrp = "", userStr = "", userIdStr = "", displayStr = "", passwordStr = "";

    public static SipFragment newInstance() {
        return new SipFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sip;
    }

    @Override
    protected void initView() {
        EmojiFilter filter = new EmojiFilter();
        layoutSipEnable = (SettingsItemLayout) rootView.findViewById(R.id.layout_sip_enable);
//        etProxy = (EditText) rootView.findViewById(R.id.et_outbound_proxy);
//        etRealm = (EditText) rootView.findViewById(R.id.et_realm);
        etUser = (EditText) rootView.findViewById(R.id.et_register_name);
        etUserId = (EditText) rootView.findViewById(R.id.et_user_name);
        etDisplayName = (EditText) rootView.findViewById(R.id.et_display_name);
        etPassword = (EditText) rootView.findViewById(R.id.et_password);

        etServerHost = (EditText) rootView.findViewById(R.id.et_server_host);
        etServerHostPort = (EditText) rootView.findViewById(R.id.et_server_host_port);
        etOutboundProxy = (EditText) rootView.findViewById(R.id.et_outbound_proxy);
        etOutboundProxyPort = (EditText) rootView.findViewById(R.id.et_outbound_proxy_port);
        etVideoPayload = (EditText) rootView.findViewById(R.id.et_video_payload);
        layoutTransportProtocol = (SettingsItemLayout) rootView.findViewById(R.id.layout_transport_protocol);

//        etProxy.setFilters(new InputFilter[]{filter});
//        etRealm.setFilters(new InputFilter[]{filter});
        etServerHost.setFilters(new InputFilter[]{filter});
        etOutboundProxy.setFilters(new InputFilter[]{filter});
        etUser.setFilters(new InputFilter[]{filter});
        etUserId.setFilters(new InputFilter[]{filter});
        etDisplayName.setFilters(new InputFilter[]{filter});
        etPassword.setFilters(new InputFilter[]{filter});

        layoutSipEnable.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutSipEnable.getSwitchBtn().isChecked()) {
                    sipEnable = 0;
                    layoutSipEnable.getSwitchBtn().setChecked(false);
                } else {
                    sipEnable = 1;
                    layoutSipEnable.getSwitchBtn().setChecked(true);
                }
            }
        });
        layoutTransportProtocol.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.voip_transport_protocol)).setSingleChoiceItems(layoutTransportProtocol.getEntriesArr(), mTransportProtocol, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTransportProtocol = which;
                        int millisecond = Integer.parseInt(layoutTransportProtocol.getEntryValuesArr()[which]);
                        DisplayUtils.setScreenSleepTime(mContext, millisecond);
                        layoutTransportProtocol.getContentTv().setText(layoutTransportProtocol.getEntriesArr()[which]);
                        alertDialog.dismiss();
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
    }

    @Override
    protected void initData() {
        doLoad();
    }

    private void doLoad() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        req.to("/ui/web/voip/read", null);
        p.parse(req.mBody);
        sipEnable = p.getInt("/params/enable", 0);
        displayStr = p.getText("/params/display_name");
        realmStr = p.getText("/params/sip_server");
        realmPortStrp = p.getText("/params/sip_port");
        proxyStr = p.getText("/params/outbound", "");
        proxyPortStr = p.getText("/params/outbound_port");
        userIdStr = p.getText("/params/register_name");
        userStr = p.getText("/params/username");
        passwordStr = p.getText("/params/passwd");
        mTransportProtocol = p.getInt("/params/transport", mTransportProtocol);
//        p.getInt("/params/ring_code", 183);//解决支持3CX设备不显示预视频问题的设置项，默认为183 ?????
        h264_code = p.getInt("/params/h264", h264_code);

        layoutSipEnable.getSwitchBtn().setChecked(sipEnable == 1);
        etDisplayName.setText(displayStr);
        etServerHost.setText(realmStr);
        etServerHostPort.setText(realmPortStrp);
        etOutboundProxy.setText(proxyStr.startsWith("sip:") ? proxyStr.replaceFirst("sip:", "") : proxyStr);
        etOutboundProxyPort.setText(proxyPortStr);
        etUser.setText(userIdStr);
        etUserId.setText(userStr);
        etPassword.setText(passwordStr);
        layoutTransportProtocol.getContentTv().setText(layoutTransportProtocol.getEntriesArr()[mTransportProtocol]);
        etVideoPayload.setText(h264_code + "");
    }

    private void doSave() {
        if (TextUtils.isEmpty(etVideoPayload.getText().toString())) {
            sound.play(sound.modify_failed);
        } else {
            int video_payload = Integer.parseInt(etVideoPayload.getText().toString().trim());
            if (video_payload < 96 || video_payload > 127) {
                etVideoPayload.setText("");
                sound.play(sound.modify_failed);
            } else {
//        String proxyStr = etProxy.getText().toString().trim();
//        if (proxyStr.indexOf("sip:") == -1) {
//            proxyStr = "sip:" + proxyStr;
//            etProxy.setText(proxyStr);
//        }
//        realmStr = etRealm.getText().toString().trim();
                String proxyStr = etOutboundProxy.getText().toString().trim() + ":" + etOutboundProxyPort.getText().toString().trim();
                if (proxyStr.indexOf("sip:") == -1) {
                    proxyStr = "sip:" + proxyStr;
                    etOutboundProxy.setText(proxyStr.split(":")[1]);
                    etOutboundProxyPort.setText(proxyStr.split(":")[2]);
                }
                realmStr = etServerHost.getText().toString().trim() + ":" + etServerHostPort.getText().toString().trim();

                passwordStr = etPassword.getText().toString().trim();
                userStr = etUser.getText().toString().trim();
                userIdStr = etUserId.getText().toString().trim();
                displayStr = etDisplayName.getText().toString().trim();

                dmsg req = new dmsg();
                dxml p = new dxml();
                p.setInt("/params/enable", sipEnable);
                p.setText("/params/display_name", displayStr);
                p.setText("/params/sip_server", etServerHost.getText().toString().trim());
                p.setText("/params/sip_port", etServerHostPort.getText().toString().trim());
                p.setText("/params/outbound", etOutboundProxy.getText().toString().trim());
                p.setText("/params/outbound_port", etOutboundProxyPort.getText().toString().trim());
                p.setText("/params/register_name", userStr);
                p.setText("/params/username", userIdStr);
                p.setText("/params/passwd", passwordStr);
                p.setInt("/params/transport", mTransportProtocol);
//        p.setInt("/params/ring_code", 183);//解决支持3CX设备不显示预视频问题的设置项，默认为183 ?????
                p.setInt("/params/h264", Integer.parseInt(etVideoPayload.getText().toString().trim()));
                req.to("/ui/web/voip/write", p.toString());

                sound.play(sound.modify_success);
            }
        }
    }

    @Override
    protected void stopView() {

    }

    @Override
    public void saveCallback() {
        doSave();
    }
}