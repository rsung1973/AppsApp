package com.dnake.message.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseActivity;
import com.dnake.BaseDialogFragment;
import com.dnake.BaseFragment;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.message.fragment.MessageDetailFragment;
import com.dnake.message.fragment.MsgIncomingFragment;
import com.dnake.message.fragment.MsgOutgoingFragment;
import com.dnake.setting.adapter.MenuListAdapter;
import com.dnake.utils.DensityUtil;
import com.dnake.utils.NavigationBarUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends BaseActivity implements MenuListAdapter.Callback, BaseDialogFragment.Callback {
    private ImageView btnBack;
    private TextView btnClear;

    private RecyclerView rvMenu;
    private MenuListAdapter adapter;

    private List<MenuListAdapter.MenuItem> menuList;
    private BaseFragment[] mFragments = new BaseFragment[1];
    private int mPreFragmentFlag = 0;
    private FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initData();
        initView();
        initFragment();
    }

    private void initView() {
        layoutMain = (FrameLayout) findViewById(R.id.layout_main);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        btnClear = (TextView) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupDeleteDialog();
            }
        });
        rvMenu = (RecyclerView) findViewById(R.id.rv_menu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.addItemDecoration(new GridSpaceItemDecoration(2, DensityUtil.dip2px(MessageActivity.this, 1), DensityUtil.dip2px(MessageActivity.this, 0)));
        adapter = new MenuListAdapter(MessageActivity.this, menuList);
        adapter.setCallback(this);
        rvMenu.setAdapter(adapter);
    }

    private void initData() {
        menuList = new ArrayList<>();
        menuList.add(new MenuListAdapter.MenuItem(1, getString(R.string.title_incoming)));
//        menuList.add(new MenuListAdapter.MenuItem(2, getString(R.string.title_outgoing)));
    }

    private void initFragment() {
        mFragments[0] = new MsgIncomingFragment();
//        mFragments[1] = new MsgOutgoingFragment();
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
        adapter.setSelected(index);
        switch (id) {
            case 1:
            case 2:
                showAndHideFragment(mFragments[index], mFragments[mPreFragmentFlag]);
                mPreFragmentFlag = index;
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideInputWhenTouchOtherView(this, ev, null);
        return super.dispatchTouchEvent(ev);
    }

    public void popupMsgDetail(String detail) {
        MessageDetailFragment messageDetailFragment = new MessageDetailFragment();
        messageDetailFragment.setCallback(this);
        messageDetailFragment.setDetailStr(detail);
        messageDetailFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void doCancel() {
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private AlertDialog deleteDialog;

    private void popupDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setCancelable(true);
        builder.setTitle(getResources().getString(R.string.dialog_title_delete));
        builder.setMessage(getString(R.string.prompt_message_clear_all));
        builder.setPositiveButton(getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mPreFragmentFlag == 0) {

                } else {

                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MessageActivity.this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
        deleteDialog = builder.create();
        deleteDialog.show();
        NavigationBarUtil.hideNavigationBar(deleteDialog.getWindow());
        NavigationBarUtil.clearFocusNotAle(deleteDialog.getWindow());
    }

    @Override
    protected void onDestroy() {
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        super.onDestroy();
    }
}