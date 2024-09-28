package org.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    private Meta meta;
    private List<Integer> timestamp;
    private Events events;
    private Indicators indicators;
}
