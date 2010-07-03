package com.wiegandfamily.portscan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ScanResults extends BaseWindow {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "ScanResults";

	protected ProgressDialog pd = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		
		run();
	}

	protected void run() {
		if (pd == null) {
			pd = new ProgressDialog(this);
			pd.show();
		}

		TextView txtBox = (TextView) findViewById(R.id.TextView02);
		txtBox.setText("");

		NetworkScanRequest scanner = new NetworkScanRequest(handler);
		scanner.parseIntent(getIntent());

		if (!NetworkHelper.verifyWifiConnected(this)) {
			Toast.makeText(this, getAppString(R.string.wifi_only),
					Toast.LENGTH_LONG).show();
			return;
		}

		txtBox = (TextView) findViewById(R.id.TextView01);
		txtBox.setText(getAppString(R.string.scanning) + " " + scanner.getNetworkSubnet() + "...");

		Thread thread = new Thread(scanner);
		thread.start();
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(
				R.drawable.ic_menu_info_details);
		menu.add(0, MENU_RERUN, 0, R.string.menu_rerun).setIcon(
				R.drawable.ic_menu_refresh);
		menu.add(0, MENU_EXIT, 1, R.string.menu_exit).setIcon(
				R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	/* Handles menu item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAbout();
			return true;
		case MENU_RERUN:
			run();
			return true;
		case MENU_EXIT:
			this.finish();
			return true;
		}
		return false;
	}

	/** Handler to get results/updates from scanning thread */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TextView txtBox;
			switch (msg.what) {
			case NetworkScanRequest.MSG_DONE:
				txtBox = (TextView) findViewById(R.id.TextView01);
				txtBox.setText(getAppString(R.string.results));
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
				break;
			case NetworkScanRequest.MSG_UPDATE:
				if (pd != null)
					pd.setMessage(getAppString(R.string.scanning) + " "
							+ msg.obj.toString());
				break;
			case NetworkScanRequest.MSG_FOUND:
				txtBox = (TextView) findViewById(R.id.TextView02);
				txtBox.setText(txtBox.getText().toString() + "\n"
						+ msg.obj.toString());
				break;
			}
		}
	};
}
