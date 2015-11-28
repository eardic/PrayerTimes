package eardic.namazvakti;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.util.Log;

import java.util.Locale;

import eardic.namazvakti.utils.Localization;
import eardic.namazvakti.widget.PrayerTimeWidget;
import eardic.namazvakti.widget.SmallPrayerTimeWidget;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            addPreferencesFromResource(R.xml.pref_alarm);
            PreferenceManager.setDefaultValues(this.getActivity(), R.xml.pref_alarm, false);

            final RingtonePreference ringPref = (RingtonePreference) findPreference("alarm_sound");
            ringPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    try {
                        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse((String) newValue));
                        ringPref.setSummary(ringtone.getTitle(getActivity()));
                        return true;
                    } catch (Exception ex) {
                        Log.v("RingtoneChangeListener", "Failed to update summary : " + ex.getMessage());
                    }
                    return false;
                }
            });

            final ListPreference listPref = (ListPreference) findPreference("language");
            listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String newVal = o.toString();
                    listPref.setSummary("en".equals(newVal) ? getString(R.string.english) : getString(R.string.turkish));
                    Localization localization = new Localization(getActivity());
                    localization.ChangeLocale(new Locale(newVal));
                    // Send update signal to widgets
                    Intent wgBigIntent = new Intent(getActivity(), PrayerTimeWidget.class);
                    wgBigIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    Intent wgSmIntent = new Intent(getActivity(), SmallPrayerTimeWidget.class);
                    wgSmIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    getActivity().sendBroadcast(wgSmIntent);
                    getActivity().sendBroadcast(wgBigIntent);
                    // Restart app
                    Intent intent = new Intent(getActivity().getApplicationContext(), Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(intent);
                    return true;
                }
            });

            final CheckBoxPreference azanCheckBox = (CheckBoxPreference) findPreference("azan_sound");
            azanCheckBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    if (value instanceof Boolean) {
                        ringPref.setEnabled(((Boolean) value) != true);
                    }
                    return true;
                }
            });

            // Initialize summary
            SharedPreferences sharePrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String alarmSnd = sharePrefs.getString("alarm_sound", null);
            Uri uri = alarmSnd != null ? Uri.parse(alarmSnd) : Settings.System.DEFAULT_NOTIFICATION_URI;
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
            ringPref.setSummary(ringtone.getTitle(getActivity()));

            String lang = sharePrefs.getString("language", getString(R.string.locale));
            listPref.setSummary("en".equals(lang) ? getString(R.string.english) : getString(R.string.turkish));
            listPref.setValue(getString(R.string.locale));

        } catch (Exception ex) {
            Log.v("SettingsFragment",
                    "Failed to init settings : " + ex.getMessage());
        }
    }
}
