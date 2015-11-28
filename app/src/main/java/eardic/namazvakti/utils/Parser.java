package eardic.namazvakti.utils;

import java.util.Map;

public interface Parser {
    Map<String, Integer> fetchCities(int cityId);

    Map<String, Integer> fetchStates(int stateId);

    Map<String, Integer> fetchCountries();

    PrayerTimes getPrayerTimes(Location loc);

}
