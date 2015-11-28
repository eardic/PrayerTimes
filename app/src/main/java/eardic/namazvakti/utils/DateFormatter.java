package eardic.namazvakti.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ASUS-PC on 03.02.2015.
 */
public class DateFormatter {

    private Locale locale;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    public DateFormatter() {
        this(Locale.getDefault());
    }

    public DateFormatter(Locale locale) {
        this.locale = locale;
        this.calendar = Calendar.getInstance();
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String ToDay(String date, boolean isShort) throws ParseException {
        calendar.setTime(dateFormat.parse(date));
        DateFormat showFormat = new SimpleDateFormat(isShort ? "EEE" : "EEEE", locale);
        return showFormat.format(calendar.getTime());
    }

    public String ToDate(String date) throws ParseException {
        calendar.setTime(dateFormat.parse(date));
        DateFormat showFormat = new SimpleDateFormat("dd MMMM yyyy", locale);
        return showFormat.format(calendar.getTime());
    }
}
