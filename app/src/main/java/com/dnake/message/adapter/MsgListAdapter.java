package com.dnake.message.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.apps.R;
import com.dnake.logger.TextLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MsgListAdapter extends RecyclerView.Adapter<MsgListAdapter.ViewHolder> {

    private Context mContext;
    private List<TextLogger.data> mDatas;
    private int selectedIndex = -1;

    public MsgListAdapter(Context context, List<TextLogger.data> datas) {
        mContext = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public MsgListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MsgListAdapter.ViewHolder holder = new MsgListAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_message_rv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MsgListAdapter.ViewHolder holder, int position) {
        TextLogger.data item = mDatas.get(position);
        holder.tvText.setText((position + 1) + "." + item.text);
        SimpleDateFormat fmt = new SimpleDateFormat("yy-MM-dd HH:mm");
        Date date = new Date(item.date);
        holder.tvDate.setText(fmt.format(date));
        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedIndex = position;
                if (mCallback != null) mCallback.onClickSelect(holder.layoutMain, position, item);
            }
        });
        if (item.iRead != 0) {
            if (selectedIndex == position) {
                holder.tvText.setTextColor(mContext.getResources().getColor(R.color.blue));
                holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.blue));
            } else {
                holder.tvText.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        } else {
            holder.tvText.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.tvDate.setTextColor(mContext.getResources().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setSelected(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedByVal(String selectedVal) {
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).equals(selectedVal)) {
                this.selectedIndex = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutMain;
        TextView tvText;
        TextView tvDate;

        public ViewHolder(View view) {
            super(view);
            layoutMain = (FrameLayout) view.findViewById(R.id.layout_main);
            tvText = (TextView) view.findViewById(R.id.tv_text);
            tvDate = (TextView) view.findViewById(R.id.tv_date);
        }
    }

    private MsgListAdapter.Callback mCallback;

    public void setCallback(MsgListAdapter.Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onClickSelect(View view, int index, TextLogger.data item);
    }
}
