package org.tradingsignal.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

@Data
public class BackTestResult {
    @JsonIgnore
    List<TimeValue> portfolioValues;
    List<TimeValue> portfolioValuesPct;
    // used for performance comparison
    List<StockPerformance> stockPerformances;
    Portfolio finalPortfolio;

    @JsonIgnore
    BigDecimal initialValue;
    PerformanceMetaData performanceMetaData;

    public BackTestResult(Portfolio portfolio, PerformanceMetaData performanceMetaData) {
        this.finalPortfolio = portfolio;
        this.portfolioValues = new LinkedList<>();
        this.stockPerformances = new LinkedList<>();
        this.portfolioValuesPct = new LinkedList<>();
        this.performanceMetaData = performanceMetaData;
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
        DatePrice initialDatePrice = null;
        for (int i = 0; i < stockData.getDatePrices().size(); i++) {
            DatePrice currentDatePrice = stockData.getDatePrices().get(i);
            if (currentDatePrice.getTimestamp() < portfolioValues.get(0).getTimestamp()) {
                continue;
            }
            if (initialValue == null) {
                initialValue = BigDecimal.valueOf(stockData.getDatePrices().get(i).getClose());
                initialDatePrice = currentDatePrice;
            }
            BigDecimal pct = BigDecimal.valueOf(currentDatePrice.getClose()).divide(initialValue, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
            stockValuesPct.add(new TimeValue(currentDatePrice.getTimestamp(), ActionLog.round(pct).doubleValue()));
        }
        List<DatePrice> datePrices = stockData.getDatePrices();

        this.performanceMetaData.getCompareSymbols().add(new CompareSymbol(symbol, DateCalc.toDateString(initialDatePrice.getTimestamp()),
                initialDatePrice.getClose(), DateCalc.toDateString(datePrices.getLast().getTimestamp()),
                datePrices.getLast().getClose(),
                stockValuesPct.getLast().getValue()));
        this.stockPerformances.add(new StockPerformance(symbol,stockValuesPct));
    }

    public void sparsePortfolioData(int sparseData) {
        // sparse data on portfolio value and pct
        List<TimeValue> portfolioValues = new LinkedList<>();
        List<TimeValue> portfolioValuesPct = new LinkedList<>();
        for (int i = 0; i < this.portfolioValues.size(); i++) {
            if (i % sparseData == 0 || i == this.portfolioValues.size() - 1) {
                portfolioValues.add(this.portfolioValues.get(i));
                portfolioValuesPct.add(this.portfolioValuesPct.get(i));
            }
        }
        this.portfolioValues = portfolioValues;
        this.portfolioValuesPct = portfolioValuesPct;

    }
}
@Data
@AllArgsConstructor
class StockPerformance {
    String symbol;
    List<TimeValue> stockValuesPct;
}
