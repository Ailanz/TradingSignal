package indicator;

import lombok.Data;
import org.trading.tradingsignal.stock.DatePrice;
import org.trading.tradingsignal.stock.StockData;

import java.util.AbstractMap;
import java.util.List;

@Data
public class StockPriceIndicator implements TechnicalIndicator {
    //Enum for OPEN, HIGH, LOW, CLOSE
    public enum StockPriceType {
        OPEN,
        HIGH,
        LOW,
        CLOSE
    }

    private StockPriceType stockPriceType;

    public StockPriceIndicator(StockPriceType stockPriceType) {
        this.stockPriceType = stockPriceType;
    }

    @Override
    public List<AbstractMap.SimpleEntry<Long, Double>> calculate(StockData stockData) {
        return stockData.getDatePrices().stream()
                .map(this::getPrice)
                .toList();
    }

    public AbstractMap.SimpleEntry<Long, Double> getPrice(DatePrice datePrice) {
        Double price;
        if (stockPriceType == StockPriceType.OPEN) {
            price = datePrice.getOpen();
        } else if (stockPriceType == StockPriceType.HIGH) {
            price = datePrice.getHigh();
        } else if (stockPriceType == StockPriceType.LOW) {
            price = datePrice.getLow();
        } else if (stockPriceType == StockPriceType.CLOSE) {
            price = datePrice.getClose();
        } else {
            throw new RuntimeException("Invalid StockPriceType");
        }
        return new AbstractMap.SimpleEntry<>(datePrice.getTimestamp().longValue(), price);
    }
}
