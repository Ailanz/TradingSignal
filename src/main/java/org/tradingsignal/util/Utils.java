package org.tradingsignal.util;

public class Utils {

    public static double roundDownToTwoDecimals(double value) {
        return Math.floor(value * 100) / 100;
    }
}
