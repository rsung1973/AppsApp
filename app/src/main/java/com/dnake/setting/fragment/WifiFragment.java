package com.dnake.setting.fragment;

import static android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.setting.adapter.WifiAdapter;
import com.dnake.utils.WifiUtils;
import com.dnake.widget.SettingsItemLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiFragment extends BaseFragment implements WifiAdapter.Callback {
    private SettingsItemLayout layoutWifiEnable;
    private RecyclerView rvWifi;
    private WifiAdapter mAdapter;
    private WifiUtils mWifiUtils;

    private List<ScanResult> mWifiList = new ArrayList<>();

    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wifi;
    }

    @Override
    protected void initView() {
        mWifiUtils = new WifiUtils(mContext);
        layoutWifiEnable = (SettingsItemLayout) rootView.findViewById(R.id.layout_wifi_enable);
        rvWifi = (RecyclerView) rootView.findViewById(R.id.rv_wifi);
        rvWifi.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new WifiAdapter(mContext);
        mAdapter.setCallback(this);
        rvWifi.setAdapter(mAdapter);

        layoutWifiEnable.getSwitchBtn().setChecked(mWifiUtils.isWifiEnable());
        layoutWifiEnable.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutWifiEnable.getSwitchBtn().isChecked()) {
                    layoutWifiEnable.getSwitchBtn().setChecked(false);
                    mWifiUtils.closeWifi();
                    mWifiList.clear();
                    mAdapter.setmDatas(mWifiList);
                    mAdapter.setConnectedIds(new ArrayList<>());
                    mAdapter.notifyDataSetChanged();

                } else {
                    layoutWifiEnable.getSwitchBtn().setChecked(true);
                    layoutWifiEnable.getProgressBar().setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWifiUtils.openWifi();
                            mWifiUtils.startScan();
                        }
                    }, 500);
                }
            }
        });

        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerWifiReceiver(wifiFilter);
    }

    @Override
    protected void initData() {
        if (mWifiUtils.isWifiEnable()) {
            layoutWifiEnable.getProgressBar().setVisibility(View.VISIBLE);
            mWifiUtils.startScan();
        }
    }

    @Override
    protected void stopView() {

    }

    @Override
    public void onWifiConnect(int index) {
        popupConnectWifiDialog(mWifiList.get(index));
    }

    @Override
    public void onEnterDetail(int index) {
        popupWifiDetail(mWifiList.get(index));
    }

    WifiBroadCastReceiver wifiReceiver;

    public void registerWifiReceiver(IntentFilter intentFilter) {
        if (null == wifiReceiver) {
            wifiReceiver = new WifiBroadCastReceiver();
        }
        mContext.registerReceiver(wifiReceiver, intentFilter);
    }

    class WifiBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            break;
                    }
//                    refreshLocalWifiListData();
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    refreshLocalWifiListData();
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    refreshLocalWifiListData();
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//                    NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
                    int error = intent.getIntExtra(EXTRA_SUPPLICANT_ERROR, 0);
                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        String ssid = getCurentWifiSSID();
//                        mWifiUtils.disconnectCurrentWifi();
                        if (mWifiUtils.getConfigFromConfiguredNetworksBySsid(ssid) != null) {
                            mWifiUtils.disconnectWifi(mWifiUtils.getConfigFromConfiguredNetworksBySsid(ssid).networkId);
                        }
                        mAdapter.setConnectedIds(new ArrayList<>());
                        mAdapter.notifyDataSetChanged();
                        layoutWifiEnable.getProgressBar().setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    //判断当前是否已经连接
    public boolean isGivenWifiConnect(String SSID) {
        return isWifiConnected(mContext) && getCurentWifiSSID().equals(SSID);
    }

    //得到当前连接的WiFi  SSID
    public String getCurentWifiSSID() {
        String ssid = "";
        ssid = mWifiUtils.getConnectionInfo().getSSID();
        if (ssid.substring(0, 1).equals("\"") && ssid.substring(ssid.length() - 1).equals("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * 是否处于wifi连接的状态
     */
    public boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        } else if (wifiNetworkInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    public void refreshLocalWifiListData() {
        layoutWifiEnable.getProgressBar().setVisibility(View.GONE);
        mWifiList.clear();
        List<Integer> connectedIds = new ArrayList<>();
        List<ScanResult> tmpList = mWifiUtils.getWifiList();
        for (ScanResult tmp : tmpList) {
            if (isGivenWifiConnect(tmp.SSID)) {
                mWifiList.add(tmp);
                connectedIds.add(mWifiList.size() - 1);
            }
        }
        //从wifi列表中删除已经连接的wifi
        for (ScanResult tmp : mWifiList) {
            tmpList.remove(tmp);
        }
        Collections.sort(tmpList, new SortRssi());
        mWifiList.addAll(tmpList);
        mAdapter.setmDatas(mWifiList);
        mAdapter.setConnectedIds(connectedIds);
        mAdapter.notifyDataSetChanged();
    }

    private class SortRssi implements Comparator<ScanResult> {
        @Override
        public int compare(ScanResult o1, ScanResult o2) {
            return o2.level - o1.level;
        }
    }

    private void popupConnectWifiDialog(ScanResult data) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_connect_wifi, (ViewGroup) rootView.findViewById(R.id.dialog_main));
        TextView tvSsid = (TextView) layout.findViewById(R.id.tv_ssid);
        TextView tvSafety = (TextView) layout.findViewById(R.id.tv_safety_wifi);
        EditText pwdWifiEt = (EditText) layout.findViewById(R.id.et_password_wifi);
        CheckBox pwdShowCb = (CheckBox) layout.findViewById(R.id.cb_pwd_show);
        CheckBox moreInfoCb = (CheckBox) layout.findViewById(R.id.cb_more_info);
        LinearLayout moreInfoLayout = (LinearLayout) layout.findViewById(R.id.layout_more_info);
        Spinner spinnerIpType = (Spinner) layout.findViewById(R.id.spinner_ip_type);
        EditText ipAddrEt = (EditText) layout.findViewById(R.id.et_ip_addr);
        EditText gatewayEt = (EditText) layout.findViewById(R.id.et_gateway);
        EditText netmaskLenEt = (EditText) layout.findViewById(R.id.et_netmask_length);
        EditText dns1Et = (EditText) layout.findViewById(R.id.et_dns1);
        EditText dns2Et = (EditText) layout.findViewById(R.id.et_dns2);
        tvSsid.setText(data.SSID);
        tvSafety.setText(WifiUtils.getSecurityString(mContext, data.capabilities));
        ArrayAdapter<CharSequence> ad = ArrayAdapter.createFromResource(mContext, R.array.entries_ip_mode, R.layout.spinner_text);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIpType.setAdapter(ad);
        spinnerIpType.setSelection(1);
        spinnerIpType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    for (int i = 1; i < moreInfoLayout.getChildCount(); i++) {
                        moreInfoLayout.getChildAt(i).setVisibility(View.GONE);
                    }
                } else {
                    for (int i = 0; i < moreInfoLayout.getChildCount(); i++) {
                        moreInfoLayout.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        pwdShowCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pwdWifiEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    pwdWifiEt.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        moreInfoCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    moreInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    moreInfoLayout.setVisibility(View.GONE);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(data.SSID);
        builder.setView(layout);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.networking_dialog_connect), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(pwdWifiEt.getText().toString().trim())) {
                    Toast.makeText(mContext, getResources().getString(R.string.validate_password_enter), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    if (spinnerIpType.getSelectedItemId() == 1) {
                        String capabilities = data.capabilities;
                        if (capabilities.contains("WEP")) {
                            mWifiUtils.connectWEPNetwork(data.SSID, pwdWifiEt.getText().toString().trim());
                        } else if (capabilities.contains("WPA")) {
                            mWifiUtils.connectWPA2Network(data.SSID, pwdWifiEt.getText().toString().trim());
                        } else if (capabilities.contains("WPA2")) {
                            mWifiUtils.connectWPA2Network(data.SSID, pwdWifiEt.getText().toString().trim());
                        } else {
                            mWifiUtils.connectOpenNetwork(data.SSID);
                        }
                    } else {
                        String address = ipAddrEt.getText().toString().trim();
                        String gate = gatewayEt.getText().toString().trim();
                        String maskLen = netmaskLenEt.getText().toString().trim();
                        String dns1 = dns1Et.getText().toString().trim();
                        String dns2 = dns2Et.getText().toString().trim();
//                        if (ipValidate(address) && ipValidate(gate) && ipValidate(dns1) && ipMatch(address, mask, gate)) {
                        mWifiUtils.connectWifiWithStaticIP(mContext, data.SSID, data.capabilities, pwdWifiEt.getText().toString().trim(), address, gate, maskLen, dns1, dns2);
//                        }
                    }
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog connectWifiDialog = builder.create();
        connectWifiDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        connectWifiDialog.show();
        connectWifiDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        connectWifiDialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) connectWifiDialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(connectWifiDialog.getWindow().getDecorView().getWindowToken(), 0);
                return true;
            }
        });
    }

    private void popupWifiDetail(ScanResult data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(data.SSID);
        builder.setMessage(getResources().getString(R.string.wifi_dialog_forget_config));
        builder.setPositiveButton(getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mWifiUtils.disconnectWifi(mWifiUtils.getConfigFromConfiguredNetworksBySsid(data.SSID).networkId);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog deleteDialog = builder.create();
        deleteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        deleteDialog.show();
        deleteDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wifiReceiver != null) {
            mContext.unregisterReceiver(wifiReceiver);
        }
    }
}
