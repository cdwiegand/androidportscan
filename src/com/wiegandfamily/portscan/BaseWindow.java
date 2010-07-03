package com.wiegandfamily.portscan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class BaseWindow extends Activity {
	private static final String LOG_TAG = "BaseWindow";

	protected static final int MENU_ABOUT = 4;
	protected static final int MENU_RERUN = 6;
	protected static final int MENU_EXIT = 5;

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
			Log.e(LOG_TAG, e.getMessage());
		}
		builder
				.setMessage("Port Scanner "
						+ versionNumber
						+ "\nCopyright 2010 by Chris Wiegand\n\nCovered by MIT license");
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected String getAppString(int id) {
		return getAppString(getApplicationContext(), id);
	}

	protected static String getAppString(Context context, int id) {
		return context.getResources().getString(id);
	}
}
