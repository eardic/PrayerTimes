package eardic.namazvakti;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link AboutFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link AboutFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class AboutFragment extends Fragment {
    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionText = (TextView) root.findViewById(R.id.versionText);
        TextView dateText = (TextView) root.findViewById(R.id.lastUpdateText);
        PackageInfo pInfo;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionText.setText(getActivity().getString(R.string.version) + " " + pInfo.versionName);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pInfo.lastUpdateTime);
            dateText.setText(cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            versionText.setText("");
            dateText.setText("");
        }
        // Inflate the layout for this fragment
        return root;
    }

}
