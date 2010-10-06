package com.wiegandfamily.portscan;

import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class PortScanRequest implements Runnable {
	private static final String LOG_TAG = "PortScanRequest";

	private String host = "";
	private int port = 0;
	private int timeout = 0;
	private NetworkScanRequest request = null;

	protected static final HttpParams httpParameters = new BasicHttpParams();

	public PortScanRequest() {
		HttpConnectionParams.setConnectionTimeout(httpParameters, getTimeout());
		HttpConnectionParams.setSoTimeout(httpParameters, getTimeout());
	}

	public PortScanRequest(String host, int port, int timeout,
			NetworkScanRequest request) {
		this();
		setHost(host);
		setPort(port);
		setTimeout(timeout);
		this.request = request;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return host + ":" + port;
	}

	public void scanPort() {
		String host = getHost();
		int port = getPort();
		String str = toString();

		java.net.Socket s = new Socket();

		request.sendUpdate(NetworkScanRequest.MSG_UPDATE, str);

		try {
			java.net.InetSocketAddress remoteAddr = new java.net.InetSocketAddress(
					host, port);
			s.connect(remoteAddr, timeout);
			if (s.isConnected())
				try {
					// now try to read header line (if any)
					String tmp = "";
					if (port == 80 || port == 443) {
						s.close(); // we don't need it anymore
						s = null;
						HttpClient wc = new DefaultHttpClient(httpParameters);
						HttpGet req = new HttpGet((port == 443 ? "https"
								: "http")
								+ "://" + host + ":" + port + "/");
						HttpResponse resp = wc.execute(req);
						tmp = resp.getStatusLine().toString();
						tmp += " " + resp.getFirstHeader("Server").getValue();
					} else if (port == 137 || port == 138 || port == 139
							|| port == 445) {
						tmp = "(SMB/Windows Port)";
					} else {
						java.io.InputStreamReader isr = new java.io.InputStreamReader(
								s.getInputStream());
						java.io.BufferedReader br = new java.io.BufferedReader(
								isr);
						tmp = br.readLine();
						br.close();
						isr.close();
						s.close();
						s = null;
					}
					str += " OK '" + tmp + "'";
				} catch (Exception e) {
					SafeLogger.e(LOG_TAG, e.getMessage());
					str += " OK (cant read)";
					if (s != null) {
						s.close();
						s = null;
					}
				}
			else
				str += " (disconnected)";
			request.sendUpdate(NetworkScanRequest.MSG_FOUND, str);
		} catch (Exception e) {
			SafeLogger.e(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public void run() {
		scanPort();
	}
}
