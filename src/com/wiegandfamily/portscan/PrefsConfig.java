package com.wiegandfamily.portscan;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsConfig extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
	}
}
