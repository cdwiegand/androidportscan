package com.wiegandfamily.portscan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	public static final String EXTRA_NETSUBNET = "EXTRA_NETSUBNET";
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
	protected String networkSubnet = "192.168.15";
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

	/** Runs scan against network */
	public void scanNetwork() {
		if (numThreads < 1)
			numThreads = DEFAULT_THREADS; // default

		if (pool != null)
			killAll();
		pool = Executors.newFixedThreadPool(numThreads);

		// fix networkSubnet to be x.y.z format (no fourth quad!)
		String[] parts = networkSubnet.split("\\.");
		if (parts.length < 3) {
			if (handler != null)
				handler.sendMessage(handler.obtainMessage(MSG_BADREQ));
			return;
		}
		networkSubnet = parts[0] + "." + parts[1] + "." + parts[2]; // force the right format
		
		poolRunning = true;
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
		poolRunning = false;
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
		intent.putExtra(EXTRA_NETSUBNET, this.getNetworkSubnet());
		intent.putExtra(EXTRA_TIMEOUT, this.getTimeout());
		intent.putExtra(EXTRA_NUMTHREADS, this.getNumThreads());
	}

	public void parseIntent(Intent intent) {
		this.setPortList(intent.getIntExtra(EXTRA_PORTLIST, this.getPortList()));
		this.setNetworkSubnet(intent.getStringExtra(EXTRA_NETSUBNET));
		this.setTimeout(intent.getIntExtra(EXTRA_TIMEOUT, this.getTimeout()));
		this.setNumThreads(intent.getIntExtra(EXTRA_NUMTHREADS, this.getNumThreads()));
	}
}
