package com.wiegandfamily.portscan;

import java.net.Socket;
import java.nio.CharBuffer;

import android.os.Handler;
import android.util.Log;

public class NetworkScanner implements Runnable {
	private static final String LOGTAG = "NetworkScanner";

	public static final int MSG_DONE = 1;
	public static final int MSG_UPDATE = 2;
	public static final int MSG_FOUND = 3;
	
	public static final int PORTLIST_COMMON = 1;
	public static final int PORTLIST_ALL = 2;

	protected Handler handler = null;
	protected int portList = POSTLIST_COMMON;
	protected String networkSubnet = "192.168.15";

	public void setPortList(int portList) {
		this.portList = portList;
	}
	public void setNetworkSubnet(String networkSubnet) {
		this.networkSubnet = networkSubnet;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void run() {
		scanNetwork(this.networkSubnet, this.portList);
	}

	/** Runs scan against network */
	public void scanNetwork(String networkSubnet, int portList) {
		for (int i = 1; i < 255; i++)
			scanBox(networkSubnet, i, portList);
		if (handler != null)
			handler.sendMessage(handler.obtainMessage(MSG_DONE));
	}

	/** Runs scan against box */
	public void scanBox(String networkSubnet, int i, int portList) {
		java.net.Socket s = new Socket();
		int[] commonPorts = { 21, 22, 23, 25, 80, 110, 143, 389, 443, 445, 465, 587, 993, 995 };

		int[] ports = null;
		switch (portList) {
		case PORTLIST_ALL:
			ports = new int[65535];
			for (int idx= 0; idx < 65535; idx++)
				ports[idx] = idx;
			break;
		case PORTLIST_COMMON:
			ports = commonPorts;
			break;
		}
		for (int idx = 0; idx < ports.length; idx++)
			try {
				int port = ports[idx];
				String host = networkSubnet + "." + i;
				String str = host + ":" + port;
				if (handler != null)
					handler.sendMessage(handler.obtainMessage(MSG_UPDATE,
							str));
				java.net.InetSocketAddress remoteAddr = new java.net.InetSocketAddress(
						host, port);
				s.connect(remoteAddr, 10);
				if (s.isConnected()) 
					str += " ";
				else
					str += " (disconnected)";
				/*try {
					java.io.InputStreamReader isr = new java.io.InputStreamReader(
							s.getInputStream());
					CharBuffer line = CharBuffer.allocate(8 * 1024);
					isr.read(line);
					String tmp = line.toString();
					int idx2 = tmp.indexOf("\n");
					if (idx2 == 0) // first line starts with one? ok
						str += "OK (blank line)";
					else if (idx2 > 0) // OK we have a valid line
						str += "OK " + tmp.substring(0, idx2 - 1);
					else
						str += "OK " + tmp;
				} catch (Exception e) {
					str += "OK (cant read)";
				}*/
				if (handler != null)
					handler.sendMessage(handler.obtainMessage(MSG_FOUND,
							str));
				s.close();
			} catch (Exception e) {
				Log.e(LOGTAG, e.getMessage());
				Log.e(LOGTAG, e.getStackTrace().toString());
			}
	}
}
