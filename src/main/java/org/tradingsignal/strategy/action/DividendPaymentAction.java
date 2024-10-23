package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.DateDividends;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.Utils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class DividendPaymentAction implements StrategyAction {

    private StockDataService stockDataService;
    private Set<String> symbols;


    public DividendPaymentAction() {
        this.stockDataService = new StockDataService();
        this.symbols = new HashSet<>();
    }


    @Override
    public Portfolio execute(Portfolio portfolio, Long timestamp, PerformanceMetaData performanceMetaData) {
        //for each stock in portfolio, check if there is a dividend payment
        for (Map.Entry<String, Asset> entry : portfolio.getAssets().entrySet()) {
            String symbol = entry.getKey();
            if (symbol.equals(Asset.CASH)) {
                continue;
            }
            this.symbols.add(symbol);

            StockData stockData = stockDataService.getStockPrice(symbol);
            List<DateDividends> dividends = stockData.getDividends();

            if (dividends != null) {
                for (DateDividends dividend : dividends) {
                    if (dividend.getTimestamp().equals(timestamp)) {
                        BigDecimal dividendAmount = BigDecimal.valueOf(dividend.getValue()).multiply(entry.getValue().getQuantity());
                        portfolio.addCash(dividendAmount);

                        if (dividendAmount.doubleValue() != 0d) {
                            performanceMetaData.getActionLog().addAction(timestamp, String.format("Dividend payment for %s of %s (%s per share, %s shares)", symbol, Utils.roundDownToTwoDecimals(dividendAmount), dividend.getValue(), entry.getValue().getQuantity()));
                            performanceMetaData.getDividends().putIfAbsent(symbol, 0d);
                            performanceMetaData.getDividends().put(symbol, performanceMetaData.getDividends().get(symbol) + dividendAmount.doubleValue());

                        }
                    }
                }
            }
        }
        return portfolio;
    }

    @Override
    public Set<String> getSymbols() {
        return symbols;
    }

}
