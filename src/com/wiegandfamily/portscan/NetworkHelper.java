package com.wiegandfamily.portscan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public abstract class NetworkHelper {
	private static final String LOG_TAG = "NetworkHelper";
	
	public static boolean verifyWifiConnected(Context con) {
		try {
			ConnectivityManager cm = (ConnectivityManager) con
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			State state = cm.getActiveNetworkInfo().getState();
			return (state.equals(State.CONNECTED));
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex.toString());
			return false;
		}
	}

	protected static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(LOG_TAG, ex.toString());
		}
		return null;
	}
}
