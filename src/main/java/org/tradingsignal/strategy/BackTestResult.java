package org.tradingsignal.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Data
public class BackTestResult {
    List<TimeValue> portfolioValues;
    List<TimeValue> portfolioValuesPct;
    // used for performance comparison
    List<StockPerformance> stockPerformances;
    Portfolio finalPortfolio;

    BigDecimal initialValue;

    public BackTestResult(Portfolio portfolio) {
        this.finalPortfolio = portfolio;
        this.portfolioValues = new LinkedList<>();
        this.stockPerformances = new LinkedList<>();
        this.portfolioValuesPct = new LinkedList<>();
    }

    public void addPortfolioValue(Long timestamp, BigDecimal value) {
        this.portfolioValues.add(new TimeValue(timestamp, value.doubleValue()));
        if (this.initialValue == null) {
            this.initialValue = value;
        }
        BigDecimal pct = value.divide(this.initialValue, 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
        this.portfolioValuesPct.add(new TimeValue(timestamp, pct.doubleValue()));
    }

    public void addStockValues(String symbol, StockData stockData) {
        if (portfolioValues.isEmpty()) {
            throw new InvalidStrategyException("Portfolio values must be added before stock values");
        }

        List<TimeValue> stockValuesPct = new LinkedList<>();
        BigDecimal initialValue = null;
        for (int i = 0; i < stockData.getDatePrices().size(); i++) {
            DatePrice currentDatePrice = stockData.getDatePrices().get(i);
            if (currentDatePrice.getTimestamp() < portfolioValues.get(0).getTimestamp()) {
                continue;
            }
            if (initialValue == null) {
                initialValue = BigDecimal.valueOf(stockData.getDatePrices().get(i).getClose());
            }
            BigDecimal pct = BigDecimal.valueOf(currentDatePrice.getClose()).divide(initialValue, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            stockValuesPct.add(new TimeValue(currentDatePrice.getTimestamp(), pct.doubleValue()));
        }
        this.stockPerformances.add(new StockPerformance(symbol,stockValuesPct));
    }
}
@Data
@AllArgsConstructor
class StockPerformance {
    String symbol;
    List<TimeValue> stockValuesPct;
}
