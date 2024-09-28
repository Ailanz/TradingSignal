package org.tradingsignal.indicator;

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
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<AbstractMap.SimpleEntry<Long, Double>> smaValues = new ArrayList<>();

        for (int i = 0; i <= datePrices.size() - period; i++) {
            double sum = 0.0;
            for (int j = i; j < i + period; j++) {
                sum += datePrices.get(j).getClose();
            }
            double sma = sum / period;
            Long timestamp = datePrices.get(i + period - 1).getTimestamp().longValue();
            smaValues.add(new AbstractMap.SimpleEntry<>(timestamp, sma));
        }

        return smaValues;
    }
}
