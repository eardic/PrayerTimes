package eardic.namazvakti;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.LocationManager;
import eardic.namazvakti.utils.PrayerTime;
import eardic.namazvakti.utils.PrayerTime.Type;
import eardic.namazvakti.utils.PrayerTimes;

public class MonthlyTimesFragment extends Fragment {
    private LocationManager locMan;
    private ListView listView;
    private TextView locText;

    public MonthlyTimesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monthly_times, container, false);
        this.locMan = new LocationManager(getActivity());
        this.listView = (ListView) rootView.findViewById(R.id.monthlyTimesList);
        this.locText = (TextView) rootView.findViewById(R.id.ml_loc_text);
        List<Location> locs = locMan.fetchLocations();
        if (!locs.isEmpty()) {
            Location loc = locs.get(0);
            PrayerTimes pTimes = loc.getPrayerTimes();
            PrayerTime currTime = pTimes.getPrayerTimesOfDay();
            List<PrayerTime> times = new ArrayList<PrayerTime>();
            if (currTime != null) {
                for (int i = 0; i < pTimes.getPrayerTimes().size(); ++i) {
                    times.add(pTimes.getPrayerTimes().get(i));
                }
            }
            int todayRow = pTimes.getPrayerTimes().indexOf(currTime);
            this.locText.setText(loc.getName().substring(loc.getName().lastIndexOf(",") + 1).trim());
            final ArrayAdapter<PrayerTime> adapter = new TimeListAdapter(getActivity(), times, todayRow);
            this.listView.setAdapter(adapter);
            this.listView.setSelection(todayRow);
        } else {
            this.locText.setText("");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_loc_found);
            builder.setCancelable(false);
            builder.setMessage(R.string.monthly_times_loc_error);
            builder.setPositiveButton(R.string.select_loc, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                            .replace(R.id.container, new LocationFragment(), LocationFragment.TAG)
                            .commit();
                }
            });
            builder.create().show();

        }
        return rootView;
    }

    public class TimeListAdapter extends ArrayAdapter<PrayerTime> {
        private int todayRowIndex;

        public TimeListAdapter(Context context, List<PrayerTime> items, int todayRow) {
            super(context, R.layout.monthly_times_list_item, items);
            this.todayRowIndex = todayRow;
        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.monthly_times_list_item, parent, false);
            }
            if (position == todayRowIndex) {
                rowView.setBackgroundColor(Color.parseColor("#F1DEA6"));
            } else {
                rowView.setBackgroundColor(Color.WHITE);
            }
            TextView date = (TextView) rowView.findViewById(R.id.ml_date);
            TextView imsak = (TextView) rowView.findViewById(R.id.ml_imsak);
            TextView gunes = (TextView) rowView.findViewById(R.id.ml_gunes);
            TextView ogle = (TextView) rowView.findViewById(R.id.ml_ogle);
            TextView ikindi = (TextView) rowView.findViewById(R.id.ml_ikindi);
            TextView aksam = (TextView) rowView.findViewById(R.id.ml_aksam);
            TextView yatsi = (TextView) rowView.findViewById(R.id.ml_yatsi);
            TextView kible = (TextView) rowView.findViewById(R.id.ml_kible);
            PrayerTime time = getItem(position);
            if (time != null) {
                date.setText(time.getTime(Type.DATE));
                imsak.setText(time.getTime(Type.IMSAK));
                gunes.setText(time.getTime(Type.GUNES));
                ogle.setText(time.getTime(Type.OGLE));
                ikindi.setText(time.getTime(Type.IKINDI));
                aksam.setText(time.getTime(Type.AKSAM));
                yatsi.setText(time.getTime(Type.YATSI));
                kible.setText(time.getTime(Type.KIBLE));
            } else {
                Log.v("TimeAdapter", "Time is NULL!!!");
            }
            return rowView;
        }

    }
}
