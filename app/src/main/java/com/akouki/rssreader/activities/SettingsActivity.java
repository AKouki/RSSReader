package com.akouki.rssreader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.akouki.rssreader.MainActivity;
import com.akouki.rssreader.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof ListPreference) {
                    ListPreference listPref = (ListPreference) preference;
                    preference.setSummary(listPref.getEntry());
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            Preference preference = findPreference(key);
            if (preference instanceof ListPreference) {
                ListPreference listPref = (ListPreference) preference;
                preference.setSummary(listPref.getEntry());

                switch (key) {
                    case "syncAutomatic":
                        if (listPref.getValue().equals("0"))
                            getActivity().sendBroadcast(new Intent().setAction(MainActivity.ACTION_ALARM_STOP));
                        else
                            getActivity().sendBroadcast(new Intent().setAction(MainActivity.ACTION_ALARM_UPDATE));
                        break;
                    case "syncInterval":
                        getActivity().sendBroadcast(new Intent().setAction(MainActivity.ACTION_ALARM_UPDATE));
                        break;
                }
            }
        }
    }

}