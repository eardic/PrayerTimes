package eardic.namazvakti.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import eardic.namazvakti.utils.PrayerTime.Type;

/**
 * Gets and parses prayer times at diyanet-api
 *
 * @author ASUS-PC
 */
public class DiyanetParser implements Parser {

    private final String ALT_BASE_URL = "http://diyanet-api.herokuapp.com/namaz_vakti";//Heroku'daki /namaz_vakti şeklinde, onu roota çekersin sonra
    private final String BASE_URL = "http://eardic.com/ezanvakti";
    private Locale locale;

    public DiyanetParser(Locale locale) {
        this.locale = locale;
    }

    private String getResponse(String urlAddress) {
        try {
            URL url = new URL(urlAddress);
            URLConnection urlConnection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            String data, jsonData = "";
            while ((data = reader.readLine()) != null) {
                jsonData += data;
            }
            return jsonData;
        } catch (Exception ex) {
            Log.e("UrlConnectionFailed", urlAddress + ", Error : " + ex.toString());
        }
        return null;
    }

    private Map<String, Integer> fetch(String urlAddress) {

        try {
            Log.d("fetchUrl", urlAddress);
            String jsonData = getResponse(urlAddress);
            Map<String, Integer> dataMap = new TreeMap<String, Integer>(Collator.getInstance(locale));
            JSONObject jsonReader = new JSONObject(jsonData);
            JSONArray names = jsonReader.names();
            if (names != null) {
                for (int i = 0; i < names.length(); ++i) {
                    String name = names.getString(i);
                    String value = jsonReader.getString(name);
                    dataMap.put(value, Integer.parseInt(name));
                }
            }
            return dataMap;
        } catch (Exception ex) {
            Log.e("FetchFailed", urlAddress + ", Error : " + ex.toString());
        }
        return null;
    }

    public Map<String, Integer> fetchCities(int stateId) {
        Map<String, Integer> result = fetch(BASE_URL + "/ilceler/" + stateId);
        if (result == null) {
            return fetch(ALT_BASE_URL + "/ilceler/" + stateId);
        }
        return result;
    }

    public Map<String, Integer> fetchCountries() {
        Map<String, Integer> result = fetch(BASE_URL + "/ulkeler");
        if (result == null || result.isEmpty()) {
            return fetch(ALT_BASE_URL + "/ulkeler");
        }
        return result;
    }

    public Map<String, Integer> fetchStates(int countryId) {
        Map<String, Integer> result = fetch(BASE_URL + "/sehirler/" + countryId);
        if (result == null || result.isEmpty()) {
            return fetch(ALT_BASE_URL + "/sehirler/" + countryId);
        }
        return result;
    }

    private Type getColumn(String name) {
        Type col = Type.DATE;
        if ("imsak".equals(name)) {
            col = Type.IMSAK;
        } else if ("gunes".equals(name)) {
            col = Type.GUNES;
        } else if ("ogle".equals(name)) {
            col = Type.OGLE;
        } else if ("ikindi".equals(name)) {
            col = Type.IKINDI;
        } else if ("aksam".equals(name)) {
            col = Type.AKSAM;
        } else if ("yatsi".equals(name)) {
            col = Type.YATSI;
        } else if ("kible".equals(name)) {
            col = Type.KIBLE;
        }
        return col;
    }

    public PrayerTimes getPrayerTimes(Location loc) {
        try {
            String urlAddress = BASE_URL + "/" + loc.getCountryId() + "/" + loc.getStateId() + "/" + loc.getCityId();
            String jsonData = getResponse(urlAddress);
            if (jsonData == null || jsonData.length() < 10) {// If data is empty or not valid then try another source
                urlAddress = ALT_BASE_URL + "/" + loc.getCountryId() + "/" + loc.getStateId() + "/" + loc.getCityId();
            }
            jsonData = getResponse(urlAddress);
            PrayerTimes prayerTimes = new PrayerTimes();
            JSONArray timeArray = new JSONArray(jsonData);
            for (int i = 0; i < timeArray.length(); ++i) {
                JSONObject timeObject = timeArray.getJSONObject(i);
                JSONArray names = timeObject.names();
                PrayerTime time = new PrayerTime();
                for (int j = 0; j < names.length(); ++j) {
                    String name = names.getString(j);
                    String value = timeObject.getString(name);
                    time.setTime(getColumn(name), value);
                }
                prayerTimes.getPrayerTimes().add(time);
            }
            loc.setPrayerTimes(prayerTimes);
            Log.d("getPrayerTimes", urlAddress);
            return prayerTimes;
        } catch (Exception ex) {
            Log.e("getPrayerTimesFailed", "Error : " + ex.toString());
        }
        return null;
    }

}
