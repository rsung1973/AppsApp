package com.dnake.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    public static final String HOURS_12 = "12";
    public static final String HOURS_24 = "24";

    public static boolean is24Hour(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    public static void set24Hour(Context context, boolean is24Hour) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour ? HOURS_24 : HOURS_12);
    }

    public static String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }

    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /* package */
    public static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /* package */
    public static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    public static void setTime(Context context, int hourOfDay, int minute, int sec) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, sec);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    public static void setTime(Context context, int year, int month, int day, int hour, int minute, int second) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar s = Calendar.getInstance();
        s.set(year, month - 1, day, hour, minute, second);
        mAlarmManager.setTime(s.getTimeInMillis());
    }

    public static void setTimeZone(Context context, String timezoneId) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(timezoneId);
    }

    public static String getTimeZoneID() {
        Calendar now = Calendar.getInstance();
        return now.getTimeZone().getID();
    }

    public static boolean isAutoTime(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME) > 0;
        } catch (Settings.SettingNotFoundException snfe) {
            return false;
        }
    }

    public static void setAutoTime(Context context, boolean isAuto) {
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, (isAuto ? 1 : 0));
    }

    public static void setAutoTimezone(Context context, boolean isAuto) {
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, (isAuto ? 1 : 0));
    }

    public static String getTimezoneGMT(String timezoneId) {
        StringBuilder gmt = new StringBuilder();
        final TimeZone tz = TimeZone.getTimeZone(timezoneId);
        long date = Calendar.getInstance().getTimeInMillis();
        final int offset = tz.getOffset(date);
        int p = Math.abs(offset);
        if (offset < 0) {
            gmt.append('-');
        } else {
            gmt.append('+');
        }

        gmt.append(p / (60 * 60000));
        gmt.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            gmt.append('0');
        }
        gmt.append(min);
        return gmt.toString();
    }

    public static String getNTPServerIp() {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String ntpIp = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("settings get global ntp_server");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            ntpIp = reader.readLine();
            return ntpIp;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }

    public static String setNTPServerIp(String ip) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String ntpIp = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("settings put global ntp_server " + ip);
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            ntpIp = reader.readLine();
            return ntpIp;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }
}
