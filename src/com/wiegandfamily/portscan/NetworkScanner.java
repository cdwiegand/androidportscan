package com.wiegandfamily.portscan;

import java.net.Socket;
import java.nio.CharBuffer;

import android.os.Handler;
import android.util.Log;

public class NetworkScanner implements Runnable {
	private static final String LOGTAG = "NetworkScanner";

	private static final int MSG_DONE = 1;
	private static final int MSG_UPDATE = 2;
	private static final int MSG_FOUND = 3;

	protected Handler handler = null;

	public void setHandler(Handler handlerToCall) {
		handler = handlerToCall;
	}

	public void run() {
		scanNetwork();
	}

	/** Runs scan against network */
	public void scanNetwork() {
		for (int i = 1; i < 255; i++)
			scanBox(i);
		if (handler != null)
			handler.sendMessage(handler.obtainMessage(MSG_DONE));
	}

	/** Runs scan against box */
	public void scanBox(int i) {
		java.net.Socket s = new Socket();
		int[] commonPorts = { 22, 25, 110, 143, 80, 443, 587, 465 };
		for (int idx = 0; idx < commonPorts.length; idx++)
			try {
				int port = commonPorts[idx];
				String str = "192.168.15." + i + ":" + port;
				if (handler != null)
					handler.sendMessage(handler.obtainMessage(MSG_UPDATE,
							str));
				java.net.InetSocketAddress remoteAddr = new java.net.InetSocketAddress(
						"192.168.15." + i, port);
				s.connect(remoteAddr, 5);
				if (s.isConnected()) {
					str = "192.168.15." + i + ":" + port + ":";
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
				}
			} catch (Exception e) {
				Log.e(LOGTAG, e.getMessage());
				Log.e(LOGTAG, e.getStackTrace().toString());
			}
	}
}
