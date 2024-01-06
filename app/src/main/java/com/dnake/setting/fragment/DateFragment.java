package com.dnake.setting.fragment;

import static com.dnake.v700.apps.ctx;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.setting.adapter.TimeZonesAdapter;
import com.dnake.utils.DateUtils;
import com.dnake.utils.DisplayUtils;
import com.dnake.utils.NtpConfUtils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.widget.SettingsItemLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateFragment extends BaseFragment {

    private SettingsItemLayout layoutAutoDateTime, layoutSetDate, layoutSetTime, layoutSelectZone, layoutFormat24, layoutNtpConfig;
    private AlertDialog ntpConfigDialog;

    public static DateFragment newInstance() {
        return new DateFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_date;
    }

    @Override
    protected void initView() {
        if (getAutoState(Settings.Global.AUTO_TIME_ZONE)) {
            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
        }
        timezoneListAdapter = new TimeZonesAdapter(mContext);
        layoutAutoDateTime = (SettingsItemLayout) rootView.findViewById(R.id.layout_auto_date_time);
        layoutSetDate = (SettingsItemLayout) rootView.findViewById(R.id.layout_set_date);
        layoutSetTime = (SettingsItemLayout) rootView.findViewById(R.id.layout_set_time);
        layoutSelectZone = (SettingsItemLayout) rootView.findViewById(R.id.layout_select_zone);
        layoutFormat24 = (SettingsItemLayout) rootView.findViewById(R.id.layout_format_24);
        layoutNtpConfig = (SettingsItemLayout) rootView.findViewById(R.id.layout_ntp_config);
        layoutAutoDateTime.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAutoDateTime.getSwitchBtn().isChecked()) {
                    layoutAutoDateTime.getSwitchBtn().setChecked(false);
                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0);
                } else {
                    layoutAutoDateTime.getSwitchBtn().setChecked(true);
                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AUTO_TIME, 1);
                }
                initData();
                submitTimeConf();
            }
        });
        layoutSetDate.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                Dialog dialog = new DatePickerDialog(mContext,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                DateUtils.setDate(mContext, year, month, dayOfMonth);
                                initData();
                                submitTimeConf();
                            }
                        },
                        ca.get(Calendar.YEAR),
                        ca.get(Calendar.MONTH),
                        ca.get(Calendar.DAY_OF_MONTH));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                });
                dialog.show();
                dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        layoutSetTime.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                Dialog dialog = new TimePickerDialog(
                        mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                DateUtils.setTime(mContext, hourOfDay, minute);
                                initData();
                                submitTimeConf();
                            }
                        },
                        ca.get(Calendar.HOUR_OF_DAY),
                        ca.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(mContext));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                });
                dialog.show();
                dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        layoutSelectZone.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupTimeZoneListDialog();
            }
        });
        layoutFormat24.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutFormat24.getSwitchBtn().isChecked()) {
                    layoutFormat24.getSwitchBtn().setChecked(false);
                    set24Hour(false);
                } else {
                    layoutFormat24.getSwitchBtn().setChecked(true);
                    set24Hour(true);
                }
                initData();
                submitTimeConf();
            }
        });
        layoutNtpConfig.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutNtpConfig.getSwitchBtn().isChecked()) {
                    layoutNtpConfig.getSwitchBtn().setChecked(false);
                    popupNTPConfigDialog();
                } else {
                    layoutNtpConfig.getSwitchBtn().setChecked(true);
                    NtpConfUtils.Data data = new NtpConfUtils.Data();
                    data.type = 0;
                    data.ntp_server_1 = "";
                    data.ntp_server_2 = "";
                    NtpConfUtils.update(data);
                    DateUtils.setNTPServerIp(null);
                }
                initData();
            }
        });
    }

    @Override
    protected void initData() {
        Calendar now = Calendar.getInstance();
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        layoutAutoDateTime.getSwitchBtn().setChecked(autoTimeEnabled);

        layoutSetDate.setmLayoutClickable(!autoTimeEnabled);
        layoutSetDate.getContentTv().setText(DateFormat.getDateFormat(mContext).format(now.getTime()));
        layoutSetTime.setmLayoutClickable(!autoTimeEnabled);
        layoutSetTime.getContentTv().setText(DateFormat.getTimeFormat(mContext).format(now.getTime()));

        layoutSelectZone.getContentTv().setText(getTimeZoneText(now.getTimeZone()));

        layoutFormat24.getSwitchBtn().setChecked(is24Hour());
        layoutFormat24.getContentTv().setText(is24Hour() ? getResources().getString(R.string.date_24_hour_example) : getResources().getString(R.string.date_12_hour_example));

        layoutNtpConfig.getSwitchBtn().setChecked(NtpConfUtils.load().type == 0);
        layoutNtpConfig.getContentTv().setText((NtpConfUtils.load().type == 0) ? getResources().getString(R.string.date_ntp_config_automatic) : getResources().getString(R.string.date_ntp_config_manual));
        layoutNtpConfig.setmLayoutClickable(autoTimeEnabled);
    }

    @Override
    protected void stopView() {
        if (ntpConfigDialog != null && ntpConfigDialog.isShowing()) {
            ntpConfigDialog.dismiss();
            ntpConfigDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.System.getInt(mContext.getContentResolver(), name) > 0;
        } catch (Settings.SettingNotFoundException snfe) {
            return false;
        }
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(mContext);
    }

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour ? DateUtils.HOURS_24 : DateUtils.HOURS_12);
    }

    private static String getTimeZoneText(TimeZone tz) {
        SimpleDateFormat sdf = new SimpleDateFormat("ZZZZ, zzzz");
        sdf.setTimeZone(tz);
        return sdf.format(new Date());
    }

    private AlertDialog mTimeZoneDialog;
    private TimeZonesAdapter timezoneListAdapter;

    private void popupTimeZoneListDialog() {
        AlertDialog.Builder singleChoiceDialog = new AlertDialog.Builder(mContext);
        singleChoiceDialog.setTitle(getResources().getString(R.string.date_dialog_title_select_time_zone));
        singleChoiceDialog.setSingleChoiceItems(timezoneListAdapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mTimeZoneDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                final TimeZone tz = timezoneListAdapter.obtainTimeZoneFromItem(timezoneListAdapter.getItem(which));
                AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(tz.getID());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initData();
                submitTimeConf();
            }
        });
        mTimeZoneDialog = singleChoiceDialog.create();
        mTimeZoneDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        mTimeZoneDialog.show();
        mTimeZoneDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void popupNTPConfigDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_date_ntp_set, (ViewGroup) rootView.findViewById(R.id.dialog_main));
        EditText ntpServer1Et = (EditText) layout.findViewById(R.id.et_ntp_server_1);
        ntpServer1Et.setText(NtpConfUtils.getNtpServerUrl());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.date_dialog_title_ntp_server_configuration));
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                String ntp1Url = ntpServer1Et.getText().toString().trim();
                NtpConfUtils.Data data = new NtpConfUtils.Data();
//                data.ntp_server_1 = "0.europe.pool.ntp.org";
                data.ntp_server_1 = ntp1Url;
                NtpConfUtils.update(data);
                DateUtils.setNTPServerIp(ntp1Url);
                initData();
                submitTimeConf();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        ntpConfigDialog = builder.create();
        ntpConfigDialog.show();
        ntpConfigDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ntpConfigDialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) ntpConfigDialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ntpConfigDialog.getWindow().getDecorView().getWindowToken(), 0);
                return true;
            }
        });
    }

    private void submitTimeConf() {
        dxml p = new dxml();
        dmsg req = new dmsg();

        int is_auto = getAutoState(Settings.Global.AUTO_TIME) ? 1 : 0;
        p.setInt("/params/ntp_enable", is_auto);
        p.setText("/params/ntp_server", NtpConfUtils.getNtpServerUrl());
        p.setText("/params/tz", Calendar.getInstance().getTimeZone().getID());
//        p.setInt("/params/date_format", sys.time.format);// 0：MM-DD-YYYY、1：DD-MM-YYYY、2：YYYY-MM-DD
        p.setInt("/params/time_format", is24Hour() ? 1 : 0);

        SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        switch (sys.time.format) {
            case 0:
                sDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                break;
            case 1:
                sDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                break;
            case 2:
                sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                break;
        }
        if (is_auto != 1) {
            Date time = Calendar.getInstance().getTime();
            p.setText("/params/current_time", sDateFormat.format(time) + " " + new SimpleDateFormat(is24Hour() ? "HH:mm:ss" : "hh:mm:ss").format(time));
        }
        req.to("/ui/web/time/write", p.toString());
    }
}
