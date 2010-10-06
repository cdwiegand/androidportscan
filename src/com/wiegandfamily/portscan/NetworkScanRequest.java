package com.wiegandfamily.portscan;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Handler;

public class NetworkScanRequest implements Runnable {
	private static final String LOG_TAG = "NetworkScanRequest";

	private static NetworkScanRequest _instance = null;

	private static String results = "";

	private static final int DEFAULT_THREADS = 32;
	private static final int DEFAULT_TIMEOUT = 500;

	public static final int MSG_DONE = 1;
	public static final int MSG_UPDATE = 2;
	public static final int MSG_FOUND = 3;
	public static final int MSG_BADREQ = 4;

	public static final String EXTRA_PORTLIST = "EXTRA_PORTLIST";
	public static final String EXTRA_SUBNET = "EXTRA_SUBNET";
	public static final String EXTRA_TIMEOUT = "EXTRA_TIMEOUT";
	public static final String EXTRA_SUBNETBITMASK = "EXTRA_SUBNETBITMASK";
	public static final String EXTRA_NUMTHREADS = "EXTRA_NUMTHREADS";

	private ExecutorService pool = null;
	private Handler handler = null;
	protected int[] portList = {};
	protected String networkSubnet = "192.168.15.0";
	protected byte subnetBitMask = 24; // "/24"
	protected int timeout = 0;
	protected int numThreads = 0;

	private NetworkScanRequest() {
	}

	public static NetworkScanRequest getInstance() {
		if (_instance == null)
			_instance = new NetworkScanRequest();
		return _instance;
	}

	public int[] getPortList() {
		return this.portList;
	}

	public int getTimeout() {
		return timeout;
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

	public Handler getHandler() {
		return this.handler;
	}

	public String getResults() {
		return results;
	}

	public boolean isRunning() {
		return pool != null;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setPortList(int[] portList) {
		this.portList = portList;
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

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Runs scan against network
	 * 
	 * @throws Exception
	 */
	public void scanNetwork() throws Exception {
		if (numThreads < 1)
			numThreads = DEFAULT_THREADS; // default
		if (timeout < 1)
			timeout = DEFAULT_TIMEOUT;

		List<String> hosts = getHostsForSubnet();
		if (hosts == null || hosts.size() == 0) {
			sendUpdate(MSG_BADREQ, "Invalid IP address. Use 1.2.3.4 format.");
			return;
		}

		if (isRunning())
			killAll();
		results = ""; // reset them
		pool = Executors.newFixedThreadPool(numThreads);

		for (int i = 0; i < hosts.size(); i++) {
			String host = hosts.get(i);
			for (int idx = 0; idx < portList.length; idx++) {
				int port = portList[idx];
				PortScanRequest req = new PortScanRequest(host, port, timeout,
						this);
				pool.execute(req);
			}
		}
		pool.shutdown(); // no new tasks now

		// now wait for all threads to finish
		try {
			pool.awaitTermination(65535, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			SafeLogger.e(LOG_TAG, e.getMessage());
		}

		sendUpdate(MSG_DONE, "");
		pool = null;
	}

	private List<String> getHostsForSubnet() {
		List<String> hosts = new ArrayList<String>();
		try {
			Inet4Address addr = (Inet4Address) InetAddress
					.getByName(this.networkSubnet);
			// now that we have a standardized form, let's do our math
			String ip = addr.getHostAddress();
			String networkStr = ip.substring(0, ip.lastIndexOf('.'));
			// so now it's 192.168.15
			int part4 = Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1));
			// and that's 67 (for example)

			// now, calculate out our range based on subNetBitMask
			int subnetSize = (int) Math.pow(2, 32 - this.subnetBitMask);
			// /24 -> 2^(32-24) -> 256
			int offset = part4 % subnetSize; // .67 would give us 67
			int minVal = part4 - offset; // give us .0
			int maxVal = minVal + subnetSize - 1; // give us .255

			// can't send to network or broadcast addresses!
			if (minVal != maxVal) {
				minVal++;
				maxVal--;
			}

			// now add each host
			for (int host = minVal; host <= maxVal; host++)
				hosts.add(networkStr + "." + host);
			return hosts;
		} catch (UnknownHostException e) {
			SafeLogger.e(LOG_TAG, e.getMessage());
			return null;
		}
	}

	public void sendUpdate(int updateType, String info) {
		if (updateType == MSG_FOUND)
			results += info + "\n";
		if (handler != null)
			handler.sendMessage(handler.obtainMessage(updateType, info));
	}

	public void killAll() {
		if (pool != null)
			try {
				pool.shutdownNow();
			} catch (Exception ex) {
				// ignore! best attempt to kill threads
			}
		pool = null;
	}

	@Override
	public void run() {
		if (handler != null)
			try {
				scanNetwork();
			} catch (Exception e) {
				sendUpdate(MSG_BADREQ,
						"Unable to run network scan - check your request.");
			}
	}

	public void setupIntent(Intent intent) {
		intent.putExtra(EXTRA_PORTLIST, this.getPortList());
		intent.putExtra(EXTRA_SUBNET, this.getNetworkSubnet());
		intent.putExtra(EXTRA_TIMEOUT, this.getTimeout());
		intent.putExtra(EXTRA_SUBNETBITMASK, this.getSubnetBitMask());
		intent.putExtra(EXTRA_NUMTHREADS, this.getNumThreads());
	}

	public void parseIntent(Intent intent) {
		try {
			this.setPortList(intent.getIntArrayExtra(EXTRA_PORTLIST));
			this.setTimeout(intent
					.getIntExtra(EXTRA_TIMEOUT, this.getTimeout()));
			this.setNetworkSubnet(intent.getStringExtra(EXTRA_SUBNET));
			this.setSubnetBitMask(intent.getByteExtra(EXTRA_SUBNETBITMASK, this
					.getSubnetBitMask()));
			this.setNumThreads(intent.getIntExtra(EXTRA_NUMTHREADS, this
					.getNumThreads()));
		} catch (Exception e) {
			SafeLogger.e(LOG_TAG, e.getMessage());
		}
	}

	public static List<String> getListOfSubnetMasks() {
		List<String> items = new ArrayList<String>();
		for (int i = 24; i <= 29; i++) {
			int subnetSize = (int) Math.pow(2, 32 - i);
			items.add("/" + i + " (255.255.255." + (256 - subnetSize) + ") "
					+ (subnetSize - 2) + " hosts");
		}
		items.add("/32 (255.255.255.255) 1 host");
		return items;
	}

	public static byte parseSubnetMaskString(String value) {
		return Byte.parseByte(value.substring(1, 3));
		// "/24..." becomes "24" becomes 24
	}

	public String getSubnetMaskString() {
		return "/" + String.valueOf(this.subnetBitMask);
	}
}
