package com.wiegandfamily.portscan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ScanConfig extends Activity {
	private static final String LOGTAG = "ScanConfig";

	private static final int MENU_ABOUT = 4;
	private static final int MENU_RERUN = 6;
	private static final int MENU_EXIT = 5;

	protected ProgressDialog pd = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
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

		NetworkScanner scanner = new NetworkScanner();
		scanner.setHandler(handler);
		scanner.setPortList(NetworkScanner.PORTLIST_COMMON);
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

	public void showAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		String versionNumber = "";
		try {
			PackageManager manager = getApplicationContext()
					.getPackageManager();
			PackageInfo info = manager.getPackageInfo(getApplicationContext()
					.getPackageName(), 0);
			versionNumber = info.versionName;
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
		builder
				.setMessage("Port Scanner "
						+ versionNumber
						+ "\nCopyright 2010 by Chris Wiegand\n\nCovered by MIT license");
		AlertDialog alert = builder.create();
		alert.show();
	}

	/** Handler to get results/updates from scanning thread */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TextView txtBox;
			switch (msg.what) {
			case NetworkScanner.MSG_DONE:
				txtBox = (TextView) findViewById(R.id.TextView01);
				txtBox.setText(getAppString(R.string.results));
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
				break;
			case NetworkScanner.MSG_UPDATE:
				if (pd != null)
					pd.setMessage(getAppString(R.string.scanning) + " "
							+ msg.obj.toString());
				break;
			case NetworkScanner.MSG_FOUND:
				txtBox = (TextView) findViewById(R.id.TextView02);
				txtBox.setText(txtBox.getText().toString() + "\n"
						+ msg.obj.toString());
				break;
			}
		}
	};

	protected String getAppString(int id) {
		return getApplicationContext().getResources().getString(id);
	}

}