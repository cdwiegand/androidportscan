package com.wiegandfamily.portscan;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class NetworkScanRequest implements Runnable {
	private static final String LOG_TAG = "NetworkScanRequest";

	private static final int DEFAULT_THREADS = 8;

	public static final int MSG_DONE = 1;
	public static final int MSG_UPDATE = 2;
	public static final int MSG_FOUND = 3;
	public static final int MSG_BADREQ = 4;

	public static final String EXTRA_PORTLIST = "EXTRA_PORTLIST";
	public static final String EXTRA_SUBNET = "EXTRA_SUBNET";
	public static final String EXTRA_SUBNETBITMASK = "EXTRA_SUBNETBITMASK";
	public static final String EXTRA_TIMEOUT = "EXTRA_TIMEOUT";
	public static final String EXTRA_NUMTHREADS = "EXTRA_NUMTHREADS";

	public static final int PORTLIST_COMMON = 1;
	public static final int PORTLIST_ALL = 2;
	public static final int PORTLIST_LESSTHAN1024 = 3;

	protected static final int[] commonPorts = { 21, 22, 23, 25, 80, 110, 143 };
	// { 21, 22, 23, 25, 80, 110, 143, 389, 443, 445, 465, 587, 993, 995 };

	private ExecutorService pool = null;
	private boolean poolRunning = false;
	protected Handler handler = null;
	protected int portList = PORTLIST_COMMON;
	protected String networkSubnet = "192.168.15.0";
	protected byte subnetBitMask = 24; // "/24"
	protected int timeout = 2000;
	protected int numThreads = 4;

	public NetworkScanRequest() {
	}

	public NetworkScanRequest(Handler handler) {
		this();
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

	public byte getSubnetBitMask() {
		return this.subnetBitMask;
	}

	public boolean isRunning() {
		return poolRunning;
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

	public void setSubnetBitMask(byte subnetBitMask) {
		this.subnetBitMask = subnetBitMask;
	}

	/** Runs scan against network */
	public void scanNetwork() {
		if (numThreads < 1)
			numThreads = DEFAULT_THREADS; // default

		if (pool != null)
			killAll();
		pool = Executors.newFixedThreadPool(numThreads);

		List<String> hosts = getHostsForSubnet();

		poolRunning = true;
		for (int i = 0; i < hosts.size(); i++) {
			String host = hosts.get(i);
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
		poolRunning = false;
	}

	private List<String> getHostsForSubnet() {
		List<String> hosts = new ArrayList<String>();
		try {
			Inet4Address addr = (Inet4Address) InetAddress
					.getByName(this.networkSubnet);
			// now that we have a standardized form, let's do our math
			byte[] parts = addr.getAddress(); // 4 parts
			String networkStr = parts[0] + "." + parts[1] + "." + parts[2];

			// now, calculate out our range based on subNetBitMask
			int subnetSize = (int) Math.pow(2, 32 - this.subnetBitMask); // /24
																			// would
																			// be
																			// 256
			byte part4 = parts[3]; // grab the last bit
			int offset = part4 % subnetSize; // .67 would give us 189
			int minVal = part4 - offset; // give us .0
			int maxVal = part4 + offset - 1; // give us .255
			// can't send to network or broadcast addresses!
			minVal++;
			maxVal--;

			// now add each host
			for (int host = minVal; host <= maxVal; host++)
				hosts.add(networkStr + "." + host);
			return hosts;
		} catch (UnknownHostException e) {
			Log.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	public void killAll() {
		if (pool != null)
			try {
				pool.shutdownNow();
			} catch (Exception ex) {
				// ignore! best attempt to kill threads
			}
		poolRunning = false;
	}

	@Override
	public void run() {
		scanNetwork();
	}

	public void setupIntent(Intent intent) {
		intent.putExtra(EXTRA_PORTLIST, this.getPortList());
		intent.putExtra(EXTRA_SUBNET, this.getNetworkSubnet());
		intent.putExtra(EXTRA_SUBNETBITMASK, this.getSubnetBitMask());
		intent.putExtra(EXTRA_TIMEOUT, this.getTimeout());
		intent.putExtra(EXTRA_NUMTHREADS, this.getNumThreads());
	}

	public void parseIntent(Intent intent) {
		this
				.setPortList(intent.getIntExtra(EXTRA_PORTLIST, this
						.getPortList()));
		this.setNetworkSubnet(intent.getStringExtra(EXTRA_SUBNET));
		this.setSubnetBitMask(intent.getByteExtra(EXTRA_SUBNETBITMASK, this
				.getSubnetBitMask()));
		this.setTimeout(intent.getIntExtra(EXTRA_TIMEOUT, this.getTimeout()));
		this.setNumThreads(intent.getIntExtra(EXTRA_NUMTHREADS, this
				.getNumThreads()));
	}

	public static List<String> getListOfPortLists(Context context) {
		List<String> items = new ArrayList<String>();
		items.add(BaseWindow.getAppString(context, R.string.opt_ports_common));
		items.add(BaseWindow.getAppString(context,
				R.string.opt_ports_less_than_1024));
		items.add(BaseWindow.getAppString(context, R.string.opt_ports_all));
		return items;
	}

	public static int parsePortListString(Context context, String value) {
		List<String> items = getListOfPortLists(context);
		for (int i = 0; i < items.size(); i++)
			if (items.get(i).equalsIgnoreCase(value))
				return i + 1;
		return 0;
	}

	public static List<String> getListOfSubnetMasks() {
		List<String> items = new ArrayList<String>();
		for (int i = 24; i <= 32; i++)
			items.add("/" + i + " (255.255.255."
					+ (int) (256 - Math.pow(2, 32 - i)) + ")");
		return items;
	}

	public static byte parseSubnetMaskString(String value) {
		return Byte.parseByte(value.substring(1, 2)); // "/24..." becomes "24"
														// becomes 24
	}
}
