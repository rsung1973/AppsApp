package com.dnake.v700;

public class sCaller {
	public static int NONE = 0;
	public static int QUERY = 1;
	public static int CALL = 2;

	public static int running = 0;
	public static long ts;

	public static String id;

	public static void query(String id) {
		apps.qResult.sip.url = null;

		sCaller.id = id;

		running = QUERY;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/id", id);
		req.to("/talk/sip/query", p.toString());
	}

	public static void start(String url) {
		apps.qResult.result = 0;

		running = CALL;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		req.to("/talk/sip/call", p.toString());
	}

	public static void m700(String url) {
		apps.qResult.result = 0;

		running = CALL;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		p.setInt("/params/type", 1);
		req.to("/talk/sip/call", p.toString());
	}

	public static void q600(String id) {
		apps.qResult.d600.ip = null;
		apps.qResult.d600.host = null;

		sCaller.id = id;
		running = QUERY;
		ts = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", id);
		req.to("/talk/device/query", p.toString());
	}

	public static void m600(String host, String ip) {
		apps.qResult.result = 0;

		running = CALL;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", host);
		p.setText("/params/ip", ip);
		req.to("/talk/monitor", p.toString());
	}

	public static void stop() {
		running = NONE;
	}

	public static long timeout() {
		return Math.abs(System.currentTimeMillis()-ts);
	}
}
