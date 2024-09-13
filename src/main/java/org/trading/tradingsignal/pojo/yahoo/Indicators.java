package org.trading.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Indicators {
    private List<Quote> quote;
    private List<Adjclose> adjclose;
}
