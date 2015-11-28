package eardic.namazvakti.utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eardic.namazvakti.utils.PrayerTime.Type;

public class PrayerTimes implements Serializable {

    private static final long serialVersionUID = -6621197019730137044L;
    private List<PrayerTime> prayerTimes;

    public PrayerTimes() {
        // TODO Auto-generated constructor stub
        this(new ArrayList<PrayerTime>());
    }

    public PrayerTimes(List<PrayerTime> prayerTimes) {
        super();
        this.prayerTimes = prayerTimes;
    }

    @Override
    public String toString() {
        return "Size:" + prayerTimes.size();
    }

    public PrayerTime getPrayerTimesOfDay() {
        try {
            Calendar date = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            for (PrayerTime time : prayerTimes) {
                Date prayerDate = format.parse(time.getTime(Type.DATE));
                Date currDate = date.getTime();
                if (prayerDate.getDate() == currDate.getDate()
                        && prayerDate.getMonth() == currDate.getMonth()
                        && prayerDate.getYear() == currDate.getYear()) {
                    return time;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PrayerTime> getPrayerTimes() {
        return prayerTimes;
    }

    public void setPrayerTimes(List<PrayerTime> prayerTimes) {
        this.prayerTimes = prayerTimes;
    }

}
