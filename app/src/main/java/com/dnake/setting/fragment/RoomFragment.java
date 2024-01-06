package com.dnake.setting.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sound;
import com.dnake.v700.sys;

public class RoomFragment extends BaseFragment {

    private EditText etBuilding, etRiser, etApartmentNo, etDevice, etSync;
    int building = 0, unit = 0, floor = 0, family = 0, dcode = 0;
    String buildingStr = "", riserStr = "", apartmentNoStr = "", deviceStr = "", syncStr = "";

    public static RoomFragment newInstance() {
        return new RoomFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_room;
    }

    @Override
    protected void initView() {
        etBuilding = (EditText) rootView.findViewById(R.id.et_building);
        etRiser = (EditText) rootView.findViewById(R.id.et_riser);
        etApartmentNo = (EditText) rootView.findViewById(R.id.et_apartment_no);
        etDevice = (EditText) rootView.findViewById(R.id.et_device);
        etSync = (EditText) rootView.findViewById(R.id.et_sync);
    }

    @Override
    protected void initData() {
        doLoad();
    }

    private void doLoad() {
        dmsg req = new dmsg();
        dxml p = new dxml();
        req.to("/ui/web/room/read", null);
        p.parse(req.mBody);
        building = p.getInt("/params/build", 1);
        unit = p.getInt("/params/unit", 1);
        floor = p.getInt("/params/floor", 11);
        family = p.getInt("/params/family", 11);
        dcode = p.getInt("/params/dcode", 0);
        syncStr = p.getText("/params/sync", "");

        etBuilding.setText(building + "");
        etRiser.setText(unit + "");
        etApartmentNo.setText(String.valueOf(floor * 100 + family));
        etDevice.setText(dcode + "");
        etSync.setText(syncStr);
    }

    private void doSave() {
        Boolean ok = true;
        buildingStr = etBuilding.getText().toString();
        if (buildingStr != null && buildingStr.length() > 0)
            building = Integer.valueOf(buildingStr);
        else
            ok = false;
        riserStr = etRiser.getText().toString();
        if (riserStr != null && riserStr.length() > 0)
            unit = Integer.valueOf(riserStr);
        else
            ok = false;
        apartmentNoStr = etApartmentNo.getText().toString();
        if (apartmentNoStr != null && apartmentNoStr.length() > 0) {
            int r = Integer.valueOf(apartmentNoStr);
            floor = r / 100;
            family = r % 100;
        } else
            ok = false;
        deviceStr = etDevice.getText().toString();
        if (deviceStr != null && deviceStr.length() > 0)
            dcode = Integer.valueOf(deviceStr);
        else
            ok = false;
        syncStr = etSync.getText().toString();

//        if (cmsIPStr == null || passwordStr == null || syncStr == null || syncStr.length() >= 16 || cmsIPStr.length() >= 32)
        if (syncStr == null || syncStr.length() >= 16)
            ok = false;

        if (building < 1 || building >= 10000)
            ok = false;
        if (unit >= 100 || floor >= 99 || family >= 100 || dcode >= 10)
            ok = false;

        if (ok) {
            dmsg req = new dmsg();
            dxml p = new dxml();
            p.setInt("/params/build", building);
            p.setInt("/params/unit", unit);
            p.setInt("/params/floor", floor);
            p.setInt("/params/family", family);
            p.setInt("/params/dcode", dcode);
            p.setText("/params/sync", syncStr);
//            p.setText("/params/server", cmsIPStr);
//            p.setText("/params/passwd", passwordStr);
            req.to("/ui/web/room/write", p.toString());
            sound.play(sound.modify_success);

            req.to("/talk/slave/reset", null);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sys.load();
        } else {
            sound.play(sound.modify_failed);
        }
    }

    @Override
    protected void stopView() {

    }

    @Override
    public void saveCallback() {
        if (etApartmentNo.getText().toString().trim().length() > 4) {
            etApartmentNo.setText("");
            sound.play(sound.modify_failed);
        } else {
            doSave();
        }
    }
}
