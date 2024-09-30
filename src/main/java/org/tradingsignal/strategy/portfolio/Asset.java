package org.tradingsignal.strategy.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;

import java.util.List;

@Data
public class Asset {
    public static final String CASH = "USD";

    private String symbol;
    private double averagePrice;
    private double quantity;

    public Asset(String symbol, double averagePrice, double quantity) {
        this.symbol = symbol.toUpperCase();
        this.averagePrice = averagePrice;
        this.quantity = quantity;
    }

    public Asset merge(Asset other) {
        if (!this.symbol.equals(other.symbol)) {
            throw new IllegalArgumentException("Cannot merge assets with different symbols");
        }
        double newAveragePrice = (this.averagePrice * this.quantity + other.averagePrice * other.quantity) / (this.quantity + other.quantity);
        return new Asset(this.symbol, newAveragePrice, this.quantity + other.quantity);
    }

    public double getValue(StockDataService stockDataService) {
        return stockDataService.getLatestStockPrice(symbol).getClose() * quantity;
    }

    public double getValue(StockDataService stockDataService, Long timestamp) {
        if (this.getSymbol().equals(CASH)) {
            return quantity;
        }

        StockData stockData = stockDataService.getStockPrice(symbol);
        DatePrice targetDatePrice = StockDataService.findDatePrice(timestamp, stockData);
        return targetDatePrice.getClose() * quantity;
    }


}
