package com.dnake.setting.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseFragment;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.setting.adapter.LanguageListAdapter;
import com.dnake.utils.DensityUtil;
import com.dnake.utils.LanguageUtils;
import com.dnake.utils.Utils;
import com.dnake.v700.apps;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import java.util.Locale;

public class LanguageFragment extends BaseFragment implements LanguageListAdapter.Callback {
    private RecyclerView rvLanguage;
    private LanguageListAdapter mAdapter;
    private String[] languageArr;
    private String[] languageValArr;

    public static LanguageFragment newInstance() {
        return new LanguageFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_language;
    }

    @Override
    protected void initView() {
        languageArr = getResources().getStringArray(R.array.entries_language);
        languageValArr = getResources().getStringArray(R.array.entryvalues_language);
        rvLanguage = (RecyclerView) rootView.findViewById(R.id.rv_language);
        rvLanguage.setLayoutManager(new LinearLayoutManager(mContext));
        rvLanguage.addItemDecoration(new GridSpaceItemDecoration(2, DensityUtil.dip2px(mContext, 1), DensityUtil.dip2px(mContext, 1)));
        mAdapter = new LanguageListAdapter(mContext, languageArr);
        mAdapter.setCallback(this);
        rvLanguage.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {
        mAdapter.setSelectedByVal(getLanguage(mContext));
    }

    private String getLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        Log.i("aaa", "____getLanguage_________________________language_____________" + language);
        Log.i("aaa", "____getLanguage_________________________country_____________" + country);
        for (int i = 0; i < languageValArr.length; i++) {
            if (languageValArr[i].contains(language) && languageValArr[i].contains(country)) {
                return languageArr[i];
            }
        }
        return "";
    }

    @Override
    protected void stopView() {

    }

    @Override
    public void onClickSelect(View view, int index, String val) {
        String[] vals = languageValArr[index].split("_");
        Log.i("aaa", "________onClickSelect__________________________" + languageValArr[index]);
        Locale mLocale = new Locale(vals[0], vals[1]);
//        LanguageUtils.updateLanguage(mLocale);
        dxml p = new dxml();
        dmsg req = new dmsg();
        p.setText("/params/language", vals[0]);
        p.setText("/params/country", vals[1]);
        req.to("/settings/locale", p.toString());
        req.to("/ui/language/write", p.toString());
        Intent it = new Intent("com.dnake.broadcast");
        it.putExtra("event", "set_desktop_bg");
        apps.ctx.sendBroadcast(it);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        apps.reboot();
    }
}
