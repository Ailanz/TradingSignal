package org.tradingsignal.pojo.yahoo;

import lombok.Data;

@Data
public class Split {
    private long date;
    private double numerator;
    private double denominator;
    private String splitRatio;
}
