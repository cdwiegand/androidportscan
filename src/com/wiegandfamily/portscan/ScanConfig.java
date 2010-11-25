package com.wiegandfamily.portscan;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
		String myIP = NetworkHelper.getLocalIPAddress();

		EditText txtBox = (EditText) findViewById(R.id.EditText01);
		txtBox.setText(myIP);
		// txtBox.requestFocus();

		txtBox = (EditText) findViewById(R.id.EditText06);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String defaultPorts = settings
				.getString(
						"defaultPortList",
						"21, 22, 23, 25, 53, 80, 110, 119, 143, 161, 389, 443, 445, 1433, 1521, 3306, 5900, 8080, 1604, 3389");
		txtBox.setText(defaultPorts);

		List<String> items = NetworkScanRequest.getListOfSubnetMasks();
		Spinner spinner = (Spinner) findViewById(R.id.Spinner04);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		Button btn = (Button) findViewById(R.id.Button01);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScanConfig.this.onClickRun();
			}
		});
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

			EditText txtBox = (EditText) findViewById(R.id.EditText01);
			try {
				// try to parse IP or name
				InetAddress.getByName(txtBox.getText().toString());
			} catch (Exception e) {
				throw new Exception(
						"Invalid IP address/name. Use valid name or 1.2.3.4 format instead.");
			}
			nsr.setNetworkSubnet(txtBox.getText().toString());

			txtBox = (EditText) findViewById(R.id.EditText06);
			// text box with port list in it
			String[] parts = txtBox.getText().toString().split("\\D+");
			int[] portList = new int[parts.length];
			for (int i = 0; i < parts.length; i++)
				portList[i] = Integer.parseInt(parts[i].trim());
			nsr.setPortList(portList);

			Spinner spinner = (Spinner) findViewById(R.id.Spinner04);
			String selectedSNM = spinner.getSelectedItem().toString();
			byte snmByte = NetworkScanRequest
					.parseSubnetMaskString(selectedSNM);
			nsr.setSubnetBitMask(snmByte);

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