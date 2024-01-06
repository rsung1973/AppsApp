package com.dnake.utils;

import static android.content.Context.WIFI_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dnake.apps.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WifiUtils {
    public final static String NULL_IP_INFO = "0.0.0.0";

    private Context context;
    static WifiManager mWifiManager;
    private static ConnectivityManager mConnectivityManager;

    public WifiUtils(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isWifiStatic(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo.leaseDuration == 0) {
            return true;
        } else {
            return false;
        }
    }

    //获取本机WIFI设备详细信息
    @SuppressLint("MissingPermission")
    public void getDetailsWifiInfo() {
        StringBuffer sInfo = new StringBuffer();
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int Ip = mWifiInfo.getIpAddress();
        String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);
        sInfo.append("\n--BSSID : " + mWifiInfo.getBSSID());
        sInfo.append("\n--SSID : " + mWifiInfo.getSSID());
        sInfo.append("\n--nIpAddress : " + strIp);
        sInfo.append("\n--MacAddress : " + mWifiInfo.getMacAddress());
        sInfo.append("\n--NetworkId : " + mWifiInfo.getNetworkId());
        sInfo.append("\n--LinkSpeed : " + mWifiInfo.getLinkSpeed() + "Mbps");
        sInfo.append("\n--Rssi : " + mWifiInfo.getRssi());
        sInfo.append("\n--SupplicantState : " + mWifiInfo.getSupplicantState() + mWifiInfo);
        sInfo.append("\n\n\n\n");
        Log.d("getDetailsWifiInfo", sInfo.toString());
    }

    public static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if ((inetAddress instanceof Inet4Address) && !inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
                        return inetAddress.getHostAddress().toString();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCurWifiIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return "0.0.0.0";
        }
    }

    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString().toUpperCase();
    }

    /**
     * 连接到开放网络
     *
     * @param ssid 热点名
     * @return 配置是否成功
     */
    public boolean connectOpenNetwork(@NonNull String ssid) {
        // 获取networkId
        int networkId = setOpenNetwork(ssid);
        if (-1 != networkId) {
            // 保存配置
//            boolean isSave = saveConfiguration();
            boolean isSave = true;
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WEP网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public boolean connectWEPNetwork(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWEPNetwork(ssid, password);
        if (-1 != networkId) {
            // 保存配置
//            boolean isSave = saveConfiguration();
            boolean isSave = true;
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 连接到WPA2网络
     *
     * @param ssid     热点名
     * @param password 密码
     * @return 配置是否成功
     */
    public boolean connectWPA2Network(@NonNull String ssid, @NonNull String password) {
        // 获取networkId
        int networkId = setWPA2Network(ssid, password);
        if (-1 != networkId) {
            // 保存配置
//            boolean isSave = saveConfiguration();
            boolean isSave = true;
            // 连接网络
            boolean isEnable = enableNetwork(networkId);
            return isSave && isEnable;
        }
        return false;
    }

    /**
     * 添加开放网络配置
     *
     * @param ssid SSID
     * @return NetworkId
     */
    int setOpenNetwork(@NonNull String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 生成配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 返回NetworkID
            return wifiConfiguration.networkId;
        }
    }

    /**
     * 添加WEP网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    int setWEPNetwork(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            // 添加配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.wepKeys[0] = "\"" + password + "\"";
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 添加WPA网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return NetworkId
     */
    int setWPA2Network(@NonNull String ssid, @NonNull String password) {
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
            return -1;
        }
        WifiConfiguration wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid);
        if (null == wifiConfiguration) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = addDoubleQuotation(ssid);
            wifiConfig.preSharedKey = "\"" + password + "\"";
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.status = WifiConfiguration.Status.ENABLED;
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            // 添加配置并返回NetworkID
            return addNetwork(wifiConfig);
        } else {
            // 更新配置并返回NetworkID
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            return updateNetwork(wifiConfiguration);
        }
    }

    /**
     * 通过热点名获取热点配置
     *
     * @param ssid 热点名
     * @return 配置信息
     */
    public WifiConfiguration getConfigFromConfiguredNetworksBySsid(@NonNull String ssid) {
        ssid = addDoubleQuotation(ssid);
        List<WifiConfiguration> existingConfigs = getConfiguredNetworks();
        if (null != existingConfigs) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals(ssid)) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    /**
     * 添加网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int addNetwork(WifiConfiguration wifiConfig) {
        if (null != mWifiManager) {
            int networkId = mWifiManager.addNetwork(wifiConfig);
            if (-1 != networkId) {
                boolean isSave = mWifiManager.saveConfiguration();
//                if (isSave) {
                return networkId;
//                }
            }
        }
        return -1;
    }

    /**
     * 更新网络配置
     *
     * @param wifiConfig 配置信息
     * @return NetworkId
     */
    private int updateNetwork(WifiConfiguration wifiConfig) {
        if (null != mWifiManager) {
            int networkId = mWifiManager.updateNetwork(wifiConfig);
            if (-1 != networkId) {
                boolean isSave = mWifiManager.saveConfiguration();
//                if (isSave) {
                return networkId;
//                }
            }
        }
        return -1;
    }

    /**
     * 保持配置
     *
     * @return 保持是否成功
     */
    boolean saveConfiguration() {
//        return null != mWifiManager && mWifiManager.saveConfiguration();
        return null != mWifiManager;
    }

    /**
     * 连接到网络
     *
     * @param networkId NetworkId
     * @return 连接结果
     */
    boolean enableNetwork(int networkId) {
        if (null != mWifiManager) {
            boolean isDisconnect = disconnectCurrentWifi();
            boolean isEnableNetwork = mWifiManager.enableNetwork(networkId, true);
//            boolean isSave = mWifiManager.saveConfiguration();
            boolean isSave = true;
            boolean isReconnect = mWifiManager.reconnect();
            return isDisconnect && isEnableNetwork && isSave && isReconnect;
        }
        return false;
    }

    /**
     * 获取配置过的WIFI信息
     *
     * @return 配置信息
     */
    private List<WifiConfiguration> getConfiguredNetworks() {
        if (null != mWifiManager) {
            return mWifiManager.getConfiguredNetworks();
        }
        return null;
    }

    /**
     * 断开当前的WIFI
     *
     * @return 是否断开成功
     */
    public boolean disconnectCurrentWifi() {
        WifiInfo wifiInfo = getConnectionInfo();
        if (null != wifiInfo) {
            int networkId = wifiInfo.getNetworkId();
            return disconnectWifi(networkId);
        } else {
            // 断开状态
            return true;
        }
    }

    /**
     * 断开指定 WIFI
     *
     * @param netId netId
     * @return 是否断开
     */
    public boolean disconnectWifi(int netId) {
        if (null != mWifiManager) {
            boolean isRemove = mWifiManager.removeNetwork(netId);
            boolean isDisable = mWifiManager.disableNetwork(netId);
            boolean isDisconnect = mWifiManager.disconnect();
            mWifiManager.saveConfiguration();
            return isRemove && isDisable && isDisconnect;
        }
        return false;
    }

    /**
     * 获取当前正在连接的WIFI信息
     *
     * @return 当前正在连接的WIFI信息
     */
    public static WifiInfo getConnectionInfo() {
        if (null != mWifiManager) {
            return mWifiManager.getConnectionInfo();
        }
        return null;
    }

    //得到当前连接的WiFi  SSID
    public static String getCurentWifiSSID() {
        String ssid = "";
        if (getConnectionInfo() != null) {
            ssid = getConnectionInfo().getSSID();
            if (ssid.substring(0, 1).equals("\"")
                    && ssid.substring(ssid.length() - 1).equals("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return ssid;
        } else {
            return "";
        }
    }

    public static String getCurentWifiGateway(Context context) {
        if (null != mWifiManager) {
            if (isWifiStatic(context)) {
                Class<?> staticIpConfigurationCls = null;
                try {
                    staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
                    String gatewayField = staticIpConfigurationCls.getField("gateway").toString();
                    return gatewayField;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            } else {
                return int2ip(mWifiManager.getDhcpInfo().gateway);
            }
        }
        return NULL_IP_INFO;
    }

    public static String getCurrentWifiNetmask(Context context) {
        if (null != mWifiManager) {
            if (isWifiStatic(context)) {
                Class<?> staticIpConfigurationCls = null;
                try {
                    staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
                    String ipAddressField = staticIpConfigurationCls.getField("ipAddress").toString();
                    return ipAddressField.toString();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            } else {
                return int2ip(mWifiManager.getDhcpInfo().netmask);
            }
        }
        return NULL_IP_INFO;
    }

    public static String getCurrentWifiDns1() {
        if (null != mWifiManager) {
            return int2ip(mWifiManager.getDhcpInfo().dns1);
        }
        return NULL_IP_INFO;
    }

    public static String getCurrentWifiDns2() {
        if (null != mWifiManager) {
            return int2ip(mWifiManager.getDhcpInfo().dns2);
        }
        return NULL_IP_INFO;
    }

    public boolean isWifiConnected(String ssid) {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return false;
        }
        switch (wifiInfo.getSupplicantState()) {
            case COMPLETED:
                return wifiInfo.getSSID().replace("\"", "").equals(ssid);
            default:
                return false;
        }
    }

    /**
     * 添加双引号
     *
     * @param text 待处理字符串
     * @return 处理后字符串
     */
    public String addDoubleQuotation(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return "\"" + text + "\"";
    }

    public static boolean isWifiEnable() {
        if (null != mWifiManager) {
            return mWifiManager.isWifiEnabled();
        }
        return false;
    }

    public static List<ScanResult> getWifiList() {
        if (null != mWifiManager) {
            return mWifiManager.getScanResults();
        }
        return null;
    }

    public static boolean startScan() {
        return mWifiManager.startScan();
    }

    public static boolean openWifi() {
        boolean bRet = true;
        if (!isWifiEnable()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    public static boolean closeWifi() {
        if (isWifiEnable()) {
            return mWifiManager.setWifiEnabled(false);
        }
        return false;
    }

    public boolean connectWifiWithStaticIP(Context context, String ssid, String capabilities, String password, String staticIpAddr, String gatewayAddr, String maskLen, String dns1Addr, String dns2Addr) {
        try {
            Inet4Address inetAddr = (Inet4Address) Inet4Address.getByName(staticIpAddr);
            int prefixLength = Integer.parseInt(maskLen);
            InetAddress gateway = (Inet4Address) Inet4Address.getByName(gatewayAddr);
            InetAddress dnsAddr = (Inet4Address) Inet4Address.getByName(dns1Addr);
            Class[] cl = new Class[]{InetAddress.class, int.class};
            Constructor cons = null;
            Class<?> clazz = Class.forName("android.net.LinkAddress");
            //取得所有构造函数
            try {
                cons = clazz.getConstructor(cl);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (cons == null) {
                return false;
            }
            //给传入参数赋初值
            Object[] x = {inetAddr, prefixLength};
            //构造StaticIpConfiguration对象
            Class<?> staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
            //实例化StaticIpConfiguration
            Object staticIpConfiguration = null;
            staticIpConfiguration = staticIpConfigurationCls.newInstance();
            Field ipAddress = staticIpConfigurationCls.getField("ipAddress");
            Field gatewayField = staticIpConfigurationCls.getField("gateway");
            Field dnsServers = staticIpConfigurationCls.getField("dnsServers");
            //设置ipAddress
            ipAddress.set(staticIpConfiguration, (LinkAddress) cons.newInstance(x));
            //设置网关
            gatewayField.set(staticIpConfiguration, gateway);
            //设置dns
            ArrayList<InetAddress> dnsList = (ArrayList<InetAddress>) dnsServers.get(staticIpConfiguration);
            dnsList.add(dnsAddr);
            if (!dns2Addr.isEmpty()) {
                dnsList.add((Inet4Address) Inet4Address.getByName(dns2Addr));
            }
            WifiConfiguration wifiConfig = getConfigFromConfiguredNetworksBySsid(ssid);
            @SuppressLint("PrivateApi")
            Class ipAssignmentCls = Class.forName("android.net.IpConfiguration$IpAssignment");
            Object ipAssignment = Enum.valueOf(ipAssignmentCls, "STATIC");
            Method setIpAssignmentMethod = wifiConfig.getClass().getDeclaredMethod("setIpAssignment", ipAssignmentCls);
            setIpAssignmentMethod.invoke(wifiConfig, ipAssignment);
            Method setStaticIpConfigurationMethod = wifiConfig.getClass().getDeclaredMethod("setStaticIpConfiguration", staticIpConfiguration.getClass());
            //设置静态IP，将StaticIpConfiguration设置给WifiConfiguration
            setStaticIpConfigurationMethod.invoke(wifiConfig, staticIpConfiguration);
            if (capabilities.contains("WEP")) {
                wifiConfig.wepKeys[0] = "\"" + password + "\"";
            } else if (capabilities.contains("WPA")) {
                wifiConfig.preSharedKey = "\"" + password + "\"";
            } else if (capabilities.contains("WPA2")) {
                wifiConfig.preSharedKey = "\"" + password + "\"";
            }
            updateNetwork(wifiConfig);
            //WifiConfiguration重新添加到WifiManager
            int netId = mWifiManager.addNetwork(wifiConfig);
            mWifiManager.disableNetwork(netId);
            boolean flag = mWifiManager.enableNetwork(netId, true);
            return flag;
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addWifiWithStaticIP(Context context, String ssid, String capabilities, String password, String staticIpAddr, String gatewayAddr, String maskLen, String dns1Addr, String dns2Addr) {
        try {
            Inet4Address inetAddr = (Inet4Address) Inet4Address.getByName(staticIpAddr);
            int prefixLength = Integer.parseInt(maskLen);
            InetAddress gateway = (Inet4Address) Inet4Address.getByName(gatewayAddr);
            InetAddress dnsAddr = (Inet4Address) Inet4Address.getByName(dns1Addr);
            Class[] cl = new Class[]{InetAddress.class, int.class};
            Constructor cons = null;
            Class<?> clazz = Class.forName("android.net.LinkAddress");
            //取得所有构造函数
            try {
                cons = clazz.getConstructor(cl);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (cons == null) {
                return false;
            }
            //给传入参数赋初值
            Object[] x = {inetAddr, prefixLength};
            //构造StaticIpConfiguration对象
            Class<?> staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
            //实例化StaticIpConfiguration
            Object staticIpConfiguration = null;
            staticIpConfiguration = staticIpConfigurationCls.newInstance();
            Field ipAddress = staticIpConfigurationCls.getField("ipAddress");
            Field gatewayField = staticIpConfigurationCls.getField("gateway");
            Field dnsServers = staticIpConfigurationCls.getField("dnsServers");
            //设置ipAddress
            ipAddress.set(staticIpConfiguration, (LinkAddress) cons.newInstance(x));
            //设置网关
            gatewayField.set(staticIpConfiguration, gateway);
            //设置dns
            ArrayList<InetAddress> dnsList = (ArrayList<InetAddress>) dnsServers.get(staticIpConfiguration);
            dnsList.add(dnsAddr);
            if (!dns2Addr.isEmpty()) {
                dnsList.add((Inet4Address) Inet4Address.getByName(dns2Addr));
            }
            WifiConfiguration wifiConfig = new WifiConfiguration();
            if (capabilities.contains("WEP")) {
//                wifiConfig.wepKeys[0] = "\"" + password + "\"";
                wifiConfig.SSID = addDoubleQuotation(ssid);
                wifiConfig.wepKeys[0] = "\"" + password + "\"";
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            } else if (capabilities.contains("WPA")) {
                wifiConfig.SSID = addDoubleQuotation(ssid);
                wifiConfig.preSharedKey = "\"" + password + "\"";
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                wifiConfig.preSharedKey = "\"" + password + "\"";
//            } else if (capabilities.contains("WPA2")) {
//                wifiConfig.preSharedKey = "\"" + password + "\"";
            } else {
                wifiConfig.SSID = addDoubleQuotation(ssid);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedAuthAlgorithms.clear();
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }
            @SuppressLint("PrivateApi")
            Class ipAssignmentCls = Class.forName("android.net.IpConfiguration$IpAssignment");
            Object ipAssignment = Enum.valueOf(ipAssignmentCls, "STATIC");
            Method setIpAssignmentMethod = wifiConfig.getClass().getDeclaredMethod("setIpAssignment", ipAssignmentCls);
            setIpAssignmentMethod.invoke(wifiConfig, ipAssignment);
            Method setStaticIpConfigurationMethod = wifiConfig.getClass().getDeclaredMethod("setStaticIpConfiguration", staticIpConfiguration.getClass());
            //设置静态IP，将StaticIpConfiguration设置给WifiConfiguration
            setStaticIpConfigurationMethod.invoke(wifiConfig, staticIpConfiguration);

            //WifiConfiguration重新添加到WifiManager
            int netId = addNetwork(wifiConfig);
            mWifiManager.disableNetwork(netId);
            boolean flag = mWifiManager.enableNetwork(netId, true);
            return flag;
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getSecurityString(Context context, String capabilities) {
        boolean wep = capabilities.contains("WEP");
        boolean wpa = capabilities.contains("WPA-PSK");
        boolean wpa2 = capabilities.contains("WPA2-PSK");
        if (wep) {
            return context.getString(R.string.wifi_security_wep);
        } else if (wpa2 && wpa) {
            return context.getString(R.string.wifi_security_wpa_wpa2);
        } else if (wpa2) {
            return context.getString(R.string.wifi_security_wpa2);
        } else if (wpa) {
            return context.getString(R.string.wifi_security_wpa);
        } else {
            return context.getString(R.string.wifi_security_none);
        }
    }

    public static boolean curWifiConnectStatus(Context context) {
        String curWifiIP = WifiUtils.getCurWifiIpAddress(context);
        String curWifiSSID = WifiUtils.getCurentWifiSSID();
        if (!TextUtils.isEmpty(curWifiSSID) && !curWifiIP.equals("0.0.0.0")) {
            return true;
        } else {
            return false;
        }
    }
}
