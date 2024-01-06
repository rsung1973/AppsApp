package com.dnake.apps;

import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sCaller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import android.graphics.Bitmap;

@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface", "DefaultLocale", "NewApi" })
public class BrowerLabel extends BaseLabel {
	public static String brower_url = apps.brower;

	private WebView web = null;
	private ProgressDialog prompt = null;
	private Activity label = null;
	private AlertDialog.Builder alert = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brower);

		bFinish = false;

		web = (WebView)findViewById(R.id.brower_webview);
		web.addJavascriptInterface(this, "webTalk");
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setSupportZoom(true);
		web.getSettings().setBuiltInZoomControls(true);
		web.getSettings().setMediaPlaybackRequiresUserGesture(false);

		web.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) { //表示按返回键时的操作
                    	web.goBack();
                        return true;
                    }
                }
				return false;
			}
        });

		// 创建WebViewClient对象 
		WebViewClient wvc = new WebViewClient() {
			public Boolean sys_ok = true;

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        		super.onReceivedError(view, errorCode, description, failingUrl);
        		sys_ok = false;
        	}

            @Override 
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (sys_ok)
                	view.setVisibility(WebView.VISIBLE);
                else {
                	if (alert == null && label != null) {
                		alert = new Builder(label);
                		alert.setTitle(getString(R.string.brower_err_title));
                		alert.setMessage(getString(R.string.brower_err_prompt));
                		alert.setPositiveButton(getString(R.string.brower_err_ok), new OnClickListener() {
                			@Override
                			public void onClick(DialogInterface dialog, int which) {
                				dialog.dismiss();
                				label.finish();
                			}
                		});
                		alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            	finish();
                            }  
                        }); 
                		alert.create().show();
                	}
                }

                if (prompt != null) {
                	prompt.dismiss();
                	prompt = null;
                }
            }
		};

		//设置WebViewClient对象  
		web.setWebViewClient(wvc);
		web.setVisibility(WebView.GONE);

		prompt = ProgressDialog.show(this, null, this.getString(R.string.brower_loading));
		prompt.setCancelable(true);
		prompt.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            	finish();
            }  
        });
	}

	@Override
    public void onStart() {
		super.onStart();
		web.loadUrl(brower_url);
		label = this;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (label == null)
			web.loadUrl(brower_url);
		label = this;
	}

	@Override
    public void onStop() {
		super.onStop();

		label = null;
		if (prompt != null) {
        	prompt.dismiss();
        	prompt = null;
        }
	}

	@Override
    public void onTimer() {
		super.onTimer();

		if (sCaller.running == sCaller.QUERY) {
			if (sCaller.timeout() >= 3000) {
				sCaller.stop();
				Toast.makeText(this, this.getString(R.string.brower_webtalk_err), Toast.LENGTH_LONG).show();
			} else {
				if (apps.qResult.sip.url != null) {
					sCaller.start(apps.qResult.sip.url);
					Toast.makeText(this, this.getString(R.string.brower_webtalk_call)+apps.qResult.sip.url, Toast.LENGTH_SHORT).show();
				}
			}
		} else if (sCaller.running == sCaller.CALL) {
			if (sCaller.timeout() >= 15*1000) {
				sCaller.stop();
				dmsg req = new dmsg();
				req.to("/talk/stop", null);

				Toast.makeText(this, this.getString(R.string.brower_webtalk_err), Toast.LENGTH_LONG).show();
			} else {
				if (apps.qResult.result == 180) { //对方振铃
					sCaller.stop();
				} else if (apps.qResult.result >= 400) { //呼叫失败
					sCaller.stop();
					Toast.makeText(this, this.getString(R.string.brower_webtalk_err), Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	public void desktop() {
    }

	public void voice_call(int build, int unit, int floor, int family) {
		String s = String.format("%d%02d%02d%02d", build, unit, floor, family);
		sCaller.query(s);
		Toast.makeText(this, this.getString(R.string.brower_webtalk_query), Toast.LENGTH_SHORT).show();
	}

	public void phone_call(String id) {
		if (id != null) {
			sCaller.query(id);
			Toast.makeText(this, this.getString(R.string.brower_webtalk_query), Toast.LENGTH_SHORT).show();
		}
	}

	public void phone_call2(String url) {
		if (url != null) {
			sCaller.start(url);
			Toast.makeText(this, this.getString(R.string.brower_webtalk_call)+url, Toast.LENGTH_SHORT).show();
		}
	}

	public void triggerZone(int zone) {
		dmsg req = new dmsg();
		dxml p = new dxml();

		for (int i=0; i<8; i++) {
			if (i != zone)
				p.setInt("/params/io"+i, 0x10);
			else
				p.setInt("/params/io"+i, 1);
		}
		req.to("/security/io", p.toString());
	}
}
