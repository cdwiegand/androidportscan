package com.wiegandfamily.portscan;

import android.util.Log;

public class SafeLogger {
	public static void e(String tag, String msg) {
		if (tag == null)
			tag = "PortScanner";
		if (msg != null)
			Log.e(tag, msg);
	}
}
