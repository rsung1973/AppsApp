package com.dnake.tuya;

import android.content.Context;
import android.content.SharedPreferences;

import com.dnake.v700.apps;


/**
 * SharedPreferences 工具类
 */
public class SPUtils {
    public static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences("tuya_sp", Context.MODE_PRIVATE);
    }


    public static Boolean getUpgrade() {

        return getSp(apps.ctx).getBoolean("upgrade", false);
    }

    public static void setUpgrade(Boolean flag) {
        getSp(apps.ctx).edit().putBoolean("upgrade", flag).apply();
    }

    public static void setRestart(Context context, Boolean flag) {

        getSp(context).edit().putBoolean("restart", flag).apply();

    }

    public static Boolean getRestart(Context context) {
        return getSp(context).getBoolean("restart", false);
    }
//    public static Boolean getRestart(){
//        return getSp(talk.mContext).getBoolean("restart",false);
//    }

    public static String getSPString(Context context, String key, String defaultValue) {
        return getSp(context).getString(key, defaultValue);
    }

    public static void putSPString(Context context, String key, String value) {
        getSp(context).edit().putString(key, value).commit();
    }

}