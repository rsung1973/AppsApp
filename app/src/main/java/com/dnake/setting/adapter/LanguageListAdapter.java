package com.dnake.setting.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dnake.apps.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mDatas;
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

    public LanguageListAdapter(Context context, String[] datas) {
        mContext = context;
        mDatas = new ArrayList<>();
        for (String val : datas) {
            mDatas.add(val);
        }
    }

    @NonNull
    @Override
    public LanguageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LanguageListAdapter.ViewHolder holder = new LanguageListAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_language_rv, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageListAdapter.ViewHolder holder, int position) {
        String item = mDatas.get(position);
        holder.tvName.setText(item);
        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null)
                    mCallback.onClickSelect(holder.layoutMain, position, item);
            }
        });
        if (selectedIndex == position) {
            holder.tvName.setSelected(true);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
            holder.tvName.setSelected(false);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
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
        LinearLayout layoutMain;
        ImageView ivType;
        TextView tvName;

        public ViewHolder(View view) {
            super(view);
            layoutMain = (LinearLayout) view.findViewById(R.id.layout_main);
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    private LanguageListAdapter.Callback mCallback;

    public void setCallback(LanguageListAdapter.Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onClickSelect(View view, int index, String val);
    }
}
