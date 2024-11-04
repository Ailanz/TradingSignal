package org.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.service.StrategyExecutorService;
import org.tradingsignal.stock.StockConfigBuilder;
import org.tradingsignal.strategy.BackTestResult;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.DividendPaymentAction;
import org.tradingsignal.strategy.action.SigAction;
import org.tradingsignal.strategy.condition.PeriodicTimeCondition;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/sig")
public class SigStrategyController {

    @Autowired
    private StrategyExecutorService strategyExecutorService;

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/performance")
    public BackTestResult performance(double cash, double riskWeight, int daysAgo, int rebalanceDays, double sig, String riskSymbol, String safeSymbol) {
        riskSymbol = riskSymbol.toUpperCase().trim();
        safeSymbol = safeSymbol.toUpperCase().trim();

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
                        ,new SubStrategy(SubStrategy.Operation.ALWAYS_TRUE, List.of(), new DividendPaymentAction())
                ))
                .build().build();

        StockConfigBuilder.Interval interval = daysAgo > 300 ? StockConfigBuilder.Interval.ONE_MONTH : StockConfigBuilder.Interval.ONE_DAY;

        BackTestResult backTestResult = strategyExecutorService.executeStrategy(strategyBuilder, portfolio, startDate, endDate);
        backTestResult.addStockValues(riskSymbol, stockDataService.getStockPrice(riskSymbol, interval));
        backTestResult.addStockValues(safeSymbol, stockDataService.getStockPrice(safeSymbol, interval));

        backTestResult.sparsePortfolioData(5);
        return backTestResult;
    }

}
