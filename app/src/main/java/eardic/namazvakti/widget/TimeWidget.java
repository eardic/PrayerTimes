package eardic.namazvakti.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import eardic.namazvakti.Home;
import eardic.namazvakti.utils.Localization;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.LocationManager;
import eardic.namazvakti.utils.PrayerTime;
import eardic.namazvakti.utils.PrayerTime.Type;
import eardic.namazvakti.utils.PrayerTimes;

public abstract class TimeWidget extends AppWidgetProvider {
    private LocationManager locMan = null;

    public TimeWidget() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        UpdateForAction(context, intent, AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    }

    protected void UpdateForAction(Context context, Intent intent, String action) {
        try {
            String strAction = intent.getAction();
            if (action.equalsIgnoreCase(strAction)) {
                Log.i("Wg OnReceive", action + " signal received.");
                AppWidgetManager gm = AppWidgetManager.getInstance(context);
                int[] ids = gm.getAppWidgetIds(new ComponentName(context, getClass()));
                new Localization(context).InitLocale();// Read lang from shared pref and apply it
                this.onUpdate(context, gm, ids);
            }
        } catch (Exception ex) {
            Log.v("OnReceive", "TimeWidget On Receive Error");
            ex.printStackTrace();
        }
    }

    protected Location getLocation(Context context) {
        Location loc = null;
        try {
            if (this.locMan == null) {
                this.locMan = new LocationManager(context);
            }
            loc = this.locMan.fetchLocations().get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return loc;
    }

    protected void setLaunchHomeOnClickListener(Context context, RemoteViews rv, int viewId) {
        Intent intent = new Intent(context, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        rv.setOnClickPendingIntent(viewId, pendingIntent);
    }

    protected String getLocName(String name) {
        String[] tokens = name.split(",");
        if (tokens.length > 0) {
            return tokens[tokens.length - 1].trim();
        }
        return name.trim();
    }

    protected long getRemainingTime(PrayerTime nextTime) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date nextPrayerTime = null;
        long diffMillis = 0;
        try {
            nextPrayerTime = format.parse(nextTime.getTime(Type.DATE) + " "
                    + nextTime.getTime(nextTime.getNextTime()));
            diffMillis = nextPrayerTime.getTime() - cal.getTime().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffMillis / 1000;
    }

    protected PrayerTime getTime(PrayerTimes times) {
        PrayerTime time = times.getPrayerTimesOfDay();
        if (time != null) {
            Type nextTimeType = time.getNextTime();
            if (nextTimeType == null) {
                nextTimeType = Type.IMSAK;
                time = times.getPrayerTimes().get(
                        times.getPrayerTimes().indexOf(time) + 1);
            }
        }
        return time;
    }
}
