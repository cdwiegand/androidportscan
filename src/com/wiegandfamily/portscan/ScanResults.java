package com.wiegandfamily.portscan;

import android.content.Intent;
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

	protected NetworkScanRequest scanner = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);

		run(false);
	}

	protected TextView getTextView() {
		return (TextView) findViewById(R.id.TextView02);
	}

	protected void run(boolean restart) {
		scanner = NetworkScanRequest.getInstance();
		scanner.setHandler(handler);
		if (restart && scanner.isRunning())
			scanner.killAll();

		// pull existing results (if any)
		TextView txtBox = getTextView();
		txtBox.setText(scanner.getResults());

		if (!scanner.isRunning()) {
			scanner.parseIntent(getIntent());

			txtBox = (TextView) findViewById(R.id.TextView01);
			txtBox.setText(getAppString(R.string.scanning) + " "
					+ scanner.getNetworkSubnet() + "...");

			Thread thread = new Thread(scanner);
			thread.start();
		}
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(
		// R.drawable.ic_menu_info_details);
		menu.add(0, MENU_EMAIL, 0, R.string.menu_email).setIcon(
				R.drawable.ic_menu_send);
		menu.add(0, MENU_RERUN, 0, R.string.menu_rerun).setIcon(
				R.drawable.ic_menu_refresh);
		menu.add(0, MENU_STOP, 1, R.string.menu_stop).setIcon(
				R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	/* Handles menu item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAbout();
			return true;
		case MENU_EMAIL:
			sendResultsViaEmail();
			return true;
		case MENU_RERUN:
			run(true); // restart if already running
			return true;
		case MENU_STOP:
			if (scanner != null)
				try {
					scanner.killAll();
				} catch (Exception e) {
				}
			return true;
		}
		return false;
	}

	protected void sendResultsViaEmail() {
		TextView txtBox = getTextView();
		Intent email = new Intent(Intent.ACTION_SEND);
		email.setType("text/plain");
		email.putExtra(Intent.EXTRA_SUBJECT, "Network scan results for "
				+ scanner.getNetworkSubnet() + scanner.getSubnetMaskString());
		if (scanner != null)
			email.putExtra(Intent.EXTRA_TEXT, scanner.getResults());
		else
			email.putExtra(Intent.EXTRA_TEXT, txtBox.getText());
		startActivity(Intent.createChooser(email, "Send mail via..."));
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
				Toast.makeText(txtBox.getContext(),
						getAppString(R.string.done_scanning),
						Toast.LENGTH_SHORT).show();
				break;
			case NetworkScanRequest.MSG_UPDATE:
				txtBox = (TextView) findViewById(R.id.TextView01);
				txtBox.setText(getAppString(R.string.scanning) + " "
						+ msg.obj.toString() + "...");
				txtBox.postInvalidate();
				break;
			case NetworkScanRequest.MSG_FOUND:
				txtBox = (TextView) findViewById(R.id.TextView02);
				txtBox.setText(scanner.getResults());
				break;
			case NetworkScanRequest.MSG_BADREQ:
				txtBox = (TextView) findViewById(R.id.TextView02);
				txtBox.setText(getAppString(R.string.err_badreq));
				Toast.makeText(
						txtBox.getContext(),
						getAppString(R.string.err_badreq) + ": "
								+ msg.obj.toString(), Toast.LENGTH_SHORT)
						.show();
				if (scanner != null)
					try {
						scanner.killAll();
					} catch (Exception e) {
					}
				break;
			}
		}
	};
}
