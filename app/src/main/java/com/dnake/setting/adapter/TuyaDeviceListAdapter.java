package com.dnake.setting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.apps.R;
import com.dnake.logger.TuyaDeviceLogger;

import java.util.List;

public class TuyaDeviceListAdapter extends RecyclerView.Adapter<TuyaDeviceListAdapter.ViewHolder> {

    private Context mContext;

    public List<TuyaDeviceLogger.data> getmDatas() {
        return mDatas;
    }

    public void setmDatas(List<TuyaDeviceLogger.data> mDatas) {
        this.mDatas = mDatas;
    }

    private List<TuyaDeviceLogger.data> mDatas;
    private int selectedIndex = -1;

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public TuyaDeviceListAdapter(Context context, List<TuyaDeviceLogger.data> datas) {
        mContext = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device_rv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuyaDeviceLogger.data item = mDatas.get(position);
        holder.tvNo.setText((position + 1) + "");
        holder.tvId.setText(item.id);
        String type = "";
        if (item.type == 1) {
            type = mContext.getString(R.string.talk_text_person);
        } else if (item.type == 2) {
            type = mContext.getString(R.string.talk_text_unit_person);
        } else {
            type = mContext.getString(R.string.talk_text_unit_person);
        }
        holder.tvType.setText(type);
        holder.tvName.setText(item.name);
        if (selectedIndex == position) {
            holder.tvNo.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.tvId.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.tvType.setTextColor(mContext.getResources().getColor(R.color.blue));
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
            holder.tvNo.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.tvId.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.tvType.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
        }
        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIndex == position) {
                    selectedIndex = -1;
                    holder.tvNo.setTextColor(mContext.getResources().getColor(R.color.white));
                    holder.tvId.setTextColor(mContext.getResources().getColor(R.color.white));
                    holder.tvType.setTextColor(mContext.getResources().getColor(R.color.white));
                    holder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
                } else {
                    selectedIndex = position;
                    holder.tvNo.setTextColor(mContext.getResources().getColor(R.color.blue));
                    holder.tvId.setTextColor(mContext.getResources().getColor(R.color.blue));
                    holder.tvType.setTextColor(mContext.getResources().getColor(R.color.blue));
                    holder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
                }
//                if (mCallback != null) {
//                    if (selectedIndex != -1) {
//                        mCallback.onClickSelect(v, position, item);
//                    } else {
//                        mCallback.onEnterDetail(position, item);
//                    }
//                }
                notifyDataSetChanged();
//                if (mCallback != null) mCallback.onClickSelect(holder.layoutMain, position, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setSelected(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public void setItem(int index, TuyaDeviceLogger.data data) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMain;
        TextView tvNo;
        TextView tvId;
        TextView tvType;
        TextView tvName;

        public ViewHolder(View view) {
            super(view);
            layoutMain = (LinearLayout) view.findViewById(R.id.layout_main);
            tvNo = (TextView) view.findViewById(R.id.tv_no);
            tvId = (TextView) view.findViewById(R.id.tv_id);
            tvType = (TextView) view.findViewById(R.id.tv_type);
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onClickSelect(View view, int index, TuyaDeviceLogger.data bean);

        void onEnterDetail(int index, TuyaDeviceLogger.data bean);
    }
}
