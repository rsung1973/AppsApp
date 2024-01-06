package com.dnake.utils;

import android.text.TextUtils;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.io.File;
import java.io.IOException;

public class NtpConfUtils {
    public static String dir = "/dnake/cfg";
    public static String url = dir + "/date_ntp.xml";

    public static class Data {
        public int type;//1: manual, 0: auto
        public String ntp_server_1;
        public String ntp_server_2;
    }

    public static Data load() {
        Data d = new Data();
        File f = new File(dir);
        if (f != null && !f.exists())
            f.mkdir();

        f = new File(url);
        if (f != null && !f.exists()) {
            try {
                if (!f.createNewFile()) {
                    return d;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dxml p = new dxml();
        if (p.load(url)) {
            String s = "/sys/date";
            d.type = p.getInt(s + "/type", 0);
            d.ntp_server_1 = p.getText(s + "/ntp_server_1");
            if (TextUtils.isEmpty(d.ntp_server_1)) {
                d.ntp_server_1 = "";
                d.ntp_server_2 = "";
            } else {
                d.ntp_server_2 = p.getText(s + "/ntp_server_2");
            }
        }
        return d;
    }

    public static void update(Data data) {
        dxml p = new dxml();
        String s = "/sys/date";
        p.setInt(s + "/type", data.type);
        p.setText(s + "/ntp_server_1", data.ntp_server_1);
        p.setText(s + "/ntp_server_2", data.ntp_server_2);
        p.save(url);
        dmsg req = new dmsg();
        req.to("/upgrade/sync", null);
    }

    public static String getNtpServerUrl(){
        Data d = new Data();
        File f = new File(dir);
        if (f != null && !f.exists())
            f.mkdir();

        f = new File(url);
        if (f != null && !f.exists()) {
            try {
                if (!f.createNewFile()) {
                    return "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dxml p = new dxml();
        if (p.load(url)) {
            String s = "/sys/date";
            d.type = p.getInt(s + "/type", 0);
            d.ntp_server_1 = p.getText(s + "/ntp_server_1");
            return d.ntp_server_1;
        }
        return "";
    }
}
