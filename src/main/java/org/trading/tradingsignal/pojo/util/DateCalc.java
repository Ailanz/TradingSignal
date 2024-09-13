package org.trading.tradingsignal.pojo.util;

import org.springframework.stereotype.Component;

public class DateCalc {
    public static long daysBefore(int days) {
        return (System.currentTimeMillis()/1000) - (long) days * 24 * 60 * 60;
    }

    public static long now() {
        return System.currentTimeMillis() / 1000;
    }
}
