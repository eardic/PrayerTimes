package eardic.namazvakti.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import eardic.namazvakti.R;
import eardic.namazvakti.utils.PrayerTime.Type;

public class PrayerTimeTable {
    private int CELL_PADDING_RL = 5;
    private Context baseContext;
    private View rootView;
    private PrayerTimes times;
    private int tableId;
    private DateFormatter dateFormatter;

    public PrayerTimeTable(Context baseContext, View rootView, int tableId, PrayerTimes times) {
        super();
        this.tableId = tableId;
        this.baseContext = baseContext;
        this.times = times;
        this.rootView = rootView;
        this.dateFormatter = new DateFormatter(baseContext.getString(R.string.locale).equals("en") ? Locale.ENGLISH : new Locale("tr", "TR"));
        this.CELL_PADDING_RL = (int) baseContext.getResources().getDimension(R.dimen.prayer_time_table_padding);

        init();

    }

    private TextView createColumn(String name) {
        TextView col = new TextView(baseContext);
        col.setText(name);
        //col.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        col.setTextSize(11);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        //col.setPadding(CELL_PADDING_RL, 0, CELL_PADDING_RL, 3);
        col.setTextColor(Color.WHITE);
        return col;
    }

    private TextView createRow(String name) {
        TextView col = new TextView(baseContext);
        col.setText(name);
        // col.setTypeface(Typeface.SERIF, Typeface.NORMAL);
        col.setTextSize(13);
        col.setPadding(CELL_PADDING_RL, 0, CELL_PADDING_RL, 0);
        col.setTextColor(Color.WHITE);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        return col;
    }

    public void init() {
        try {
            // Context for getting strings from resource
            Context context = rootView.getContext();

            TableLayout stk = (TableLayout) rootView.findViewById(tableId);
            stk.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

            TableRow tbrow0 = new TableRow(baseContext);
            tbrow0.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            tbrow0.setPadding(0, 0, 0, 5);

            tbrow0.addView(createColumn(context.getString(R.string.day)));
            tbrow0.addView(createColumn(context.getString(R.string.fajr)));
            tbrow0.addView(createColumn(context.getString(R.string.shuruq)));
            tbrow0.addView(createColumn(context.getString(R.string.dhuhr)));
            tbrow0.addView(createColumn(context.getString(R.string.asr)));
            tbrow0.addView(createColumn(context.getString(R.string.magrib)));
            tbrow0.addView(createColumn(context.getString(R.string.isha)));
            // tbrow0.addView(createColumn(context.getString(R.string.qibla)));
            stk.addView(tbrow0);

            List<PrayerTime> timeList = times.getPrayerTimes();
            int begin = timeList.indexOf(times.getPrayerTimesOfDay());
            int count = 0;
            for (int i = begin; i < timeList.size(); ++i) {
                if (count < 7)// Week
                {
                    PrayerTime time = timeList.get(i);
                    TableRow tbrow = new TableRow(baseContext);
                    tbrow.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                    if (count == 0) {
                        tbrow.setBackgroundResource(R.drawable.table_today_bg);
                        tbrow.addView(createRow(context.getString(R.string.today)));
                    } else {
                        tbrow.addView(createRow(dateFormatter.ToDay(time.getTime(Type.DATE), true)));
                    }
                    tbrow.addView(createRow(time.getTime(Type.IMSAK)));
                    tbrow.addView(createRow(time.getTime(Type.GUNES)));
                    tbrow.addView(createRow(time.getTime(Type.OGLE)));
                    tbrow.addView(createRow(time.getTime(Type.IKINDI)));
                    tbrow.addView(createRow(time.getTime(Type.AKSAM)));
                    tbrow.addView(createRow(time.getTime(Type.YATSI)));
                    //  tbrow.addView(createRow(time.getTime(Type.KIBLE)));

                    stk.addView(tbrow);
                } else {
                    break;
                }
                ++count;
            }
        } catch (ParseException ex) {
            Log.d("prayerTimes", "Couldn't set short day. Error :" + ex.getMessage());
        }
    }

}
