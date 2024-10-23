package org.tradingsignal.strategy.portfolio;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;

import java.math.BigDecimal;

@Data
public class Asset {
    public static final String CASH = "USD";

    private String symbol;
    private BigDecimal averagePrice;
    private BigDecimal quantity;

    public Asset(String symbol, BigDecimal averagePrice, BigDecimal quantity) {
        this.symbol = symbol.toUpperCase();
        this.averagePrice = averagePrice;
        this.quantity = quantity;
    }


    public BigDecimal getValue(StockDataService stockDataService, Long timestamp) {
        if (this.getSymbol().equals(CASH)) {
            return quantity;
        }

        StockData stockData = stockDataService.getStockPrice(symbol);
        DatePrice targetDatePrice = StockDataService.findDatePrice(timestamp, stockData);
        return BigDecimal.valueOf(targetDatePrice.getClose()).multiply(quantity);
    }


}
