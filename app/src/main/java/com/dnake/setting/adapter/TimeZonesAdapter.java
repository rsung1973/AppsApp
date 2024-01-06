package com.dnake.setting.adapter;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dnake.apps.R;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TimeZonesAdapter extends BaseAdapter {
    private static final String KEY_ID = "id";  // value: String
    private static final String KEY_DISPLAYNAME = "name";  // value: String
    private static final String KEY_GMT = "gmt";  // value: String
    private static final String KEY_OFFSET = "offset";  // value: int (Integer)
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;

    private Context ctx;
    private LayoutInflater inflater;

    private List<HashMap<String, Object>> mData = new ArrayList<HashMap<String, Object>>();

    public TimeZonesAdapter(Context context) {
        this.ctx = context;
        inflater = LayoutInflater.from(context);
        mData = getZones(context);
    }

    public TimeZonesAdapter(Context context, List<HashMap<String, Object>> datas) {
        this.ctx  = context;
        inflater = LayoutInflater.from(context);
        mData = datas;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public HashMap<String, Object> getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int idx, View view, ViewGroup arg2) {
        if (view == null)
            view = inflater.inflate(R.layout.item_date_time_zone, null);
        HashMap<String, Object> data = mData.get(idx);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvTitle.setText(data.get(KEY_DISPLAYNAME).toString());
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvContent.setText(data.get(KEY_GMT).toString());
        return view;
    }

    public static TimeZone obtainTimeZoneFromItem(Object item) {
        return TimeZone.getTimeZone((String)((Map<?, ?>)item).get(KEY_ID));
    }

    private static List<HashMap<String, Object>> getZones(Context context) {
        final List<HashMap<String, Object>> myData = new ArrayList<HashMap<String, Object>>();
        final long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(myData, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
        } catch (java.io.IOException ioe) {
        }

        return myData;
    }

    private static void addItem(
            List<HashMap<String, Object>> myData, String id, String displayName, long date) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        myData.add(map);
    }
}
