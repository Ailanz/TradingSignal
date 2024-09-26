package indicator;

import org.trading.tradingsignal.stock.DatePrice;
import org.trading.tradingsignal.stock.StockData;

import java.util.ArrayList;
import java.util.List;

public class MACD implements TechnicalIndicator {
    private int shortPeriod;
    private int longPeriod;
    private int signalPeriod;

    public MACD(int shortPeriod, int longPeriod, int signalPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.signalPeriod = signalPeriod;
    }

    @Override
    public List<Double> calculate(StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<Double> macdValues = new ArrayList<>();
        List<Double> signalValues = new ArrayList<>();
        List<Double> histogramValues = new ArrayList<>();

        List<Double> shortEma = calculateEMA(datePrices, shortPeriod);
        List<Double> longEma = calculateEMA(datePrices, longPeriod);

        for (int i = 0; i < shortEma.size(); i++) {
            macdValues.add(shortEma.get(i) - longEma.get(i));
        }

        signalValues = calculateEMAFromValues(macdValues, signalPeriod);

        for (int i = 0; i < macdValues.size(); i++) {
            histogramValues.add(macdValues.get(i) - signalValues.get(i));
        }

        return histogramValues;
    }

    private List<Double> calculateEMA(List<DatePrice> datePrices, int period) {
        List<Double> emaValues = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        double ema = datePrices.get(0).getClose();

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

        for (int i = 1; i < values.size(); i++) {
            ema = ((values.get(i) - ema) * multiplier) + ema;
            emaValues.add(ema);
        }

        return emaValues;
    }
}
