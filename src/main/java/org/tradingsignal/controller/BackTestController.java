package org.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.service.StrategyExecutorService;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.action.RebalancePortfolioAction;
import org.tradingsignal.strategy.indicator.SimpleMovingAverageIndicator;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/back-test")

public class BackTestController {

    @Autowired
    private StrategyExecutorService strategyExecutorService;

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/test")
    public List<String> test() {
        ActionLog actionLog = new ActionLog();

        List<Asset> assets = new LinkedList();
        assets.add(new Asset(Asset.CASH, 1, 10000));
        Portfolio portfolio = new Portfolio(10000);

        String symbol = "UPRO";
        StockData spyStockData = stockDataService.getStockPrice(symbol);

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .actionLog(actionLog)
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.GREATER_OR_EQUAL, new SimpleMovingAverageIndicator(200), spyStockData, Condition.ValueType.CURRENT_PRICE)
                                ),
                                new RebalancePortfolioAction().addWeight(symbol, 100d)
                        ),
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.LESS, new SimpleMovingAverageIndicator(200), spyStockData, Condition.ValueType.CURRENT_PRICE)
                                ),
                                new RebalancePortfolioAction().addWeight(Asset.CASH, 100d)
                        )))
                .build();

        strategyExecutorService.executeStrategy(strategyBuilder, portfolio, DateCalc.daysBefore(366 * 5), DateCalc.now());
        actionLog.addAction(DateCalc.now(), "Final portfolio value: " + portfolio.getPortfolioValue(DateCalc.now()));
        return actionLog.getActionLog().stream().map(action -> DateCalc.toDateString(action.getKey()) + " : " + action.getValue()).toList();
    }

    @GetMapping("/spy")

    public List<String> allSPY() {
        ActionLog actionLog = new ActionLog();

        List<Asset> assets = new LinkedList();
        assets.add(new Asset(Asset.CASH, 1, 10000));
        Portfolio portfolio = new Portfolio(10000);

        StockData spyStockData = stockDataService.getStockPrice("SPY");

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.LESS, new SimpleMovingAverageIndicator(10), spyStockData, 1d)
                                ),
                                new RebalancePortfolioAction().addWeight("SPY", 100d)
                        )))
                .build();

        strategyExecutorService.executeStrategy(strategyBuilder, portfolio, DateCalc.daysBefore(365), DateCalc.now());
        actionLog.addAction(DateCalc.now(), "Final portfolio value: " + portfolio.getPortfolioValue(DateCalc.now()));
        return actionLog.getActionLog().stream().map(action -> DateCalc.toDateString(action.getKey()) + " : " + action.getValue()).toList();
    }
}
