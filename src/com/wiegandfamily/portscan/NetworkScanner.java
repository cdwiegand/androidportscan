package com.wiegandfamily.portscan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.util.Log;

public class NetworkScanner implements Runnable {
	private static final String LOG_TAG = "NetworkScanner";

	private static final int DEFAULT_THREADS = 8;
	
	public static final int MSG_DONE = 1;
	public static final int MSG_UPDATE = 2;
	public static final int MSG_FOUND = 3;

	public static final int PORTLIST_COMMON = 1;
	public static final int PORTLIST_ALL = 2;
	public static final int PORTLIST_LESSTHAN1024 = 3;

	protected static final int[] commonPorts = { 21, 22, 23, 25, 80, 110, 143 }; 
	// { 21, 22, 23, 25, 80, 110, 143, 389, 443, 445, 465, 587, 993, 995 };

	protected Handler handler = null;
	protected int portList = PORTLIST_COMMON;
	protected String networkSubnet = "192.168.15";
	protected int timeout = 2000;
	protected int numThreads = 4;

	public NetworkScanner() {
	}

	public NetworkScanner(Handler handler) {
		this.handler = handler;
	}

	public int getPortList() {
		return this.portList;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public int getNumThreads() {
		return this.numThreads;
	}

	public String getNetworkSubnet() {
		return this.networkSubnet;
	}

	public void setPortList(int portList) {
		this.portList = portList;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public void setNetworkSubnet(String networkSubnet) {
		this.networkSubnet = networkSubnet;
	}

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

	public void run() {
		scanNetwork(this.networkSubnet, this.portList, this.numThreads);
	}

	/** Runs scan against network */
	public void scanNetwork(String networkSubnet, int portList, int numThreads) {
		if (numThreads < 1)
			numThreads = DEFAULT_THREADS; // default
		ExecutorService pool = Executors.newFixedThreadPool(numThreads);

		for (int i = 1; i < 255; i++) {
			String host = networkSubnet + "." + i;
			HostScanRequest req = new HostScanRequest(host, portList, timeout,
					handler);
			pool.execute(req);
		}

		// now wait for all threads to finish
		try {
			pool.awaitTermination(65535, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage());
		}

		if (handler != null)
			handler.sendMessage(handler.obtainMessage(MSG_DONE));
	}

}
