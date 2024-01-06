package com.dnake.logger;

import android.text.TextUtils;
import android.util.Log;

import com.dnake.apps.R;
import com.dnake.v700.apps;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TuyaDeviceLogger {

    public static List<data> logger = new LinkedList<data>();
    public static String dir = "/dnake/data";
    public static String url = dir + "/tuya_ipcs.xml";

    public static int MAX = 60;
    private static boolean needReload = false;

    public static boolean needUpdate = false;

    private static int mMAX = 0;
    public static List<data> temp = new LinkedList<data>();


    public static class data {
        public String id;
        public String name;
        public String ip;
        public String mac;
        public int type;

        public data() {
        }

        public data(String id, String ip, String mac, String name, int type) {
            this.id = id;
            this.ip = ip;
            this.mac = mac;
            this.name = name;
            this.type = type;
        }
    }

    public static void load() {
        logger.clear();

        File f = new File(dir);
        if (f != null && !f.exists()) f.mkdir();

        f = new File(url);
        if (f != null && !f.exists()) {
            try {
                if (!f.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dxml p = new dxml();
        if (p.load(url)) {
            mMAX = p.getInt("/sys/max", 0);
            sys.tuya.channel = p.getInt("/sys/channel", 1);
            for (int i = 0; i < MAX; i++) {
                String s = "/sys/n" + i;
                String name = p.getText(s + "/name");
                String id = p.getText(s + "/id");
                if (id != null) {
                    data d = new data();
                    d.name = name;
                    d.id = p.getText(s + "/id");
                    d.ip = p.getText(s + "/ip");
                    d.mac = p.getText(s + "/mac");
                    d.type = p.getInt(s + "/type", 0);
                    logger.add(d);
                }
            }
        }
        if (needReload) {
            save();
        }
    }

    public static void save(List<TuyaDeviceLogger.data> list) {
        dxml p = new dxml();
        p.setInt("/sys/max", mMAX);
        p.setInt("/sys/channel", sys.tuya.channel);
        for (int i = 0; i < list.size(); i++) {
            String s = "/sys/n" + i;
            if (!list.isEmpty() && list.get(i) != null) {
                p.setText(s + "/id", list.get(i).id);
                p.setText(s + "/ip", list.get(i).ip);
                p.setText(s + "/mac", list.get(i).mac);
                list.get(i).name = getNameNoEmpty(list.get(i).name, list.get(i).type, (i + 1) + "");
                p.setText(s + "/name", list.get(i).name);
                p.setInt(s + "/type", list.get(i).type);
            }
        }
        p.save(url);
        needUpdate = true;
    }

    public static void save() {
        dxml p = new dxml();
        p.setInt("/sys/max", mMAX);
        p.setInt("/sys/channel", sys.tuya.channel);
        for (int i = 0; i < logger.size(); i++) {
            String s = "/sys/n" + i;
            if (!logger.isEmpty() && logger.get(i) != null) {
                p.setText(s + "/id", logger.get(i).id);
                p.setText(s + "/ip", logger.get(i).ip);
                p.setText(s + "/mac", logger.get(i).mac);
                logger.get(i).name = getNameNoEmpty(logger.get(i).name, logger.get(i).type, (i + 1) + "");
                p.setText(s + "/name", logger.get(i).name);
                p.setInt(s + "/type", logger.get(i).type);
            }
        }
        p.save(url);
//        printf()
        needUpdate = true;
    }

    public static void saveIpAndName() {
        temp.clear();
        temp.addAll(logger);
    }

    public static void modifyName(int index, String name) {
        try {
            logger.get(index - 1).name = name;
//            needUpdate = true
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void add(data searchBean) {
        if (mMAX > MAX || searchBean.ip.isEmpty() || searchBean.id.isEmpty()) {
            return;
        }
        for (int i = 0; i < logger.size(); i++) {
            if (logger.get(i).ip.equals(searchBean.ip)) {
                searchBean.name = logger.get(i).name;
                logger.set(i, searchBean);
//                save();
                return;
            }
        }
        String count = "";
        boolean b = searchBean.id.matches("[0-9]*");
        if (b) {
            long num = Long.parseLong(searchBean.id);
            if (num > Long.valueOf("10000000000")) { //小门口机
                count = searchBean.id.substring(0, 1);
            } else {
                count = searchBean.id.substring(searchBean.id.length() - 2);
            }
        }
        searchBean.name = TextUtils.isEmpty(getName(searchBean.ip)) ? getNameNoEmpty(searchBean.name, searchBean.type, count) : getName(searchBean.ip);
        logger.add(searchBean);
        mMAX++;
        needUpdate = true;
//        save();
    }

    private static String getNameNoEmpty(String name, int type, String count) {
        if (TextUtils.isEmpty(name)) {
            if (type == 1) {
                return apps.ctx.getString(R.string.talk_text_person) + (TextUtils.isEmpty(count) ? "" : count);
            } else if (type == 2) {
                return apps.ctx.getString(R.string.talk_text_unit_person) + (TextUtils.isEmpty(count) ? "" : count);
            } else {
                return apps.ctx.getString(R.string.talk_text_unit_person) + (TextUtils.isEmpty(count) ? "" : count);
            }
        } else {
            return name;
        }
    }

    private static String getName(String ip) {
        for (data item : temp) {
            if (ip.equals(item.ip)) {
                return item.name;
            }
        }
        return "";
    }

    public static Boolean hasLive(int cc) {
        return cc <= mMAX;
    }

    public static void switchChannel(int cc) {
        sys.tuya.channel = cc;
//        save();
//        tuyaUrl = mData[cc - 1].ip
    }

    public static String getChannelUrl() {
        if (!logger.isEmpty()) {
            if (sys.tuya.channel < 1 || sys.tuya.channel > logger.size()) {
                sys.tuya.channel = 1;
            }
            return "sip:" + logger.get(sys.tuya.channel - 1).ip;
        } else {
            return "";
        }
    }

    public static void setTop(data bean) {
        data temp = logger.get(0);
//        mData[0] = i
        for (int i = 0; i < logger.size(); i++) {
            if (logger.get(i).id.equals(bean.id) && logger.get(i).mac.equals(bean.mac)) {
//                logger.get(i) = temp;
                logger.set(i, temp);
            }
        }
//        logger.get(0) = bean;
        logger.set(0, bean);
//        needUpdate = true
//        save();
    }

    public static void setTopNew(data bean) {
        for (int i = 0; i < logger.size(); i++) {
            if (logger.get(i).id.equals(bean.id) && logger.get(i).mac.equals(bean.mac)) {
                logger.remove(i);
            }
        }
        logger.add(0, bean);
//        save();
    }

    public static void printf() {
//        for (i in mData) {
//        }
    }

    public static void reset() {
        mMAX = 0;
        logger.clear();
        sys.tuya.channel = 1;
        needUpdate = true;
//        save();
    }
}
