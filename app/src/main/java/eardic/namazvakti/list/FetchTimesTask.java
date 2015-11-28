package eardic.namazvakti.list;

import android.os.AsyncTask;

import java.util.Locale;

import eardic.namazvakti.utils.DiyanetParser;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.PrayerTimes;

public class FetchTimesTask extends AsyncTask<Void, Void, PrayerTimes> {
    private Location location;
    private Locale locale;

    public FetchTimesTask(Location loc) {
        this.location = loc;
        this.locale = Locale.ENGLISH;
    }

    public FetchTimesTask(Location loc, Locale locale) {
        this.location = loc;
        this.locale = locale;
    }

    @Override
    protected PrayerTimes doInBackground(Void... params) {
        return new DiyanetParser(locale).getPrayerTimes(this.location);
    }
}
