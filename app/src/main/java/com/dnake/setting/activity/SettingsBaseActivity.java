package com.dnake.setting.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.dnake.BaseActivity;
import com.dnake.utils.Utils;

public class SettingsBaseActivity extends BaseActivity {
    public interface FragmentCallBack {
        void saveCallback();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
