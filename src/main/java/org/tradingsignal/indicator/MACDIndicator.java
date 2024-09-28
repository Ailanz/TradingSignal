package org.tradingsignal.indicator;

import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class MACDIndicator implements TechnicalIndicator {
    private int shortPeriod;
    private int longPeriod;
    private int signalPeriod;

    public MACDIndicator(int shortPeriod, int longPeriod, int signalPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.signalPeriod = signalPeriod;
    }

    @Override
    public List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<Double> macdValues = new ArrayList<>();
        List<Double> signalValues = new ArrayList<>();
        List<Double> histogramValues = new ArrayList<>();
        List<AbstractMap.SimpleEntry<Long, Double>> result = new ArrayList<>();

        List<Double> shortEma = calculateEMA(datePrices, shortPeriod);
        List<Double> longEma = calculateEMA(datePrices, longPeriod);

        for (int i = 0; i < shortEma.size(); i++) {
            macdValues.add(shortEma.get(i) - longEma.get(i));
        }

        signalValues = calculateEMAFromValues(macdValues, signalPeriod);

        for (int i = 0; i < macdValues.size(); i++) {
            histogramValues.add(macdValues.get(i) - signalValues.get(i));
            result.add(new AbstractMap.SimpleEntry<>(datePrices.get(i).getTimestamp().longValue(), histogramValues.get(i)));
        }

        return result;
    }

    private List<Double> calculateEMA(List<DatePrice> datePrices, int period) {
        List<Double> emaValues = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        double ema = datePrices.get(0).getClose();
        emaValues.add(ema); // Initialize the first EMA value

        for (int i = 1; i < datePrices.size(); i++) {
            ema = ((datePrices.get(i).getClose() - ema) * multiplier) + ema;
            emaValues.add(ema);
        }

        return emaValues;
    }

    private List<Double> calculateEMAFromValues(List<Double> values, int period) {
        List<Double> emaValues = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        double ema = values.get(0);
        emaValues.add(ema); // Initialize the first EMA value

        for (int i = 1; i < values.size(); i++) {
            ema = ((values.get(i) - ema) * multiplier) + ema;
            emaValues.add(ema);
        }

        return emaValues;
    }
}
