package org.tradingsignal.strategy.indicator;

import org.tradingsignal.stock.StockData;
import org.tradingsignal.stock.DatePrice;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ExponentialMovingAverageIndicator implements TechnicalIndicator {

    private final int period;
    private final double smoothingFactor;

    public ExponentialMovingAverageIndicator(int period, double smoothingFactor) {
        this.period = period;
        this.smoothingFactor = smoothingFactor;
    }

    @Override
    public List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<AbstractMap.SimpleEntry<Long, Double>> emaValues = new ArrayList<>();

        if (datePrices == null || datePrices.size() < period) {
            return emaValues; // Not enough data to calculate EMA
        }

        double multiplier = this.smoothingFactor / (period + 1);
        double ema = 0.0;

        // Calculate the initial SMA (Simple Moving Average) for the first 'period' values
        for (int i = 0; i < period; i++) {
            ema += datePrices.get(i).getClose();
        }
        ema /= period;
        emaValues.add(new AbstractMap.SimpleEntry<>(datePrices.get(period - 1).getTimestamp().longValue(), ema));

        // Calculate the EMA for the rest of the values
        for (int i = period; i < datePrices.size(); i++) {
            double closePrice = datePrices.get(i).getClose();
            ema = (closePrice * multiplier) + (ema * (1 - multiplier));

//            double closePrice = datePrices.get(i).getClose();
//            ema = ((closePrice - ema) * multiplier) + ema;
            emaValues.add(new AbstractMap.SimpleEntry<>(datePrices.get(i).getTimestamp().longValue(), ema));
        }

        return emaValues;
    }
}
