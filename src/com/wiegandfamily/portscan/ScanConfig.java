package com.wiegandfamily.portscan;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class ScanConfig extends Activity {
	private static final String LOGTAG = "ScanConfig";

	private static final int MSG_DONE = 1;
	private static final int MSG_UPDATE = 2;
	private static final int MSG_FOUND = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		setContentView(R.layout.results);

		NetworkScanner scanner = new NetworkScanner();
		scanner.setHandler(handler);
		Thread thread = new Thread(scanner);
		thread.start();
	}

	/** Handler to get results/updates from scanning thread */
	@SuppressWarnings("unused")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TextView txtBox;
			switch (msg.what) {
			case MSG_DONE:
				txtBox = (TextView) findViewById(R.id.TextView01);
				txtBox.setText(getAppString(R.string.results));
				break;
			case MSG_UPDATE:
				txtBox = (TextView) findViewById(R.id.TextView01);
				txtBox.setText(getAppString(R.string.scanning) + " " + msg.obj.toString());
				break;
			case MSG_FOUND:
				txtBox = (TextView) findViewById(R.id.TextView02);
				txtBox.setText(txtBox.getText().toString() + "\n" + msg.obj.toString());
				break;
			}
		}
	};
	
	protected String getAppString(int id) {
		return getApplicationContext().getResources().getString(id);
	}

}