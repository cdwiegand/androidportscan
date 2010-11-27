package com.wiegandfamily.portscan;

import java.lang.reflect.Array;
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
	public static final String EXTRA_HOSTPARTS = "EXTRA_SUBNET";
	public static final String EXTRA_TIMEOUT = "EXTRA_TIMEOUT";
	public static final String EXTRA_ENDINGHOSTPART4 = "EXTRA_ENDINGHOSTPART4";
	public static final String EXTRA_NUMTHREADS = "EXTRA_NUMTHREADS";

	private ExecutorService pool = null;
	private Handler handler = null;
	protected int[] portList = {};
	protected int hostPart1 = 0;
	protected int hostPart2 = 0;
	protected int hostPart3 = 0;
	protected int hostPart4s = 0;
	protected int hostPart4e = 0;
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

	public int getEndingHostPart4() {
		return hostPart4e;
	}

	public int[] getHostParts() {
		int[] parts = new int[4];
		parts[0] = hostPart1;
		parts[1] = hostPart2;
		parts[2] = hostPart3;
		parts[3] = hostPart4s;
		return parts;
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

	public void setHostParts(int[] parts) {
		setHostParts(parts[0], parts[1], parts[2], parts[3]);
	}

	public void setHostParts(int hostPart1, int hostPart2, int hostPart3,
			int hostPart4) {
		this.hostPart1 = hostPart1;
		this.hostPart2 = hostPart2;
		this.hostPart3 = hostPart3;
		this.hostPart4s = hostPart4;
	}

	public void setEndingHostPart4(int hostPart4) {
		this.hostPart4e = hostPart4;
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
	
	public String getHostDescription() {
		return hostPart1 + "." + hostPart2 + "." + hostPart3 + "." + hostPart4s + "-" + hostPart4e;
	}

	private List<String> getHostsForSubnet() {
		List<String> hosts = new ArrayList<String>();
		try {
			String networkStr = hostPart1 + "." + hostPart2 + "." + hostPart3;

			// now add each host
			for (int host = hostPart4s; host <= hostPart4e; host++)
				hosts.add(networkStr + "." + host);
			return hosts;
		} catch (Exception e) {
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
		intent.putExtra(EXTRA_HOSTPARTS, this.getHostParts());
		intent.putExtra(EXTRA_TIMEOUT, this.getTimeout());
		intent.putExtra(EXTRA_ENDINGHOSTPART4, this.getEndingHostPart4());
		intent.putExtra(EXTRA_NUMTHREADS, this.getNumThreads());
	}

	public void parseIntent(Intent intent) {
		try {
			this.setPortList(intent.getIntArrayExtra(EXTRA_PORTLIST));
			this.setTimeout(intent
					.getIntExtra(EXTRA_TIMEOUT, this.getTimeout()));
			this.setHostParts(intent.getIntArrayExtra(EXTRA_HOSTPARTS));
			this.setEndingHostPart4(intent.getIntExtra(EXTRA_ENDINGHOSTPART4,
					this.getEndingHostPart4()));
			this.setNumThreads(intent.getIntExtra(EXTRA_NUMTHREADS, this
					.getNumThreads()));
		} catch (Exception e) {
			SafeLogger.e(LOG_TAG, e.getMessage());
		}
	}
}
