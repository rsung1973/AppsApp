package com.dnake.test;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnake.apps.R;
import com.dnake.utils.NavigationBarUtil;

import java.util.Timer;
import java.util.TimerTask;

public class PicPopupDialog {
    private Context context;
    private Dialog dialog;
    private FrameLayout fLayout_bg;
    private Display display;
    private View view;

    private String mText = "";
    private TextView mDtmfView = null;
    private ImageView btnClose;

    public PicPopupDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public PicPopupDialog builder() {
        view = LayoutInflater.from(context).inflate(R.layout.dialog_pic_test, null);
        dialog = new Dialog(context, R.style.DialogTransparent);
        dialog.setContentView(view);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return this;
    }

    public PicPopupDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public PicPopupDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }

    private static Timer timer = new Timer();

    public void show() {
        if (dialog != null) {
            Looper.prepare();
            dialog.show();
            Window window = dialog.getWindow();
            window.setGravity(Gravity.TOP);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.dimAmount = 0.0f;
            window.setAttributes(lp);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
            NavigationBarUtil.hideNavigationBar(window);
            NavigationBarUtil.clearFocusNotAle(window);

            timer.schedule(new TimerTask() {
                public void run() {
                    hide();
                }
            }, 5 * 1000);
            Looper.loop();
        }
    }

    public void hide() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void stop() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
