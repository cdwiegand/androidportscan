package com.wiegandfamily.portscan;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class PhoneHelper {
	public static String carrierName_cached = null;
	public static String appVersion_cached = null;
	public static String model_cached = null;
	public static String release_cached = null;
	public static DisplayMetrics metrics_cached = null;

	public static String getCarrierName(Activity caller) {
		if (caller != null && carrierName_cached == null)
			try {
				TelephonyManager mTelephonyMgr = (TelephonyManager) caller.getSystemService(Context.TELEPHONY_SERVICE);
				carrierName_cached = mTelephonyMgr.getNetworkOperatorName();
			} catch (Exception e) {
			}
		return carrierName_cached;
	}

	public static String getAppVersion(Activity caller) {
		if (caller != null && appVersion_cached == null)
			try {
				PackageManager manager = caller.getApplicationContext().getPackageManager();
				PackageInfo info = manager.getPackageInfo(caller.getApplicationContext().getPackageName(), 0);
				appVersion_cached = info.packageName + "/" + info.versionName;
			} catch (Exception e) {
			}
		return appVersion_cached;
	}
	
	public static String getModel() {
		return android.os.Build.MODEL;
	}
	
	public static String getAndroidRelease() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static DisplayMetrics getDisplayMetrics(Activity caller) {
		if (caller != null && metrics_cached == null)
			try {
				metrics_cached = new DisplayMetrics();
				caller.getWindowManager().getDefaultDisplay().getMetrics(metrics_cached);
			} catch (Exception e) {
			}
		return metrics_cached;
	}
}
