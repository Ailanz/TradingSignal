package org.tradingsignal.strategy.indicator;

import lombok.AllArgsConstructor;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.util.DateCalc;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class PeriodicTimeIndicator implements TechnicalIndicator{

    private int days;
    @Override
    public List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData) {
        Long nextDate = null;
        List<AbstractMap.SimpleEntry<Long, Double>> result = new LinkedList<>();

        for (int i = 0; i < stockData.getDatePrices().size(); i++) {
            Long currDate = stockData.getDatePrices().get(i).getTimestamp();
           if (nextDate == null ) {
               nextDate = DateCalc.daysAfter(currDate, days);
           }
           else if (currDate > nextDate) {
               nextDate = DateCalc.daysAfter(nextDate, days);
           }
           result.add(new AbstractMap.SimpleEntry<>(currDate, (double) nextDate));
        }
        return result;
    }
}
