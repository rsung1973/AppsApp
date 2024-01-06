package com.dnake.v700;

import static com.dnake.v700.apps.ctx;

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.dnake.tuya.TuYa_ipc;
import com.dnake.tuya.utils;
import com.dnake.utils.DateUtils;
import com.dnake.utils.DisplayUtils;
import com.dnake.utils.NtpConfUtils;
import com.dnake.utils.SoundUtils;
import com.dnake.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class sys {
    public static float scaled = 1.0f;
    public static int sLimit = -1;

    public static int limit() {
        if (sLimit != -1)
            return sLimit;

        int limit = 0;
        try {
            FileInputStream in = new FileInputStream("/dnake/bin/limit");
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
                limit = Integer.parseInt(s);
            }
            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        sLimit = limit;
        return limit;
    }

    public static String url = "/dnake/cfg/sys.xml";
    private static String url_b = "/dnake/data/sys.xml";

    public static final class admin {
        public static String passwd = new String("123456");
    }

    public static final class payload {
        public static int H264 = 102;
    }

    public static String getSysPasswd() {
        String passwd = "";
        dxml p = new dxml();
        boolean result = p.load(url);
        if (result) {
            passwd = p.getText("/sys/admin/passwd", admin.passwd);
        }
        return passwd;
    }

    //Display
    public static final class display {
        public static int bright = 209;
        public static int sleep = 60;//15 seconds，30 seconds，1 minute，2 minutes，5 minutes，10 minutes，30 minutes，默认为1minute
        public static int screen_lock = 0;

        public static int sleepToId(int sec) {
            switch (sec) {
                case 15:
                    return 0;
                case 30:
                    return 1;
                case 60:
                    return 2;
                case 120:
                    return 3;
                case 300:
                    return 4;
                case 600:
                    return 5;
                case 1800:
                    return 6;
            }
            return 2;
        }

        public static int sleepToSec(int id) {
            switch (id) {
                case 0:
                    return 15;
                case 1:
                    return 30;
                case 2:
                    return 60;
                case 3:
                    return 120;
                case 4:
                    return 300;
                case 5:
                    return 600;
                case 6:
                    return 1800;
            }
            return 300;
        }
    }

    public static final class tuya {
        public static int channel = 1;
        public static String TUYA_UUID = "";
        public static String TUYA_AUTHKEY = "";
        public static String qr = "";
    }

    public static final class talk {
        public static int building = 1;
        public static int unit = 1;
        public static int floor = 11;
        public static int family = 11;
    }

    public static final class volume {
        public static int sys = 12;
        public static int talk = 0;
        public static final int MAX = 5;

        public static int ringtone_index = 5;
        public static int key_tone = 1;
    }

    public static final class special_advanced {
        public static int call_logs = 0;//1：enable, 0: disable
        public static String model = "Android Indoor Monitor";//当前设备型号,默认：所有室内机
    }

    public static final class panel {
        public static int mode = 0; // 0: 单元门口机 1:围墙机 2:小门口机
        public static int index = 1; // 编号
    }

    public static final class lcd {
        public static int portrait = 0;
    }

    public static final class time {
        public static String cur_time = "";
        public static String tz = "Asia/Shanghai";//timezone id
        public static int format = 1;// 0：MM-DD-YYYY、1：DD-MM-YYYY、2：YYYY-MM-DD
        public static String ntp_server = "2.android.pool.ntp.org";
        public static int ntp_enable = 1;
        public static int ntp_port = 123;
        public static int is24 = 1;
    }

    public static final class language {
        public static String language = "en";
        public static String country = "US";
    }

    public static int language() {
        if (apps.ctx == null)
            return 1;
        Configuration c = apps.ctx.getResources().getConfiguration();
        String s = c.locale.getLanguage();
        String s2 = c.locale.getCountry();
        if (s.equalsIgnoreCase("zh")) {
            if (s2.equalsIgnoreCase("TW"))
                return 2;
            else
                return 0;
        } else if (s.equalsIgnoreCase("en"))
            return 1;
        else if (s.equalsIgnoreCase("es"))
            return 5;
        else if (s.equalsIgnoreCase("PT"))
            return 9;
        else if (s.equalsIgnoreCase("nl"))
            return 8;
        else if (s.equalsIgnoreCase("ru"))
            return 11;
        else if (s.equalsIgnoreCase("TR"))
            return 6;
        else if (s.equalsIgnoreCase("AR"))
            return 12;
        else if (s.equalsIgnoreCase("DE"))
            return 4;
        else if (s.equalsIgnoreCase("FR"))
            return 13;
        else if (s.equalsIgnoreCase("IT"))
            return 14;
        else if (s.equalsIgnoreCase("IW"))
            return 3;
        else if (s.equalsIgnoreCase("SK"))
            return 15;
        else if (s.equalsIgnoreCase("VI"))
            return 7;
        else if (s.equalsIgnoreCase("PL"))
            return 10;
        return 1;
    }

    public static void load() {
        dxml p = new dxml();

        boolean result = p.load(url);
        if (!result)
            result = p.load(url_b);

        if (result) {
            admin.passwd = p.getText("/sys/admin/passwd", admin.passwd);
            tuya.TUYA_UUID = p.getText("/sys/tuya/uuid", tuya.TUYA_UUID);
            tuya.TUYA_AUTHKEY = p.getText("/sys/tuya/key", tuya.TUYA_AUTHKEY);
            tuya.qr = p.getText("/sys/tuya/qr", tuya.qr);

            talk.building = p.getInt("/sys/talk/building", 1);
            talk.unit = p.getInt("/sys/talk/unit", 1);
            talk.floor = p.getInt("/sys/talk/floor", 1);
            talk.family = p.getInt("/sys/talk/family", 1);
            panel.mode = p.getInt("/sys/panel/mode", 0);
            panel.index = p.getInt("/sys/panel/index", 1);
            lcd.portrait = p.getInt("/sys/lcd/portrait", lcd.portrait);

            volume.sys = p.getInt("/sys/volume/sys", 12);
            volume.talk = p.getInt("/sys/volume/talk", 0);

            //Display
            display.bright = p.getInt("/sys/display/bright", display.bright);
            display.sleep = p.getInt("/sys/display/sleep", display.sleep);
            display.screen_lock = p.getInt("/sys/display/screen_lock", display.screen_lock);
            //Sound
            volume.sys = p.getInt("/sys/volume/sys", volume.sys);
            volume.talk = p.getInt("/sys/volume/talk", volume.talk);
            if (apps.version.contains("902") || apps.version.contains("904M")) {
                volume.ringtone_index = p.getInt("/sys/volume/ringtone_index", 5);
            } else if (apps.version.contains("907")) {
                volume.ringtone_index = p.getInt("/sys/volume/ringtone_index", 6);
            } else {
                volume.ringtone_index = p.getInt("/sys/volume/ringtone_index", 9);
            }
            volume.key_tone = p.getInt("/sys/volume/key_tone", volume.key_tone);
            //Date&Time
            time.cur_time = p.getText("/sys/time/cur_time", time.cur_time);
            time.tz = p.getText("/sys/time/tz", time.tz);
            time.format = p.getInt("/sys/time/format", time.format);
            time.ntp_server = p.getText("/sys/time/ntp_server", time.ntp_server);
            time.ntp_enable = p.getInt("/sys/time/ntp_enable", time.ntp_enable);
            time.ntp_port = p.getInt("/sys/time/ntp_port", time.ntp_port);
            time.is24 = p.getInt("/sys/time/is24", time.is24);
        }
        if (panel.mode == 1) {
            talk.building = 0;
            talk.unit = 0;
        }
    }

    public static void initDevSet() {
        dxml p = new dxml();
        //亮度
        DisplayUtils.saveBrightness(ctx.getContentResolver(), display.bright);
        //睡眠时间
        DisplayUtils.setScreenSleepTime(ctx, display.sleep * 1000);
        //系统音量
        AudioManager m = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (m != null) {
            int max = m.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float vol = ((float) volume.sys / 5.0f) * max;
            m.setStreamVolume(AudioManager.STREAM_SYSTEM, (int) vol, 0);
            m.setStreamVolume(AudioManager.STREAM_RING, (int) vol, 0);
            m.setStreamVolume(AudioManager.STREAM_ALARM, (int) vol, 0);
            m.setStreamVolume(AudioManager.STREAM_MUSIC, (int) vol, 0);
        }
        //语言
        p = new dxml();
        if (p.load("/dnake/cfg/sys_locale.xml")) {
            language.language = p.getText("/sys/language", language.language);
            language.country = p.getText("/sys/country", language.country);
            dmsg req = new dmsg();
            p = new dxml();
            p.setText("/params/language", language.language);
            p.setText("/params/country", language.country);
            req.to("/settings/locale", p.toString());
        }
        //铃声
        Uri pickedUri = Uri.parse(SoundUtils.getRingtoneUriPath(ctx, AudioManager.STREAM_SYSTEM, volume.ringtone_index, ""));
        RingtoneManager.setActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_RINGTONE, pickedUri);
        //按键音
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, sys.volume.key_tone);

        //ntp
        DateUtils.setNTPServerIp(sys.time.ntp_server);
        //时区
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(sys.time.tz);
        //24小时制
        Settings.System.putString(ctx.getContentResolver(),
                Settings.System.TIME_12_24,
                (time.is24 == 1) ? DateUtils.HOURS_24 : DateUtils.HOURS_12);
        p = new dxml();
        p.setText("/params/cmd", "settings put global auto_time " + time.ntp_enable);
        new dmsg().to("/upgrade/root/cmd", p.toString());
    }

    public static void saveTuya() {
        dxml p = new dxml();
        p.setText("/sys/tuya/uuid", tuya.TUYA_UUID);
        p.setText("/sys/tuya/key", tuya.TUYA_AUTHKEY);
        p.setText("/sys/tuya/qr", tuya.qr);
        p.save(url);
    }

    public static String unlock_relay = "";
    public static String unlock_relay_url = "/dnake/cfg/unlock_set.xml";
    public static boolean has_set_unlock_relay = false;

    public static void loadUnlockRelays() {
        File f = new File(unlock_relay_url);
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
        if (p.load(unlock_relay_url)) {
            String unlock_relays = "";
            for (int i = 0; i < 3; i++) {
                String s = "/unlock/u" + i;
                int status = p.getInt(s + "/status", 0);
                if (status == 1) {
                    unlock_relays += i + ";";
                }
            }
            sys.unlock_relay = TextUtils.isEmpty(unlock_relays) ? "" : unlock_relays.substring(0, unlock_relays.length() - 1);
        } else {
            sys.unlock_relay = "";
        }
    }

    public static String default_desktop_main = "<sys><logo>1</logo><main_menu><max>4</max><app0><id>10001</id><action>com.dnake.talk.activity.CallMainActivity</action><name>Call</name><icon></icon></app0><app1><id>10002</id><action>com.dnake.talk.activity.MonitorActivity</action><name>Monitor</name><icon></icon></app1><app2><id>10006</id><action>com.dnake.message.activity.MessageActivity</action><name>Message</name><icon></icon></app2><app3><id>10004</id><action>com.dnake.talk.activity.MonitorQuadSplitterActivity</action><name>Quad Splitter</name><icon></icon></app3></main_menu></sys>";
    public static String desktop_main_url = "/dnake/data/desktop_main.xml";

    public static void resetDesktopMain() {
        dxml p = new dxml();
        p.parse(default_desktop_main);
        p.save(desktop_main_url);
    }
}
