package com.dnake.message.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseDialogFragment;
import com.dnake.BaseFragment;
import com.dnake.apps.R;
import com.dnake.logger.TextLogger;
import com.dnake.message.activity.MessageActivity;
import com.dnake.message.adapter.MsgListAdapter;

public class MsgOutgoingFragment extends BaseFragment implements MsgListAdapter.Callback, BaseDialogFragment.Callback {

    private RecyclerView rvMessage;
    private MsgListAdapter mAdapter;
    private ImageView btnMsgAdd;
    private ImageView btnMsgDel;

    public static MsgOutgoingFragment newInstance() {
        return new MsgOutgoingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_outgoing;
    }

    @Override
    protected void initView() {
        btnMsgAdd = (ImageView) rootView.findViewById(R.id.btn_msg_add);
        btnMsgDel = (ImageView) rootView.findViewById(R.id.btn_msg_del);
        rvMessage = (RecyclerView) rootView.findViewById(R.id.rv_msg_records);
        rvMessage.setLayoutManager(new LinearLayoutManager(mContext));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.white)));
        rvMessage.addItemDecoration(dividerItemDecoration);

        mAdapter = new MsgListAdapter(mContext, TextLogger.logger);
        mAdapter.setCallback(this);
        rvMessage.setAdapter(mAdapter);
        btnMsgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupSendMsgDialog();
            }
        });
        btnMsgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedIndex() != -1) {
                    TextLogger.remove(mAdapter.getSelectedIndex());
                    mAdapter.setSelected(-1);
                }
            }
        });
    }

    @Override
    protected void initData() {
        rvMessage.setAdapter(mAdapter);
    }

    @Override
    protected void stopView() {
    }

    @Override
    public void onClickSelect(View view, int index, TextLogger.data item) {
        TextLogger.setRead(index);
        mAdapter.notifyDataSetChanged();
        ((MessageActivity) getActivity()).popupMsgDetail(item.text);
    }


    private void popupSendMsgDialog() {
        MsgSendDialogFragment msgSendDialogFragment = new MsgSendDialogFragment();
        msgSendDialogFragment.setCallback(this);
        msgSendDialogFragment.setCancelable(false);
        msgSendDialogFragment.show(getActivity().getSupportFragmentManager(), "");
//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.dialog_msg_send, (ViewGroup) rootView.findViewById(R.id.dialog_main));
//        EditText etContent = (EditText) layout.findViewById(R.id.et_content);
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setView(layout);
//        builder.setCancelable(false);
//        addIpcDialog = builder.create();
//        addIpcDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
//        addIpcDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
//        addIpcDialog.show();
//        addIpcDialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                InputMethodManager imm = (InputMethodManager) addIpcDialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(addIpcDialog.getWindow().getDecorView().getWindowToken(), 0);
//                return true;
//            }
//        });
    }

    @Override
    public void doCancel() {
        mContext.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}