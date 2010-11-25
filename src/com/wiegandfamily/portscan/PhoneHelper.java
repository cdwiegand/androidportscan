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
	public static int phoneType_cached = -1;
	public static int networkType_cached = -1;
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

	public static String getNetworkType(Activity caller) {
		if (caller != null && networkType_cached == -1)
			try {
				TelephonyManager mTelephonyMgr = (TelephonyManager) caller
						.getSystemService(Context.TELEPHONY_SERVICE);
				networkType_cached = mTelephonyMgr.getNetworkType();
			} catch (Exception e) {
			}
		switch (networkType_cached) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO rev.0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVDO rev.A";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
			/*
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "iDEN";
			*/
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "UNKNOWN";
		}
		return "ERROR";
	}

	public static String getPhoneType(Activity caller) {
		if (caller != null && phoneType_cached == -1)
			try {
				TelephonyManager mTelephonyMgr = (TelephonyManager) caller
						.getSystemService(Context.TELEPHONY_SERVICE);
				phoneType_cached = mTelephonyMgr.getPhoneType();
			} catch (Exception e) {
			}
		switch (phoneType_cached) {
		case TelephonyManager.PHONE_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.PHONE_TYPE_GSM:
			return "GSM";
		case TelephonyManager.PHONE_TYPE_NONE:
			return "NONE";
		}
		return "ERROR";
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
