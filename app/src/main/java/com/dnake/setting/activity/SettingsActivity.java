package com.dnake.setting.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseFragment;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.install.ApkUtils;
import com.dnake.setting.adapter.MenuListAdapter;
import com.dnake.setting.fragment.DateFragment;
import com.dnake.setting.fragment.DevicesFragment;
import com.dnake.setting.fragment.DisplayFragment;
import com.dnake.setting.fragment.DndFragment;
import com.dnake.setting.fragment.LanguageFragment;
import com.dnake.setting.fragment.ModeFragment;
import com.dnake.setting.fragment.SoundsFragment;
import com.dnake.setting.fragment.VersionFragment;
import com.dnake.test.activity.TestDeviceActivity;
import com.dnake.utils.DensityUtil;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.login;
import com.dnake.v700.sound;
import com.dnake.widget.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends SettingsBaseActivity implements MenuListAdapter.Callback {
    private ImageView btnBack;
    private ImageView btnSave;

    private RecyclerView rvMenu;
    private MenuListAdapter adapter;

    private List<MenuListAdapter.MenuItem> menuList;
    private BaseFragment[] mFragments = new BaseFragment[6];
    private int mPreFragmentFlag = 0;
    private FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initData();
        initView();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                initFragment();
            }
        }, 300);
    }

    private void initView() {
        layoutMain = (FrameLayout) findViewById(R.id.layout_main);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        btnSave = (ImageView) findViewById(R.id.btn_save);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentCallBack fragmentCallBack = (FragmentCallBack) mFragments[mPreFragmentFlag];
                fragmentCallBack.saveCallback();
            }
        });
        rvMenu = (RecyclerView) findViewById(R.id.rv_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.addItemDecoration(new GridSpaceItemDecoration(2, DensityUtil.dip2px(SettingsActivity.this, 1), DensityUtil.dip2px(SettingsActivity.this, 0)));
        adapter = new MenuListAdapter(SettingsActivity.this, menuList);
        adapter.setCallback(this);
        rvMenu.setAdapter(adapter);
    }

    private void initData() {
        menuList = new ArrayList<>();
        menuList.add(new MenuListAdapter.MenuItem(1, getString(R.string.menu_display)));
        menuList.add(new MenuListAdapter.MenuItem(2, getString(R.string.menu_sounds)));
        menuList.add(new MenuListAdapter.MenuItem(3, getString(R.string.menu_date)));
        menuList.add(new MenuListAdapter.MenuItem(4, getString(R.string.menu_language)));
//        menuList.add(new MenuListAdapter.MenuItem(5, getString(R.string.menu_dnd)));
        menuList.add(new MenuListAdapter.MenuItem(5, getString(R.string.menu_devices)));
//        menuList.add(new MenuListAdapter.MenuItem(8, getString(R.string.menu_mode)));
        menuList.add(new MenuListAdapter.MenuItem(6, getString(R.string.menu_version)));
        menuList.add(new MenuListAdapter.MenuItem(7, getString(R.string.menu_more)));
    }

    private void initFragment() {
        mFragments[0] = new DisplayFragment();
        mFragments[1] = new SoundsFragment();
        mFragments[2] = new DateFragment();
        mFragments[3] = new LanguageFragment();
//        mFragments[4] = new DndFragment();
        mFragments[4] = new DevicesFragment();
        mFragments[5] = new VersionFragment();
        initLoadFragment(R.id.layout_main_container, 0, mFragments);
    }

    private void initLoadFragment(int containerId, int showFragment, Fragment... fragments) {
        for (int i = 0; i < fragments.length; i++) {
            Fragment tempFragment = getSupportFragmentManager().findFragmentByTag(fragments[i].getClass().getName());
//            if (tempFragment == null) {
//                transaction.add(containerId, fragments[i], fragments[i].getClass().getName());
//            }
            if (tempFragment != null) {
                transaction.remove(tempFragment);
            }
            transaction.add(containerId, fragments[i], fragments[i].getClass().getName());
            if (i != showFragment) {
                transaction.hide(fragments[i]);
            }
        }
        transaction.commitAllowingStateLoss();
    }

    private void showAndHideFragment(Fragment show, Fragment hide) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (show != hide) {
            transaction.show(show).hide(hide).commitAllowingStateLoss();
        }
    }

    @Override
    public void onClickSelect(View view, int index, int id) {
        if (index <= 5) {
            adapter.setSelected(index);
            showAndHideFragment(mFragments[index], mFragments[mPreFragmentFlag]);
            mPreFragmentFlag = index;
            switch (id) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    btnSave.setVisibility(View.GONE);
                    break;
            }
            if (id == 6) {//连续点击5次
                count++;
                if (count == 1) {
                    start = System.currentTimeMillis();
                }
                if (count == 3) {
                    end = System.currentTimeMillis();
                }
                if (count >= 5) {
                    if ((end - start) < 3000) {
                        Intent intent = new Intent(this, TestDeviceActivity.class);
                        startActivity(intent);
                    }
                    count = 0;
                }
                if ((System.currentTimeMillis() - start) > 5000) {
                    count = 0;
                    start = System.currentTimeMillis();
                }
            }
        } else if (index == 6) {
            if (login.ok()) {
                Intent intent = new Intent(this, SettingsMoreActivity.class);
                startActivity(intent);
            } else {
                loginPwdPopup();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideInputWhenTouchOtherView(this, ev, null);
        return super.dispatchTouchEvent(ev);
    }

    private void loginPwdPopup() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_system_login, (ViewGroup) findViewById(R.id.dialog_main));
        EditText etPwd = (EditText) layout.findViewById(R.id.et_pwd);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setView(layout);
        builder.setTitle(R.string.dialog_title_system);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SettingsActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                String passwd = etPwd.getText().toString();
                if (login.passwd(passwd)) {
                    Intent intent = new Intent(SettingsActivity.this, SettingsMoreActivity.class);
                    startActivity(intent);
                } else sound.play(sound.passwd_err);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        AlertDialog ad = builder.create();
        ad.show();
        ad.setCanceledOnTouchOutside(false);
        ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SettingsActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        ad.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    long start = 0;
    long end = 0;
    int count = 0;

//    public Preference.OnPreferenceClickListener versionPrefListener = new Preference.OnPreferenceClickListener() {
//
//        @Override
//        public boolean onPreferenceClick(Preference paramPreference) {
//            // TODO Auto-generated method stub
//            count++;
//            if (count == 1) {
//                start = System.currentTimeMillis();
//                System.out.println("====start====" + start);
//            }
//            if (count == 3) {
//                end = System.currentTimeMillis();
//                System.out.println("====end====" + end);
//            }
//            if (count >= 5) {
//                if ((end - start) < 3000) {
//
//                }
//                System.out.println("====end-start====" + (end - start));
//                count = 0;
//            }n
//            if ((System.currentTimeMillis() - start) > 5000) {
//                count = 0;
//                start = System.currentTimeMillis();
//                System.out.println("====超过5000ms=====");
//            }
//            return true;
//        }
//    };
}