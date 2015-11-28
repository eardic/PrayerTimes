package eardic.namazvakti.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import eardic.namazvakti.R;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.PrayerTime;
import eardic.namazvakti.utils.PrayerTime.Type;

public class SmallPrayerTimeWidget extends TimeWidget {

    public static final String WG_1X1_UPDATE_ACTION = "eardic.namazvakti.action.UPDATE_1X1_WG";

    public SmallPrayerTimeWidget() {
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        initUpdater(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelUpdater(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equalsIgnoreCase(intent.getAction())) {// Recreate updater on appwidget update
            initUpdater(context);
        }
        UpdateForAction(context, intent, WG_1X1_UPDATE_ACTION);
    }

    private void initUpdater(Context context) {
        try {
            Intent intent = new Intent(context, SmallPrayerTimeWidget.class);
            intent.setAction(WG_1X1_UPDATE_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), 1000 * 60, pendingIntent);
            Log.i("SmWgOnEnabled", "Created updater alarm...");
        } catch (Exception ex) {
            Log.v("SmWgOnDelete", "Cannot unregister updater alarm");
            ex.printStackTrace();
        }
    }

    private void cancelUpdater(Context context) {
        try {
            Intent intent = new Intent(context, SmallPrayerTimeWidget.class);
            intent.setAction(WG_1X1_UPDATE_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            Log.i("SmWgOnDelete", "Updater alarm canceled.");
        } catch (Exception ex) {
            Log.v("SmWgOnDelete", "Cannot unregister updater alarm");
            ex.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        try {
            Location loc = getLocation(context);
            PrayerTime time = getTime(loc.getPrayerTimes());
            Type nextTime = time.getNextTime();
            for (int i = 0; i < appWidgetIds.length; ++i) {

                Log.i("OnUpdateSmWg", "Updating small widget : " + i);
                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.small_widget_layout);
                if (time != null && nextTime != null) {
                    rv.setTextViewText(R.id.smallWgLocText, getLocName(loc.getName()));
                    rv.setTextViewText(R.id.smallWgTimeLabel, context.getString(nextTime.toStringCode()).toUpperCase());

                    long durationSeconds = getRemainingTime(time);
                    rv.setTextViewText(R.id.smallWgPrayerTime, String.format(
                            Locale.ENGLISH, "%02d:%02d",
                            durationSeconds / 3600,
                            (durationSeconds % 3600) / 60));
                } else {
                    Log.v("SmWidget", "Current time or next time type is null");
                }

                // Create an Intent to launch home activity
                setLaunchHomeOnClickListener(context, rv, R.id.smallWgRelativeLay);
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }
        } catch (Exception ex) {
            Log.v("SmWgOnUpdate", "Failed to update sm wg.");
            ex.printStackTrace();
        }
    }

}
