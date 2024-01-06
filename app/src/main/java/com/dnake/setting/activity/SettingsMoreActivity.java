package com.dnake.setting.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseFragment;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.setting.adapter.MenuListAdapter;
import com.dnake.setting.fragment.EthernetFragment;
import com.dnake.setting.fragment.PasswordFragment;
import com.dnake.setting.fragment.ResetFragment;
import com.dnake.setting.fragment.RoomFragment;
import com.dnake.setting.fragment.SipFragment;
import com.dnake.setting.fragment.WifiFragment;
import com.dnake.utils.DensityUtil;
import com.tencent.mars.xlog.Log;

import java.util.ArrayList;
import java.util.List;

public class SettingsMoreActivity extends SettingsBaseActivity implements MenuListAdapter.Callback {
    private ImageView btnBack;
    private ImageView btnSave;

    private RecyclerView rvMenu;
    private MenuListAdapter adapter;

    private List<MenuListAdapter.MenuItem> menuList;
    private BaseFragment[] mFragments = new BaseFragment[7];
    private int mPreFragmentFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initData();
        initView();
//        initFragment();
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
        btnSave.setVisibility(View.VISIBLE);
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
        rvMenu.addItemDecoration(new GridSpaceItemDecoration(2, DensityUtil.dip2px(SettingsMoreActivity.this, 1), DensityUtil.dip2px(SettingsMoreActivity.this, 0)));
        adapter = new MenuListAdapter(SettingsMoreActivity.this, menuList);
        adapter.setCallback(this);
        rvMenu.setAdapter(adapter);
    }

    private void initData() {
        menuList = new ArrayList<>();
        menuList.add(new MenuListAdapter.MenuItem(1, getString(R.string.menu_eth)));
        menuList.add(new MenuListAdapter.MenuItem(2, getString(R.string.menu_wifi)));
        menuList.add(new MenuListAdapter.MenuItem(3, getString(R.string.menu_room)));
        menuList.add(new MenuListAdapter.MenuItem(4, getString(R.string.menu_sip)));
        menuList.add(new MenuListAdapter.MenuItem(5, getString(R.string.menu_password)));
        menuList.add(new MenuListAdapter.MenuItem(7, getString(R.string.menu_apps)));
        menuList.add(new MenuListAdapter.MenuItem(6, getString(R.string.menu_reset)));
    }

    private void initFragment() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mFragments[0] = new EthernetFragment();
        mFragments[1] = new WifiFragment();
        mFragments[2] = new RoomFragment();
        mFragments[3] = new SipFragment();
        mFragments[4] = new PasswordFragment();
        mFragments[5] = new ResetFragment();
        mFragments[6] = new ResetFragment();
        initLoadFragment(R.id.layout_main_container, 0, mFragments);
    }

    private void initLoadFragment(int containerId, int showFragment, Fragment... fragments) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.add(containerId, fragments[i], fragments[i].getClass().getName());
            if (i != showFragment) transaction.hide(fragments[i]);
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
        if (id <= 6) {
            adapter.setSelected(index);
            showAndHideFragment(mFragments[index], mFragments[mPreFragmentFlag]);
            mPreFragmentFlag = index;
            switch (id) {
                case 2:
                case 6:
                    btnSave.setVisibility(View.GONE);
                    break;
                case 1:
                case 3:
                case 4:
                case 5:
                    btnSave.setVisibility(View.VISIBLE);
                    break;
            }
        } else if (id == 7) {
            Intent it = new Intent();
            ComponentName comp = new ComponentName("com.dnake.desktop", "com.dnake.desktop.AppsLabel");
            it.setComponent(comp);
            it.setAction("android.intent.action.VIEW");
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideInputWhenTouchOtherView(this, ev, null);
        return super.dispatchTouchEvent(ev);
    }
}