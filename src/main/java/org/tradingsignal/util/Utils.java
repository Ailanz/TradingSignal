package org.tradingsignal.util;

import java.math.BigDecimal;

public class Utils {

    public static double roundDownToTwoDecimals(double value) {
        return Math.floor(value * 100) / 100;
    }

    public static double roundDownToTwoDecimals(BigDecimal value) {
        return roundDownToTwoDecimals(value.doubleValue());
    }
}
