package org.tradingsignal.util;

public class DateCalc {
    public static long daysBefore(int days) {
        return (System.currentTimeMillis()/1000) - (long) days * 24 * 60 * 60;
    }

    public static long daysAfter(Long now, int days) {
        return now + (long) days * 24 * 60 * 60;
    }

    public static long now() {
        //beginning of next day
        return (System.currentTimeMillis() / 1000) + 24 * 60 * 60;
        //        return System.currentTimeMillis() / 1000;
    }

    public static String toDateString(long timestamp) {
        return new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date (timestamp * 1000));
    }
}
