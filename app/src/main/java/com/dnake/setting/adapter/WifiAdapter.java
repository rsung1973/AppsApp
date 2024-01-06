package com.dnake.setting.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.apps.R;
import com.dnake.utils.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.ViewHolder> {
    private static final int[] WIFI_SIGNAL_UNLOCK = {
            R.mipmap.ic_wifi_signal_1_dark,
            R.mipmap.ic_wifi_signal_2_dark,
            R.mipmap.ic_wifi_signal_3_dark,
            R.mipmap.ic_wifi_signal_4_dark,
    };

    private static final int[] WIFI_SIGNAL_LOCK = {
            R.mipmap.ic_wifi_lock_signal_1_dark,
            R.mipmap.ic_wifi_lock_signal_2_dark,
            R.mipmap.ic_wifi_lock_signal_3_dark,
            R.mipmap.ic_wifi_lock_signal_4_dark,
    };

    private Context mContext;
    private List<ScanResult> mDatas = new ArrayList<>();
    private List<Integer> connectedIds = new ArrayList<>();
    private WifiUtils mWifiUtils;

    public List<Integer> getConnectedIds() {
        return connectedIds;
    }

    public void setConnectedIds(List<Integer> connectedIds) {
        this.connectedIds = connectedIds;
    }

    public List<ScanResult> getmDatas() {
        return mDatas;
    }

    public void setmDatas(List<ScanResult> mDatas) {
        this.mDatas = mDatas;
    }

    public WifiAdapter(Context context) {
        mContext = context;
        mWifiUtils = new WifiUtils(mContext);
    }

    public WifiAdapter(Context context, List<ScanResult> datas) {
        mContext = context;
        mDatas = datas;
        mWifiUtils = new WifiUtils(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wifi_rv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult scanResult = mDatas.get(position);
        int level = getLevel(scanResult.level);
        if (connectedIds.contains(position)) {
            if (mWifiUtils.isWifiConnected(scanResult.SSID)) {
                holder.tvName.setText(scanResult.SSID + "(Connected)");
            } else {
                holder.tvName.setText(scanResult.SSID + "(Authenticating...)");
            }
        } else {
            holder.tvName.setText(scanResult.SSID);
        }
        if (level == -1) {
            holder.ivType.setImageDrawable(null);
        } else {
            String capabilities = scanResult.capabilities;
            if (capabilities.contains("WEP")) {
                holder.ivType.setImageResource(WIFI_SIGNAL_LOCK[level >= 0 ? level : 0]);
            } else if (capabilities.contains("WPA")) {
                holder.ivType.setImageResource(WIFI_SIGNAL_LOCK[level >= 0 ? level : 0]);
            } else if (capabilities.contains("WPA2")) {
                holder.ivType.setImageResource(WIFI_SIGNAL_LOCK[level >= 0 ? level : 0]);
            } else {
                holder.ivType.setImageResource(WIFI_SIGNAL_UNLOCK[level >= 0 ? level : 0]);
            }
        }
        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    if (!connectedIds.contains(position)) {
                        mCallback.onWifiConnect(position);
                    } else {
                        mCallback.onEnterDetail(position);
                    }
                }
            }
        });
    }

    private int getLevel(int rssi) {
        if (rssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(rssi, 4);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMain;
        ImageView ivType;
        TextView tvName;

        public ViewHolder(View view) {
            super(view);
            layoutMain = (LinearLayout) view.findViewById(R.id.layout_main);
            ivType = (ImageView) view.findViewById(R.id.iv_type);
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onWifiConnect(int index);

        void onEnterDetail(int index);
    }
}
