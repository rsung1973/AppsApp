package com.dnake.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.dnake.apps.R;
import com.kyleduo.switchbutton.SwitchButton;

public class SettingsItemLayout extends LinearLayout {

    private Context mContext;
    private LinearLayout mainLayout;
    private TextView titleTv, contentTv;
    private SwitchButton switchBtn;
    private ImageView ivArrow;
    private ProgressBar progressBar;
    private Dialog mDialog;

    private int entriesArrId;
    private int entryValuesArrId;
    private String[] entriesArr;
    private String[] entryValuesArr;
    private boolean isSwitchChecked = false;

    private boolean mLayoutClickable = true;
    private OnClickListener mLayoutClickListener = null;

    public boolean isSwitchChecked() {
        return isSwitchChecked;
    }

    public void setSwitchChecked(boolean switchChecked) {
        isSwitchChecked = switchChecked;
    }

    public String[] getEntriesArr() {
        return entriesArr;
    }

    public void setEntriesArr(String[] entriesArr) {
        this.entriesArr = entriesArr;
    }

    public String[] getEntryValuesArr() {
        return entryValuesArr;
    }

    public void setEntryValuesArr(String[] entryValuesArr) {
        this.entryValuesArr = entryValuesArr;
    }

    public OnClickListener getmLayoutClickListener() {
        return mLayoutClickListener;
    }

    public void setmLayoutClickListener(OnClickListener mLayoutClickListener) {
        this.mLayoutClickListener = mLayoutClickListener;
        if (mLayoutClickable) {
            mainLayout.setOnClickListener(mLayoutClickListener);
        } else {
            mainLayout.setOnClickListener(null);
        }
    }

    public boolean ismLayoutClickable() {
        return mLayoutClickable;
    }

    public void setmLayoutClickable(boolean mLayoutClickable) {
        this.mLayoutClickable = mLayoutClickable;
        if (mLayoutClickable) {
            mainLayout.setOnClickListener(mLayoutClickListener);
            titleTv.setTextColor(Color.WHITE);
            contentTv.setTextColor(Color.WHITE);
        } else {
            mainLayout.setOnClickListener(null);
            titleTv.setTextColor(Color.GRAY);
            contentTv.setTextColor(Color.GRAY);
        }
    }

    public SettingsItemLayout(Context context) {
        super(context);
        this.mContext = context;
    }

    public SettingsItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView(context);
        initEvents();
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsItemLayout);
        if (attributes != null) {
            //设置标题文字，颜色
            String titleText = attributes.getString(R.styleable.SettingsItemLayout_title_text);
            if (!TextUtils.isEmpty(titleText)) {
                titleTv.setText(titleText);
                int titleTvTextColor = attributes.getColor(R.styleable.SettingsItemLayout_title_text_color, Color.WHITE);
                titleTv.setTextColor(titleTvTextColor);
            }
            String contentText = attributes.getString(R.styleable.SettingsItemLayout_content_text);
            if (!TextUtils.isEmpty(contentText)) {
                contentTv.setText(contentText);
                int contentTvTextColor = attributes.getColor(R.styleable.SettingsItemLayout_content_text_color, Color.WHITE);
                contentTv.setTextColor(contentTvTextColor);
            }
            boolean contentTvVisiable = attributes.getBoolean(R.styleable.SettingsItemLayout_content_text_visible, true);
            contentTv.setVisibility(contentTvVisiable ? VISIBLE : GONE);
            boolean switchBtnVisiable = attributes.getBoolean(R.styleable.SettingsItemLayout_switch_btn_visible, true);
            switchBtn.setVisibility(switchBtnVisiable ? VISIBLE : GONE);
            boolean arrowIvVisiable = attributes.getBoolean(R.styleable.SettingsItemLayout_arrow_visible, true);
            ivArrow.setVisibility(arrowIvVisiable ? VISIBLE : GONE);
            boolean progressBarVisiable = attributes.getBoolean(R.styleable.SettingsItemLayout_progress_visible, false);
            progressBar.setVisibility(progressBarVisiable ? VISIBLE : GONE);

            isSwitchChecked = attributes.getBoolean(R.styleable.SettingsItemLayout_switch_checked, false);
            mLayoutClickable = attributes.getBoolean(R.styleable.SettingsItemLayout_clickable, true);
            if (mLayoutClickable) {
                titleTv.setTextColor(Color.WHITE);
                contentTv.setTextColor(Color.WHITE);
            } else {
                mainLayout.setOnClickListener(null);
                titleTv.setTextColor(Color.LTGRAY);
                contentTv.setTextColor(Color.LTGRAY);
            }
            entriesArrId = attributes.getResourceId(R.styleable.SettingsItemLayout_android_entries, View.NO_ID);
            entryValuesArrId = attributes.getResourceId(R.styleable.SettingsItemLayout_android_entryValues, View.NO_ID);
            if (entriesArrId != View.NO_ID) {
                entriesArr = attributes.getResources().getStringArray(entriesArrId);
            }
            if (entryValuesArrId != View.NO_ID) {
                entryValuesArr = attributes.getResources().getStringArray(entryValuesArrId);
            }
            attributes.recycle();
        }
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_item_settings, this);
        mainLayout = (LinearLayout) findViewById(R.id.layout_main);
        titleTv = (TextView) findViewById(R.id.tv_title);
        contentTv = (TextView) findViewById(R.id.tv_content);
        switchBtn = (SwitchButton) findViewById(R.id.switch_btn);
        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initEvents() {
        mainLayout.setOnClickListener(mLayoutClickListener);
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSwitchChecked = isChecked;
            }
        });
    }

    public void popupSetDialog(Dialog dialog) {
        mDialog = dialog;
        mDialog.show();
    }

    public void dismissSetDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public void setTitleTv(TextView titleTv) {
        this.titleTv = titleTv;
    }

    public TextView getContentTv() {
        return contentTv;
    }

    public void setContentTv(TextView contentTv) {
        this.contentTv = contentTv;
    }

    public SwitchButton getSwitchBtn() {
        return switchBtn;
    }

    public void setSwitchBtn(SwitchButton switchBtn) {
        this.switchBtn = switchBtn;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
