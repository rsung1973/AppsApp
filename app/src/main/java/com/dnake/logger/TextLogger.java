package com.dnake.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.dnake.v700.apps;
import com.dnake.v700.dxml;

public class TextLogger {
	public static List<data> logger = new LinkedList<data>();
	public static String dir = "/dnake/data/text";
	public static String url = "/dnake/data/text/logger.xml";
	public static int MAX = 64;

	public static class data {
		public long date;
		public int seq;
		public int type;
		public int iRead;
		public String text;
	}

	public static void load() {
		logger.clear();

		File f = new File(dir);
		if (!f.exists())
			f.mkdir();

		f = new File(url);
        if(!f.exists())
        	return;

		try {
			FileInputStream in = new FileInputStream(url);
			dxml p = new dxml();
			if (p.parse(in)) {
				for(int i=0; i<MAX; i++) {
					String s = "/logger/d"+i;
					String text = p.getText(s+"/text");
					if (text != null) {
						data d = new data();
						d.date = Long.parseLong(p.getText(s+"/date"));
						d.seq = p.getInt(s+"/seq", 0);
						d.type = p.getInt(s+"/type", 0);
						d.iRead = p.getInt(s+"/iRead", 0);
						d.text = text;
						logger.add(d);
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		dxml p = new dxml();
		for(int i=0; i<logger.size(); i++) {
			data d = logger.get(i);
			String s = "/logger/d"+i;
			p.setText(s+"/date", String.valueOf(d.date));
			p.setInt(s+"/seq", d.seq);
			p.setInt(s+"/type", d.type);
			p.setInt(s+"/iRead", d.iRead);
			p.setText(s+"/text", d.text);
		}

		try {
			FileOutputStream out = new FileOutputStream(url);
			out.write(p.toString().getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		apps.broadcast();
	}

	public static void insert(int seq, int type, String text) {
		data d = new data();
		d.seq = seq;
		d.date = System.currentTimeMillis();
		d.type = type;
		d.text = text;
		logger.add(0, d);

		if (logger.size() > MAX)
			logger.remove(logger.size()-1);

		save();
	}

	public static void remove(int idx) {
		data d = logger.get(idx);
		if (d != null) {
			logger.remove(idx);
			save();
		}
	}

	public static void setRead(int idx) {
		data d = logger.get(idx);
		if (d != null) {
			d.iRead = 1;
			logger.set(idx, d);
			save();
		}
	}

	public static int nRead() {
		int n = 0;
		for(int i=0; i<logger.size(); i++) {
			data d = logger.get(i);
			if (d.iRead == 0)
				n++;
		}
		return n;
	}
}
