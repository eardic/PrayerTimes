package eardic.namazvakti.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.util.Locale;

import eardic.namazvakti.R;

/**
 * Created by ASUS-PC on 05.02.2015.
 */
public class Localization {
    private Context activity;

    public Localization(Context activity) {
        this.activity = activity;
    }

    public void InitLocale() {
        SharedPreferences sharePrefs = PreferenceManager.getDefaultSharedPreferences(this.activity);
        String lang = sharePrefs.getString("language", activity.getString(R.string.locale));
        ChangeLocale(new Locale(lang));
    }

    public void ChangeLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
    }
}
