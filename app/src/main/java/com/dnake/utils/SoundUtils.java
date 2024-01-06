package com.dnake.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import java.util.ArrayList;
import java.util.List;

public class SoundUtils {

    public static int getCurMediaVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return current;
    }

    public static int getCurAlarmVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        return current;
    }

    public static int getCurRingVolume(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        return current;
    }

    public static String getDefaultRingtoneTitle(Context context) {
        String ringtoneTitle = RingtoneManager.getRingtone(context, RingtoneManager.getActualDefaultRingtoneUri(context, AudioManager.STREAM_SYSTEM)).getTitle(context);
        return ringtoneTitle;
    }

    public static Ringtone getDefaultRingtone(Context context, int type) {
        return RingtoneManager.getRingtone(context, RingtoneManager.getActualDefaultRingtoneUri(context, type));
    }

    public static Uri getDefaultRingtoneUri(Context context, int type) {
        return RingtoneManager.getActualDefaultRingtoneUri(context, type);
    }

    public static int getRingtoneIndex(Context context, int type) {
        int index = 0;
        String curRingtone = getDefaultRingtone(context, type).getTitle(context);
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();
        if (cursor.moveToFirst()) {
            do {
                int position = cursor.getPosition();
                if (curRingtone.equals(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX))) {
                    index = position;
                    break;
                }
            } while (cursor.moveToNext());
        }
        return index;
    }

    public static int isDialPadSwitchOn(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1);
    }

    public static int isTouchSoundSwitchOn(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, Settings.System.SOUND_EFFECTS_ENABLED, 1);
    }

    public static int isScreenLockSoundSwitchOn(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.System.getInt(resolver, "lockscreen_sounds_enabled", 1);
    }

    public static void setDialPadSwitch(Context context, int value) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, value);
    }

    public static void setTouchSoundSwitch(Context context, int value) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, value);
    }

    public static void setScreenLockSoundSwitch(Context context, int value) {
        Settings.System.putInt(context.getContentResolver(), "lockscreen_sounds_enabled", value);
    }

    public static void setMediaVolume(Context context, int value) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
//        playSound(SoundUtils.getDefaultRingtoneUri(context, AudioManager.STREAM_MUSIC), AudioManager.STREAM_MUSIC);
    }

    public static void setAlarmVolume(Context context, int value) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, value, AudioManager.FLAG_PLAY_SOUND);
//        playSound(SoundUtils.getDefaultRingtoneUri(context, AudioManager.STREAM_ALARM), AudioManager.STREAM_ALARM);
    }

    public static void setRingVolume(Context context, int value) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, value, AudioManager.FLAG_PLAY_SOUND);
//        playSound(SoundUtils.getDefaultRingtoneUri(context, AudioManager.STREAM_MUSIC), AudioManager.STREAM_RING);
    }

    public static String[] getRingtoneTitleList(Context context, int type) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(AudioManager.STREAM_SYSTEM);
        Cursor cursor = manager.getCursor();
        String[] ringtones = new String[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                int position = cursor.getPosition();
                String ringName = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                ringtones[position] = ringName;
            } while (cursor.moveToNext());
        }
        return ringtones;
    }

    public static List<RingtoneModel> getRingtoneList(Context context, int type) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(AudioManager.STREAM_SYSTEM);
        Cursor cursor = manager.getCursor();
        List<RingtoneModel> ringtones = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                RingtoneModel model = new RingtoneModel();
                int position = cursor.getPosition();
                String ringName = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                if (ringName.equals("ring_default")) {
                    model.name = "Flow";
                } else if (ringName.equals("ring2")) {
                    model.name = "Emotion";
                } else if (ringName.equals("ring3")) {
                    model.name = "Dream";
                } else if (ringName.equals("ring4")) {
                    model.name = "Door bell";
                } else {
                    model.name = ringName;
                }
                model.uri = manager.getRingtoneUri(position).toString();
                ringtones.add(model);
            } while (cursor.moveToNext());
        }
        return ringtones;
    }

    public static void setDefautRingtone(Context context, String uri) {
        Uri pickedUri = Uri.parse(uri);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, pickedUri);
//        playSound(pickedUri, AudioManager.STREAM_RING);
    }

    public static void setDefaultRingVol(Context context) {
//            dmsg req = new dmsg();
//            dxml p = new dxml();
//            req.to("/ui/web/sys_vol/read", null);
//            p.parse(req.mBody);
//            int vol = p.getInt("/params/volume", 12);
        int vol = sys.volume.sys;
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);
    }

    public static String getRingtoneUriPath(Context context, int type, int pos, String def) {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(type);
        Cursor cursor = manager.getCursor();
        Uri uri = null;
        if (cursor.moveToFirst()) {
            do {
                int position = cursor.getPosition();
                if (pos == position) {
                    uri = manager.getRingtoneUri(position);
                    break;
                }
            } while (cursor.moveToNext());
        }
        return uri == null ? def : uri.toString();
    }

    public static class RingtoneModel {
        public String name;
        public String uri;
    }
}
