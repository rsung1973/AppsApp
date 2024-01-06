package com.dnake.apps;

import com.dnake.v700.apps;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
public class AdLabel extends BaseLabel {
	public static Boolean bStart = true;
	public static AdLabel mCtx = null;

	private WebView mWeb = null;
	private long mTs = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ad);
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		for (int i = 0; i < 10; i++) {
			WakeTask.acquire();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			if (apps.isScreenOn())
				break;
		}

		if (sys.lcd.portrait != 0) {
			if (this.getRequestedOrientation() == 0) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				return;
			}
		}

		sys.load();

		bFinish = false;
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (bStart == false) {
			if (!this.isFinishing())
				this.finish();
		} else {
			int timeout = apps.ad.timeout;
			if (Math.abs(System.currentTimeMillis() - mTs) < timeout * 1000) {
				WakeTask.acquire();
			} else {
				this.finish();
			}
		}
	}

	public void doReload() {
		mWeb = (WebView) findViewById(R.id.ad_webview);

		mWeb.setBackgroundColor(Color.BLACK);
		mWeb.setScrollBarStyle(0);
		mWeb.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		mWeb.getSettings().setUseWideViewPort(true);
		mWeb.getSettings().setLoadWithOverviewMode(true);
		mWeb.getSettings().setJavaScriptEnabled(true);
		mWeb.getSettings().setSupportZoom(true);
		mWeb.getSettings().setBuiltInZoomControls(false);
		mWeb.getSettings().setDisplayZoomControls(false);
		mWeb.getSettings().setDomStorageEnabled(true);
		mWeb.getSettings().setAllowFileAccess(true);
		mWeb.getSettings().setAllowFileAccessFromFileURLs(true);
		mWeb.getSettings().setAllowUniversalAccessFromFileURLs(true);
		mWeb.getSettings().setAppCacheEnabled(true);
		mWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		mWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWeb.getSettings().setMediaPlaybackRequiresUserGesture(false);
		mWeb.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

		// 创建WebViewClient对象
		WebViewClient wvc = new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		};

		// 设置WebViewClient对象
		mWeb.setWebViewClient(wvc);

		String url = apps.ad.url;
		if (url.contains("?"))
			url = url + "&smode=normal&building=" + sys.talk.building + "&unit=" + sys.talk.unit + "&index=" + sys.panel.index;
		else
			url = url + "?smode=normal&building=" + sys.talk.building + "&unit=" + sys.talk.unit + "&index=" + sys.panel.index;
		mWeb.loadUrl(url);
	}

	private void onAdStart() {
		if (sys.lcd.portrait != 0) {
			if (this.getRequestedOrientation() == 0) {
				return;
			}
		}

		this.doReload();
		mTs = System.currentTimeMillis();
		mCtx = this;
	}

	@Override
	public void onStart() {
		super.onStart();

		this.onAdStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		this.onAdStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mWeb != null) {
			mWeb.destroy();
			mWeb = null;
		}
		mCtx = null;
	}
}
