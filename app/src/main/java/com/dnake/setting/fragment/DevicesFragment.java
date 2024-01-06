package com.dnake.setting.fragment;

import static com.dnake.tuya.TuYa_ipc.TUYE_DP_SWITCH_CHANNEL;
import static com.dnake.tuya.TuYa_ipc.isReport;
import static com.dnake.tuya.TuYa_ipc.tuyaStatus;
import static com.dnake.tuya.utils.tuyaJson;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.logger.TuyaDeviceLogger;
import com.dnake.setting.adapter.TuyaDeviceListAdapter;
import com.dnake.tuya.MenKouJiIdAdapter;
import com.dnake.tuya.TuYa_ipc;
import com.dnake.v700.sound;
import com.dnake.v700.sys;
import com.hjq.toast.ToastUtils;
import com.tuya.smart.aiipc.ipc_sdk.api.IControllerManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IDeviceManager;
import com.tuya.smart.aiipc.ipc_sdk.callback.DPConst;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;

import java.util.ArrayList;
import java.util.Timer;

public class DevicesFragment extends BaseFragment {

    private ImageView searchBtn, sortBtn, editBtn, saveBtn;
    private RecyclerView rvDevices;
    private TuyaDeviceListAdapter mAdapter;

    private ArrayList<TuyaDeviceLogger.data> sbList;

    public static DevicesFragment newInstance() {
        return new DevicesFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_devices;
    }

    @Override
    protected void initView() {
        if (isFirstLoad) {
            isFirstLoad = false;
        } else {
            return;
        }
        TuyaDeviceLogger.load();
        if (TuYa_ipc.isTuya(false) && isReport && tuyaStatus == 7) tuya_dp_report();

        e_timer = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (timer != null) doProcess();
            }
        };

        searchBtn = (ImageView) rootView.findViewById(R.id.btn_search);
        sortBtn = (ImageView) rootView.findViewById(R.id.btn_sort);
        editBtn = (ImageView) rootView.findViewById(R.id.btn_edit);
        saveBtn = (ImageView) rootView.findViewById(R.id.btn_save);
        rvDevices = (RecyclerView) rootView.findViewById(R.id.rv_devices);
        rvDevices.setLayoutManager(new LinearLayoutManager(mContext));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.white)));
        rvDevices.addItemDecoration(dividerItemDecoration);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setSelected(-1);
                sbList.clear();
                mAdapter.setmDatas(sbList);
                mAdapter.notifyDataSetChanged();
                TuyaDeviceLogger.saveIpAndName();
                TuyaDeviceLogger.reset();
                menKouJiId();
            }
        });
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedIndex() != -1 && sbList.size() > mAdapter.getSelectedIndex()) {
                    TuyaDeviceLogger.setTopNew(sbList.get(mAdapter.getSelectedIndex()));
//                    TuyaDeviceLogger.setTop(sbList.get(mAdapter.getSelectedIndex()));

//                    TuyaDeviceLogger.load();
                    sbList.clear();
                    sbList.addAll(TuyaDeviceLogger.logger);
                    mAdapter = new TuyaDeviceListAdapter(mContext, sbList);
                    mAdapter.setSelected(0);
                    rvDevices.setAdapter(mAdapter);
                }
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter != null && mAdapter.getmDatas().size() > 0 && mAdapter.getSelectedIndex() != -1) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.layout_search_dialog, rootView.findViewById(R.id.dialog_main));
                    EditText edName = layout.findViewById(R.id.et_search);

                    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                    b.setView(layout);
                    b.setPositiveButton(R.string.login_passwd_ok, (dialog, whichButton) -> {

                        String name = edName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) return;

                        TuyaDeviceLogger.modifyName((mAdapter.getSelectedIndex() + 1), name);
                        mAdapter.notifyDataSetChanged();
                    });
                    b.setNegativeButton(R.string.login_passwd_cancel, null);

                    AlertDialog ad = b.create();
                    ad.setCanceledOnTouchOutside(false);
                    ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        }
                    });
                    ad.show();
                    ad.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if ((mAdapter.getSelectedIndex() + 1) > 4) {
//                    Toast.makeText(mContext, "仅支持4通道视频切换", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                if (TuYa_ipc.isTuya(false)) {
                    tuya_dp_report();
                } else {
                    TuyaDeviceLogger.save();
                    sound.play(sound.modify_success);
                }
            }
        });

        initMenKouJiId();
    }

    @Override
    protected void initData() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new tRun(), 1000, 1000);

        TuyaDeviceLogger.saveIpAndName();
    }

    @Override
    protected void stopView() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private Timer timer = null;
    private Handler e_timer = null;

    private class tRun extends java.util.TimerTask {
        @Override
        public void run() {
            if (e_timer != null) e_timer.sendMessage(e_timer.obtainMessage());
        }
    }

    private void doProcess() {
        if (isVisible) {
            updateMenKkouJiId();
        }
    }

    private void initMenKouJiId() {
        sbList = new ArrayList<>();
        sbList.addAll(TuyaDeviceLogger.logger);

        checkBtn();
        mAdapter = new TuyaDeviceListAdapter(mContext, sbList);
        mAdapter.setSelected(0);
//        mAdapter.setSelected(sys.tuya.channel - 1);
        rvDevices.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
    }

    private void menKouJiId() {
        TuYa_ipc.searchPanel();
    }

    public void updateMenKkouJiId() {
        sbList.clear();
        for (TuyaDeviceLogger.data data : TuyaDeviceLogger.logger) {
            sbList.add(data);
        }
//        sbList.addAll(TuyaDeviceLogger.logger);
//        mAdapter.setSelected(sys.tuya.channel - 1);
        mAdapter.setmDatas(sbList);
        mAdapter.notifyDataSetChanged();
        checkBtn();
    }

    private void checkBtn() {
        if (sbList.size() > 0) {
            sortBtn.setEnabled(true);
            editBtn.setEnabled(true);
        } else {
            sortBtn.setEnabled(false);
            editBtn.setEnabled(false);
        }
    }

    public void tuya_dp_report() {
        IControllerManager controllerManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.CONTROLLER_SERVICE);
        IDeviceManager iDeviceManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.DEVICE_SERVICE);
        if (iDeviceManager.getRegisterStatus() != 2) {
//            Toast.makeText(mContext, this.getString(R.string.please_scan_qr), Toast.LENGTH_SHORT).show();
//            ToastUtils.show(getString(R.string.please_scan_qr));
//            sound.play(sound.modify_failed);
            TuyaDeviceLogger.save();
            sound.play(sound.modify_success);
            return;
        }

        MenKouJiIdAdapter.UploadTuYaBean uploadTuYaBean = new MenKouJiIdAdapter.UploadTuYaBean();

        ArrayList<MenKouJiIdAdapter.Chs> chsList = new ArrayList<>();
//        if (sbList.size() > 4) {
//            for (int i = 0; i < 4; i++) {
//                MenKouJiIdAdapter.Chs chs = new MenKouJiIdAdapter.Chs();
//                chs.setId(i + 1);
//                chs.setN(sbList.get(i).name);
//                chsList.add(chs);
//            }
//        } else
        for (int i = 0; i < sbList.size(); i++) {
            MenKouJiIdAdapter.Chs chs = new MenKouJiIdAdapter.Chs();
            chs.setId(i + 1);
            chs.setN(sbList.get(i).name);
            chsList.add(chs);
        }

//        sys.tuya.channel = mAdapter.getSelectedIndex() + 1;
//        uploadTuYaBean.setCc(mAdapter.getSelectedIndex() + 1);
        sys.tuya.channel = 1;
        uploadTuYaBean.setCc(1);

        uploadTuYaBean.setErr(0);
        uploadTuYaBean.setRes(1);
        uploadTuYaBean.setChs(chsList);

        controllerManager.dpReport(TUYE_DP_SWITCH_CHANNEL, DPConst.Type.PROP_STR, tuyaJson(uploadTuYaBean));
        TuyaDeviceLogger.save();
        sound.play(sound.modify_success);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (e_timer != null) {
            e_timer.removeCallbacksAndMessages(null);
            e_timer = null;
        }
    }
}
