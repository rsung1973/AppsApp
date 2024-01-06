package com.dnake.tuya;

import static com.dnake.v700.apps.TYPE_PANEL;
import static com.dnake.v700.apps.TYPE_PERSON;
import static com.dnake.v700.sys.talk.building;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@SuppressLint("DefaultLocale")
public class utils {
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

    public static String getLocalMac() {
        String mac_s = "";
        try {
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(utils.getLocalIp()));
            if (ne != null) {
                byte[] mac = ne.getHardwareAddress();
                if (mac != null)
                    mac_s = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac_s.toUpperCase();
    }

//    public static void setWifiEnable(boolean enable) {
//        if (talk.mContext == null)
//            return;
//
//        WifiManager wm = (WifiManager) talk.mContext.getSystemService(Context.WIFI_SERVICE);
//        if (wm != null) {
//            if (enable) {
//                if (!wm.isWifiEnabled())
//                    wm.setWifiEnabled(true);
//            } else {
//                if (wm.isWifiEnabled())
//                    wm.setWifiEnabled(false);
//            }
//        }
//    }

    public static boolean getWifiEnable() {
        if (apps.ctx == null)
            return false;

        WifiManager wm = (WifiManager) apps.ctx.getSystemService(Context.WIFI_SERVICE);
        if (wm != null)
            return wm.isWifiEnabled();
        return false;
    }

    public static Boolean getEthWifi() {
        int val = 0;
        try {
            FileInputStream in = new FileInputStream("/var/etc/eth_wifi");
            byte[] data = new byte[256];
            int ret = in.read(data);
            if (ret > 0) {
                String s = new String();
                char[] d = new char[1];
                for (int i = 0; i < ret; i++) {
                    if (data[i] >= '0' && data[i] <= '9') {
                        d[0] = (char) data[i];
                        s += new String(d);
                    } else
                        break;
                }
                val = Integer.parseInt(s);
            }
            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return (val == 1 ? true : false);
    }

    public static int getLanValue(String url) {
        int val = 0;
        try {
            FileInputStream in = new FileInputStream(url);
            byte[] data = new byte[32];
            int ret = in.read(data);
            if (ret > 0) {
                String s = new String();
                char[] d = new char[1];
                for (int i = 0; i < ret; i++) {
                    if (data[i] >= '0' && data[i] <= '9') {
                        d[0] = (char) data[i];
                        s += new String(d);
                    } else
                        break;
                }
                val = Integer.parseInt(s);
            }
            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return val;
    }

    public static void copyTo(String from, String to) {
        File fin = new File(from);
        if (!fin.exists())
            return;
        if (!fin.isFile())
            return;
        if (!fin.canRead())
            return;

        File fout = new File(to);
        if (!fout.getParentFile().exists())
            fout.getParentFile().mkdirs();
        if (fout.exists())
            fout.delete();

        try {
            FileInputStream is = new FileInputStream(fin);
            FileOutputStream os = new FileOutputStream(fout);

            byte[] bt = new byte[1024];
            int sz;
            while ((sz = is.read(bt)) > 0) {
                os.write(bt, 0, sz);
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean x_y_mirror() {
        int x_mirror = 0, y_mirror = 0;
        try {
            FileReader f = new FileReader("/sys/class/graphics/fb0/screen_info");
            BufferedReader b = new BufferedReader(f);
            String s;
            while ((s = b.readLine()) != null) {
                if (s.startsWith("x_mirror:")) {
                    s = s.replace("x_mirror:", "");
                    x_mirror = Integer.parseInt(s);
                } else if (s.startsWith("y_mirror:")) {
                    s = s.replace("y_mirror:", "");
                    y_mirror = Integer.parseInt(s);
                }
            }
            b.close();
            f.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        if (x_mirror == 1 && y_mirror == 1)
            return true;
        return false;
    }

    public static int getDeviceType(String id) {
        sys.load();
        long dev_id = Long.parseLong(id);
        if (dev_id > 30000) {
            if (dev_id > Long.parseLong("10000000000")) {
                int build = (int) (dev_id % 1000000000 / 1000000);
                int unit = (int) (dev_id % 1000000 / 10000);
                int floor = (int) (dev_id % 10000 / 100);
                int family = (int) (dev_id % 10000 % 100);
                if ((build != building) || (unit != sys.talk.unit)) {
                    return -1;
                }
                if ((floor != sys.talk.floor) || (family != sys.talk.family)) {
                    return -1;
                }
                return TYPE_PERSON;
            } else {
                int tag = (int) (dev_id % 10000 / 100);
                if (tag == 99) {
                    int build = (int) (dev_id / 1000000);
                    int unit = (int) (dev_id % 1000000 / 10000);
                    if ((build != building) || (unit != sys.talk.unit)) {
                        return -1;
                    }
                    return TYPE_PANEL;
                } else {//TYPE_MONITOR
                    return 0;
                }
            }
        } else if (dev_id > 10000) {
            int tag = (int) (dev_id / 10000);
            if (tag == 1) {//TYPE_CENTER
                return 0;
            } else if (tag == 2) {
                //TYPE_WALL
                return 0;
            }
        }
        //TYPE_UNKNOW
        return -1;
    }

//    private static Gson mGson;
//
//    public static Gson getGson() {
//        if (mGson == null) {
//            mGson = new Gson();
//        }
//        return mGson;
//    }

    public static MenKouJiIdAdapter.LoadTuyaBean getLoadTuyaBean(String str) {
        return JSON.parseObject(str, MenKouJiIdAdapter.LoadTuyaBean.class);
    }

    public static void reboot() {
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

    public static void clearTuya() {
//        dxml p = new dxml();
//        dmsg req = new dmsg();
//        String cmd ="";
////        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M || android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
////            cmd = "rm /sdcard/tuya_ip/*";
////        }else{
//            cmd = "rm -rf /dnake/data/tuya/*";
////        }
//
//        p.setText("/params/cmd", cmd);
//        req.to("/upgrade/root/cmd", p.toString());
        Log.e("jamie", "clear tuya");
        try {
            deleteDirectory("/dnake/data/tuya");
        } catch (Exception e) {
        }
    }


    public static boolean deleteDirectory(String filePath) {
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.e("jamie", "删除目录失败：" + filePath + "不存在！");
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            } else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.e("jamie", "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.e("jamie", " 删除目录" + filePath + "成功！");
            return true;
        } else {
            Log.e("jamie", "删除目录：" + filePath + "失败！");
            return false;
        }
    }

    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("jamie", " 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.e("jamie", "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("jamie", "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }


    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        if (null != bitmap) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);    //注意压缩png和jpg的格式和质量 100 是质量
            return baos.toByteArray();
        }
        return null;
    }


    public static String tuyaJson(MenKouJiIdAdapter.UploadTuYaBean bean) {
        StringBuilder chs = new StringBuilder();
        for (int i = 0; i < bean.getChs().size(); i++) {
            chs.append(String.format("{\\\"id\\\":%d,\\\"n\\\":\\\"%s\\\"}", i + 1, bean.getChs().get(i).getN()));
            if (i < bean.getChs().size() - 1) {
                chs.append(",");
            }
        }

        return String.format("{\\\"res\\\":%d,\\\"err\\\":%d,\\\"cc\\\":%d,\\\"chs\\\":[%s]}", bean.getRes(), bean.getErr(), bean.getCc(), chs.toString());
    }

    public static boolean isNetworkConnected() {
        if (apps.ctx != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) apps.ctx
                    .getSystemService(apps.ctx.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
