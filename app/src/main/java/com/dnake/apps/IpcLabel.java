package com.dnake.apps;

import java.text.SimpleDateFormat;

import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.widget.Button2;
import com.dnake.widget.Storage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;

@SuppressLint({ "SetJavaScriptEnabled", "SimpleDateFormat" })
public class IpcLabel extends BaseLabel {
	public static Boolean bStart = true;
	public static String mUrl;

	private IpcLabel mContext = this;
	private Boolean mIsRec = false;
	private long rTs = 0;
	private long mTs = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ipc);
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		WakeTask.acquire();
		for(int i=0; i<10; i++) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			if (apps.isScreenOn())
				break;
		}
		mTs = System.currentTimeMillis();
		bFinish = false;

		Button2 b;
		b = (Button2) this.findViewById(R.id.ipc_btn_exit);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		b = (Button2) this.findViewById(R.id.ipc_btn_rec);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mIsRec) {
					mIsRec = false;
					Button2 b = (Button2) findViewById(R.id.ipc_btn_rec);
					b.setBackgroundDrawable(getResources().getDrawable(R.drawable.ipc_rec_start));
				} else {
					if (Storage.isMount(mContext, Storage.extsd)) {
						mIsRec = true;
						Button2 b = (Button2) findViewById(R.id.ipc_btn_rec);
						b.setBackgroundDrawable(getResources().getDrawable(R.drawable.ipc_rec_stop));

						dmsg req = new dmsg();
						dxml p = new dxml();
						SimpleDateFormat df = new SimpleDateFormat("yy_MM_dd_hh_mm_ss");
						String s = df.format(new java.util.Date());
						String url = Storage.extsd + "/ipc_" + s + ".avi";
						p.setText("/params/url", url);
						req.to("/media/rec/start", p.toString());
					}
				}
			}
		});
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (Math.abs(System.currentTimeMillis() - mTs) < 60*60*1000) {
			if (Math.abs(System.currentTimeMillis()-rTs) > 2000) {
				rTs = System.currentTimeMillis();
				dmsg req = new dmsg();
				if (req.to("/media/rtsp/length", null) != 200) { // 播放异常或结束
					this.playUrl(mUrl);
				}
				if (mIsRec) {
					if (req.to("/media/rec/length", null) != 200) {
						mIsRec = false;
						Button2 b = (Button2) findViewById(R.id.ipc_btn_rec);
						b.setBackgroundDrawable(getResources().getDrawable(R.drawable.ipc_rec_start));
					}
				}
			}
			WakeTask.acquire();
		} else {
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		this.playUrl(mUrl);
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		dmsg req = new dmsg();
		req.to("/media/rtsp/stop", null);
	}

	private void screen() {
		DisplayMetrics dm = new DisplayMetrics(); 
		this.getWindowManager().getDefaultDisplay().getMetrics(dm); 

		int w = dm.widthPixels;
		int h = dm.heightPixels;
		if (w == 800)
			h = 480;
		else if (w == 1024)
			h = 600;
		else if (w == 1280)
			h = 800;
		else if (w <= 480) {
			w = 480;
			h = 272;
		}
		w = (int)(0.86*w);

		dxml p = new dxml();
		dmsg req = new dmsg();
		p.setInt("/params/x", 0);
		p.setInt("/params/y", 0);
		p.setInt("/params/w", w);
		p.setInt("/params/h", h);
		req.to("/media/rtsp/screen", p.toString());
	}

	private void playUrl(String url) {
		this.screen();
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		req.to("/media/rtsp/play", p.toString());
	}
}
