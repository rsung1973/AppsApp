package com.dnake.tuya;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dnake.apps.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenKouJiIdAdapter extends BaseAdapter {
    private List<MenKouJiIdBean> mlist;
    private Context mContext;

    public MenKouJiIdAdapter(Context context, List<MenKouJiIdBean> list) {
        this.mContext = context;
        mlist = new ArrayList<MenKouJiIdBean>();
        this.mlist = list;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        ItemView view = null;
//        if (convertView == null) {
//            LayoutInflater inflater = LayoutInflater.from(mContext);
//            convertView = inflater.inflate(R.layout.item_menkoujiid, null);
//            view = new ItemView();
//            view.no = (TextView) convertView.findViewById(R.id.tv_no);
//            view.id = (TextView) convertView.findViewById(R.id.tv_id);
//            view.type = (TextView) convertView.findViewById(R.id.tv_type);
//            view.ip = (TextView) convertView.findViewById(R.id.tv_ip);
//            view.name = (TextView) convertView.findViewById(R.id.tv_name);
//            convertView.setTag(view);
//        } else {
//            view = (ItemView) convertView.getTag();
//        }
//        view.no.setText(String.format("%s", position + 1));
//        view.ip.setText(mlist.get(position).ip);
//        view.name.setText(mlist.get(position).name);
//        view.id.setText(mlist.get(position).id);
//        view.type.setText(mlist.get(position).type);
//        textChanged(position, view);
//        view.name.setOnTouchListener((v, event) -> {
//            ((ViewGroup) v.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//            return false;
//        });
//        convertView.setOnTouchListener((v, event) -> {
//            ((ViewGroup) v).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
//            return false;
//        });

        return convertView;
    }

//    private void textChanged(final int position, final ItemView view) {
//        view.name.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                int tag = (Integer) view.name.getTag();
//                if (tag == position)
//                    mlist.get(position).setName(editable.toString());
//            }
//        });
//    }

    public static class ItemView {
        TextView no;
        TextView id;
        TextView type;
        TextView ip;
        TextView name;
    }

    public static class MenKouJiIdBean {
        private String no;
        private String id;
        private String type;
        private String ip;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }
    public static class MenKouJiIdBeanList{
        private List<MenKouJiIdBean> list;

        public List<MenKouJiIdBean> getList() {
            return list;
        }

        public void setList(List<MenKouJiIdBean> list) {
            this.list = list;
        }
    }

    public static class Chs {
        private int id;

        private String n;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public void setN(String n) {
            this.n = n;
        }

        public String getN() {
            return this.n;
        }

        @Override
        public String toString() {
            return "Chs{" +
                    "id=" + id +
                    ", n='" + n + '\'' +
                    '}';
        }
    }

    public static class LoadTuyaBean{
        private int cmd;
        private int cc;
        private Chs[] chs;

        public int getCmd() {
            return cmd;
        }

        public void setCmd(int cmd) {
            this.cmd = cmd;
        }

        public int getCc() {
            return cc;
        }

        public void setCc(int cc) {
            this.cc = cc;
        }


        public Chs[] getChs() {
            return chs;
        }

        public void setChs(Chs[] chs) {
            this.chs = chs;
        }

        @Override
        public String toString() {
            return "LoadTuyaBean{" +
                    "cmd=" + cmd +
                    ", cc=" + cc +
                    ", chs=" + Arrays.toString(chs) +
                    '}';
        }
    }



    public static class UploadTuYaBean {
        private int res;

        private int err;

        private int cc;

        private ArrayList<Chs> chs;

        public void setRes(int res) {
            this.res = res;
        }

        public int getRes() {
            return this.res;
        }

        public void setErr(int err) {
            this.err = err;
        }

        public int getErr() {
            return this.err;
        }

        public void setCc(int cc) {
            this.cc = cc;
        }

        public int getCc() {
            return this.cc;
        }

        public ArrayList<Chs> getChs() {
            return chs;
        }

        public void setChs(ArrayList<Chs> chs) {
            this.chs = chs;
        }

        @Override
        public String toString() {
            return "UploadTuYaBean{" +
                    "res=" + res +
                    ", err=" + err +
                    ", cc=" + cc +
                    ", chs=" + chs +
                    '}';
        }
    }


}
