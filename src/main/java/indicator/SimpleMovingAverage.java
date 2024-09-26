package indicator;

import org.trading.tradingsignal.stock.DatePrice;
import org.trading.tradingsignal.stock.StockData;

import java.util.ArrayList;
import java.util.List;

public class SimpleMovingAverage implements TechnicalIndicator {
    private int period;

    public SimpleMovingAverage(int period) {
        this.period = period;
    }

    @Override
    public List<Double> calculate(StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        List<Double> smaValues = new ArrayList<>();

        for (int i = 0; i <= datePrices.size() - period; i++) {
            double sum = 0.0;
            for (int j = i; j < i + period; j++) {
                sum += datePrices.get(j).getClose();
            }
            smaValues.add(sum / period);
        }

        return smaValues;
    }


}
