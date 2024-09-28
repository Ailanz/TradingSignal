package org.tradingsignal.pojo.yahoo;

import lombok.Data;

@Data
public class Split {
    private int date;
    private double numerator;
    private double denominator;
    private String splitRatio;
}
