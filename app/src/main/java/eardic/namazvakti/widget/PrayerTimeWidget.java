package eardic.namazvakti.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import eardic.namazvakti.R;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.PrayerTime;
import eardic.namazvakti.utils.PrayerTime.Type;

public class PrayerTimeWidget extends TimeWidget {

    public static final String WG_2X2_UPDATE_ACTION = "eardic.namazvakti.action.UPDATE_2X2_WG";

    public PrayerTimeWidget() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        UpdateForAction(context, intent, WG_2X2_UPDATE_ACTION);
    }

    private int highlightResId(Type nextTime) {
        switch (nextTime) {
            case IMSAK:
                return R.id.imsakTextWg;
            case GUNES:
                return R.id.gunesTextWg;
            case OGLE:
                return R.id.ogleTextWg;
            case IKINDI:
                return R.id.ikindiTextWg;
            case AKSAM:
                return R.id.aksamTextWg;
            case YATSI:
                return R.id.yatsiTextWg;
//            case KIBLE:
//                return R.id.kibleTextWg;
            default:
                return R.id.imsakTextWg;
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

                Log.i("OnUpdateWg", "Updating big widget : " + i);
                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

                if (time != null && nextTime != null) {

                    rv.setTextViewText(R.id.locationTextWg, getLocName(loc.getName()));
                    // Make all bg of time texts transparent
                    resetTextBg(rv);
                    // Write prayer times
                    rv.setTextViewText(R.id.imsakTextWg, time.getTime(Type.IMSAK));
                    rv.setTextViewText(R.id.imsakTextLabel, context.getString(R.string.fajr));

                    rv.setTextViewText(R.id.gunesTextWg, time.getTime(Type.GUNES));
                    rv.setTextViewText(R.id.gunesTextLabel, context.getString(R.string.shuruq));

                    rv.setTextViewText(R.id.ogleTextWg, time.getTime(Type.OGLE));
                    rv.setTextViewText(R.id.ogleTextLabel, context.getString(R.string.dhuhr));

                    rv.setTextViewText(R.id.ikindiTextWg, time.getTime(Type.IKINDI));
                    rv.setTextViewText(R.id.ikindiTextLabel, context.getString(R.string.asr));

                    rv.setTextViewText(R.id.aksamTextWg, time.getTime(Type.AKSAM));
                    rv.setTextViewText(R.id.aksamTextLabel, context.getString(R.string.magrib));

                    rv.setTextViewText(R.id.yatsiTextWg, time.getTime(Type.YATSI));
                    rv.setTextViewText(R.id.yatsiTextLabel, context.getString(R.string.isha));
                    // Highlight next prayer time
                    //setTextBg(rv, highlightResId(nextTime), android.graphics.Color.parseColor("#045FB4"));
                    setTextBgRes(rv, highlightResId(nextTime), R.drawable.table_today_bg);

                } else {
                    Log.v("Widget", "Current time or next time type is null");
                }

                // Create an Intent to launch home activity
                setLaunchHomeOnClickListener(context, rv, R.id.relativeLayoutWg);
                appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            }
        } catch (Exception ex) {
            Log.v("OnUpdate", "Failed to update 2x2 wg.");
            ex.printStackTrace();
        }
    }

    private void setTextBgRes(RemoteViews rv, int id, int res) {
        rv.setInt(id, "setBackgroundResource", res);
    }

    private void setTextBg(RemoteViews rv, int id, int color) {
        rv.setInt(id, "setBackgroundColor", color);
    }

    private void resetTextBg(RemoteViews rv) {
        setTextBg(rv, R.id.imsakTextWg, Color.TRANSPARENT);
        setTextBg(rv, R.id.gunesTextWg, Color.TRANSPARENT);
        setTextBg(rv, R.id.ogleTextWg, Color.TRANSPARENT);
        setTextBg(rv, R.id.ikindiTextWg, Color.TRANSPARENT);
        setTextBg(rv, R.id.aksamTextWg, Color.TRANSPARENT);
        setTextBg(rv, R.id.yatsiTextWg, Color.TRANSPARENT);
        //setTextBg(rv, R.id.kibleTextWg, Color.TRANSPARENT);
    }
}
