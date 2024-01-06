package com.dnake.utils;

import android.content.Context;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class EthernetUtils {
    public final static String NULL_IP_INFO = "0.0.0.0";

    /**
     * @return 获取所有有效的网卡
     */
    public static String[] getAllNetInterface() {
        ArrayList<String> availableInterface = new ArrayList<>();
        String[] interfaces = null;
        try {
            //获取本地设备的所有网络接口
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) {
                        if (ni.getName().substring(0, 3).equals("eth")) {//筛选出以太网
                            availableInterface.add(ni.getName());
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for (int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    /**
     * 获取指定网卡ip
     *
     * @param netInterface
     * @return
     * @throws SocketException
     */
    public static String getIpAddress(String netInterface) throws SocketException {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if (ni.getName().equals(netInterface)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;// skip ipv6
                        }
                        String ip = ia.getHostAddress();
                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip)) {
                            hostIp = ia.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 获取掩码
     *
     * @param name
     * @return
     */
    public static String getLocalMask(String name) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp." + name + ".mask");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }

    /**
     * 获取网关地址
     *
     * @param name
     * @return
     */
    public static String getLocalGATE(String name) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp." + name + ".gateway");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }

    /**
     * 根据adb shell命令获取
     * DNS地址
     *
     * @param name网卡名称
     * @param dnsIndex dns序号
     * @return
     */
    public static String getLocalDNS(String name, int dnsIndex) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp." + name + ".dns" + dnsIndex);
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }

    public static boolean isEthOn(String name) {
        return true;
//        boolean r = false;
//        Process cmdProcess = null;
//        BufferedReader reader = null;
//        String line;
//        try {
//            cmdProcess = Runtime.getRuntime().exec("netcfg");
//            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                if (line.contains(name)) {
//                    if (line.contains("UP")) {
//                        r = true;
//                    } else {
//                        r = false;
//                    }
//                }
//            }
//            return r;
//        } catch (IOException e) {
//            return false;
//        } finally {
//            try {
//                reader.close();
//            } catch (IOException e) {
//            }
//            cmdProcess.destroy();
//        }
    }

    public static String getLocalMac() {
        String mac_s = "";
        try {
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(Utils.getLocalIp()));
            if (ne != null) {
                byte[] mac = ne.getHardwareAddress();
                if (mac != null)
                    mac_s = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac_s;
    }

    public static boolean setDynamicIp(Context context) {
        try {
            Class<?> ethernetManagerCls = Class.forName("android.net.EthernetManager");
            //获取EthernetManager实例
            Object ethManager = context.getSystemService("ethernet");
            //创建IpConfiguration
            Class<?> ipConfigurationCls = Class.forName("android.net.IpConfiguration");
            Object ipConfiguration = ipConfigurationCls.newInstance();
            //获取ipAssignment、proxySettings的枚举值
            Map<String, Object> ipConfigurationEnum = getIpConfigurationEnum(ipConfigurationCls);
            //设置ipAssignment
            Field ipAssignment = ipConfigurationCls.getField("ipAssignment");
            ipAssignment.set(ipConfiguration, ipConfigurationEnum.get("IpAssignment.DHCP"));
            //设置proxySettings
            Field proxySettings = ipConfigurationCls.getField("proxySettings");
            proxySettings.set(ipConfiguration, ipConfigurationEnum.get("ProxySettings.NONE"));
            //获取EthernetManager的setConfiguration()
            Method setConfigurationMethod = ethernetManagerCls.getDeclaredMethod("setConfiguration", ipConfiguration.getClass());
            //设置动态IP
            setConfigurationMethod.invoke(ethManager, ipConfiguration);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置以太网静态IP地址
     *
     * @param address ip地址
     * @param mask    子网掩码
     * @param gate    网关
     * @param dns     dns
     */
    public static boolean setEthernetStaticIp(Context context, String address, String mask, String gate, String dns, String dns2) {
        try {
            Class<?> ethernetManagerCls = Class.forName("android.net.EthernetManager");
            //获取EthernetManager实例
            Object ethManager = context.getSystemService("ethernet");
            //创建StaticIpConfiguration
            Object staticIpConfiguration = newStaticIpConfiguration(address, gate, mask, dns, dns2);
            //创建IpConfiguration
            Object ipConfiguration = newIpConfiguration(staticIpConfiguration);
            //获取EthernetManager的setConfiguration()
            Method setConfigurationMethod = ethernetManagerCls.getDeclaredMethod("setConfiguration", ipConfiguration.getClass());
            //设置静态IP
            setConfigurationMethod.invoke(ethManager, ipConfiguration);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取StaticIpConfiguration实例
     */
    private static Object newStaticIpConfiguration(String address, String gate, String mask, String dns, String dns2) throws Exception {
        Class<?> staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");
        //实例化StaticIpConfiguration
        Object staticIpConfiguration = staticIpConfigurationCls.newInstance();
        Field ipAddress = staticIpConfigurationCls.getField("ipAddress");
        Field gateway = staticIpConfigurationCls.getField("gateway");
        Field domains = staticIpConfigurationCls.getField("domains");
        Field dnsServers = staticIpConfigurationCls.getField("dnsServers");
        //设置ipAddress
        ipAddress.set(staticIpConfiguration, newLinkAddress(address, mask));
        //设置网关
        gateway.set(staticIpConfiguration, InetAddress.getByName(gate));
        //设置掩码
        domains.set(staticIpConfiguration, mask);
        //设置dns
        ArrayList<InetAddress> dnsList = (ArrayList<InetAddress>) dnsServers.get(staticIpConfiguration);
        dnsList.add(InetAddress.getByName(dns));
        //设置dns2
        if (!dns2.isEmpty()) {
            dnsList.add((Inet4Address) Inet4Address.getByName(dns2));
        }
        return staticIpConfiguration;
    }

    /**
     * 获取LinkAddress实例
     */
    private static Object newLinkAddress(String address, String mask) throws Exception {
        Class<?> linkAddressCls = Class.forName("android.net.LinkAddress");
        Constructor<?> linkAddressConstructor = linkAddressCls.getDeclaredConstructor(InetAddress.class, int.class);
        return linkAddressConstructor.newInstance(InetAddress.getByName(address), getPrefixLength(mask));
    }

    /**
     * 获取IpConfiguration实例
     */
    private static Object newIpConfiguration(Object staticIpConfiguration) throws Exception {
        Class<?> ipConfigurationCls = Class.forName("android.net.IpConfiguration");
        Object ipConfiguration = ipConfigurationCls.newInstance();
        //设置StaticIpConfiguration
        Field staticIpConfigurationField = ipConfigurationCls.getField("staticIpConfiguration");
        staticIpConfigurationField.set(ipConfiguration, staticIpConfiguration);
        //获取ipAssignment、proxySettings的枚举值
        Map<String, Object> ipConfigurationEnum = getIpConfigurationEnum(ipConfigurationCls);
        //设置ipAssignment
        Field ipAssignment = ipConfigurationCls.getField("ipAssignment");
        ipAssignment.set(ipConfiguration, ipConfigurationEnum.get("IpAssignment.STATIC"));
        //设置proxySettings
        Field proxySettings = ipConfigurationCls.getField("proxySettings");
        proxySettings.set(ipConfiguration, ipConfigurationEnum.get("ProxySettings.STATIC"));
        return ipConfiguration;
    }

    /**
     * 获取IpConfiguration的枚举值
     */
    private static Map<String, Object> getIpConfigurationEnum(Class<?> ipConfigurationCls) {
        Map<String, Object> enumMap = new HashMap<>();
        Class<?>[] enumClass = ipConfigurationCls.getDeclaredClasses();
        for (Class<?> enumC : enumClass) {
            Object[] enumConstants = enumC.getEnumConstants();
            if (enumConstants == null) continue;
            for (Object enu : enumConstants) {
                enumMap.put(enumC.getSimpleName() + "." + enu.toString(), enu);
            }
        }
        return enumMap;
    }

    /**
     * 获取长度
     */
    private static int getPrefixLength(String mask) {
        String[] strs = mask.split("\\.");
        int count = 0;
        for (String str : strs) {
            if (str.equals("255")) {
                ++count;
            }
        }
        return count * 8;
    }

    public static String getAvahiAutoIp() {
        String ip = new ExeCommand().run("ifconfig eth0:avahi |sed -nr '2s#^.*dr:(.*)  B.*$#\\1#gp'", 1000).getResult();
        return ip;
    }

    public static void startAvahiAutoipd() {
        dmsg req = new dmsg();
        dxml p1 = new dxml();
        p1.setText("/params/cmd", "./dnake/cfg/avahi-autoipd --force-bind -t /dnake/cfg/avahi/avahi-autoipd.action eth0");
        req.to("/upgrade/root/cmd", p1.toString());
    }

    public static void stopAvahiAutoipd(){
        dmsg req = new dmsg();
        dxml p1 = new dxml();
        p1.setText("/params/cmd", "killall avahi-autoipd");
        req.to("/upgrade/root/cmd", p1.toString());
    }

    public static void delAvahiAutoipdNetwork(){
        dmsg req = new dmsg();
        dxml p1 = new dxml();
        p1.setText("/params/cmd", "ifconfig eth0:avahi down");
        req.to("/upgrade/root/cmd", p1.toString());
    }
}
