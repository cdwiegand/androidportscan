package com.wiegandfamily.portscan;

import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ScanConfig extends Activity {
	private static final String LOGTAG = "ScanConfig";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String s = scanNetwork();

		// done!
		setContentView(R.layout.results);

		TextView txtResults = (TextView) findViewById(R.id.TextView02);
		txtResults.setText(s);
	}

	/** Called when user clicks button "Scan Now!" */

	/** Runs scan against network */
	public String scanNetwork() {
		String ret = "";
		for (int i = 1; i < 255; i++)
			ret += scanBox(i);
		return ret;
	}

	/** Runs scan against box */
	public String scanBox(int i) {
		String ret = "";
		int[] commonPorts = { 22, 25, 110, 143, 80, 443 };
		for (int idx = 0; idx < commonPorts.length; idx++)
			try {
				int port = commonPorts[idx];
				java.net.InetSocketAddress remoteAddr = new java.net.InetSocketAddress(
						"192.168.15." + i, port);
				java.net.Socket s = new Socket();
				s.connect(remoteAddr, 5);
				if (s.isConnected()) {
					ret += ",192.168.15." + i + ":" + port + ":OK\n";
					s.close();
				}
			} catch (Exception e) {
				Log.e(LOGTAG, e.getMessage());
				Log.e(LOGTAG, e.getStackTrace().toString());
			}
		return ret;
	}
}