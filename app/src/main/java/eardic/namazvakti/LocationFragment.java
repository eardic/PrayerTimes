package eardic.namazvakti;

import android.app.AlertDialog;
import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import eardic.namazvakti.list.FetchListTask;
import eardic.namazvakti.list.FetchListTask.ListType;
import eardic.namazvakti.list.FetchTimesTask;
import eardic.namazvakti.list.ItemNotification;
import eardic.namazvakti.list.Selection;
import eardic.namazvakti.list.TextNotification;
import eardic.namazvakti.utils.DiyanetParser;
import eardic.namazvakti.utils.Location;
import eardic.namazvakti.utils.LocationManager;
import eardic.namazvakti.utils.Parser;

public class LocationFragment extends Fragment {
    public static final String TAG = "location";
    private LocationManager locMan;
    private ListView listView;
    private Map<String, Integer> countryMap, cityMap, stateMap;
    private Selection selectedCountry, selectedState, selectedCity;
    private ListType currentList;
    private ItemNotification itemNotification;
    private Parser timeParser;

    public LocationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location, container,
                false);
        this.locMan = new LocationManager(getActivity());
        this.listView = (ListView) rootView.findViewById(R.id.locationList);
        this.currentList = ListType.Country;
        this.selectedCountry = new Selection();
        this.selectedState = new Selection();
        this.selectedCity = new Selection();
        this.itemNotification = new TextNotification(getString(R.string.loading));
        this.timeParser = new DiyanetParser(new Locale(getString(R.string.locale)));

        initializeCountryList();

        return rootView;
    }

    public ListType switchList() {
        switch (currentList) {
            case State:
                initializeCountryList();
                break;
            case City:
                initalizeStateList();
                break;
            default:// If country then switch home
                switchToHomeActivity();
                break;
        }
        return currentList;
    }

    private void initializeCountryList() {

        Toast.makeText(getActivity(), R.string.select_country, Toast.LENGTH_SHORT).show();

        this.currentList = ListType.Country;
        this.countryMap = CreateCountryMap();
        List<String> countryList = new ArrayList<String>(countryMap.keySet());
        if (countryMap.containsKey(("TURKEY"))) {
            countryList.remove("TURKEY");
            countryList.add(0, "TURKEY");
        } else if (countryMap.containsKey(("TURKIYE"))) {
            countryList.remove("TURKIYE");
            countryList.add(0, "TURKIYE");
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item, R.id.list_entry_name,
                countryList);

        this.listView.setAdapter(adapter);
        this.listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent,
                                            final View view, final int position, long id) {
                        itemNotification.setView(view
                                .findViewById(R.id.list_entry_msg));
                        itemNotification.show();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String item = (String) parent.getItemAtPosition(position);
                                    selectedCountry.id = countryMap.get(item);
                                    selectedCountry.name = item;
                                    stateMap = new FetchListTask(ListType.State, timeParser, selectedCountry.id).execute().get();
                                    if (stateMap == null) {
                                        alert();
                                    } else {
                                        initalizeStateList();
                                    }
                                } catch (InterruptedException
                                        | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                itemNotification.hide();
                            }
                        });
                    }
                });
    }

    private void initalizeStateList() {

        Toast.makeText(getActivity(), R.string.select_state, Toast.LENGTH_SHORT).show();

        this.currentList = ListType.State;
        List<String> stateList = new ArrayList<String>(stateMap.keySet());
        if ("TURKIYE".equals(selectedCountry.name) || "TURKEY".equals(selectedCountry.name)) {
            stateList.remove("İSTANBUL");
            stateList.remove("İZMİR");
            stateList.add(0, "İSTANBUL");
            stateList.add(1, "İZMİR");
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item, R.id.list_entry_name,
                stateList);
        this.listView.setAdapter(adapter);
        this.listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent,
                                            final View view, final int position, long id) {
                        itemNotification.setView(view
                                .findViewById(R.id.list_entry_msg));
                        itemNotification.show();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String item = (String) parent.getItemAtPosition(position);
                                    selectedState.id = stateMap.get(item);
                                    selectedState.name = item;
                                    cityMap = new FetchListTask(ListType.City, timeParser, selectedState.id).execute().get();
                                    if (cityMap == null) {
                                        alert();
                                    } else if (cityMap.isEmpty()) {
                                        Location loc = new Location(selectedCountry.id, selectedState.id, selectedCountry.name + ", " + selectedState.name);
                                        new FetchTimesTask(loc).execute().get();
                                        locMan.saveLocation(loc);
                                        switchToHomeActivity();
                                    } else {
                                        initializeCityList();
                                    }
                                } catch (InterruptedException
                                        | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                itemNotification.hide();
                            }
                        });
                    }
                });
    }

    private void initializeCityList() {

        Toast.makeText(getActivity(), R.string.select_city, Toast.LENGTH_SHORT).show();

        this.currentList = ListType.City;
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item, R.id.list_entry_name,
                new ArrayList<String>(cityMap.keySet()));
        this.listView.setAdapter(adapter);
        this.listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent,
                                            final View view, final int position, long id) {
                        itemNotification.setView(view
                                .findViewById(R.id.list_entry_msg));
                        itemNotification.show();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final String item = (String) parent
                                            .getItemAtPosition(position);
                                    selectedCity.id = cityMap.get(item);
                                    selectedCity.name = item;
                                    Location loc = new Location(
                                            selectedCountry.id,
                                            selectedState.id, selectedCity.id,
                                            selectedCountry.name + ", " + selectedState.name + ", " + selectedCity.name);
                                    new FetchTimesTask(loc).execute().get();
                                    locMan.saveLocation(loc);
                                    switchToHomeActivity();
                                } catch (InterruptedException
                                        | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                itemNotification.hide();
                            }
                        });

                    }
                });
    }

    private void switchToHomeActivity() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.fragment_enter_anim, R.animator.fragment_exit_anim)
                .replace(R.id.container, new HomeFragment()).commit();
        // Send update request to widget
        Intent initialUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        getActivity().sendBroadcast(initialUpdateIntent);
    }

    private void alert() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.connection_lost)
                .setMessage(R.string.internet_needed_for_loc)
                .setPositiveButton(R.string.alert_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS));
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private Map<String, Integer> CreateCountryMap() {
        // Get country names as String []
        String[] countryList = getResources().getStringArray(
                R.array.country_list);
        // Get country codes as int []
        String[] countryCodes = getResources().getStringArray(
                R.array.country_codes);

        // Create hash map whose key is country name and value is country code.
        Map<String, Integer> countryMap = new TreeMap<String, Integer>();
        for (int i = 0; i < countryList.length; i++) {
            countryMap.put(countryList[i], Integer.parseInt(countryCodes[i]));
        }

        return countryMap;
    }

}
