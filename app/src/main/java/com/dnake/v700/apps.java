package com.dnake.v700;

import com.dnake.IPStatusReceiver;
import com.dnake.apps.AdLabel;
import com.dnake.apps.IpcLabel;
import com.dnake.apps.R;
import com.dnake.apps.SysReceiver;
import com.dnake.apps.WakeTask;
import com.dnake.logger.TextLogger;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.message.activity.MessageActivity;
import com.dnake.test.PicPopupDialog;
import com.dnake.tuya.PullAudio;
import com.dnake.tuya.PullVideo;
import com.dnake.tuya.TuYa_ipc;
import com.dnake.tuya.utils;
import com.dnake.utils.DisplayUtils;
import com.dnake.utils.FileUtils;
import com.dnake.utils.SoundUtils;
import com.dnake.utils.Utils;
import com.hjq.toast.ToastUtils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class apps extends Service {
    public static int version_major = 1; // 主版本
    public static int version_minor = 0; // 次版本
    public static int version_minor2 = 1; // 次版本2

    public static String version_date = "20160330"; // 日期

    public static String version_ex = "(std)"; // 扩展标注
    public static String version = "";

    public static final int TYPE_PERSON = 1;
    public static final int TYPE_PANEL = 2;

    public static String url = "/dnake/cfg/apps.xml";

    public static PowerManager mPM = null;

    public static final class ad { //在线广告
        public static int enable = 0;
        public static String url = "http://192.168.12.40/ad.html";
        public static int timeout = 5 * 60;
    }

    public static int autoIpc = 0;

    public static String brower = "about:home";
    public static String mall = "about:home";
    public static String stock = "about:home";
    public static String cook = "about:home";
    public static String map = "about:home";

    private SysReceiver sysReceiver;

    public class qResult {
        public sip sip = new sip();
        public d600 d600 = new d600();
        public int result = 0;

        public class sip {
            public String url = null;
            public int proxy;
        }

        public class d600 {
            public String ip = null;
            public String host = null;
        }
    }

    public static qResult qResult = null;

    public static void load() {
        dxml p = new dxml();
        if (p.load(url)) {
            ad.enable = p.getInt("/apps/ad/enable", 0);
            ad.url = p.getText("/apps/ad/url", ad.url);
            ad.timeout = p.getInt("/apps/ad/timeout", 5 * 60);

            autoIpc = p.getInt("/apps/autoIpc", 0);

            brower = p.getText("/apps/brower", brower);
            mall = p.getText("/apps/mall", mall);
            stock = p.getText("/apps/stock", stock);
            cook = p.getText("/apps/cook", cook);
            map = p.getText("/apps/map", map);
        } else save();
    }

    public static void save() {
        dxml p = new dxml();

        p.setInt("/apps/ad/enable", ad.enable);
        p.setText("/apps/ad/url", ad.url);
        p.setInt("/apps/ad/timeout", ad.timeout);

        p.setInt("/apps/autoIpc", autoIpc);

        p.setText("/apps/brower", apps.brower);
        p.setText("/apps/mall", apps.mall);
        p.setText("/apps/stock", apps.stock);
        p.setText("/apps/cook", apps.cook);
        p.setText("/apps/map", apps.map);

        p.save(url);
    }

    public static Boolean isScreenOn() {
        if (mPM != null) return mPM.isScreenOn();
        return false;
    }

    private static long test_current = -1;
    static PicPopupDialog picPopupDialog;

    public static class ProcessThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                //===================================
//                //测试
//                WakeTask.acquire();//永不休眠
//                if (test_current == -1) {
//                    test_current = System.currentTimeMillis();
//                } else if (System.currentTimeMillis() - test_current >= 60 * 60 * 60 * 1000 && !picPopupDialog.isShowing()) {
//                    //弹出全屏图片
//                    picPopupDialog.show();
//                    test_current = -1;
//                }
                //===================================
                if (ad.enable != 0 && AdLabel.bStart == false) {
                    int st = 0;
                    if (sys.limit() >= 970 && sys.limit() < 980) {
                    } else {
                        dmsg req = new dmsg();
                        dxml p = new dxml();
                        req.to("/security/alarm", null);
                        p.parse(req.mBody);
                        st = p.getInt("/params/have", 0);
                    }
                    if (st == 0 && isScreenOn() == false) {
                        AdLabel.bStart = true;
                        Intent it = new Intent(ctx, AdLabel.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(it);
                    }
                } else {
                    if (apps.autoIpc != 0 && IpcLabel.bStart == false && isScreenOn() == false) {
//                        IpcLabel.bStart = true;
//                        dxml p = new dxml();
//                        p.load("/dnake/cfg/ipc.xml");
//                        IpcLabel.mUrl = p.getText("/sys/r0/url");
//                        if (IpcLabel.mUrl != null) {
//                            Intent it = new Intent(apps.ctx, IpcLabel.class);
//                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            apps.ctx.startActivity(it);
//                        }
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public static NotificationManager nManager;
    private static Handler e_notify = null;
    public static String notifyText = null;

    public static void notifyText(String text) {
        notifyText = text;
        if (e_notify != null) e_notify.sendMessage(e_notify.obtainMessage());
    }

    public static void broadcast() {
        if (apps.ctx != null) {
            Intent it = new Intent("com.dnake.broadcast");
            it.putExtra("event", "com.dnake.apps.sms");
            it.putExtra("nRead", TextLogger.nRead());
            ctx.sendBroadcast(it);
        }
    }

    public static void notifyCancel() {
        nManager.cancelAll();
    }

    public static Context ctx = null;

    @Override
    public void onCreate() {
        super.onCreate();
        apps.ctx = this;
        ToastUtils.init(getApplication());
        Utils.execSHFile();
        version = Utils.getVersionNameAll();

        apps.qResult = new qResult();

        dmsg.start("/apps");
        devent.setup();
        apps.load();
        sys.load();
        sys.initDevSet();
        sound.load();

        TextLogger.load();
        TuyaDeviceLogger.load();

        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        e_notify = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (notifyText == null) return;

                WakeTask.acquire();

                Intent i = new Intent(ctx, MessageActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, 0);

                String title = ctx.getString(R.string.logger_notify_title);

//				Notification n = new Notification();
//				n.icon = android.R.drawable.ic_dialog_email;
//				n.tickerText = title;
//				n.defaults |= Notification.DEFAULT_SOUND;
//				n.when = System.currentTimeMillis();
//				n.setLatestEventInfo(ctx, title, notifyText, pi);

                Notification n = new Notification.Builder(ctx).setSmallIcon(android.R.drawable.ic_dialog_email).setContentTitle(title).setTicker(title).setContentText(notifyText).setWhen(System.currentTimeMillis()).setContentIntent(pi).setNumber(0).getNotification();
                n.flags |= Notification.FLAG_AUTO_CANCEL;

                nManager.notify(0, n);

                notifyText = null;
            }
        };

        mPM = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        picPopupDialog = new PicPopupDialog(apps.ctx).builder().setCancelable(false).setCanceledOnTouchOutside(false);

        ProcessThread pt = new ProcessThread();
        Thread t = new Thread(pt);
        t.start();

        apps.broadcast();

        devent.boot = true;
        if (DisplayUtils.isBrightnessDefault()) {
            DisplayUtils.setBrightnessDefaultFalse();
            DisplayUtils.saveBrightness(apps.ctx.getContentResolver(), 209);
        }
        SoundUtils.setDefaultRingVol(apps.ctx);

        if (TuYa_ipc.isTuya(false)) {//涂鸦初始化
            TuYa_ipc.initSDK(apps.this);
        }

        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            sysReceiver = new SysReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.addAction("com.dnake.broadcast");
            registerReceiver(sysReceiver, filter);
        }

        //监听ip状态
        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        actionFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        actionFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        actionFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        actionFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        actionFilter.addAction("android.net.ethernet.STATE_CHANGE");
        actionFilter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        registerIPReceiver(apps.ctx, actionFilter);

        if (!isAppInstalled()) {
            installChromeApk();
        } else {
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
                dxml p = new dxml();
                dmsg req = new dmsg();
                p = new dxml();
                p.setText("/params/cmd", "rm " + chromeFilePath);
                req.to("/upgrade/root/cmd", p.toString());
            }
        }
        sys.loadUnlockRelays();
    }

    IPStatusReceiver ipReceiver;

    public void registerIPReceiver(Context context, IntentFilter intentFilter) {
        if (null == ipReceiver) {
            ipReceiver = new IPStatusReceiver(context);
        }
        context.registerReceiver(ipReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nManager.cancelAll();
        if (ipReceiver != null) {
            ctx.unregisterReceiver(ipReceiver);
        }
    }

    public static void reboot() {
        e_reboot.sendEmptyMessageDelayed(0, 1500);
    }

    private static Handler e_reboot = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            utils.reboot();
        }
    };

    public static void start_monitor() {
//        Log.e("jamie", "start_monitor");
        e_monitor_start.sendMessageDelayed(e_monitor_start.obtainMessage(), 1000);
    }

    private static Handler e_monitor_start = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            Log.e("jamie", "start_monitor2");
            dmsg req = new dmsg();
//            sCaller.mmm();
            dxml p = new dxml();
            p.setText("/params/channel_url", TuyaDeviceLogger.getChannelUrl());
            req.to("/ui/tuya/mmm", p.toString());

            PullVideo.getInstance().start();
            p = new dxml();
            p.setInt("/params/x", 0);
            p.setInt("/params/y", 0);
            p.setInt("/params/width", 0);
            p.setInt("/params/height", 0);
//            Log.e("jamie", "/talk/vo_start:" + p.toString());
            req.to("/talk/vo_start", p.toString());

            p = new dxml();
            p.setInt("/params/mode", 1);
//            Log.e("jamie", "/talk/audio/ext/start:" + p);
            req.to("/talk/audio/ext/start", p.toString());
            PullAudio.start();
            TuYa_ipc.tuyaStatus = 3;
        }
    };

    //安装谷歌浏览器
    private void installChromeApk() {
        String apkPathUSB = "/product/usr/chrome.apk";
        String apkPath = "/storage/emulated/0/Download/chrome.apk";
        if (apps.version.contains("902")) {
            apkPath = "/storage/emulated/legacy/Download/chrome.apk";
        } else {
            apkPath = "/storage/emulated/0/Download/chrome.apk";
        }
        File apkUsb = new File(apkPathUSB);
        if (apkUsb.exists()) {
            dmsg req = new dmsg();
            dxml p = new dxml();
            p.setText("/params/path", apkPathUSB);
            req.to("/apps/web/install_apk", p.toString());
        } else {
            File apk = new File(apkPath);
            if (apk.exists()) {
                dmsg req = new dmsg();
                dxml p = new dxml();
                p.setText("/params/path", apkPath);
                req.to("/apps/web/install_apk", p.toString());
            }
        }
    }

    private boolean isAppInstalled() {
        PackageInfo packageInfo;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(
                    "com.android.chrome",
                    0);//APPNAME应用包名
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    private void aaa() {
    }
}
