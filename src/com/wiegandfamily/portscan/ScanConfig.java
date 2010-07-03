package com.wiegandfamily.portscan;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ScanConfig extends BaseWindow {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "ScanConfig";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// make sure prefs are setup
		android.preference.PreferenceManager.setDefaultValues(this,
				R.xml.settings, false);

		setup();
	}

	protected void setup() {
		// get local IP address
		String myIP = NetworkHelper.getLocalIPAddress();

		EditText txtBox = (EditText) findViewById(R.id.EditText01);
		txtBox.setText(myIP);
		txtBox.requestFocus();

		List<String> items = NetworkScanRequest.getListOfPortLists(this);
		Spinner spinner = (Spinner) findViewById(R.id.Spinner03);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		items = NetworkScanRequest.getListOfSubnetMasks();
		spinner = (Spinner) findViewById(R.id.Spinner04);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		Button btn = (Button) findViewById(R.id.Button01);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Context context = getApplicationContext();
				Intent intent = new Intent(context, ScanResults.class);
				NetworkScanRequest nsr = new NetworkScanRequest();
				try {

					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(context);
					Boolean requireWifi = true;
					if (settings != null)
						try {
							requireWifi = settings.getBoolean("wifiOnly", true);
						} catch (Exception ee) {
						}

					if (requireWifi && !NetworkHelper.verifyWifiConnected(context)) {
						Toast.makeText(context,
								getAppString(R.string.err_wifi_only),
								Toast.LENGTH_LONG).show();
						return;
					}

					EditText txtBox = (EditText) findViewById(R.id.EditText01);
					nsr.setNetworkSubnet(txtBox.getText().toString());

					Spinner spinner = (Spinner) findViewById(R.id.Spinner03);
					nsr.setPortList(NetworkScanRequest.parsePortListString(
							getApplicationContext(), spinner.getSelectedItem()
									.toString()));

					spinner = (Spinner) findViewById(R.id.Spinner04);
					nsr.setSubnetBitMask(NetworkScanRequest
							.parseSubnetMaskString(spinner.getSelectedItem()
									.toString()));

					if (settings != null)
						try {
							String pref = settings.getString("connTimeout",
									"1000");
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
					Toast.makeText(getApplicationContext(),
							getAppString(R.string.err_badreq),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
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