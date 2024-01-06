package com.dnake.setting.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.utils.DateUtils;
import com.dnake.widget.SettingsItemLayout;

import java.util.Calendar;

public class DndFragment extends BaseFragment {

    private SettingsItemLayout layoutWholeDay, layoutTimeDuration, layoutStartTime, layoutEndTime;

    public static VersionFragment newInstance() {
        return new VersionFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dnd;
    }

    @Override
    protected void initView() {
        layoutWholeDay = (SettingsItemLayout) rootView.findViewById(R.id.layout_whole_day);
        layoutTimeDuration = (SettingsItemLayout) rootView.findViewById(R.id.layout_time_duration);
        layoutStartTime = (SettingsItemLayout) rootView.findViewById(R.id.layout_start_time);
        layoutEndTime = (SettingsItemLayout) rootView.findViewById(R.id.layout_end_time);

        layoutStartTime.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                Dialog dialog = new TimePickerDialog(
                        mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
        layoutEndTime.setmLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                Dialog dialog = new TimePickerDialog(
                        mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void stopView() {

    }
}
