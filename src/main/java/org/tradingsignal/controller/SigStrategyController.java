package org.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.service.StrategyExecutorService;
import org.tradingsignal.strategy.BackTestResult;
import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.SigAction;
import org.tradingsignal.strategy.condition.PeriodicTimeCondition;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import java.util.List;

@RestController
@RequestMapping("/sig")
public class SigStrategyController {

    @Autowired
    private StrategyExecutorService strategyExecutorService;

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/performance")
    public PerformanceMetaData performance(double cash, double riskWeight, int daysAgo, int rebalanceDays, double sig, String riskSymbol, String safeSymbol) {
        Portfolio portfolio = new Portfolio(cash);
        long startDate = DateCalc.daysBefore(daysAgo);
        long endDate = DateCalc.now();

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new PeriodicTimeCondition(rebalanceDays)
                                ),
                                new SigAction(sig, riskSymbol, safeSymbol, riskWeight)
                        )
                        , SubStrategy.DIVIDEND_PAYMENT
                ))
                .build().build();

        BackTestResult backTestResult = strategyExecutorService.executeStrategy(strategyBuilder, portfolio, startDate, endDate);
        return backTestResult.getPerformanceMetaData();
    }

}
