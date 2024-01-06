package com.dnake.v700;

import static com.dnake.setting.fragment.ResetFragment.doDataClear;
import static com.dnake.tuya.utils.clearTuya;
import static com.dnake.tuya.utils.getDeviceType;
import static com.dnake.v700.apps.TYPE_PANEL;
import static com.dnake.v700.apps.TYPE_PERSON;
import static com.dnake.v700.apps.ctx;
import static com.dnake.v700.sys.tuya.TUYA_AUTHKEY;
import static com.dnake.v700.sys.tuya.TUYA_UUID;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RecoverySystem;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.dnake.apps.R;
import com.dnake.apps.SysReceiver;
import com.dnake.apps.WakeTask;
import com.dnake.install.ApkUtils;
import com.dnake.logger.TextLogger;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.setting.fragment.DateFragment;
import com.dnake.test.activity.TestDeviceActivity;
import com.dnake.tuya.PullAudio;
import com.dnake.tuya.PullVideo;
import com.dnake.tuya.TuYa_ipc;
import com.dnake.tuya.utils;
import com.dnake.utils.DateUtils;
import com.dnake.utils.DisplayUtils;
import com.dnake.utils.FileUtils;
import com.dnake.utils.LanguageUtils;
import com.dnake.utils.NtpConfUtils;
import com.dnake.utils.Utils;
import com.hjq.toast.ToastUtils;
import com.tuya.smart.aiipc.ipc_sdk.api.IDeviceManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;

public class devent {
    private static List<devent> elist = null;
    public static Boolean boot = false;

    public String url;

    public devent(String url) {
        this.url = url;
    }

    public void process(String xml) {
    }

    public static void event(String url, String xml) {
        Boolean err = true;
        if (boot && elist != null) {
            Iterator<devent> it = elist.iterator();
            while (it.hasNext()) {
                devent e = it.next();
                if (url.equals(e.url)) {
                    e.process(xml);
                    err = false;
                    break;
                }
            }
        }
        if (err) dmsg.ack(480, null);
    }

    public static void setup() {
        elist = new LinkedList<devent>();

        devent de;

        de = new devent("/apps/run") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
            }
        };
        elist.add(de);

        de = new devent("/apps/version") {
            @Override
            public void process(String body) {
                dxml p = new dxml();
                String v = String.valueOf(apps.version_major) + "." + apps.version_minor + "." + apps.version_minor2;
                v = v + " " + apps.version_date + " " + apps.version_ex;
                p.setText("/params/version", v);
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);

        de = new devent("/apps/device/query") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                if (apps.qResult.d600.host == null) {
                    dxml p = new dxml();
                    p.parse(body);
                    apps.qResult.d600.host = p.getText("/params/name");
                    apps.qResult.d600.ip = p.getText("/params/ip");
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/sip/query") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                if (apps.qResult.sip.url == null) {
                    dxml p = new dxml();
                    p.parse(body);
                    apps.qResult.sip.url = new String(p.getText("/params/url"));
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/sip/result") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);

                dxml p = new dxml();
                p.parse(body);
                apps.qResult.result = p.getInt("/params/result", 0);
            }
        };
        elist.add(de);

        de = new devent("/apps/center/text") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                if (SysReceiver.dnd == 0) {
                    sound.play(sound.msg_prompt);
                }

                dxml p = new dxml();
                p.parse(body);

                int seq = p.getInt("/params/seq", 0);
                int type = p.getInt("/params/type", 0);
                String text = p.getText("/params/data");
                if (text != null) {
                    TextLogger.insert(seq, type, text);
                    apps.notifyText(text);
                }
            }
        };
        elist.add(de);

//        de = new devent("/apps/web/webkit/read") {
//            @Override
//            public void process(String body) {
//                dxml p = new dxml();
//                p.setInt("/params/ad/enable", apps.ad.enable);
//                p.setText("/params/ad/url", apps.ad.url);
//                p.setInt("/params/ad/timeout", apps.ad.timeout);
//
//                p.setInt("/params/autoIpc", apps.autoIpc);
//
//                p.setText("/params/app/url", apps.brower);
//                p.setText("/params/mall/url", apps.mall);
//                p.setText("/params/stock/url", apps.stock);
//                p.setText("/params/cook/url", apps.cook);
//                p.setText("/params/map/url", apps.map);
//                dmsg.ack(200, p.toString());
//            }
//        };
//        elist.add(de);
//
//        de = new devent("/apps/web/webkit/write") {
//            @Override
//            public void process(String body) {
//                dmsg.ack(200, null);
//
//                dxml p = new dxml();
//                p.parse(body);
//
//                apps.ad.enable = p.getInt("/params/ad/enable", 0);
//                if (p.getText("/params/ad/url") != null) apps.ad.url = p.getText("/params/ad/url");
//                apps.ad.timeout = p.getInt("/params/ad/timeout", 5 * 60);
//
//                apps.autoIpc = p.getInt("/params/autoIpc", apps.autoIpc);
//
//                if (p.getText("/params/app/url") != null)
//                    apps.brower = p.getText("/params/app/url");
//                if (p.getText("/params/mall/url") != null)
//                    apps.mall = p.getText("/params/mall/url");
//                if (p.getText("/params/stock/url") != null)
//                    apps.stock = p.getText("/params/stock/url");
//                if (p.getText("/params/cook/url") != null)
//                    apps.cook = p.getText("/params/cook/url");
//                if (p.getText("/params/map/url") != null) apps.map = p.getText("/params/map/url");
//                apps.save();
//            }
//        };
//        elist.add(de);
        de = new devent("/apps/screen_off") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                DisplayUtils.screenOff(apps.ctx);
            }
        };
        elist.add(de);
        de = new devent("/apps/net_status") {
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.setText("/params/status", Utils.hasValidIP() + "");
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/is_login_ok") {
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.setInt("/params/is_ok", login.ok() ? 1 : 0);
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/is_password_ok") {
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.parse(body);
                String password = p.getText("/params/password", "");
                p.setInt("/params/is_ok", login.passwd(password) ? 1 : 0);
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/sound_err") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                sound.play(sound.passwd_err);
            }
        };
        elist.add(de);

        //涂鸦相关接口
        de = new devent("/apps/tuya/read") {
            @Override
            public void process(String xml) {
                dxml p = new dxml();
                p.setText("/params/uuid", TUYA_UUID);
                p.setText("/params/key", TUYA_AUTHKEY);
//                Log.e("jamie","ppp:"+p.toString());
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);

        de = new devent("/apps/tuya/write") {
            @Override
            public void process(String xml) {
                Log.i("aaa", "_____________/apps/tuya/write_________" + xml);
                isLock = true;
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(xml);

                String uuid = p.getText("/params/uuid", "");
                String key = p.getText("/params/key", "");
                boolean need_reboot = false;
                if (TUYA_UUID.equals(uuid) && TUYA_AUTHKEY.equals(key)) {
                    need_reboot = false;
                    isLock = false;
                } else {
                    need_reboot = true;
                    TUYA_UUID = p.getText("/params/uuid", "");
                    TUYA_AUTHKEY = p.getText("/params/key", "");
                    sys.tuya.qr = "";
                    TuyaDeviceLogger.reset();
                    TuyaDeviceLogger.save();
                    dmsg req = new dmsg();
                    p.setText("/params/qrcode", "");
                    req.to("/ui/tuya/info/write", p.toString());
//                sys.save();
//                if (TuYa_ipc.isTuya(false)) {
//                    TuYa_ipc.closeTuya();
//                    TuYa_ipc.initSDK(talk.mContext);
//                } else {
//                    TuYa_ipc.closeTuya();
//                }
                    clearTuya();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (need_reboot) {
                    apps.reboot();
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/tuya/search/result_insert") {
            @Override
            public void process(String body) {
                Log.i("aaa", "____________/apps/tuya/search/result_insert___________________" + body);
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                String id = p.getText("/event/id");
                int type = getDeviceType(id);
                Log.i("aaa", "____________/apps/tuya/search/result_insert_____________type______" + type);
                if (type == TYPE_PERSON || type == TYPE_PANEL) {
                    String ip = p.getText("/event/ip");
                    String mac = p.getText("/event/mac");
                    TuyaDeviceLogger.add(new TuyaDeviceLogger.data(id, ip, mac, "", type));
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/tuya/set_tuya_status") {//设置涂鸦状态
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                TuYa_ipc.tuyaStatus = Integer.parseInt(body);
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/get_tuya_status") {//获取涂鸦状态
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.setInt("/params/tuyaStatus", TuYa_ipc.tuyaStatus);
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/stop_pull") {//停止涂鸦推音视频
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                PullVideo.getInstance().stop();
                PullAudio.stop();
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/is_tuya_status") {//涂鸦信息是否完整
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.parse(body);
                int isShow = p.getInt("/params/is_show", 0);
                boolean isTuya = TuYa_ipc.isTuya(isShow == 1);
                p = new dxml();
                p.setInt("/params/is_tuya_status", isTuya ? 1 : 0);
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/read_qr") {
            @Override
            public void process(String xml) {
                dxml p = new dxml();
                p.setText("/params/qr", sys.tuya.qr);
//                Log.e("jamie","ppp:"+p.toString());
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/do_load_tuya_qr") {
            @Override
            public void process(String xml) {
                dmsg.ack(200, null);
//                if (TuYa_ipc.isTuya(false) && TextUtils.isEmpty(sys.tuya.qr)) {
                if (TuYa_ipc.isTuya(false)) {
                    loadTuYaQr();
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/tuya/send_door_cancel") {
            @Override
            public void process(String xml) {
                dmsg.ack(200, null);
                TuYa_ipc.sendDoorCancel();
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/send_door_bell") {
            @Override
            public void process(String xml) {
                dmsg.ack(200, null);
                TuYa_ipc.sendDoorBell();
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/set_isOut") {
            @Override
            public void process(String xml) {
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(xml);
                int isOut = p.getInt("/params/isOut", 0);
            }
        };
        elist.add(de);
        de = new devent("/apps/tuya/close") {
            @Override
            public void process(String xml) {
                dmsg.ack(200, null);
                TuYa_ipc.closeTuya();
            }
        };
        elist.add(de);

        //web相关接口
        de = new devent("/apps/web/apk/write") {//原url:/apps/web/install_apk
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                String apkPath = p.getText("/params/path", "");
                if (!TextUtils.isEmpty(apkPath)) {
                    Utils.slientInstall(new File(apkPath));
                    Intent it = new Intent("com.dnake.broadcast");
                    it.putExtra("event", "com.dnake.apps.install_apk");
                    it.putExtra("need_refresh_apps", 1);
                    it.putExtra("new_app_pkg_name", "");
                    apps.ctx.sendBroadcast(it);
                }
            }
        };
        elist.add(de);
        de = new devent("/apps/delete_apk") {
            @Override
            public void process(String body) {
                Log.i("aaa", "___________apps/delete_apk___________" + body);
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                String filePath = "/storage/emulated/0/Download";
                if (apps.version.contains("902")) {
                    filePath = "/storage/emulated/legacy/Download";
                } else {
                    filePath = "/storage/emulated/0/Download";
                }
                FileUtils.delAllFile(filePath);
                String chromeFilePath = "/product/usr/chrome.apk";
                File file = new File(chromeFilePath);
                if (file.exists()) {
                    dxml p1 = new dxml();
                    dmsg req = new dmsg();
                    p1.setText("/params/cmd", "rm " + chromeFilePath);
                    req.to("/upgrade/root/cmd", p1.toString());
                }
            }
        };
        elist.add(de);
        de = new devent("/apps/web/install_apk_num/read") {
            @Override
            public void process(String body) {
                dxml p = new dxml();
                p.setInt("/params/app_num", Integer.parseInt(Utils.getInstallAppNum()));
                dmsg.ack(200, p.toString());
            }
        };
        elist.add(de);
        de = new devent("/apps/web/install_apk_num/write") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                int num = p.getInt("/params/app_num", 0);
                Utils.setInstallAppNum(num + "");
            }
        };
        elist.add(de);
        de = new devent("/apps/web/set_current_date_time") {
            @Override
            public void process(String body) {
                Log.i("aaa", "____________/apps/web/set_current_date_time__________________" + body);
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                //ntp
                String ntp_server = p.getText("/params/ntp_server", sys.time.ntp_server);
                NtpConfUtils.Data data = new NtpConfUtils.Data();
                data.ntp_server_1 = ntp_server;
                NtpConfUtils.update(data);
                DateUtils.setNTPServerIp(ntp_server);
                //时区
                String tz = p.getText("/params/tz", sys.time.tz);
                AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(tz);
                int format = p.getInt("/params/date_format", sys.time.format);// 0：MM-DD-YYYY、1：DD-MM-YYYY、2：YYYY-MM-DD
                //24小时制
                int is_24_hours = p.getInt("/params/time_format", 1);
                Settings.System.putString(ctx.getContentResolver(), Settings.System.TIME_12_24, (is_24_hours == 1) ? DateUtils.HOURS_24 : DateUtils.HOURS_12);

                String current_time = p.getText("/params/current_time", "");
                if (!TextUtils.isEmpty(current_time)) {
                    p = new dxml();
                    p.setText("/params/cmd", "settings put global auto_time 0");
                    new dmsg().to("/upgrade/root/cmd", p.toString());
                    String date = current_time.split(" ")[0];
                    int year = Integer.parseInt(date.split("-")[0]);
                    int month = Integer.parseInt(date.split("-")[1]);
                    int day = Integer.parseInt(date.split("-")[2]);
//                        DateUtils.setDate(SysTalk.mContext, year, month, day);
                    String time = current_time.split(" ")[1];
                    int hour = Integer.parseInt(time.split(":")[0]);
                    int minute = Integer.parseInt(time.split(":")[1]);
                    int second = Integer.parseInt(time.split(":")[2]);
                    DateUtils.setTime(apps.ctx, year, month, day, hour, minute, second);
                } else {
                    p = new dxml();
                    p.setText("/params/cmd", "settings put global auto_time 1");
                    new dmsg().to("/upgrade/root/cmd", p.toString());
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/RS485/test/off") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                int onoff = p.getInt("/params/onoff", 0);
                if (TestDeviceActivity.ctx != null) {
                    TestDeviceActivity.isOn = onoff;
                    TestDeviceActivity.freshViews();
                }
            }
        };
        elist.add(de);

        de = new devent("/apps/set_language") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                dxml p = new dxml();
                p.parse(body);
                String language = p.getText("/params/language");
                String country = p.getText("/params/country");
                if (language != null) {
                    Locale mLocale = new Locale(language, country);
                    LanguageUtils.updateLanguage(mLocale);
                }
            }
        };
        elist.add(de);
        de = new devent("/apps/do_factory_reset") {
            @Override
            public void process(String body) {
                dmsg.ack(200, null);
                loadDevModel();
                utils.deleteDirectory("/dnake/data/tuya");
                doDataClear();
                sys.resetDesktopMain();
                //设置模型model, tuya相关
                setDevModel();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread thr = new Thread("Reboot") {
                    @Override
                    public void run() {
                        try {
                            RecoverySystem.rebootWipeUserData(ctx);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thr.start();
            }
        };
        elist.add(de);
    }

    private static Boolean isLock = false;

    private static void loadTuYaQr() {
//        if (!sys.tuya.qr.isEmpty()) {
//            showQr();
//        }
        if (isLock) return;
        if (!utils.isNetworkConnected()) {
            ToastUtils.show(apps.ctx.getString(R.string.net_err));
            return;
        }
        IDeviceManager iDeviceManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.DEVICE_SERVICE);
        if (iDeviceManager != null) {
            int regStat = iDeviceManager.getRegisterStatus();
            Thread t = new Thread(() -> {
                isLock = true;
                String code = iDeviceManager.getQrCode("19");  //没有网络会卡住
                isLock = false;
                if (!TextUtils.isEmpty(code) && TextUtils.isEmpty(sys.tuya.qr)) {
                    sys.tuya.qr = code;
//                    sys.save();
//                    Log.d(TAG, "qrcode: " + code);
                    dmsg req = new dmsg();
                    dxml p = new dxml();
                    p.setText("/params/uuid", TUYA_UUID);
                    p.setText("/params/key", TUYA_AUTHKEY);
                    p.setText("/params/qrcode", sys.tuya.qr);
                    req.to("/ui/tuya/info/write", p.toString());

                    Intent it = new Intent("com.dnake.broadcast");
                    it.putExtra("event", "set_tuya_qr");
                    it.putExtra("tuya_qr", sys.tuya.qr);
                    ctx.sendBroadcast(it);
                }
            });
            t.start();
        }
    }

    private static void loadDevModel() {
        dxml p = new dxml();
        boolean result = p.load("/dnake/cfg/sys.xml");
        if (result) {
            sys.tuya.TUYA_UUID = p.getText("/sys/tuya/uuid", sys.tuya.TUYA_UUID);
            sys.tuya.TUYA_AUTHKEY = p.getText("/sys/tuya/key", sys.tuya.TUYA_AUTHKEY);
            sys.tuya.qr = p.getText("/sys/tuya/qr", sys.tuya.qr);
            sys.special_advanced.model = p.getText("/sys/special_advanced/model", "Android Indoor Monitor");
        } else {
            sys.special_advanced.model = "Android Indoor Monitor";
        }
    }

    private static void setDevModel() {
        dxml p = new dxml();
        p.setText("/sys/tuya/uuid", sys.tuya.TUYA_UUID);
        p.setText("/sys/tuya/key", sys.tuya.TUYA_AUTHKEY);
        p.setText("/sys/tuya/qr", sys.tuya.qr);
        p.setText("/sys/special_advanced/model", sys.special_advanced.model);
        p.save("/dnake/cfg/sys.xml");
    }
}
