package com.dnake.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.Log;

import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isURL1(String str) {
        String regex = "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
//                 + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        return match(regex, str);
    }

    public static boolean isURL2(String str) {
        String regex = "^((https|http|ftp|rtsp|mms)?://)" + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
//                 + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        return match(regex, str);
    }

    public static Boolean ipValidate(String addr) {
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        if (!addr.matches(REGX_IP)) return false;
        return true;
    }

    public static Boolean domainValidate(String addr) {
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        final String REGX_DOMAIN = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";
        if (!addr.matches(REGX_IP) && !addr.matches(REGX_DOMAIN)) return false;
        return true;
    }

    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static String getDeviceTag() {
        return "SM";
    }

    public static String getHostname() {
        String device_tag = getDeviceTag();
        String host_name = "";
        String macStr = "";
        macStr = getEth0Mac();
        macStr = macStr.replace(":", "");
        macStr = macStr.substring(macStr.length() - 6, macStr.length());
        host_name = device_tag + macStr;
        return host_name.equals("") ? device_tag : host_name;
    }

    public static String getEth0Mac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial.toUpperCase();
    }

    public static String getsha256HashCode(String filePath) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        String sha256Str = getHashCode(fis, "SHA-256");
        return addZeroForNum(sha256Str, 64);
    }

    public static String getHashCode(InputStream fis, String hashkeytype) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashkeytype);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            byte[] md5Bytes = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static String getVersionName() {//version(ex:1.2 dev)
        String[] strs = getVersionNameAll().split(" ");
        if (strs != null && strs.length > 0) {
            return strs[0];
        }
        return "";
    }

    public static boolean compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return false;
        }
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;
        while (index < minLen && (diff = Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return true;
                }
            }
            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return false;
                }
            }
            return false;
        } else {
            return diff > 0 ? true : false;
        }
    }

    public static int convertNetmaskToCIDR() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        while (true) {
            req.to("/settings/lan/query", null);
            p.parse(req.mBody);
            if (req.mBody != null) {
                break;
            }
        }
        int dhcp = p.getInt("/params/dhcp", 0);
        String netmask = "";
        if (dhcp == 1) {
            netmask = getIpAddrMask();
        } else {
            netmask = p.getText("/params/mask");
        }
        int cidr = 0;
        try {
            byte[] netmaskBytes = InetAddress.getByName(netmask).getAddress();
            boolean zero = false;
            for (byte b : netmaskBytes) {
                int mask = 0x80;
                for (int i = 0; i < 8; i++) {
                    int result = b & mask;
                    if (result == 0) {
                        zero = true;
                    } else if (zero) {
                        throw new IllegalArgumentException("Invalid netmask.");
                    } else {
                        cidr++;
                    }
                    mask >>>= 1;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return cidr;
    }

    public static String getIpAddrMask() {
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                if (!networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {
                        return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String calcMaskByPrefixLength(int length) {
        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int[] maskParts = new int[partsNum];
        int selector = 0x000000ff;
        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }
        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

    public static String curDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return sdf.format(date);
    }

    public static void execSHFile() {//初始化必要的文件目录
        File file = new File("/dnake/data/ui_ver");
        if (!file.exists()) {
            dmsg req = new dmsg();
            dxml p1 = new dxml();
            p1.setText("/params/cmd", "./dnake/bin/init_data.sh");
            req.to("/upgrade/root/cmd", p1.toString());
        }
    }

    public static void setEthWifiEnable() {
    }

    public static void saveBitmap(Bitmap bitmap, String filePath) {
        File f = new File(filePath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getWebLanguage() {
        String webLocale = "";
        try {
            FileInputStream in = new FileInputStream("/dnake/cfg/web_language");
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) != -1) {
                webLocale = new String(bytes, 0, length);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return webLocale;
    }

    public static void setWebLanguage(String locale) {
        try {
            FileOutputStream out = new FileOutputStream("/dnake/cfg/web_language");
            out.write(locale.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasValidIP() {
        String ethIP = "0.0.0.0";
        try {
            ethIP = EthernetUtils.getIpAddress("eth0");
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String wifiIp = WifiUtils.getCurWifiIpAddress(apps.ctx);
        if ((ethIP == null || ethIP.equals("0.0.0.0")) && wifiIp.equals("0.0.0.0")) {
            return false;
        }
        return true;
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

    public static String getInstallAppNum() {
        String num = "";
        try {
            FileInputStream in = new FileInputStream("/dnake/cfg/third_app_num");
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) != -1) {
                num = new String(bytes, 0, length);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return TextUtils.isEmpty(num) ? "0" : num;
    }

    public static void setInstallAppNum(String num) {
        try {
            FileOutputStream out = new FileOutputStream("/dnake/cfg/third_app_num");
            out.write(num.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVersionNameAll() {
        String version = "";
        try {
            FileInputStream in = new FileInputStream("/dnake/data/ui_ver");
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) != -1) {
                version = new String(bytes, 0, length);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 静默安装
     *
     * @param file
     * @return
     */
    public static void slientInstall(File file) {
        boolean result = false;
        File tmp_file = new File(file.getPath() + ".apk");
        file.renameTo(tmp_file);
        file.delete();
        dxml p = new dxml();
        dmsg req = new dmsg();
        String cmd = "chmod 777 " + tmp_file.getPath() + "\n";
        p.setText("/params/cmd", cmd);
        req.to("/upgrade/root/cmd", p.toString());

        p = new dxml();
        cmd = "pm install -f " + tmp_file.getPath();
        p.setText("/params/cmd", cmd);
        req.to("/upgrade/root/cmd", p.toString());
    }

    public static Drawable path2Drawable(Context context, String file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Drawable drawable = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public static String getDesktopBgPath() {
        String path = "";
        try {
            FileInputStream in = new FileInputStream("/dnake/cfg/desktop_bg_path");
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) != -1) {
                path = new String(bytes, 0, length);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            path = "";
        } catch (IOException e) {
            e.printStackTrace();
            path = "";
        }
        if (TextUtils.isEmpty(path)) {
            path = "/dnake/data/bg/bg_default.webp";
            return path;
        } else {
            return path;
        }
    }
}
