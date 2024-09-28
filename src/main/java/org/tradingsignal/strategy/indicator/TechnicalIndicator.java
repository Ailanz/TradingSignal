package org.tradingsignal.strategy.indicator;

import org.tradingsignal.stock.StockData;

import java.util.AbstractMap;
import java.util.List;

public interface TechnicalIndicator {
    //Pair of Date and Indicator value
    List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData);

}
