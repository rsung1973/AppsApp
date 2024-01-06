package com.dnake.message.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.BaseDialogFragment;
import com.dnake.BaseFragment;
import com.dnake.GridSpaceItemDecoration;
import com.dnake.apps.R;
import com.dnake.logger.TextLogger;
import com.dnake.message.activity.MessageActivity;
import com.dnake.message.adapter.MsgListAdapter;
import com.dnake.utils.DensityUtil;
import com.dnake.utils.NavigationBarUtil;

public class MsgIncomingFragment extends BaseFragment implements MsgListAdapter.Callback {

    private RecyclerView rvMessage;
    private MsgListAdapter mAdapter;
    private ImageView btnMsgDel;

    public static MsgIncomingFragment newInstance() {
        return new MsgIncomingFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_incoming;
    }

    @Override
    protected void initView() {
        btnMsgDel = (ImageView) rootView.findViewById(R.id.btn_msg_del);
        rvMessage = (RecyclerView) rootView.findViewById(R.id.rv_msg_records);
        rvMessage.setLayoutManager(new LinearLayoutManager(mContext));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.white)));
        rvMessage.addItemDecoration(dividerItemDecoration);

        mAdapter = new MsgListAdapter(mContext, TextLogger.logger);
        mAdapter.setCallback(this);
        rvMessage.setAdapter(mAdapter);
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
}
