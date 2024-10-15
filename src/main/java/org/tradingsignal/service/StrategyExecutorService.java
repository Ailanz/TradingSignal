package org.tradingsignal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.BackTestResult;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.action.StrategyAction;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.util.LinkedList;
import java.util.List;

@Service
public class StrategyExecutorService {

    @Autowired
    private StockDataService stockDataService;

    public BackTestResult executeStrategy(StrategyBuilder strategy, Portfolio portfolio, Long fromTimestamp, Long toTimestamp) {
        BackTestResult backTestResult = new BackTestResult(portfolio);
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
                allTimestamps = getIntersectTimeStamps(symbol, allTimestamps);
            }

            for (String symbol : subStrategy.getAction().getSymbols()) {
                allTimestamps = getIntersectTimeStamps(symbol, allTimestamps);
            }
        }

        //De Duplication
        allTimestamps = allTimestamps.stream().distinct().sorted().toList();

        //Run Strategy
        for (Long timestamp : allTimestamps) {
            if (timestamp < fromTimestamp || timestamp > toTimestamp) {
                continue;
            }

            for (SubStrategy subStrategy : strategy.getSubStrategies()) {
                boolean allConditionsMet = true;
                for (Condition condition : subStrategy.getConditions()) {
                    boolean conditionMet = condition.isMet(timestamp, actionLog);
                    if(subStrategy.getOperation().equals(SubStrategy.Operation.ALWAYS_TRUE)) {
                        allConditionsMet = true;
                        break;
                    }

                    if (subStrategy.getOperation().equals(SubStrategy.Operation.AND)) {
                        allConditionsMet = allConditionsMet && conditionMet;
                    } else if (subStrategy.getOperation().equals(SubStrategy.Operation.OR)) {
                        allConditionsMet = allConditionsMet || conditionMet;
                    }
                }

                if (allConditionsMet) {
                    subStrategy.getAction().execute(portfolio, timestamp, actionLog);
                }
            }
            backTestResult.addPortfolioValue(timestamp, portfolio.getPortfolioValue(timestamp));
        }

        return backTestResult;

    }

    private List<Long> getIntersectTimeStamps(String symbol, List<Long> allTimestamps) {
        StockData stockData = stockDataService.getStockPrice(symbol);
        List<Long> timestamps = stockData.getDatePrices().stream().map(DatePrice::getTimestamp).toList();
        if (allTimestamps.isEmpty()) {
            allTimestamps.addAll(timestamps);
        } else {
            allTimestamps = intersectList(allTimestamps, timestamps);
        }
        return allTimestamps;
    }

    private List<Long> intersectList(List<Long> list1, List<Long> list2) {
        List<Long> result = new LinkedList<>();
        for (Long l : list1) {
            if (list2.contains(l)) {
                result.add(l);
            }
        }
        return result;
    }
}
