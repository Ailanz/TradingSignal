package org.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Quote {
    private List<Long> volume;
    private List<Double> low;
    private List<Double> open;
    private List<Double> high;
    private List<Double> close;
}
