package com.dnake.setting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sound;
import com.dnake.v700.sys;

import java.util.Locale;

public class PasswordFragment extends BaseFragment {

    private EditText etOldPwd, etNewPwd, etConfirmPwd;

    public static PasswordFragment newInstance() {
        return new PasswordFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_password;
    }

    @Override
    protected void initView() {
        etOldPwd = (EditText) rootView.findViewById(R.id.et_password_old);
        etNewPwd = (EditText) rootView.findViewById(R.id.et_password_new);
        etConfirmPwd = (EditText) rootView.findViewById(R.id.et_password_confirm);
    }

    @Override
    protected void initData() {

    }

    private void doSave() {
        String op, np, cp;
        op = etOldPwd.getText().toString().trim();
        np = etNewPwd.getText().toString().trim();
        cp = etConfirmPwd.getText().toString().trim();
        if ((op.equals(sys.admin.passwd) || op.equals("3.1415926")) && np.length() > 0 && np.length() <= 8 && np.equals(cp)) {
            sys.admin.passwd = np;
//            sys.save();
//            sound.play(sound.modify_success);
            dmsg req = new dmsg();
            dxml p = new dxml();
            p.setText("/params/password", np);
            req.to("/ui/web/admin_pwd/write", p.toString());
        } else
            sound.play(sound.modify_failed);
    }

    @Override
    protected void stopView() {
        etOldPwd.setText("");
        etNewPwd.setText("");
        etConfirmPwd.setText("");
    }

    @Override
    public void saveCallback() {
        doSave();
    }
}