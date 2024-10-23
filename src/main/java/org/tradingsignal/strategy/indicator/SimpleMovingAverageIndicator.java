package org.tradingsignal.strategy.indicator;

import lombok.Data;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Data
public class SimpleMovingAverageIndicator implements TechnicalIndicator {
    private int period;

    public SimpleMovingAverageIndicator(int period) {
        this.period = period;
    }

    @Override
    public List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData) {
        // StockData is ascending in date
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<AbstractMap.SimpleEntry<Long, Double>> smaValues = new ArrayList<>();

        if (datePrices.size() < period) {
            return smaValues; // Not enough data to calculate SMA
        }

        for(int i= 0; i < this.period; i++) {
            smaValues.add(new AbstractMap.SimpleEntry<>(datePrices.get(i).getTimestamp(), datePrices.get(i).getClose()));
        }

        for (int i = period; i < datePrices.size(); i++) {
            double sum = 0;
            for (int j = i - period; j < i; j++) {
                sum += datePrices.get(j).getClose();
            }
            double sma = sum / period;
            smaValues.add(new AbstractMap.SimpleEntry<>(datePrices.get(i).getTimestamp(), sma));
        }
        return smaValues;
    }
}
