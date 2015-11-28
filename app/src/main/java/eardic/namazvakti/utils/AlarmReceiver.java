package eardic.namazvakti.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eardic.namazvakti.Home;
import eardic.namazvakti.R;
import eardic.namazvakti.utils.PrayerTime.Type;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int RQS = 1;
    private Context context;

    public AlarmReceiver() {
        this(null);
    }

    public AlarmReceiver(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        try {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                setAlarmToPrayerTime(context);
                Log.d("OnReceive", "StartUpBootReceiver BOOT_COMPLETED");
            } else {
                Log.d("OnReceive", "Alarming.....");
                // Alarmmmmm !!!!
                alarm(intent.getStringExtra("prayer_time"));
                // Send update request to widget
                Intent initialUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                context.sendBroadcast(initialUpdateIntent);
                setAlarmToPrayerTime(context);
            }
        } catch (Exception e) {
            Log.v("AlarmHandler OnReceive", e.toString());
        }
    }

    public boolean createAlarm(long timeInMillis, Type prayerTime) {
        try {
            Intent intent = new Intent(this.context, AlarmReceiver.class);
            intent.putExtra("prayer_time", context.getString(prayerTime.toStringCode()));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RQS, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Log.i("InitAlarm", "Setted alarm for " + prayerTime.toString());
            return true;
        } catch (Exception ex) {
            Log.v("AlarmHandler", "Cannot create alarm because : " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public void alarm(String prayerTime) {
        try {
            new Localization(context).InitLocale();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
            boolean alarm_on = sharedPref.getBoolean("alarm_on", false);
            if (alarm_on) {
                boolean play_azan_sound = sharedPref.getBoolean("azan_sound", false);
                Uri soundUri;
                if (play_azan_sound) {
                    if (context.getString(R.string.fajr).equals(prayerTime)) {
                        soundUri = Uri.parse(String.format("android.resource://%s/%d", context.getPackageName(), R.raw.ezan));
                    } else {
                        soundUri = Uri.parse(String.format("android.resource://%s/%d", context.getPackageName(), R.raw.aksam_ezan));
                    }
//                    MediaPlayer mMediaPlayer;
//                    mMediaPlayer = MediaPlayer.create(context, R.raw.ezan);
//                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mMediaPlayer.setLooping(true);
//                    mMediaPlayer.start();
                } else {
                    String alarms = sharedPref.getString("alarm_sound", "default ringtone");
                    soundUri = Uri.parse(alarms);
                }
                boolean vibrate = sharedPref.getBoolean("vibrate", false);
                if (vibrate) {
                    Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(new long[]{0, 1000, 200, 100, 50, 100}, -1);
                }

                Intent notificationIntent = new Intent(context, Home.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                String notifText = context.getString(R.string.prayer_time_has_come).replace("X", prayerTime);
                Notification prayerTimeNotif = new NotificationCompat.Builder(context)
                        .setContentTitle(context.getString(R.string.notif_title))
                        .setContentText(notifText)
                        .setTicker(notifText)
                        .setWhen(System.currentTimeMillis())
                        .setSound(soundUri, AudioManager.STREAM_MUSIC)
                        .setLights(Color.RED, 1000, 1000)
                        .setContentIntent(intent)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .build();

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, prayerTimeNotif);

                Log.i("AlarmSound", soundUri.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("alarmError", ex.toString());
        }
    }

    private void setAlarmToPrayerTime(Context context) {
        LocationManager locMan = new LocationManager(context);
        List<Location> locs = locMan.fetchLocations();
        if (!locs.isEmpty()) {
            Location loc = locs.get(0);
            PrayerTimes times = loc.getPrayerTimes();
            PrayerTime time = times.getPrayerTimesOfDay();
            if (time != null) {
                Type nextTimeType = time.getNextTime();
                String nextTime = time.getTime(Type.DATE) + " "
                        + time.getTime(nextTimeType);

                if (nextTimeType == null) {
                    nextTimeType = Type.IMSAK;
                    PrayerTime tempT = times.getPrayerTimes().get(
                            times.getPrayerTimes().indexOf(time) + 1);
                    nextTime = tempT.getTime(Type.DATE) + " "
                            + tempT.getTime(nextTimeType);
                }
                if (!nextTime.contains("null")) {
                    SimpleDateFormat format = new SimpleDateFormat(
                            "dd.MM.yyyy HH:mm");
                    Date nextPrayerTime = null;
                    try {
                        nextPrayerTime = format.parse(nextTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    createAlarm(nextPrayerTime.getTime(), nextTimeType);

                } else {
                    Log.d("AlarmService",
                            "cannot find next time in prayer times");
                }
            } else {
                Log.d("AlarmService",
                        "cannot find current day in prayer time table");
            }
        } else {
            Log.d("AlarmService", "no location");
        }
    }

}
