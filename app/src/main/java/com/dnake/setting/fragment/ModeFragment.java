package com.dnake.setting.fragment;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.utils.DateUtils;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Calendar;

public class ModeFragment extends BaseFragment {

    private SwitchButton switchBtnDND;
    private TextView btnStartTime, btnEndTime;

    public static ModeFragment newInstance() {
        return new ModeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mode;
    }

    @Override
    protected void initView() {
        switchBtnDND = (SwitchButton) rootView.findViewById(R.id.switch_btn_dnd);
        btnStartTime = (TextView) rootView.findViewById(R.id.btn_start_time);
        btnEndTime = (TextView) rootView.findViewById(R.id.btn_end_time);
        switchBtnDND.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent it = new Intent("com.dnake.broadcast");
                    it.putExtra("event", "com.dnake.talk.dnd");
                    it.putExtra("data", 1);
                    mContext.sendBroadcast(it);
                } else {
                    Intent it = new Intent("com.dnake.broadcast");
                    it.putExtra("event", "com.dnake.talk.dnd");
                    it.putExtra("data", 0);
                    mContext.sendBroadcast(it);
                }
            }
        });
        btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                new TimePickerDialog(
                        mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                btnStartTime.setText(hourOfDay + ":" + minute);
                            }
                        },
                        ca.get(Calendar.HOUR_OF_DAY),
                        ca.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(mContext)).show();
            }
        });
        btnEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar ca = Calendar.getInstance();
                new TimePickerDialog(
                        mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                btnEndTime.setText(hourOfDay + ":" + minute);
                            }
                        },
                        ca.get(Calendar.HOUR_OF_DAY),
                        ca.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(mContext)).show();
            }
        });
    }

    @Override
    protected void initData() {
//        switchBtnDND.setChecked();
    }

    @Override
    protected void stopView() {
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
