package indicator;

import org.trading.tradingsignal.stock.StockData;
import org.trading.tradingsignal.stock.DatePrice;

import java.util.ArrayList;
import java.util.List;

public class ExponentialMovingAverage implements TechnicalIndicator {

    private final int period;

    public ExponentialMovingAverage(int period) {
        this.period = period;
    }

    @Override
    public List<Double> calculate(StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<Double> emaValues = new ArrayList<>();

        if (datePrices == null || datePrices.size() < period) {
            return emaValues; // Not enough data to calculate EMA
        }

        double multiplier = 2.0 / (period + 1);
        double ema = 0.0;

        // Calculate the initial SMA (Simple Moving Average) for the first 'period' values
        for (int i = 0; i < period; i++) {
            ema += datePrices.get(i).getClose();
        }
        ema /= period;
        emaValues.add(ema);

        // Calculate the EMA for the rest of the values
        for (int i = period; i < datePrices.size(); i++) {
            double closePrice = datePrices.get(i).getClose();
            ema = ((closePrice - ema) * multiplier) + ema;
            emaValues.add(ema);
        }

        return emaValues;
    }
}
