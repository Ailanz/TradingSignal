package org.tradingsignal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.util.LinkedList;
import java.util.List;

@Service
public class StrategyExecutorService {

    @Autowired
    private StockDataService stockDataService;

    public Portfolio executeStrategy(StrategyBuilder strategy, Portfolio portfolio, Long fromTimestamp, Long toTimestamp) {
        ActionLog actionLog = strategy.getActionLog();
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio cannot be null");
        }
        //TODO: ERROR timestamp may not match up
        List<Long> allTimestamps = new LinkedList<>();

        for (SubStrategy subStrategy : strategy.getSubStrategies()) {
            for (Condition condition : subStrategy.getConditions()) {
                String symbol = condition.getSymbol();
                StockData stockData = stockDataService.getStockPrice(symbol);
                allTimestamps.addAll(stockData.getDatePrices().stream().map(DatePrice::getTimestamp).toList());
            }
        }

        //De Duplication
        allTimestamps = allTimestamps.stream().distinct().sorted().toList();

        for (Long timestamp : allTimestamps) {
            if (timestamp < fromTimestamp || timestamp > toTimestamp) {
                continue;
            }

            for (SubStrategy subStrategy : strategy.getSubStrategies()) {

                for (Condition condition : subStrategy.getConditions()) {
                    if (condition.isMet(timestamp, actionLog)) {
                        // Execute the action
                        subStrategy.getAction().execute(portfolio, timestamp, actionLog);
                    }
                }
            }
        }

        return portfolio;

    }
}
