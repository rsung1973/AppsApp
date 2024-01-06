package com.dnake.apps;

import com.dnake.setting.activity.SettingsBaseActivity;
import com.dnake.tuya.TuYa_ipc;
import com.dnake.utils.Utils;
import com.dnake.v700.apps;
import com.dnake.v700.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class SysReceiver extends BroadcastReceiver {
    public static int mute = 0;
    public static int dnd = 0;

    @Override
    public void onReceive(Context ctx, Intent it) {
        String a = it.getAction();
        if (a.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent = new Intent(ctx, apps.class);
            ctx.startService(intent);
        } else if (a.equals("com.dnake.broadcast")) {
            String e = it.getStringExtra("event");
            if (e.equals("com.dnake.boot"))
                apps.broadcast();
            else if (e.equals("com.dnake.talk.touch")) {
                WakeTask.refresh();
                IpcLabel.bStart = false;
                AdLabel.bStart = false;
                if (AdLabel.mCtx != null && !AdLabel.mCtx.isFinishing()) {
                    AdLabel.mCtx.finish();
                    AdLabel.mCtx = null;
                }
            } else if (e.equals("set_desktop_bg")) {//背景更新
                SettingsBaseActivity.layoutMain.setBackground(Utils.path2Drawable(apps.ctx, Utils.getDesktopBgPath()));
            } else if (e.equals("set_unlock_relays")) {//unlock relay设置
                long currentTime = System.currentTimeMillis();
                if (currentTime - TuYa_ipc.set_lastTime > 500) {
                    TuYa_ipc.set_lastTime = currentTime;
                    sys.unlock_relay = it.getStringExtra("unlock_relays");
                    TuYa_ipc.setUnlockRelays(sys.unlock_relay);
                }
            } else if (e.equals("com.dnake.talk.mute")) {
                mute = it.getIntExtra("mute", 0);
                dnd = it.getIntExtra("dnd", 0);
            }
        }
    }
}
