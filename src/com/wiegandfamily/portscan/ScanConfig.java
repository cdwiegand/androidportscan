package com.wiegandfamily.portscan;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MPMetrics;

public class ScanConfig extends BaseWindow {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "ScanConfig";
	private static final String MIXPANEL_TOKEN = "676d415a50a79f2fe914cb0f18f42e40";
	private MPMetrics mMPMetrics = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// to cache info
		PhoneHelper.getCarrierName(this);
		PhoneHelper.getAppVersion(this);
		PhoneHelper.getDisplayMetrics(this);
		PhoneHelper.getPhoneType(this);
		PhoneHelper.getNetworkType(this);

		try {
			if (mMPMetrics == null)
				mMPMetrics = new MPMetrics(this, MIXPANEL_TOKEN);
		} catch (Exception e) {
		}

		// make sure prefs are setup
		android.preference.PreferenceManager.setDefaultValues(this,
				R.xml.settings, false);

		setup();
	}

	@Override
	protected void onDestroy() {
		try {
			mMPMetrics.flush();
		} catch (Exception ee) {
			// Toast.makeText(this, ee.getMessage(), Toast.LENGTH_LONG).show();
		}
		super.onDestroy();
	}

	protected void setup() {
		// get local IP address
		String myIP = NetworkHelper.getLocalIPv4Address();
		if (myIP == null)
			myIP = "0.0.0.0";
		String[] parts = myIP.split("\\.");

		KeyListener kl = new KeyListener();

		EditText txtBox = (EditText) findViewById(R.id.IPStart1);
		txtBox.setText(parts[0]);
		txtBox.setOnKeyListener(kl);
		TextView txtView = (TextView) findViewById(R.id.IPEnd1);
		txtView.setText(parts[0]);

		txtBox = (EditText) findViewById(R.id.IPStart2);
		txtBox.setOnKeyListener(kl);
		txtBox.setText(parts[1]);
		txtView = (TextView) findViewById(R.id.IPEnd2);
		txtView.setText(parts[1]);

		txtBox = (EditText) findViewById(R.id.IPStart3);
		txtBox.setOnKeyListener(kl);
		txtBox.setText(parts[2]);
		txtView = (TextView) findViewById(R.id.IPEnd3);
		txtView.setText(parts[2]);

		txtBox = (EditText) findViewById(R.id.IPStart4);
		// no copy of content!
		txtBox.setText(parts[3]);
		txtBox = (EditText) findViewById(R.id.IPEnd4);
		txtBox.setText(parts[3]);
		// txtBox.requestFocus();

		txtBox = (EditText) findViewById(R.id.EditText06);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String defaultPorts = settings
				.getString(
						"defaultPortList",
						"21, 22, 23, 25, 53, 80, 110, 119, 143, 161, 389, 443, 445, 1433, 1521, 3306, 5900, 8080, 1604, 3389");
		txtBox.setText(defaultPorts);

		Button btn = (Button) findViewById(R.id.Button01);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScanConfig.this.onClickRun();
			}
		});
	}

	protected class KeyListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			syncUI();
			return false;
		}
	}

	protected void syncUI() {
		EditText start1 = (EditText) findViewById(R.id.IPStart1);
		EditText start2 = (EditText) findViewById(R.id.IPStart2);
		EditText start3 = (EditText) findViewById(R.id.IPStart3);
		TextView end1 = (TextView) findViewById(R.id.IPEnd1);
		TextView end2 = (TextView) findViewById(R.id.IPEnd2);
		TextView end3 = (TextView) findViewById(R.id.IPEnd3);
		end1.setText(start1.getText().toString());
		end2.setText(start2.getText().toString());
		end3.setText(start3.getText().toString());
	}

	private void track() {
		HashMap<String, String> properties = new HashMap<String, String>();
		try {
			properties.put("carrier", coalesce(
					PhoneHelper.getCarrierName(null), "Unknown"));
			properties.put("appVersion", coalesce(PhoneHelper
					.getAppVersion(null), "Unknown"));
			properties.put("androidVer", coalesce(PhoneHelper
					.getAndroidRelease(), "Unknown"));
			properties.put("phoneType", coalesce(
					PhoneHelper.getPhoneType(null), "Unknown"));
			properties.put("networkType", coalesce(PhoneHelper
					.getNetworkType(null), "Unknown"));
			properties
					.put("model", coalesce(PhoneHelper.getModel(), "Unknown"));
			DisplayMetrics dm = PhoneHelper.getDisplayMetrics(null);
			properties.put("display", coalesce("" + dm.widthPixels, "Unknown")
					+ "x" + coalesce("" + dm.heightPixels, "Unknown"));
		} catch (Exception e) {
		}
		// mMPMetrics.enableTestMode(); // just for testing
		mMPMetrics.event("ScanStarted", properties);
		mMPMetrics.flush();
	}

	private String coalesce(String value, String defValue) {
		return (value == null ? defValue : value);
	}

	// Scan Now button click function
	public void onClickRun() {
		try {
			track();
		} catch (Exception e) {
		}
		Intent intent = new Intent(this, ScanResults.class);
		NetworkScanRequest nsr = NetworkScanRequest.getInstance();
		try {
			if (nsr.isRunning())
				nsr.killAll(); // stop doing all work!

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			Boolean requireWifi = true;
			if (settings != null)
				try {
					requireWifi = settings.getBoolean("wifiOnly", true);
				} catch (Exception ee) {
				}

			if (requireWifi && !NetworkHelper.verifyWifiConnected(this)) {
				Toast.makeText(this, getAppString(R.string.err_wifi_only),
						Toast.LENGTH_LONG).show();
				return;
			}

			EditText txtBox1 = (EditText) findViewById(R.id.IPStart1);
			EditText txtBox2 = (EditText) findViewById(R.id.IPStart2);
			EditText txtBox3 = (EditText) findViewById(R.id.IPStart3);
			EditText txtBox4s = (EditText) findViewById(R.id.IPStart4);
			EditText txtBox4e = (EditText) findViewById(R.id.IPEnd4);
			int start1 = Integer.parseInt(txtBox1.getText().toString());
			int start2 = Integer.parseInt(txtBox2.getText().toString());
			int start3 = Integer.parseInt(txtBox3.getText().toString());
			int start4 = Integer.parseInt(txtBox4s.getText().toString());
			int end4 = Integer.parseInt(txtBox4e.getText().toString());
			nsr.setHostParts(start1, start2, start3, start4);
			nsr.setEndingHostPart4(end4);

			EditText txtBox = (EditText) findViewById(R.id.EditText06);
			// text box with port list in it
			String[] parts = txtBox.getText().toString().split("\\D+");
			int[] portList = new int[parts.length];
			for (int i = 0; i < parts.length; i++)
				portList[i] = Integer.parseInt(parts[i].trim());
			nsr.setPortList(portList);

			if (settings != null)
				try {
					String pref = settings.getString("connTimeout", "1000");
					int timeout = Integer.parseInt(pref);
					nsr.setTimeout(timeout);
				} catch (Exception ee) {
					nsr.setTimeout(1000);
				}
			else
				nsr.setTimeout(1000);

			nsr.setupIntent(intent);
			startActivity(intent);
		} catch (Exception e) {
			if (e.getMessage() != null)
				Toast.makeText(
						this,
						getAppString(R.string.err_badreq) + ": "
								+ e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(
				R.drawable.ic_menu_info_details);
		menu.add(0, MENU_PREFS, 1, R.string.menu_prefs).setIcon(
				R.drawable.ic_menu_preferences);
		menu.add(0, MENU_EXIT, 2, R.string.menu_exit).setIcon(
				R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	/* Handles menu item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAbout();
			return true;
		case MENU_PREFS:
			showPrefs();
			return true;
		case MENU_EXIT:
			this.finish();
			return true;
		}
		return false;
	}

}