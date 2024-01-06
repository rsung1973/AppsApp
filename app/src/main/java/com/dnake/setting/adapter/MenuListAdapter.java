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

import java.util.List;

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {

    private Context mContext;
    private List<MenuItem> mDatas;
    private int selectedIndex = 0;

    public static class MenuItem {
        private int id;
        private String name;

        public MenuItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public MenuListAdapter(Context context, List<MenuItem> datas) {
        mContext = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_settings_rv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = mDatas.get(position);
        holder.tvName.setText(item.getName());
        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null)
                    mCallback.onClickSelect(holder.layoutMain, position, item.getId());
            }
        });
        if (selectedIndex == position) {
            holder.ivType.setSelected(true);
            holder.tvName.setSelected(true);
            holder.tvName.setTextSize(28);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.layoutMain.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_white));
        } else {
            holder.ivType.setSelected(false);
            holder.tvName.setSelected(false);
            holder.tvName.setTextSize(24);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.layoutMain.setBackground(null);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMain;
        ImageView ivType;
        TextView tvName;

        public ViewHolder(View view) {
            super(view);
            layoutMain = (LinearLayout) view.findViewById(R.id.layout_main);
            ivType = (ImageView) view.findViewById(R.id.iv_type);
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onClickSelect(View view, int index, int id);
    }
}
