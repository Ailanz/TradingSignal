package indicator;

import org.trading.tradingsignal.stock.StockData;

import java.util.List;

public interface TechnicalIndicator {
    List<Double> calculate(StockData stockData);
}
