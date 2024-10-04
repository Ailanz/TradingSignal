package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.DateDividends;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DividendPaymentAction implements StrategyAction {

    private StockDataService stockDataService;


    public DividendPaymentAction() {
        stockDataService = new StockDataService();
    }


    @Override
    public Portfolio execute(Portfolio portfolio, Long timestamp, ActionLog actionLog) {
        //for each stock in portfolio, check if there is a dividend payment
        for (Map.Entry<String, Asset> entry : portfolio.getAssets().entrySet()) {
            String symbol = entry.getKey();
            if (symbol.equals(Asset.CASH)) {
                continue;
            }


            StockData stockData = stockDataService.getStockPrice(symbol);
            List<DateDividends> dividends = stockData.getDividends();

            if (dividends != null) {
                for (DateDividends dividend : dividends) {
                    if (dividend.getTimestamp().equals(timestamp)) {
                        BigDecimal dividendAmount = BigDecimal.valueOf(dividend.getValue()).multiply(entry.getValue().getQuantity());
                        portfolio.addCash(dividendAmount);

                        if (dividendAmount.doubleValue() != 0d) {
                            actionLog.addAction(timestamp, "Dividend payment for " + symbol + " of " + Utils.roundDownToTwoDecimals(dividendAmount));
                        }
                    }
                }
            }
        }
        return portfolio;
    }

}
