package com.dnake.test.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dnake.BaseActivity;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.message.activity.MessageActivity;
import com.dnake.setting.adapter.MenuListAdapter;
import com.dnake.setting.fragment.DevicesFragment;
import com.dnake.utils.DensityUtil;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.util.Timer;

public class TestDeviceActivity extends BaseActivity {
    public static Context ctx = null;

    private ImageView btnBack;
    private TextView btn_relay_test;
    private LinearLayout btn_rs485_test;
    private static ImageView btnTestPic;
    public static Handler mHandler;

    public static int isOn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ctx = this;
        e_timer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (timer != null) doProcess();
            }
        };
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new tRun(), 1000, 1000);

        mHandler = new Handler(Looper.getMainLooper());

        setContentView(R.layout.activity_test_device);
        initView();
    }

    private void initView() {
        btnBack = (ImageView) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        btn_relay_test = (TextView) findViewById(R.id.btn_relay_test);
        btn_relay_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dmsg req = new dmsg();
                dxml p2 = new dxml();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        p2.setInt("/params/onoff", 1);
                        req.to("/control/monitor/lock", p2.toString());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        p2.setInt("/params/onoff", 0);
                        req.to("/control/monitor/lock", p2.toString());
                    }
                }, 500);
            }
        });
        btn_rs485_test = (LinearLayout) findViewById(R.id.btn_rs485_test);
        btn_rs485_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOn == 1) {
                    isOn = 0;
                } else {
                    isOn = 1;
                }
                dmsg req = new dmsg();
                dxml p = new dxml();
                p.setInt("/params/onoff", isOn);
                req.to("/smart/RS485/test", null);
                freshViews();
            }
        });
        btnTestPic = (ImageView) findViewById(R.id.iv_pic);
        freshViews();
    }

    public static void freshViews() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isOn == 1) {
                    btnTestPic.setImageResource(R.drawable.light_btn_on);
                } else {
                    btnTestPic.setImageResource(R.drawable.light_btn_off);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ctx = null;
        if (isOn == 1) {
            isOn = 0;
            dmsg req = new dmsg();
            dxml p = new dxml();
            p.setInt("/params/onoff", isOn);
            req.to("/smart/RS485/test", null);
            freshViews();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private Timer timer = null;
    private Handler e_timer = null;

    private class tRun extends java.util.TimerTask {
        @Override
        public void run() {
            if (e_timer != null) e_timer.sendMessage(e_timer.obtainMessage());
        }
    }

    private void doProcess() {
        dmsg req = new dmsg();
        req.to("/smart/RS485/test/join", null);
    }
}