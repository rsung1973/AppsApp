package com.dnake.utils;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.dnake.apps.R;
import com.dnake.v700.sys;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DisplayUtils {
    public static void setScreenBrightness(Activity activity, int process) {
        //设置当前窗口的亮度值.这种方法需要权限android.permission.WRITE_EXTERNAL_STORAGE
        WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
        float f = process / 255.0F;
        localLayoutParams.screenBrightness = f;
        activity.getWindow().setAttributes(localLayoutParams);
        //修改系统的亮度值,以至于退出应用程序亮度保持
        saveBrightness(activity.getContentResolver(), process);
    }

    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }

    public static void setScreenSleepTime(Context context, int millisecond) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                    millisecond);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public static void setFontSize(Context context, int index) {
        float fScale = Float.parseFloat(context.getResources().getStringArray(R.array.entryvalues_font_size)[index]);
        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object am = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Object config = am.getClass().getMethod("getConfiguration").invoke(am);
            config.getClass().getDeclaredField("fontScale").set(config, fScale);
            am.getClass().getMethod("updatePersistentConfiguration", android.content.res.Configuration.class).invoke(am, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度
     */
    public static int getScreenMode(Context context) {
        int screenMode = 0;
        try {
            screenMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception localException) {
        }
        return screenMode;
    }

    /**
     * 获得当前屏幕亮度值  0--255
     */
    public static int getScreenBrightness(Context context) {
        int screenBrightness = 209;
        try {
            screenBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return screenBrightness;
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度
     */
    public static void setScreenMode(Context context, int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * @param context
     * @return
     */
    public static int getScreenSleepTime(Context context) {
        int millisecond = 0;
        try {
            millisecond = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return millisecond;
    }

    public static int getFontSizeIndex(Context context) {
        int fontSizeIndex = 0;
        float fScale = context.getResources().getConfiguration().fontScale;
        String[] fontSizeVals = context.getResources().getStringArray(R.array.entryvalues_font_size);
        for (int i = 0; i < fontSizeVals.length; i++) {
            float tmp = Float.parseFloat(fontSizeVals[i]);
            if (tmp == fScale) {
                fontSizeIndex = i;
            }
        }
        return fontSizeIndex;
    }


    public static void screenOff(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", new Class[]{long.class}).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void setBrightnessDefaultFalse() {
        String val = "0";
        try {
            FileOutputStream out = new FileOutputStream("/dnake/cfg/is_brightness_default");
            out.write(val.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBrightnessDefault() {
        String val = "";
        try {
            FileInputStream in = new FileInputStream("/dnake/cfg/is_brightness_default");
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) != -1) {
                val = new String(bytes, 0, length);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (TextUtils.isEmpty(val) || val.equals("1")) ? true : false;
    }
}
