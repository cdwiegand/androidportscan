package com.wiegandfamily.portscan;

import android.os.Handler;

public class HostScanRequest implements Runnable {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "HostScanRequest";
	
	private String host = "";
	private int portList = 0;
	private int timeout = 1000;
	private Handler handler = null;

	public HostScanRequest() {
	}

	public HostScanRequest(String host, int portList, int timeout, Handler handler) {
		this();
		setHost(host);
		setPortList(portList);
		setTimeout(timeout);
		this.handler = handler;
	}

	public String getHost() {
		return host;
	}

	public int getPortList() {
		return portList;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getPostListString() {
		switch (getPortList()) {
		case NetworkScanRequest.PORTLIST_ALL:
			return "All ports";
		case NetworkScanRequest.PORTLIST_COMMON:
			return "Common ports";
		case NetworkScanRequest.PORTLIST_LESSTHAN1024:
			return "Ports < 1024";
		default:
			return "Unknown";
		}
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPortList(int portList) {
		this.portList = portList;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return host + ":" + getPostListString();
	}
	
	/**
	 * Runs scan against box
	 * 
	 * @throws InterruptedException
	 */
	public void scanHost() {		
		int[] ports = null;
		switch (portList) {
		case NetworkScanRequest.PORTLIST_ALL:
			ports = new int[65535];
			for (int idx = 0; idx < 65535; idx++)
				ports[idx] = idx;
			break;
		case NetworkScanRequest.PORTLIST_COMMON:
			ports = NetworkScanRequest.commonPorts;
			break;
		case NetworkScanRequest.PORTLIST_LESSTHAN1024:
			ports = new int[1024];
			for (int idx = 0; idx < 1024; idx++)
				ports[idx] = idx;
			break;
		}

		if (handler != null)
			handler.sendMessage(handler.obtainMessage(NetworkScanRequest.MSG_UPDATE, host));
		
		for (int idx = 0; idx < ports.length; idx++) {
			int port = ports[idx];
			PortScanRequest req = new PortScanRequest(host, port, timeout, handler);
			req.scanPort();
		}
	}

	@Override
	public void run() {
		scanHost();
	}
}
