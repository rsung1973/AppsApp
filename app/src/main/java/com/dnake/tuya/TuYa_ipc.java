package com.dnake.tuya;

import static com.dnake.tuya.utils.clearTuya;
import static com.dnake.tuya.utils.tuyaJson;
import static com.dnake.v700.sys.tuya.TUYA_AUTHKEY;
import static com.dnake.v700.sys.tuya.TUYA_UUID;
import static com.dnake.v700.sys.tuya.channel;
import static com.tuya.smart.aiipc.ipc_sdk.api.Common.AudioSampleRate.TUYA_AUDIO_SAMPLE_8K;
import static com.tuya.smart.aiipc.ipc_sdk.api.Common.AuduiBitRate.TUYA_AUDIO_DATABITS_16;
import static com.tuya.smart.aiipc.ipc_sdk.api.Common.DOORBELL_NOTIFICATION_TYPE.NOTIFICATION_NAME_DOORBELL;
import static com.tuya.smart.aiipc.ipc_sdk.api.Common.NOTIFICATION_CONTENT_TYPE_E.NOTIFICATION_CONTENT_JPEG;
import static com.tuya.smart.aiipc.ipc_sdk.api.Common.NOTIFICATION_CONTENT_TYPE_E.NOTIFICATION_CONTENT_PNG;
import static com.tuya.smart.aiipc.ipc_sdk.callback.DPConst.Type.PROP_BOOL;
import static com.tuya.smart.aiipc.ipc_sdk.callback.DPConst.Type.PROP_ENUM;
import static com.tuya.smart.aiipc.ipc_sdk.callback.DPConst.Type.PROP_RAW;
import static com.tuya.smart.aiipc.ipc_sdk.callback.DPConst.Type.PROP_STR;
import static com.tuya.smart.aiipc.ipc_sdk.callback.DPConst.Type.PROP_VALUE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dnake.apps.R;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sCaller;
import com.dnake.v700.sys;
import com.dnake.widget.Storage;
import com.hjq.toast.ToastUtils;
import com.tuya.smart.aiipc.ipc_sdk.IPCSDK;
import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IControllerManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IDeviceManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IFeatureManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMqttProcessManager;
import com.tuya.smart.aiipc.ipc_sdk.api.INetConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IParamConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.callback.DPConst;
import com.tuya.smart.aiipc.ipc_sdk.callback.DPEventSimpleCallback;
import com.tuya.smart.aiipc.ipc_sdk.callback.IP2PEventCallback;
import com.tuya.smart.aiipc.ipc_sdk.callback.NetConfigCallback;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;
import com.tuya.smart.aiipc.netconfig.ConfigProvider;
import com.tuya.smart.aiipc.netconfig.mqtt.TuyaNetConfig;
import com.tuya.smart.aiipc.trans.TransJNIInterface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

public class TuYa_ipc {
    private static final String TAG = "TuYa_ipc";

    private static final String TUYA_PID = "smawkmfi4m7jjp4p";
    private static final int TUYA_DP_DOOR_BELL = 136;
    private static final int TUYE_DP_LOCK = 148;
    private static final int TUYE_DP_BELL_CALL = 231;
    public static final int TUYE_DP_SWITCH_CHANNEL = 232;
    public static final int TUYE_DP_DOUBLE_LOCK = 236;

    public static final int TUYE_DP_LOCK_DIY_1 = 233;
    public static final int TUYE_DP_LOCK_DIY_2 = 234;
    public static final int TUYE_DP_LOCK_DIY_3 = 235;

    public static BufferedOutputStream bos;
    public static int tuyaStatus = 0;
    public static boolean isOut = false;
    public static Boolean speakStatus = false;
    public static int BellStatus = -1;
    public static boolean isReport = false;
    public static int mqttStatus = 0;


    public static boolean isTuya(boolean isShow) {
        if (!TUYA_UUID.isEmpty() && !TUYA_AUTHKEY.isEmpty()) {
            return true;
        } else {
            if (isShow && apps.ctx != null) {
                ToastUtils.show(apps.ctx.getString(R.string.uuid_isEmpty));
            }
            return false;
        }
    }

    public static void initSDK(Context context) {
        searchPanel();
        IPCSDK.initSDK(context);
        LoadParamConfig();
        INetConfigManager iNetConfigManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.NET_CONFIG_SERVICE);
        iNetConfigManager.setPID(TUYA_PID);
        iNetConfigManager.setUserId(TUYA_UUID);
        iNetConfigManager.setAuthorKey(TUYA_AUTHKEY);
        TuyaNetConfig.setDebug(true);
        ConfigProvider.enableMQTT(true);
        ConfigProvider.enableQR(false);
        ConfigProvider.enableBluetooth(false);
        Handler mHandler = new Handler();
        IPCServiceManager.getInstance().setResetHandler(isHardward -> {

            if (mHandler != null) {
                //提示
                ToastUtils.show("This device has been removed!");
                clearTuya();
//                sCaller.stopTalk();
                dmsg req = new dmsg();
                req.to("/ui/tuya/stop_talk", null);
                SPUtils.setRestart(apps.ctx, true);

                mHandler.postDelayed(() -> {
                    try {
                        Intent mStartActivity = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        if (mStartActivity != null) {
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);

                            Runtime.getRuntime().exit(0);
                        }
//                        initSDK(apps.ctx);
                        apps.reboot();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, 1500);
            }
        });
        NetConfigCallback netConfigCallback = new NetConfigCallback() {
            @Override
            public void configOver(boolean first, String token) {
                IMediaTransManager transManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                IMqttProcessManager mqttProcessManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MQTT_SERVICE);
                IMediaTransManager mediaTransManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                IFeatureManager featureManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.FEATURE_SERVICE);
                transManager.initTransSDK(token, "/dnake/data/tuya/", "/dnake/data/tuya/video/", TUYA_PID, TUYA_UUID, TUYA_AUTHKEY);

                featureManager.initDoorBellFeatureEnv();
                mqttProcessManager.setMqttStatusChangedCallback(status -> {
                    Log.i("aaa", "_________________mHandler.postDelayed((_________________mqttProcessManager.setMqttStatusChangedCallback(status __" + TUYA_UUID);
                    mqttStatus = status;
                    if (mqttStatus == Common.MqttConnectStatus.STATUS_CLOUD_CONN) {
                        transManager.startMultiMediaTrans(5);
                    }
                    if (!TextUtils.isEmpty(sys.unlock_relay) && !sys.has_set_unlock_relay) {
                        TuYa_ipc.setUnlockRelays(sys.unlock_relay);
                    }
                });

                IDeviceManager iDeviceManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.DEVICE_SERVICE);
                iDeviceManager.setRegion(IDeviceManager.IPCRegion.REGION_CN);

                mediaTransManager.setDoorBellCallStatusCallback(status -> {
                    BellStatus = status;
                    if (status == 1) {
                        PullVideo.getInstance().stop();
//                        sCaller.stopTalk();
                        dmsg req = new dmsg();
                        req.to("/ui/tuya/stop_talk", null);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PullAudio.stop();
                        tuyaStatus = 0;
                    }
                });

                mediaTransManager.addAudioTalkCallback(bytes -> {
                    PullAudio.send(bytes, bytes.length);

                });
                mediaTransManager.setP2PEventCallback((p2PEvent, o) -> {
                    dmsg req = new dmsg();
//                    Log.i("aaa", "_________________TRANS_LIVE_VIDEO_000000000000____" + tuyaStatus + "_____________" + p2PEvent);
                    switch (p2PEvent) {
                        case TRANS_LIVE_VIDEO_START:
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_START___1111____" + tuyaStatus);
                            dxml p = new dxml();
                            req.to("/ui/tuya/is_talking", null);
                            p.parse(req.mBody);
                            int mTalking = p.getInt("/params/is_talking", 0);
                            p = new dxml();
                            req.to("/ui/tuya/is_monitoring", null);
                            p.parse(req.mBody);
                            int mMRunning = p.getInt("/params/is_monitoring", 0);
                            if (tuyaStatus == 1 && mTalking != 1) {
                                startVideo();
                                startAudio();
                                tuyaStatus = 2;

                            } else if (tuyaStatus == 0 && mMRunning == 0 && !isOut) {
                                tuyaStatus = 3;
                                hand.sendEmptyMessageDelayed(0, 1000);
                            }
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_START____22222___" + tuyaStatus);
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_START____22222___mTalking_____" + mTalking);
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_START____22222___mMRunning_____" + mMRunning);
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_START____22222___isOut_____" + isOut);
                            break;
                        case TRANS_LIVE_VIDEO_STOP:
//                            Log.i("aaa", "_________________TRANS_LIVE_VIDEO_STOP_______" + tuyaStatus);
                            if (tuyaStatus == 2 || tuyaStatus == 3) {
                                PullVideo.getInstance().stop();
//                                sCaller.stopTalk();
                                req.to("/ui/tuya/stop_talk", null);
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                PullAudio.stop();
                            }

                            break;
                        case TRANS_SPEAKER_STOP:
                            speakStatus = false;
                            break;
                        case TRANS_SPEAKER_START:
                            speakStatus = true;
                            break;
                        case TRANS_LIVE_AUDIO_START:

                            break;
                        case TRANS_LIVE_AUDIO_STOP:
                            break;
                        default:
                            break;
                    }
                });
                IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
                controllerManager.setDpEventSimpleCallback((o, i, l) -> {
                    dmsg req = new dmsg();
                    switch (i) {
                        case 149:
                            return new DPConst.DPResult(true, PROP_BOOL);
                        case TUYE_DP_LOCK:
//                            sCaller.unlock();
                            req.to("/ui/tuya/unlock", null);

                            hander.sendEmptyMessageDelayed(0, 2000);
                            return new DPConst.DPResult(true, PROP_BOOL);
                        case TUYE_DP_LOCK_DIY_1:
                            dxml p1 = new dxml();
                            p1.setInt("/params/unlock_id", 0);
                            req.to("/ui/tuya/unlock", p1.toString());
                            Message msg1 = new Message();
                            msg1.what = 1;
                            msg1.arg1 = TUYE_DP_LOCK_DIY_1;
                            hander.sendMessageDelayed(msg1, 2000);
//                            hander.sendEmptyMessageDelayed(1, 2000);
                            return new DPConst.DPResult(true, PROP_BOOL);
                        case TUYE_DP_LOCK_DIY_2:
                            dxml p2 = new dxml();
                            p2.setInt("/params/unlock_id", 1);
                            req.to("/ui/tuya/unlock", p2.toString());
                            Message msg2 = new Message();
                            msg2.what = 1;
                            msg2.arg1 = TUYE_DP_LOCK_DIY_2;
                            hander.sendMessageDelayed(msg2, 2000);
                            return new DPConst.DPResult(true, PROP_BOOL);
                        case TUYE_DP_LOCK_DIY_3:
                            dxml p3 = new dxml();
                            p3.setInt("/params/unlock_id", 2);
                            req.to("/ui/tuya/unlock", p3.toString());
                            Message msg3 = new Message();
                            msg3.what = 1;
                            msg3.arg1 = TUYE_DP_LOCK_DIY_3;
                            hander.sendMessageDelayed(msg3, 2000);
                            return new DPConst.DPResult(true, PROP_BOOL);
                        case TUYE_DP_SWITCH_CHANNEL:
                            MenKouJiIdAdapter.LoadTuyaBean bean = utils.getLoadTuyaBean(o.toString());
                            MenKouJiIdAdapter.UploadTuYaBean uploadTuYaBean = new MenKouJiIdAdapter.UploadTuYaBean();
                            if (tuyaStatus == 1 || tuyaStatus == 2) {
                                ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>(Arrays.asList(bean.getChs()));
                                uploadTuYaBean.setChs(chsList);
                                uploadTuYaBean.setCc(bean.getCc());
                                uploadTuYaBean.setRes(0);
                                uploadTuYaBean.setErr(101);
                                String str = tuyaJson(uploadTuYaBean);
                                return new DPConst.DPResult(str, PROP_STR);
                            }

                            if (bean.getCmd() == 1) {
                                if (TuyaDeviceLogger.hasLive(bean.getCc())) {
                                    Log.i("aaa", "_________________mHandler.postDelayed((_________________TuyaDeviceLogger.hasLive(bean.getCc()) 1111111111111111111111_______" + TuyaDeviceLogger.hasLive(bean.getCc()));
                                    req.to("/ui/tuya/stop", null);
//                                    sCaller.stop(true);
                                    tuyaStatus = 0;
                                    ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>(Arrays.asList(bean.getChs()));
                                    uploadTuYaBean.setChs(chsList);
                                    uploadTuYaBean.setCc(bean.getCc());
                                    uploadTuYaBean.setRes(1);
                                    uploadTuYaBean.setErr(0);
                                    TuyaDeviceLogger.switchChannel(bean.getCc());
                                    apps.start_monitor();
                                    tuyaStatus = 3;
                                } else {
                                    ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>(Arrays.asList(bean.getChs()));
                                    uploadTuYaBean.setChs(chsList);
                                    uploadTuYaBean.setCc(bean.getCc());
                                    uploadTuYaBean.setRes(0);
                                    uploadTuYaBean.setErr(102);
                                }
                            } else if (bean.getCmd() == 2) {
                                ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>(Arrays.asList(bean.getChs()));
                                for (int j = 0; j < chsList.size(); j++) {
                                    TuyaDeviceLogger.modifyName(chsList.get(j).getId(), chsList.get(j).getN());
                                }
                                uploadTuYaBean.setChs(chsList);
                                uploadTuYaBean.setCc(bean.getCc());
                                uploadTuYaBean.setRes(1);
                                uploadTuYaBean.setErr(0);
                            } else {
                                ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>(Arrays.asList(bean.getChs()));
                                uploadTuYaBean.setChs(chsList);
                                uploadTuYaBean.setCc(bean.getCc());
                                uploadTuYaBean.setRes(0);
                                uploadTuYaBean.setErr(104);
                            }
                            return new DPConst.DPResult(tuyaJson(uploadTuYaBean), PROP_STR);

                    }
                    return null;
                });

                syncTimeZone();
            }

            @Override
            public void startConfig() {
                searchPanel();
                isReport = true;
            }

            @Override
            public void recConfigInfo() {
            }


            @Override
            public void onNetConnectFailed(int type, String msg) {
            }

            @Override
            public void onNetPrepareFailed(int type, String msg) {
            }
        };

        iNetConfigManager.configNetInfo(netConfigCallback);
    }


    private static void startVideo() {
        PullVideo.getInstance().start();
        dxml p = new dxml();
        dmsg req = new dmsg();
        p.setInt("/params/x", 0);
        p.setInt("/params/y", 0);
        p.setInt("/params/width", 0);
        p.setInt("/params/height", 0);
        req.to("/talk/vo_start", p.toString());
    }

    private static void startAudio() {
        dxml p = new dxml();
        dmsg req = new dmsg();
        p.setInt("/params/mode", 1);
        req.to("/talk/audio/ext/start", p.toString());
        PullAudio.start();
    }


    private static void LoadParamConfig() {
        IParamConfigManager configManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_PARAM_SERVICE);

        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_WIDTH, 1280);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_HEIGHT, 720);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_FRAME_RATE, 24);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_I_FRAME_INTERVAL, 2);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.ParamKey.KEY_VIDEO_BIT_RATE, 1024000);

        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_CHANNEL_NUM, 1);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_SAMPLE_RATE, TUYA_AUDIO_SAMPLE_8K);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_SAMPLE_BIT, TUYA_AUDIO_DATABITS_16);
        configManager.setInt(Common.ChannelIndex.E_CHANNEL_AUDIO, Common.ParamKey.KEY_AUDIO_FRAME_RATE, 25);
    }

    private static void syncTimeZone() {
        int rawOffset = TransJNIInterface.getInstance().getAppTimezoneBySecond();
        String[] availableIDs = TimeZone.getAvailableIDs(rawOffset * 1000);
        if (availableIDs.length > 0) {
            Log.d(TAG, "syncTimeZone: " + rawOffset + " , " + availableIDs[0] + " ,  ");
        }
    }


    public static void sendDoorBell() {
        if (!isTuya(false) || mqttStatus != 7) return;
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        controllerManager.dpReport(TUYE_DP_BELL_CALL, PROP_STR, "");
//        IMediaTransManager mediaTransManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
//        mediaTransManager.sendDoorBellCallForPress(new byte[0], Common.NOTIFICATION_CONTENT_TYPE_E.NOTIFICATION_CONTENT_PNG);
        tuyaStatus = 1;
    }


    public static void sendDoorCancel() {
        if (!isTuya(false) || mqttStatus != 7) return;
        IMediaTransManager mediaTransManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
        mediaTransManager.sendDoorBellCancelForPress();
    }

    private static Handler hander = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
//            Looper.prepare();
                sendDoorLock();
//            doubleLock();
//            Looper.loop();
            } else {
                sendDoorLockById(msg.arg1);
            }
        }
    };

    private static Handler hand = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            sCaller.mmm();
            if (!TextUtils.isEmpty(TuyaDeviceLogger.getChannelUrl())) {
                dmsg req = new dmsg();
                dxml p = new dxml();
                p.setText("/params/channel_url", TuyaDeviceLogger.getChannelUrl());
                req.to("/ui/tuya/mmm", p.toString());
                startVideo();
                startAudio();
            } else {
                //提示
            }
        }
    };


    public static void sendDoorLock() {
        if (!isTuya(false) || mqttStatus != 7) return;
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        controllerManager.dpReport(TUYE_DP_LOCK, PROP_BOOL, false);
    }

    public static void sendDoorLockById(int id) {
        if (!isTuya(false) || mqttStatus != 7) return;
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        controllerManager.dpReport(id, PROP_BOOL, false);
    }

    public static long set_lastTime = 0;

    public static void setUnlockRelays(String str) {
        if (!isTuya(false) || mqttStatus != 7) return;
        String doorIds = "";
        sys.has_set_unlock_relay = true;
        if (!TextUtils.isEmpty(str)) {
            String[] array = str.split(";");
            for (int i = 0; i < array.length; i++) {
                switch (array[i]) {
                    case "0":
                        doorIds = doorIds + TUYE_DP_LOCK_DIY_1 + ",";
                        break;
                    case "1":
                        doorIds = doorIds + TUYE_DP_LOCK_DIY_2 + ",";
                        break;
                    case "2":
                        doorIds = doorIds + TUYE_DP_LOCK_DIY_3 + ",";
                        break;
                }
            }
        }
        doorIds = TextUtils.isEmpty(doorIds) ? "" : doorIds.substring(0, doorIds.length() - 1);
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        controllerManager.dpReport(TUYE_DP_DOUBLE_LOCK, PROP_STR, doorIds);
        Log.i("aaa", "__________TUYE_DP_DOUBLE_LOCK________________" + doorIds);
    }

    public static void doubleLock() {
        if (!isTuya(false) || mqttStatus != 7) return;
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        controllerManager.dpReport(TUYE_DP_DOUBLE_LOCK, PROP_STR, TUYE_DP_LOCK_DIY_1 + "," + TUYE_DP_LOCK_DIY_2 + "," + TUYE_DP_LOCK_DIY_3);
    }


    public static void closeTuya() {

        SPUtils.setUpgrade(true);
        new Thread(() -> h.sendEmptyMessageDelayed(0, 500)).start();

    }

    static Handler h = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Runtime.getRuntime().exit(0);
        }
    };

    public static void searchPanel() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            dxml p = new dxml();
            p.setText("/event/active", "search");
            p.setText("/event/type", "req");
            byte[] data = p.toString().getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 8400);
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) {
            socket.close();
        }
    }
}
