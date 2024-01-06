package com.dnake.setting.fragment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.utils.NtpConfUtils;
import com.dnake.utils.SoundUtils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.widget.SettingsItemLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SoundsFragment extends BaseFragment {
    private static final int[] SEEKBAR_UNMUTED_RES_ID = new int[]{
            Resources.getSystem().getIdentifier("ic_audio_vol", "drawable", "android"),
            Resources.getSystem().getIdentifier("ic_audio_ring_notif", "drawable", "android"),
            Resources.getSystem().getIdentifier("ic_audio_notification", "drawable", "android"),
            Resources.getSystem().getIdentifier("ic_audio_alarm", "drawable", "android")};

    private SettingsItemLayout layoutAutoAnswer, layoutBtnPressVol, layoutSoundRingtone;
    private ImageView ivRingVol, ivIntercomVol;
    private SeekBar seekbarRingVol, seekBarIntercomVol;
    private AudioManager mAudioManager;
    private int ringtoneIndex = 0;
    private MediaPlayer mPlayer;

    public static SoundsFragment newInstance() {
        return new SoundsFragment();
    }

    @Override
    protected int getLayoutId() {
        if (!TextUtils.isEmpty(apps.version) && apps.version.contains("902")) {
            return R.layout.fragment_sounds_902;
        } else {
            return R.layout.fragment_sounds;
        }
    }

    @Override
    protected void initView() {
        layoutAutoAnswer = (SettingsItemLayout) rootView.findViewById(R.id.layout_auto_answer);
        ivRingVol = (ImageView) rootView.findViewById(R.id.iv_ring_vol);
        ivIntercomVol = (ImageView) rootView.findViewById(R.id.iv_intercom_vol);
        ivRingVol.setImageResource(SEEKBAR_UNMUTED_RES_ID[1]);
        ivIntercomVol.setImageResource(SEEKBAR_UNMUTED_RES_ID[1]);
        seekbarRingVol = (SeekBar) rootView.findViewById(R.id.seekbar_ring_vol);
        seekBarIntercomVol = (SeekBar) rootView.findViewById(R.id.seekbar_intercom_vol);
        layoutBtnPressVol = (SettingsItemLayout) rootView.findViewById(R.id.layout_btn_press_vol);
        layoutSoundRingtone = (SettingsItemLayout) rootView.findViewById(R.id.layout_sound_ringtone);
        layoutAutoAnswer.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAutoAnswer.getSwitchBtn().isChecked()) {
                    layoutAutoAnswer.getSwitchBtn().setChecked(false);
                    setAutoAnswer(0);
                } else {
                    layoutAutoAnswer.getSwitchBtn().setChecked(true);
                    setAutoAnswer(1);
                }
            }
        });
        seekbarRingVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar arg0, int progrees, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg0.getProgress(), AudioManager.FLAG_PLAY_SOUND);
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, arg0.getProgress(), 0);
                sys.volume.sys = arg0.getProgress();
                setSysVolume(sys.volume.sys);
                playSound(SoundUtils.getDefaultRingtoneUri(mContext, AudioManager.STREAM_MUSIC), AudioManager.STREAM_MUSIC);
            }
        });
        seekBarIntercomVol.setMax(4 * sys.volume.MAX);
        seekBarIntercomVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean mFromUser = false;

            @Override
            public void onProgressChanged(SeekBar arg0, int progrees, boolean fromUser) {
                mFromUser = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                int m = (arg0.getProgress() + 3) / 4;
                sys.volume.talk = sys.volume.MAX - m;
                if (mFromUser) {
                    dmsg req = new dmsg();
                    dxml p = new dxml();
                    p.setInt("/params/volume", sys.volume.MAX - m);
                    req.to("/talk/volume", p.toString());
                }
                mFromUser = false;
                seekBarIntercomVol.setProgress(4 * m);
                setVolume(sys.volume.talk);
            }
        });
        layoutBtnPressVol.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutBtnPressVol.getSwitchBtn().isChecked()) {
                    Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    layoutBtnPressVol.getSwitchBtn().setChecked(false);
                } else {
                    Settings.System.putInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                    layoutBtnPressVol.getSwitchBtn().setChecked(true);
                }
            }
        });
        layoutSoundRingtone.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] ringtones = SoundUtils.getRingtoneTitleList(mContext, AudioManager.STREAM_SYSTEM);
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.sound_phone_ringtone)).setSingleChoiceItems(ringtones, ringtoneIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ringtoneIndex = which;
                        layoutSoundRingtone.getContentTv().setText(ringtones[which]);
                        dialog.dismiss();
                        Uri pickedUri = Uri.parse(SoundUtils.getRingtoneUriPath(mContext, AudioManager.STREAM_SYSTEM, which, ""));
                        RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, pickedUri);
                        playSound(pickedUri, AudioManager.STREAM_RING);
                        submitSoundConf();
                    }
                }).create();
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                });
                alertDialog.show();
                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
    }

    @Override
    protected void initData() {
        layoutAutoAnswer.getSwitchBtn().setChecked(getAutoAnswer() == 1);
        mPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //铃声音量
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sys.volume.sys = getSysVolume();
        seekbarRingVol.setMax(max);
        seekbarRingVol.setProgress(sys.volume.sys);
        //通话音量
        seekBarIntercomVol.setMax(4 * sys.volume.MAX);
        sys.volume.talk = getVolume();
        seekBarIntercomVol.setProgress(4 * (sys.volume.MAX - sys.volume.talk));

        layoutBtnPressVol.getSwitchBtn().setChecked(Settings.System.getInt(mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0);
        ringtoneIndex = SoundUtils.getRingtoneIndex(mContext, AudioManager.STREAM_SYSTEM);
        layoutSoundRingtone.getContentTv().setText(SoundUtils.getDefaultRingtoneTitle(mContext));
    }

    private void playSound(Uri uri, int type) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.reset();
        try {
            mPlayer.setAudioStreamType(type);
            mPlayer.setDataSource(mContext, uri);
            mPlayer.prepare();
            mPlayer.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPlayer != null) {
                        mPlayer.stop();
                    }
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVolume(int volume) {
        dmsg req = new dmsg();
        dxml p = new dxml();
        p.setInt("/params/volume", volume);
        req.to("/ui/web/talk_vol/write", p.toString());
    }

    private int getVolume() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        req.to("/ui/web/talk_vol/read", null);
        p.parse(req.mBody);
        return p.getInt("/params/volume", 0);
    }

    private int getSysVolume() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        req.to("/ui/web/sys_vol/read", null);
        p.parse(req.mBody);
        return p.getInt("/params/volume", 12);
    }

    private void setSysVolume(int volume) {
        dmsg req = new dmsg();
        dxml p = new dxml();
        p.setInt("/params/volume", volume);
        req.to("/ui/web/sys_vol/write", p.toString());
    }

    private int getAutoAnswer() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        req.to("/ui/web/auto_answer/read", null);
        p.parse(req.mBody);
        return p.getInt("/params/auto_pickup", 0);
    }

    private void setAutoAnswer(int isAuto) {
        dmsg req = new dmsg();
        dxml p = new dxml();
        p.setInt("/params/auto_pickup", isAuto);
        req.to("/ui/web/auto_answer/write", p.toString());
    }

    @Override
    protected void stopView() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void submitSoundConf() {
        dxml p = new dxml();
        dmsg req = new dmsg();
        p.setInt("/params/ring", ringtoneIndex);
        req.to("/ui/web/basic/write", p.toString());
    }
}

