package com.wiegandfamily.portscan;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
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

		setup();
	}

	protected void setup() {
		// get local IP address
		String myIP = NetworkHelper.getLocalIPAddress();

		EditText txtBox = (EditText) findViewById(R.id.EditText01);
		txtBox.setText(myIP);
		txtBox.requestFocus();

		txtBox = (EditText) findViewById(R.id.EditText02);
		txtBox.setText("1000");

		List<String> items = NetworkScanRequest.getListOfPortLists(this);
		Spinner spinner = (Spinner) findViewById(R.id.Spinner03);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		items = NetworkScanRequest.getListOfSubnetMasks();
		spinner = (Spinner) findViewById(R.id.Spinner04);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		Button btn = (Button) findViewById(R.id.Button01);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(),
						ScanResults.class);
				NetworkScanRequest nsr = new NetworkScanRequest();
				try {
					EditText txtBox = (EditText) findViewById(R.id.EditText01);
					nsr.setNetworkSubnet(txtBox.getText().toString());

					txtBox = (EditText) findViewById(R.id.EditText02);
					nsr.setTimeout(Integer.parseInt(txtBox.getText().toString()));

					Spinner spinner = (Spinner) findViewById(R.id.Spinner03);
					nsr.setPortList(NetworkScanRequest.parsePortListString(getApplicationContext(), spinner.getSelectedItem().toString()));

					spinner = (Spinner) findViewById(R.id.Spinner04);
					nsr.setSubnetBitMask(NetworkScanRequest.parseSubnetMaskString(spinner.getSelectedItem().toString()));

					nsr.setupIntent(intent);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							getAppString(R.string.err_badreq), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(
				R.drawable.ic_menu_info_details);
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
		case MENU_EXIT:
			this.finish();
			return true;
		}
		return false;
	}

}