package eardic.namazvakti.list;

import android.os.AsyncTask;

import java.util.Map;

import eardic.namazvakti.utils.Parser;

public class FetchListTask extends AsyncTask<Void, Void, Map<String, Integer>> {
    private int id;
    private ListType type;
    private Parser parser;

    public FetchListTask(ListType type, Parser parser, int id) {
        this.id = id;
        this.type = type;
        this.parser = parser;
    }

    @Override
    protected Map<String, Integer> doInBackground(Void... params) {
        Map<String, Integer> data = null;
        switch (type) {
            case State:
                data = parser.fetchStates(id);
                break;
            case City:
                data = parser.fetchCities(id);
                break;
            case Country:
                data = parser.fetchCountries();
                break;
        }
        return data;
    }

    public enum ListType {
        Country, State, City
    }
}
