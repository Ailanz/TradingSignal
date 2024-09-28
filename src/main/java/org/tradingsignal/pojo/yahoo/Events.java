package org.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.Map;

@Data
public class Events {
    private Map<String, Dividend> dividends;
    private Map<String, Split> splits;
}
