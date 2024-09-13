package org.trading.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Quote {
    private List<Integer> volume;
    private List<Double> low;
    private List<Double> open;
    private List<Double> high;
    private List<Double> close;
}
