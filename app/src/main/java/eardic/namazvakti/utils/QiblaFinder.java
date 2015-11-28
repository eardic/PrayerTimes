package eardic.namazvakti.utils;

public class QiblaFinder {
    /**
     * Params: lat1, long1 => Latitude and Longitude of current point lat2,
     * long2 => Latitude and Longitude of target point
     * <p/>
     * headX => x-Value of built-in phone-compass
     * <p/>
     * Returns the degree of a direction from current point to target point
     */
    public static double getDegrees(double lat1, double long1, double lat2,
                                    double long2, double headX) {
        lat1 = Math.toRadians(lat1);
        long1 = Math.toRadians(long1);
        lat2 = Math.toRadians(lat2);
        long2 = Math.toRadians(long2);

        double y = Math.sin(long2 - long1) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(long2 - long1);
        double brng = Math.toDegrees(Math.atan2(y, x));

        // fix negative degrees
        if (brng < 0) {
            brng = 360 - Math.abs(brng);
        }

        return brng - headX;
    }
}
