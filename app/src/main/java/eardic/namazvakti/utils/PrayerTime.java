package eardic.namazvakti.utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eardic.namazvakti.R;

public class PrayerTime implements Serializable {
    private static final long serialVersionUID = -9045902452674135219L;

    private Map<Type, String> time;

    public PrayerTime() {
        time = new HashMap<Type, String>();
    }

    public Type getNextTime() {
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date currDate = cal.getTime();
            Type nextType = null;
            long diff = Long.MAX_VALUE;
            for (Type type : this.time.keySet()) {
                if (type != Type.DATE && type != Type.KIBLE && type != Type.GUNES) {
                    Date pTime = format.parse(time.get(Type.DATE) + " " + time.get(type));
                    long d = pTime.getTime() - currDate.getTime();
                    if (pTime.after(currDate) && d > 0 && d < diff) {
                        nextType = type;
                        diff = d;
                    }
                }
            }
            return nextType;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTime(Type type) {
        return time.get(type);
    }

    public void setTime(Type type, String data) {
        time.put(type, data);
    }

    public enum Type {
        DATE, IMSAK, GUNES, OGLE, IKINDI, AKSAM, YATSI, KIBLE;

        public int toStringCode() {
            switch (this) {
                case DATE:
                    return R.string.day;
                case IMSAK:
                    return R.string.fajr;
                case GUNES:
                    return R.string.shuruq;
                case OGLE:
                    return R.string.dhuhr;
                case IKINDI:
                    return R.string.asr;
                case AKSAM:
                    return R.string.magrib;
                case YATSI:
                    return R.string.isha;
                case KIBLE:
                    return R.string.qibla;
            }
            return 0;
        }
    }

}
